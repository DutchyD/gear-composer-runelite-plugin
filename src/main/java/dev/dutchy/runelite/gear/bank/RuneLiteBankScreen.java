package dev.dutchy.runelite.gear.bank;

import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ScriptID;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.bank.BankSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Writes the display's frame onto the real bank interface.
 *
 * <p>A bank item widget is bound by the game to the bank slot it was drawn for, and withdrawing acts
 * on that binding, which is why a frame says which child each face belongs to rather than being
 * drawn anywhere convenient. Everything here touches widgets, so every method runs on the client
 * thread and nothing here decides what the bank should look like.
 */
@Singleton
public final class RuneLiteBankScreen implements BankScreen {

    private static final Logger log = LoggerFactory.getLogger(RuneLiteBankScreen.class);

    private static final String BUILD_TAB_CALLBACK = "bankBuildTab";
    private static final String SEARCH_FILTER_CALLBACK = "bankSearchFilter";
    private static final int SINGLE_TAB_VIEW = 1;

    static final String SHOW_VARIANT = "Show variant";
    static final String SCROLL_LEFT = "Scroll left";
    static final String SCROLL_RIGHT = "Scroll right";
    private static final int FIRST_ACTION = 1;
    private static final int CHOSEN_TEXT = 0xff981f;
    private static final int OTHER_TEXT = 0xb0b0b0;
    private static final int ARROW_TEXT = 0xd0d0d0;
    private static final int TAB_FILL = 0x3a3a3a;
    private static final int CHOSEN_FILL = 0x5a4420;
    private static final int TAB_OPACITY = 40;

    /** Offset from the top of the int stack of the height argument to if_setscrollsize. */
    private static final int SCROLL_HEIGHT_STACK_OFFSET = 9;

    private final Client client;
    private final ClientThread clientThread;
    private final BankSearch bankSearch;
    private final BankDisplay display;

    private volatile boolean bankOpen;
    private int firstSpareChild = -1;

    @Inject
    public RuneLiteBankScreen(Client client, ClientThread clientThread, BankSearch bankSearch, BankDisplay display) {
        this.client = Objects.requireNonNull(client, "client");
        this.clientThread = Objects.requireNonNull(clientThread, "clientThread");
        this.bankSearch = Objects.requireNonNull(bankSearch, "bankSearch");
        this.display = Objects.requireNonNull(display, "display");
    }

    /** Re-runs the bank build on a later tick: inline it would nest inside whatever script raised the event. */
    @Override
    public void rebuild() {
        clientThread.invokeLater(() -> {
            Widget container = itemContainer();
            if (container != null && !container.isHidden()) {
                bankSearch.layoutBank();
            }
        });
    }

    @Override
    public boolean isBankOpen() {
        return bankOpen;
    }

    /** Runs after Bank Tags, which hides every item layer child past its own first one on each build. */
    @Subscribe(priority = -1)
    public void onScriptPreFired(ScriptPreFired event) {
        if (event.getScriptId() != ScriptID.BANKMAIN_FINISHBUILDING) {
            return;
        }
        restoreSlotSizes();
        Widget container = itemContainer();
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        Widget[] children = container == null ? null : container.getChildren();
        if (bank == null || children == null) {
            return;
        }
        BankSnapshot snapshot = read(container, children, bank);
        BankFrame frame = display.frameFor(snapshot);
        if (frame.isEmpty()) {
            hideAppendedChildren(container);
            return;
        }
        hide(container, snapshot);
        hideAppendedChildren(container);
        draw(container, frame);
        overrideScrollHeight(frame.scrollHeight());
        if (frame.rewind()) {
            scrollToTop();
        }
    }

    /** What the game drew this build, where a child in the item area is also the bank slot it shows. */
    private BankSnapshot read(Widget container, Widget[] children, ItemContainer bank) {
        if (firstSpareChild == -1) {
            firstSpareChild = children.length;
        }
        List<BankSnapshot.Shown> shown = new ArrayList<>();
        for (int index = 0; index < bank.size(); index++) {
            Widget child = container.getChild(index);
            if (child == null) {
                break;
            }
            int itemId = child.getItemId();
            if (!child.isSelfHidden() && itemId > -1 && itemId != ItemID.BLANKOBJECT) {
                shown.add(new BankSnapshot.Shown(index, itemId, child.getItemQuantity()));
            }
        }
        return new BankSnapshot(shown, firstSpareChild, container.getWidth());
    }

