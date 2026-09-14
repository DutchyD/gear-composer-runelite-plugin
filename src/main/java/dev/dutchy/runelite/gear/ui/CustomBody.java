package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.CellRef;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** A map of the grid on top, then one row of cells at a time, chosen by tab or by clicking the map. */
final class CustomBody extends ContentBody {

    private final CustomContent custom;
    private final ItemIconFactory icons;
    private final SlotView.Listener listener;
    private final CellActions cellActions;
    private final EquipmentSlotArtwork artwork;
    private final LayoutMap map;
    private final JPanel rowStrip = Ui.panel(new FlowLayout(FlowLayout.LEFT, Ui.SMALL_GAP, 0));
    private final List<Chip> rowChips = new ArrayList<>();
    private final JPanel cellArea = Ui.column(0);
    private final List<CellView> cellViews = new ArrayList<>();
    private int shownRow;

    CustomBody(CustomContent custom, ItemIconFactory icons, EquipmentSlotArtwork artwork, SlotView.Listener listener, CellActions cellActions) {
        this.custom = custom;
        this.icons = icons;
        this.artwork = artwork;
        this.listener = listener;
        this.cellActions = cellActions;
        map = Help.describe(new LayoutMap(custom, this::showCell), HelpTopic.LAYOUT_MAP);
        add(centered(map));
        add(Box.createVerticalStrut(Ui.GAP));
        if (custom.rows() > 1) {
            add(rowStripRow());
        }
        cellArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(cellArea);
        showRow(0);
    }

    @Override
    List<SlotView> slots() {
        List<SlotView> slots = new ArrayList<>();
        cellViews.forEach(view -> slots.addAll(view.slots()));
        return slots;
    }

    @Override
    void show(ContentViewState view) {
        if (view.row() != shownRow) {
            showRow(view.row());
        }
        view.cell().ifPresent(this::showCell);
    }

    @Override
    ContentViewState remember(ContentViewState view) {
        return map.current() == null ? view.withRow(shownRow).withCell(null) : view.at(map.current());
    }

    @Override
    void clicked(SlotRef ref) {
        if (ref instanceof SlotRef.Cell) {
            map.setCurrent(((SlotRef.Cell) ref).cell());
        }
    }

    @Override
    void showRowWithCell(boolean blank) {
        for (CellRef ref : custom.cellRefs()) {
            if (custom.cell(ref).isBlank() == blank) {
                if (ref.row() != shownRow) {
                    showRow(ref.row());
                }
                return;
            }
        }
    }

    @Override
    List<CellView> cellViews() {
        return List.copyOf(cellViews);
    }

    /** Shows one row of the grid: its two cells, stacked. */
    private void showRow(int row) {
        shownRow = Math.max(0, Math.min(custom.rows() - 1, row));
        cellViews.clear();
        cellArea.removeAll();
        boolean filledAnchored = false;
        boolean blankAnchored = false;
        for (int column = 0; column < CustomContent.COLUMNS; column++) {
            CellRef ref = CellRef.of(shownRow, column);
            CellView view = new CellView(ref, custom.cell(ref), icons, artwork, listener, cellActions);
            if (custom.cell(ref).isBlank() && !blankAnchored) {
                Help.anchor(view, HelpTopic.EMPTY_CELL);
                blankAnchored = true;
            } else if (!custom.cell(ref).isBlank() && !filledAnchored) {
                Help.anchor(view, HelpTopic.CELL);
                filledAnchored = true;
            }
            cellViews.add(view);
            cellArea.add(view);
        }
        for (int i = 0; i < rowChips.size(); i++) {
            rowChips.get(i).setChosen(i == shownRow);
        }
        cellArea.revalidate();
        cellArea.repaint();
    }

    /** Marks a cell on the map, switches to its row, and brings it into view. */
    private void showCell(CellRef ref) {
        if (ref.row() != shownRow) {
            showRow(ref.row());
        }
        map.setCurrent(ref);
        cellViews.stream().filter(view -> view.ref().equals(ref)).findFirst()
                .ifPresent(view -> view.scrollRectToVisible(new Rectangle(0, 0, view.getWidth(), view.getHeight())));
    }

    private JPanel rowStripRow() {
        rowStrip.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int row = 0; row < custom.rows(); row++) {
            int index = row;
            Chip chip = Chip.sized("Row " + (row + 1), "Show row " + (row + 1) + " of the grid", () -> showRow(index));
            rowChips.add(chip);
            rowStrip.add(chip);
        }
        return Help.anchor(rowStrip, HelpTopic.ROW_TABS);
    }
}
