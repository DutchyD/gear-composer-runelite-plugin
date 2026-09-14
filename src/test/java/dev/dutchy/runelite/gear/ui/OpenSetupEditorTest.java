package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.history.InMemoryHistoryStore;
import dev.dutchy.runelite.gear.history.SetupHistory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenSetupEditorTest {

    private static final SetupItem WHIP = SetupItem.of(4151);
    private static final SetupItem SHARK = SetupItem.of(385, 4);

    private final GearSetupBook book = new GearSetupBook();
    private final SetupHistory history = new SetupHistory(book, new InMemoryHistoryStore(), Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneOffset.UTC));
    private final List<String> causes = new ArrayList<>();
    private final List<ContentViewState> focuses = new ArrayList<>();
    private int gone;
    private final OpenSetupEditor editor = new OpenSetupEditor(book, history, new OpenSetupEditor.Listener() {
        @Override
        public void changed(String cause, UnaryOperator<ContentViewState> focus) {
            causes.add(cause);
            focuses.add(focus.apply(ContentViewState.initial()));
        }

        @Override
        public void gone() {
            gone++;
        }
    });
    private final GearSetup setup = book.addSetup(book.sections().get(0).id(), "Vorkath");

    private SetupContent content() {
        return book.setup(setup.id()).orElseThrow().content();
    }

    private static List<String> names(GearSetup setup) {
        return setup.variants().stream().map(SetupVariant::name).collect(Collectors.toList());
    }

    @Test
    void slotChangesAreRecordedWithACauseAndIdenticalOnesAreDropped() {
        editor.open(setup.id());
        editor.setItem(SlotRef.of(EquipmentSlot.WEAPON), WHIP);
        editor.setItem(SlotRef.of(EquipmentSlot.WEAPON), WHIP);
        editor.moveItem(SlotRef.of(EquipmentSlot.WEAPON), SlotRef.of(GridKind.INVENTORY, 0), true);
        editor.clearSlots(List.of(SlotRef.of(EquipmentSlot.WEAPON)));

        assertEquals(List.of("Set Weapon", "Copied to Inventory slot 1", "Emptied 1 slot(s)"), causes);
        assertEquals(3, history.revisions(setup.id()).size());
        GearContent gear = (GearContent) content();
        assertTrue(gear.equipped(EquipmentSlot.WEAPON).isEmpty());
        assertEquals(WHIP, gear.inventory().slot(0).orElseThrow());
        assertEquals(0, gone);
    }

    @Test
    void copiedSlotsPasteIntoWhateverSetupIsOpen() {
        GearSetup other = book.addSetup(book.sections().get(0).id(), "Zulrah");
        editor.open(setup.id());
        editor.setItem(SlotRef.of(GridKind.INVENTORY, 0), SHARK);
        assertFalse(editor.canPaste());
        assertEquals(1, editor.copy(List.of(SlotRef.of(GridKind.INVENTORY, 0))));
        assertTrue(editor.canPaste());

        editor.open(other.id());
        editor.paste(SlotRef.of(GridKind.INVENTORY, 5));
        assertEquals(SHARK, ((GearContent) book.setup(other.id()).orElseThrow().content()).inventory().slot(5).orElseThrow());
        assertEquals("Pasted at Inventory slot 6", causes.get(causes.size() - 1));
    }

    @Test
    void variantsAreAddedAsCopiesRenamedSelectedAndRemoved() {
        editor.open(setup.id());
        editor.setItem(SlotRef.of(EquipmentSlot.WEAPON), WHIP);
        assertEquals(Optional.of("Default (2)"), editor.freeVariantName("Default"));
        assertEquals(Optional.of("There is already a variant named Default"), editor.newVariantNameProblem(" default "));
        assertTrue(editor.newVariantNameProblem("").isPresent());
        assertTrue(editor.renamedVariantNameProblem(0, "Default").isEmpty(), "a variant may keep its own name");

        editor.addVariant("Mage", content());
        GearSetup withMage = book.setup(setup.id()).orElseThrow();
        assertEquals("Added variant Mage", causes.get(causes.size() - 1));
        assertEquals(0, withMage.selectedIndex(), "the bank keeps showing the first variant");
        assertEquals(1, editor.editingIndex(), "while the page opens the new one");
        assertEquals(content(), withMage.variants().get(1).content(), "which holds what it was given");
        assertTrue(editor.newVariantNameProblem("Mage").isPresent());
        assertTrue(editor.renamedVariantNameProblem(1, "Default").isPresent());

        editor.setItem(SlotRef.of(EquipmentSlot.WEAPON), SetupItem.of(11791));
        assertEquals(SetupItem.of(11791), ((GearContent) book.setup(setup.id()).orElseThrow().variants().get(1).content()).equipped(EquipmentSlot.WEAPON).orElseThrow(),
                "an edit goes to the variant on the page");
        assertEquals(WHIP, ((GearContent) book.setup(setup.id()).orElseThrow().content()).equipped(EquipmentSlot.WEAPON).orElseThrow(),
                "and leaves the shown one alone");

        editor.renameVariant(1, "Magic");
        assertEquals("Magic", book.setup(setup.id()).orElseThrow().variants().get(1).name());

        editor.duplicateVariant(0);
        GearSetup duplicated = book.setup(setup.id()).orElseThrow();
        assertEquals(List.of("Default", "Default (2)", "Magic"), names(duplicated));
        assertEquals("Added variant Default (2)", causes.get(causes.size() - 1));
        assertEquals(0, duplicated.selectedIndex(), "the bank's choice stays");
        assertEquals(1, editor.editingIndex(), "the copy sits after its original and is opened on the page");
        assertEquals(WHIP, ((GearContent) editor.content().orElseThrow()).equipped(EquipmentSlot.WEAPON).orElseThrow());

        editor.moveVariant(1, 2);
        assertEquals(List.of("Default", "Magic", "Default (2)"), names(book.setup(setup.id()).orElseThrow()));
        assertEquals(2, editor.editingIndex(), "the page follows the moved variant");
        assertEquals(0, book.setup(setup.id()).orElseThrow().selectedIndex());
        editor.moveVariant(0, 5);
        assertEquals(List.of("Default", "Magic", "Default (2)"), names(book.setup(setup.id()).orElseThrow()), "a move off the end is ignored");
        editor.editVariant(1);
        assertEquals("Editing Magic", causes.get(causes.size() - 1));
        assertEquals(1, editor.editingIndex());
        assertEquals(0, book.setup(setup.id()).orElseThrow().selectedIndex(), "opening a variant on the page does not show it");
        editor.editVariant(9);
        assertEquals(1, editor.editingIndex(), "an unknown variant is ignored");
        editor.removeVariant(2);
        assertEquals(1, editor.editingIndex(), "removing a later variant leaves the page where it was");
        editor.selectVariant(1);
        assertEquals("Showing Magic", causes.get(causes.size() - 1));
        editor.selectVariant(1);
        assertEquals(1, book.setup(setup.id()).orElseThrow().selectedIndex());
        editor.selectVariant(9);
        assertEquals(1, book.setup(setup.id()).orElseThrow().selectedIndex(), "an unknown variant is ignored");

        editor.removeVariant(0);
        GearSetup afterRemoval = book.setup(setup.id()).orElseThrow();
        assertEquals("Magic", afterRemoval.variant().name());
        assertEquals(0, editor.editingIndex(), "removing an earlier variant shifts the page with it");
        editor.removeVariant(0);
        assertEquals(1, book.setup(setup.id()).orElseThrow().variants().size(), "the last variant stays");
        assertEquals(2, history.revisions(setup.id()).size(), "only the two item edits are history; variant changes are not");

        while (editor.canAddVariant()) {
            editor.addVariant(editor.freeVariantName("Magic").orElseThrow(), GearContent.empty());
        }
        assertEquals(GearSetup.MAX_VARIANTS, book.setup(setup.id()).orElseThrow().variants().size());
        assertEquals(0, gone);
    }

    @Test
    void cellsChangeKindNameAndItemsAndComeIntoViewAfterwards() {
        GearSetup custom = book.addSetup(book.sections().get(0).id(), GearSetup.named("Trip").withContent(CustomContent.empty(2)));
        editor.open(custom.id());
        CellRef ref = CellRef.of(1, 0);
        editor.setCellKind(ref, CellKind.EQUIPMENT);
        editor.renameCell(ref, "Melee");
        editor.fillCellFromGame(ref, new Loadout(Map.of(EquipmentSlot.WEAPON, WHIP), ItemGrid.EMPTY));
        LayoutCell cell = editor.cellAt(ref).orElseThrow();
        assertEquals(CellKind.EQUIPMENT, cell.kind());
        assertEquals("Melee", cell.label());
        assertEquals(WHIP, cell.equipment().get(EquipmentSlot.WEAPON));
        assertEquals(ref, focuses.get(focuses.size() - 1).cell().orElseThrow());
        assertEquals(1, focuses.get(focuses.size() - 1).row());

        editor.fillCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SHARK)), "Vorkath");
        assertEquals("Vorkath", editor.cellAt(CellRef.of(0, 1)).orElseThrow().label());
        editor.clearCell(ref);
        assertTrue(editor.cellAt(ref).orElseThrow().isEmpty());
        assertEquals(CellKind.EQUIPMENT, editor.cellAt(ref).orElseThrow().kind());
        editor.emptyCell(ref);
        assertEquals(CellKind.EMPTY, editor.cellAt(ref).orElseThrow().kind());
        assertTrue(editor.cellAt(CellRef.of(2, 0)).isEmpty(), "a row the layout does not have");
    }

    @Test
    void dividersSyncAndRestoreGoThroughTheHistory() {
        editor.open(setup.id());
        SlotRef rowSlot = SlotRef.of(GridKind.INVENTORY, 4);
        editor.setDivider(rowSlot, Divider.acrossRow(1, "Food"));
        assertEquals("Food", editor.dividerAt(rowSlot).orElseThrow().label());
        editor.changeDivider(rowSlot, DividerChange.TEXT_CENTRE);
        assertEquals(TextAlign.CENTRE, editor.dividerAt(rowSlot).orElseThrow().align());
        assertEquals("Divider: text centre", causes.get(causes.size() - 1));
        editor.changeDivider(rowSlot, DividerChange.NO_UNDERLINE);
        assertFalse(editor.dividerAt(rowSlot).orElseThrow().underlined());
        editor.setDivider(rowSlot, new Divider(1, 0, 1, "Supplies", TextAlign.LEFT));
        assertEquals(List.of("Supplies"), editor.dividersAbove(rowSlot).stream().map(Divider::label).collect(Collectors.toList()),
                "setting from a covered column replaces the divider there");
        editor.setDivider(SlotRef.of(GridKind.INVENTORY, 7), new Divider(1, 1, 3, "Pots", TextAlign.RIGHT));
        assertEquals(List.of(0, 1), List.of(editor.dividersAbove(rowSlot).get(0).toColumn(), editor.dividersAbove(rowSlot).get(1).fromColumn()),
                "a divider added from a free column trims the neighbour it reaches into");
        editor.removeDivider(rowSlot);
        assertEquals(1, editor.dividersAbove(rowSlot).size(), "only the divider over that column goes");
        assertTrue(editor.dividerAt(rowSlot).isEmpty());

        editor.sync(SyncScope.GEAR, new Loadout(Map.of(EquipmentSlot.WEAPON, WHIP), ItemGrid.EMPTY.withSlot(0, SHARK)), "Vorkath synced from the game");
        assertEquals(WHIP, ((GearContent) content()).equipped(EquipmentSlot.WEAPON).orElseThrow());

        editor.restore(history.revisions(setup.id()).get(0));
        assertTrue(((GearContent) content()).equipped(EquipmentSlot.WEAPON).isEmpty());
        assertEquals("Restored Vorkath", causes.get(causes.size() - 1));
    }

    @Test
    void aSetupThatIsGoneIsReportedNotEdited() {
        editor.open(setup.id());
        book.removeSetup(setup.id());
        editor.setItem(SlotRef.of(EquipmentSlot.WEAPON), WHIP);
        editor.restore(null);
        assertEquals(2, gone);
        assertTrue(causes.isEmpty());
        assertTrue(editor.setup().isEmpty());
        assertTrue(editor.isOpen(setup.id()));
        editor.close();
        assertTrue(editor.id().isEmpty());
        assertFalse(editor.isOpen(setup.id()));
    }
}