    private void hide(Widget container, BankSnapshot snapshot) {
        for (BankSnapshot.Shown shown : snapshot.shown()) {
            Widget child = container.getChild(shown.child());
            if (child != null) {
                child.setHidden(true);
            }
        }
    }

    private void draw(Widget container, BankFrame frame) {
        for (int child : frame.children()) {
            SlotFace face = frame.at(child).orElseThrow(IllegalStateException::new);
            Widget widget = child >= firstSpareChild ? appendedSlot(container, child) : container.getChild(child);
            if (widget == null) {
                continue;
            }
            switch (face.kind()) {
                case OWNED:
                    placeOwned(widget, face);
                    break;
                case TWIN:
                    placeTwin(widget, container.getChild(face.templateChild()), face);
                    break;
                default:
                    placePlaceholder(widget, face);
                    break;
            }
        }
        drawStrip(container, frame);
    }

    /**
     * The game's own widget for an item the player has. Its id and menu are untouched so the game
     * still withdraws from the right bank slot; a face with a count hides the game's own so the
     * overlay can write held over required in its place.
     */
    private void placeOwned(Widget widget, SlotFace face) {
        widget.setOpacity(face.opacity());
        if (face.hasQuantity()) {
            widget.setItemQuantity(face.quantity());
            widget.setItemQuantityMode(ItemQuantityMode.NEVER);
        }
        position(widget, face);
    }

    /**
     * A further copy of an item the player has. It looks and acts like the game's own widget; the
     * click handler steers its withdraws to the real bank slot.
     */
    private void placeTwin(Widget widget, Widget template, SlotFace face) {
        if (template == null) {
            return;
        }
        widget.setItemId(template.getItemId());
        widget.setBorderType(template.getBorderType());
        widget.setName(template.getName());
        widget.clearActions();
        String[] actions = template.getActions();
        if (actions != null) {
            for (int index = 0; index < actions.length; index++) {
                if (actions[index] != null) {
                    widget.setAction(index, actions[index]);
                }
            }
        }
        widget.setOnDragListener((Object[]) null);
        widget.setOnDragCompleteListener((Object[]) null);
        widget.setOpacity(face.opacity());
        widget.setItemQuantity(face.quantity());
        widget.setItemQuantityMode(face.hideQuantity() ? ItemQuantityMode.NEVER : template.getItemQuantityMode());
        position(widget, face);
    }

    private void placePlaceholder(Widget widget, SlotFace face) {
        widget.setItemId(face.plan().shown().value());
        widget.setBorderType(1);
        widget.clearActions();
        widget.setOnDragListener((Object[]) null);
        widget.setOnDragCompleteListener((Object[]) null);
        widget.setOpacity(face.opacity());
        widget.setItemQuantity(face.quantity());
        widget.setItemQuantityMode(ItemQuantityMode.NEVER);
        widget.setName(face.name());
        position(widget, face);
    }

    private void position(Widget widget, SlotFace face) {
        widget.setHidden(false);
        widget.setOriginalX(face.x());
        widget.setOriginalY(face.y());
        widget.revalidate();
    }

