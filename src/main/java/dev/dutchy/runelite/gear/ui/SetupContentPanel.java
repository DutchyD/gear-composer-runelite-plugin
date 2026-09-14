package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Prayer;
import dev.dutchy.runelite.gear.Requirements;
import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.gear.ledger.ItemFactsSource;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.selector.ItemRowDragListener;
import dev.dutchy.runelite.libs.ui.selector.ItemSelector;
import dev.dutchy.runelite.libs.ui.selector.ItemSelectorFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

final class SetupContentPanel extends JPanel implements SlotView.Listener {

    static final String SYNC_GEAR = "Sync from game";
    static final String SYNC_SIDE = "Sync inventory";
    private static final int SEARCH_RESULTS = 5;

    private final ContentActions actions;
    private final SlotSelection selection = new SlotSelection();
    private final SlotDragController drags;
    private final ItemSelector finder;
    private final JPanel finderStrip;
    private final JPanel selectionStrip = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
    private final JLabel selectionCount = Ui.body("");
    private final FlatButton pasteButton;
    private final LedgerView ledger;
    private final ContentBody body;
    private final VariantsPanel variants;

    SetupContentPanel(GearSetup setup,
                      int editing,
                      ItemIconFactory icons,
                      EquipmentSlotArtwork artwork,
                      ItemSelectorFactory selectors,
                      DropRule dropRule,
                      ContentActions actions,
                      CellActions cellActions,
                      ItemFactsSource facts,
                      ContentViewState view) {
        Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(view, "view");
        Objects.requireNonNull(selectors, "selectors");
        Objects.requireNonNull(icons, "icons");
        SetupContent content = setup.variantAt(editing).map(SetupVariant::content).orElse(setup.content());
        this.actions = Objects.requireNonNull(actions, "actions");
        Objects.requireNonNull(cellActions, "cellActions");
        Objects.requireNonNull(facts, "facts");
        this.ledger = Help.anchor(new LedgerView(content, facts, false), HelpTopic.LEDGER);
        Help.anchor(this, HelpTopic.CONTENTS_PAGE);
        this.drags = new SlotDragController(this, this::slotAt, dropRule, actions);
        this.pasteButton = new FlatButton("Paste", "Put copied items down from the first selected slot (Ctrl+V)", this::pasteSelection);

        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        finder = selectors.selector()
                .limit(SEARCH_RESULTS)
                .onRowDrag(finderDrag())
                .keepResultsOnSelect()
                .onSelect(this::finderPicked)
                .build();
        finderStrip = finderStrip();

        body = ContentBody.of(content, icons, Objects.requireNonNull(artwork, "artwork"), this, actions, cellActions);
        variants = new VariantsPanel(setup, editing, icons, facts, actions, view.variantsOpen());

        JPanel header = Ui.panel(new BorderLayout(0, Ui.SMALL_GAP));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, Ui.SMALL_GAP, 0));
        header.add(navBar(setup), BorderLayout.NORTH);
        header.add(Ui.column(Ui.SMALL_GAP, variants, toolbar(body.toolbarAction().orElse(null)), notesLine(setup), requirementsLine(setup.meta().requirements()),
                finderStrip, selectionStrip()), BorderLayout.CENTER);

        JPanel middle = Ui.column(0, body, ledger);
        add(header, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        installShortcuts();
        refreshSelectionStrip();
        show(view);
    }

    /** Where the page is looking now, to carry over to the page that replaces it. */
    ContentViewState viewState() {
        return body.remember(ContentViewState.initial()).withVariantsOpen(variants.isOpen()).withLedgerOpen(ledger.isOpen())
                .withFinder(finderStrip.isVisible(), finder.query());
    }

    private void show(ContentViewState view) {
        body.show(view);
        if (view.ledgerOpen() != ledger.isOpen()) {
            ledger.setOpen(view.ledgerOpen());
        }
        if (view.finderOpen()) {
            finderStrip.setVisible(true);
            if (!view.finderQuery().isBlank()) {
                finder.setQuery(view.finderQuery());
            }
        }
    }

    LedgerView ledger() {
        return ledger;
    }

    /** Recomputes every total on the page, as when item facts arrive. */
    void refreshLedger() {
        ledger.refresh();
        variants.refresh();
    }

    VariantsPanel variants() {
        return variants;
    }

    int shownRow() {
        return viewState().row();
    }

    Optional<CellRef> currentCell() {
        return viewState().cell();
    }

    List<CellView> cellViews() {
        return body.cellViews();
    }

    void showCell(CellRef ref) {
        body.show(viewState().at(ref));
    }

    void showRowWithCell(boolean blank) {
        body.showRowWithCell(blank);
    }

    List<SlotView> slots() {
        return body.slots();
    }

    List<SlotRef> selectedSlots() {
        return selection.ordered(slotOrder());
    }

    boolean isFinderShowing() {
        return finderStrip.isVisible();
    }

    ItemSelector finder() {
        return finder;
    }

    SlotDragController drags() {
        return drags;
    }

    /** Selects as a click would: shift extends from the anchor, control toggles. */
    void select(SlotRef ref, boolean shift, boolean control) {
        selection.click(ref, shift, control, slotOrder());
        refreshSelection();
    }

    void clearSelection() {
        selection.clear();
        refreshSelection();
    }

    void copySelection() {
        if (!selection.isEmpty()) {
            actions.copySlots(selectedSlots());
        }
    }

    void pasteSelection() {
        selection.anchor().ifPresent(actions::pasteAt);
    }

    void deleteSelection() {
        if (!selection.isEmpty()) {
            actions.clearSlots(selectedSlots());
        }
    }

    void toggleFinder() {
        finderStrip.setVisible(!finderStrip.isVisible());
        if (finderStrip.isVisible()) {
            finder.requestFocusInWindow();
        }
        revalidate();
        repaint();
    }

    @Override
    public void clicked(SlotView view, MouseEvent event) {
        if (drags.isDragging()) {
            return;
        }
        body.clicked(view.ref());
        if (event.isShiftDown() || event.isControlDown()) {
            select(view.ref(), event.isShiftDown(), event.isControlDown());
            return;
        }
        if (!selection.isEmpty()) {
            clearSelection();
        }
        actions.editSlot(view.ref());
    }

    @Override
    public void pressed(SlotView view, MouseEvent event) {
        if (view.item().isPresent() && !event.isShiftDown() && !event.isControlDown()) {
            drags.press(new SlotDragController.FromSlot(view), view, event);
        }
    }

    @Override
    public void dragged(SlotView view, MouseEvent event) {
        drags.drag(view, event);
    }

    @Override
    public void released(SlotView view, MouseEvent event) {
        drags.release(view, event);
    }

    @Override
    public void edit(SlotRef ref) {
        actions.editSlot(ref);
    }

    @Override
    public void clear(SlotRef ref) {
        actions.clearSlot(ref);
    }

    @Override
    public void fillRemaining(SlotRef from) {
        actions.fillRemaining(from);
    }

    @Override
    public void fillRow(SlotRef from) {
        actions.fillRow(from);
    }

    @Override
    public void editDivider(SlotRef rowSlot) {
        actions.editDivider(rowSlot);
    }

    @Override
    public void removeDivider(SlotRef rowSlot) {
        actions.removeDivider(rowSlot);
    }

    @Override
    public void changeDivider(SlotRef slot, DividerChange change) {
        actions.changeDivider(slot, change);
    }

    private ItemRowDragListener finderDrag() {
        return new ItemRowDragListener() {
            @Override
            public void pressed(ResolvedItem item, MouseEvent event) {
                drags.press(new SlotDragController.FromSearch(item), (JComponent) event.getComponent(), event);
            }

            @Override
            public void dragged(ResolvedItem item, MouseEvent event) {
                drags.drag((JComponent) event.getComponent(), event);
            }

            @Override
            public void released(ResolvedItem item, MouseEvent event) {
                drags.release((JComponent) event.getComponent(), event);
            }
        };
    }

    /** Clicking a result with exactly one slot selected fills that slot. */
    private void finderPicked(ResolvedItem item) {
        List<SlotRef> selected = selectedSlots();
        if (selected.size() == 1) {
            actions.dropItem(item, selected.get(0));
        }
    }

    private SlotView slotAt(Point inPanel) {
        for (SlotView slot : body.slots()) {
            if (slot.getParent() == null) {
                continue;
            }
            Rectangle bounds = SwingUtilities.convertRectangle(slot.getParent(), slot.getBounds(), this);
            if (bounds.contains(inPanel)) {
                return slot;
            }
        }
        return null;
    }

    private List<SlotRef> slotOrder() {
        return body.slots().stream().map(SlotView::ref).collect(Collectors.toList());
    }

    private void refreshSelection() {
        for (SlotView slot : body.slots()) {
            slot.setSelected(selection.contains(slot.ref()));
        }
        refreshSelectionStrip();
    }

    private void refreshSelectionStrip() {
        selectionStrip.setVisible(!selection.isEmpty());
        selectionCount.setText(selection.size() + " selected");
        pasteButton.setEnabled(actions.canPaste());
        revalidate();
        repaint();
    }

    private JPanel selectionStrip() {
        selectionStrip.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        Help.anchor(selectionStrip, HelpTopic.SELECTION);
        JPanel buttons = Ui.panel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttons.add(new FlatButton("Copy", "Copy the selected items (Ctrl+C)", this::copySelection));
        buttons.add(pasteButton);
        buttons.add(new FlatButton("Clear", "Empty the selected slots (Delete)", this::deleteSelection));
        selectionStrip.add(selectionCount, BorderLayout.CENTER);
        selectionStrip.add(buttons, BorderLayout.EAST);
        return selectionStrip;
    }

    private JPanel finderStrip() {
        JPanel strip = Ui.panel(new BorderLayout());
        strip.add(Help.anchor(finder, HelpTopic.FINDER), BorderLayout.CENTER);
        strip.setVisible(false);
        return strip;
    }

    private JPanel navBar(GearSetup setup) {
        JPanel bar = NavBar.create(setup.name(), actions::back);
        JPanel tools = Ui.panel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        FlatButton find = new FlatButton(ActionIcon.SEARCH, "Find items to drag into slots", this::toggleFinder);
        FlatButton compare = new FlatButton(ActionIcon.GRID, "Compare with another setup or with what you wear", actions::compare);
        FlatButton share = new FlatButton(ActionIcon.COPY, "Share as an image or text", actions::share);
        FlatButton history = new FlatButton(ActionIcon.CLOCK, "Earlier versions of this setup", actions::showHistory);
        for (FlatButton button : List.of(find, compare, share, history)) {
            button.setPreferredSize(new Dimension(24, 24));
            tools.add(button);
        }
        Help.anchor(tools, HelpTopic.CONTENTS_TOOLS);
        bar.add(tools, BorderLayout.EAST);
        return bar;
    }

    private static JPanel notesLine(GearSetup setup) {
        JPanel line = Ui.panel(new BorderLayout());
        if (!setup.meta().hasNotes()) {
            line.setVisible(false);
            return line;
        }
        WrappedText notes = Ui.paragraph(setup.meta().notes());
        notes.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        line.add(Ui.hint("Notes "), BorderLayout.WEST);
        line.add(notes, BorderLayout.CENTER);
        return line;
    }

    private static JPanel requirementsLine(Requirements requirements) {
        JPanel line = Ui.panel(new BorderLayout());
        if (requirements.isEmpty()) {
            line.setVisible(false);
            return line;
        }
        List<String> parts = new ArrayList<>();
        if (requirements.spellbook().isRequirement()) {
            parts.add(requirements.spellbook().displayName() + " spellbook");
        }
        if (requirements.hasQuickPrayers()) {
            parts.add("Quick prayers: " + Prayer.describe(requirements.quickPrayers()));
        }
        String joined = String.join(" · ", parts);
        WrappedText text = Ui.paragraph(joined);
        text.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        line.add(Ui.hint("Needs "), BorderLayout.WEST);
        line.add(text, BorderLayout.CENTER);
        return line;
    }

    private void installShortcuts() {
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy", this::copySelection);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "paste", this::pasteSelection);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete", this::deleteSelection);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "deselect", this::clearSelection);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "find", this::toggleFinder);
    }

    private void bind(KeyStroke key, String name, Runnable action) {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private static JPanel toolbar(JComponent action) {
        JPanel row = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        JLabel hint = Ui.hint("Click a slot to edit");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        row.add(hint, BorderLayout.CENTER);
        if (action != null) {
            row.add(action, BorderLayout.EAST);
        }
        return row;
    }

}
