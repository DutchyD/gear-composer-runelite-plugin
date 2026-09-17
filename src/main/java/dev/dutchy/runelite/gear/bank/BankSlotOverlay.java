package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.config.GearComposerConfig;
import dev.dutchy.runelite.gear.content.ItemQuantityFormat;
import dev.dutchy.runelite.gear.items.ItemCharges;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Draws over laid-out bank slots: held over required for every slot with an amount, an N for noted
 * slots, the tab an item really lives in, and a hover tooltip breaking a family down by dose or charge.
 */
public final class BankSlotOverlay extends WidgetItemOverlay {

    static final Color SHORT = new Color(255, 92, 92);
    /** The family covers the need but the drawn stack alone does not. */
    static final Color PARTIAL = new Color(255, 190, 70);
    private static final Color NOTE = new Color(120, 190, 255);
    private static final Color TAB = new Color(190, 190, 190);
    private static final Color SHADOW = Color.BLACK;
    private static final String LINE = "</br>";

    private final Client client;
    private final DrawnBankSource bank;
    private final GearComposerConfig config;
    private final TooltipManager tooltips;
    private final ItemCharges charges;
    private final BankItemNames names;
    private final BankTabSizes tabSizes;

    @Inject
    public BankSlotOverlay(Client client, DrawnBankSource bank, GearComposerConfig config, TooltipManager tooltips,
                           ItemCharges charges, BankItemNames names, BankTabSizes tabSizes) {
        this.client = Objects.requireNonNull(client, "client");
        this.bank = Objects.requireNonNull(bank, "bank");
        this.config = Objects.requireNonNull(config, "config");
        this.tooltips = Objects.requireNonNull(tooltips, "tooltips");
        this.charges = Objects.requireNonNull(charges, "charges");
        this.names = Objects.requireNonNull(names, "names");
        this.tabSizes = Objects.requireNonNull(tabSizes, "tabSizes");
        showOnBank();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem item) {
        DrawnBank shown = bank.drawnBank();
        if (!shown.active() || item.getWidget() == null || GroupStorage.isOpen(client)) {
            return;
        }
        Optional<DrawnSlot> drawn = shown.at(item.getWidget().getIndex());
        if (drawn.isEmpty()) {
            return;
        }
        DrawnSlot slot = drawn.get();
        Rectangle bounds = item.getCanvasBounds();
        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics metrics = graphics.getFontMetrics();

        if (slot.plan().item().hasQuantity()) {
            draw(graphics, amountText(slot), bounds.x + 1, bounds.y + metrics.getAscent(), amountColor(slot));
        }
        if (slot.plan().item().noted()) {
            draw(graphics, "N", bounds.x + bounds.width - metrics.stringWidth("N") - 1, bounds.y + metrics.getAscent(), NOTE);
        }
        if (config.tabBadges() && slot.inBank()) {
            int tab = BankTabs.tabOf(slot.bankIndex(), tabSizes.counts());
            if (tab != BankTabs.MAIN_TAB) {
                String text = "T" + tab;
                draw(graphics, text, bounds.x + bounds.width - metrics.stringWidth(text) - 1, bounds.y + bounds.height - 1, TAB);
            }
        }
        if (slot.plan().supply().hasOthers() && bounds.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY())) {
            tooltips.add(new Tooltip(tooltipText(slot, names::of, charges)));
        }
    }

    /** Held across the family over required, both the way the game writes stack sizes. */
    static String amountText(DrawnSlot slot) {
        return ItemQuantityFormat.text(slot.held()) + "/" + ItemQuantityFormat.text(slot.required());
    }

    /** Red while short, amber while only the other doses make up the need, otherwise the colour the game gives a stack of that size. */
    static Color amountColor(DrawnSlot slot) {
        if (slot.isShort()) {
            return SHORT;
        }
        return slot.isShownShort() ? PARTIAL : ItemQuantityFormat.color(slot.held());
    }

    /** One line per held stack, the drawn one first, then the family total with its doses or charges when they are known. */
    static String tooltipText(DrawnSlot slot, Function<ItemId, String> names, ItemCharges charges) {
        List<String> lines = new ArrayList<>();
        int doses = 0;
        boolean dosesKnown = true;
        for (SlotSupply.Stack stack : slot.plan().supply().stacks()) {
            String line = names.apply(stack.id()) + " x" + ItemQuantityFormat.text(stack.count());
            lines.add(lines.isEmpty() ? "<col=ff9040>" + line + "</col>" : line);
            if (charges.chargesOf(stack.id()).isPresent()) {
                doses += charges.chargesOf(stack.id()).getAsInt() * stack.count();
            } else {
                dosesKnown = false;
            }
        }
        String total = "Total " + ItemQuantityFormat.text(slot.held()) + (dosesKnown ? " · " + ItemQuantityFormat.text(doses) + " doses/charges" : "");
        lines.add(total);
        lines.add("Right-click to show another here");
        return String.join(LINE, lines);
    }

    private static void draw(Graphics2D graphics, String text, int x, int y, Color color) {
        graphics.setColor(SHADOW);
        graphics.drawString(text, x + 1, y + 1);
        graphics.setColor(color);
        graphics.drawString(text, x, y);
    }
}