    /**
     * The variant tabs along the top of the item area. The game rebuilds the layer's children every
     * build, so the strip is drawn afresh from children past the game's own.
     */
    private void drawStrip(Widget container, BankFrame frame) {
        if (frame.tabs().isEmpty()) {
            return;
        }
        int[] next = {frame.nextChild()};
        Widget wheel = as(appendedChild(container, next[0]++), WidgetType.TEXT);
        wheel.setText("");
        wheel.setNoScrollThrough(true);
        wheel.setHasListener(true);
        wheel.setOnScrollWheelListener((JavaScriptCallback) event -> display.scrollStrip(Integer.signum(event.getMouseY())));
        place(wheel, stripLeft(frame), 0, stripWidth(frame), BankGeometry.VARIANT_STRIP_HEIGHT);

        for (ArrowFace arrow : frame.arrows()) {
            Widget button = as(appendedChild(container, next[0]++), WidgetType.RECTANGLE);
            button.setFilled(true);
            button.setOpacity(TAB_OPACITY);
            button.setTextColor(TAB_FILL);
            button.setName("<col=ff9040>variants</col>");
            button.clearActions();
            button.setAction(FIRST_ACTION, arrow.forward() ? SCROLL_RIGHT : SCROLL_LEFT);
            button.setHasListener(true);
            button.setOnOpListener((JavaScriptCallback) event -> display.scrollStrip(arrow.step()));
            place(button, arrow.x(), arrow.y(), arrow.width(), arrow.height());
            place(text(appendedChild(container, next[0]++), arrow.glyph(), ARROW_TEXT),
                    arrow.x(), arrow.y(), arrow.width(), arrow.height());
        }

        for (TabFace tab : frame.tabs()) {
            Widget widget = as(appendedChild(container, next[0]++), WidgetType.RECTANGLE);
            widget.setFilled(true);
            widget.setOpacity(TAB_OPACITY);
            widget.setTextColor(tab.chosen() ? CHOSEN_FILL : TAB_FILL);
            widget.setName("<col=ff9040>" + tab.name() + "</col>");
            widget.clearActions();
            widget.setAction(FIRST_ACTION, SHOW_VARIANT);
            widget.setHasListener(true);
            widget.setOnOpListener((JavaScriptCallback) event -> display.chooseVariant(tab.variant()));
            place(widget, tab.x(), tab.y(), tab.width(), tab.height());
            place(text(appendedChild(container, next[0]++), tab.name(), tab.chosen() ? CHOSEN_TEXT : OTHER_TEXT),
                    tab.x(), tab.y(), tab.width(), tab.height());
        }
    }

    /** The strip catches the wheel across everything it drew, arrows included. */
    private static int stripLeft(BankFrame frame) {
        return frame.arrows().stream().mapToInt(ArrowFace::x)
                .min()
                .orElseGet(() -> frame.tabs().stream().mapToInt(TabFace::x).min().orElse(BankGeometry.ITEM_START_X));
    }

    private static int stripWidth(BankFrame frame) {
        int right = Math.max(
                frame.arrows().stream().mapToInt(arrow -> arrow.x() + arrow.width()).max().orElse(0),
                frame.tabs().stream().mapToInt(tab -> tab.x() + tab.width()).max().orElse(0));
        return Math.max(1, right - stripLeft(frame));
    }

    private static Widget text(Widget widget, String content, int colour) {
        Widget label = as(widget, WidgetType.TEXT);
        label.setText(content);
        label.setFontId(FontID.PLAIN_11);
        label.setTextShadowed(true);
        label.setTextColor(colour);
        label.setXTextAlignment(WidgetTextAlignment.CENTER);
        label.setYTextAlignment(WidgetTextAlignment.CENTER);
        label.setHasListener(false);
        label.clearActions();
        return label;
    }

    /** A spare child made into the kind of widget the strip needs, whatever it drew last build. */
    private static Widget as(Widget widget, int type) {
        widget.setType(type);
        widget.setItemId(-1);
        widget.setItemQuantity(0);
        widget.setSpriteId(-1);
        widget.setOpacity(0);
        widget.setBorderType(0);
        widget.setOnDragListener((Object[]) null);
        widget.setOnDragCompleteListener((Object[]) null);
        return widget;
    }

    private static void place(Widget widget, int x, int y, int width, int height) {
        widget.setOriginalX(x);
        widget.setOriginalY(y);
        widget.setOriginalWidth(width);
        widget.setOriginalHeight(height);
        widget.setHidden(false);
        widget.revalidate();
    }

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent event) {
        if (!display.isActive()) {
            return;
        }
        if (BUILD_TAB_CALLBACK.equals(event.getEventName())) {
            int[] stack = client.getIntStack();
            stack[client.getIntStackSize() - 1] = SINGLE_TAB_VIEW;
        } else if (SEARCH_FILTER_CALLBACK.equals(event.getEventName()) && display.dimsSearchMisses()) {
            keepEverythingVisibleWhileSearching();
        }
    }

    /**
     * The game hides items that miss the search; the layout should keep its shape, so every item is
     * reported as a match and the display fades the ones that miss instead.
     */
    private void keepEverythingVisibleWhileSearching() {
        Object[] strings = client.getObjectStack();
        int stringSize = client.getObjectStackSize();
        display.searching(stringSize > 0 && strings[stringSize - 1] instanceof String
                ? ((String) strings[stringSize - 1]).strip() : "");
        int[] stack = client.getIntStack();
        int size = client.getIntStackSize();
        if (size >= 2) {
            stack[size - 2] = 1;
        }
    }

    /** Withdraw scripts act on the slot in the menu entry, so a twin must point at the slot its item really occupies. */
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!display.isActive() || event.getParam1() != InterfaceID.Bankmain.ITEMS) {
            return;
        }
        Widget widget = event.getMenuEntry().getWidget();
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        // The strip sits in the item container too, and its widgets hold no item; an empty bank slot answers to -1.
        if (widget == null || bank == null || widget.getItemId() <= -1) {
            return;
        }
        int realSlot = bank.find(widget.getItemId());
        if (realSlot >= 0) {
            event.getMenuEntry().setParam0(realSlot);
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == InterfaceID.BANKMAIN) {
            bankOpen = true;
        }
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event) {
        if (event.getGroupId() == InterfaceID.BANKMAIN && event.isUnload()) {
            bankOpen = false;
            firstSpareChild = -1;
            display.searching("");
        }
    }

    /**
     * The game sizes the item slots once when the bank opens, and a previous layout may have grown
     * them, so they are put back before anything else is drawn.
     */
    private void restoreSlotSizes() {
        Widget container = itemContainer();
        if (container == null || container.getChildren() == null) {
            return;
        }
        for (Widget child : container.getChildren()) {
            if (child.getOriginalHeight() < BankGeometry.ITEM_HEIGHT) {
                break;
            }
            if (child.getOriginalWidth() != BankGeometry.ITEM_WIDTH
                    || child.getOriginalHeight() != BankGeometry.ITEM_HEIGHT) {
                child.setOriginalWidth(BankGeometry.ITEM_WIDTH);
                child.setOriginalHeight(BankGeometry.ITEM_HEIGHT);
                child.revalidate();
            }
        }
    }

    /** Children from an earlier frame survive the game's build, so anything not drawn again is hidden. */
    private void hideAppendedChildren(Widget container) {
        if (firstSpareChild < 0) {
            return;
        }
        int index = firstSpareChild;
        Widget child;
        while ((child = container.getChild(index++)) != null) {
            child.setHidden(true);
        }
    }

    private Widget appendedSlot(Widget container, int index) {
        Widget child = appendedChild(container, index);
        child.setType(WidgetType.GRAPHIC);
        child.setOriginalWidth(BankGeometry.ITEM_WIDTH);
        child.setOriginalHeight(BankGeometry.ITEM_HEIGHT);
        return child;
    }

    /** A child of ours past the game's own, made once and stripped of whatever it did last build before it is reshaped. */
    private static Widget appendedChild(Widget container, int index) {
        Widget child = container.getChild(index);
        if (child == null) {
            child = container.createChild(-1, WidgetType.GRAPHIC);
        }
        child.setHasListener(false);
        child.setOnOpListener((Object[]) null);
        child.setOnScrollWheelListener((Object[]) null);
        child.setNoScrollThrough(false);
        child.clearActions();
        child.setText("");
        child.setFilled(false);
        child.setName("");
        return child;
    }

    /**
     * The bank sizes its scroll area for the items it drew, not for ours, so the height argument is
     * overwritten while it is still on the script stack.
     */
    private void overrideScrollHeight(int height) {
        if (height <= 0) {
            return;
        }
        int[] stack = client.getIntStack();
        int size = client.getIntStackSize();
        if (size >= SCROLL_HEIGHT_STACK_OFFSET) {
            stack[size - SCROLL_HEIGHT_STACK_OFFSET] = height;
        } else {
            log.debug("Bank script stack too small to resize the scroll area");
        }
        clampScroll(height);
    }

    private void clampScroll(int height) {
        Widget container = itemContainer();
        if (container != null && container.getScrollY() > height) {
            scrollToTop();
        }
    }

    /** A fresh layout is read from its top, so the bank scrolls back up before the strip and first rows would be cut off. */
    private void scrollToTop() {
        Widget container = itemContainer();
        if (container == null) {
            return;
        }
        container.setScrollY(0);
        client.setVarcIntValue(VarClientID.BANK_SCROLLPOS, 0);
    }

    private Widget itemContainer() {
        return client.getWidget(InterfaceID.Bankmain.ITEMS);
    }
}
