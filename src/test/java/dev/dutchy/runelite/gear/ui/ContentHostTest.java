package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.content.CellKind;
import dev.dutchy.runelite.gear.content.CellRef;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.LayoutCell;
import dev.dutchy.runelite.gear.content.Loadout;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.gear.content.SyncScope;
import dev.dutchy.runelite.gear.history.InMemoryHistoryStore;
import dev.dutchy.runelite.gear.history.SetupHistory;
import dev.dutchy.runelite.gear.history.SetupRevision;
import dev.dutchy.runelite.gear.player.PlayerItems;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentHostTest {

    private static final Loadout WORN = new Loadout(
            Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151)),
            ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 3)));

    private final GearSetupBook book = new GearSetupBook();
    private final SetupHistory history = new SetupHistory(book, new InMemoryHistoryStore(),
            Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneOffset.UTC));
    private final RecordingPrompts prompts = new RecordingPrompts();
    private final List<String> causes = new ArrayList<>();
    private final List<String> backToContents = new ArrayList<>();
    private Loadout loadout = WORN;
    private boolean confirmBeforeSync = true;

    private final PlayerItems playerItems = (onCaptured, onUnavailable) ->
            Optional.ofNullable(loadout).ifPresentOrElse(onCaptured, onUnavailable);

    private final OpenSetupEditor editor = new OpenSetupEditor(book, history, new OpenSetupEditor.Listener() {
        @Override
        public void changed(String cause, UnaryOperator<ContentViewState> focus) {
            causes.add(cause);
        }

        @Override
        public void gone() {
            causes.add("gone");
        }
    });

    private final ContentHost content = new ContentHost(editor, history, playerItems, () -> confirmBeforeSync);

    ContentHostTest() {
        content.shownBy(prompts, () -> backToContents.add("contents"));
    }

    private GearSetup open(GearSetup setup) {
        GearSetup added = book.addSetup(book.sections().get(0).id(), setup);
        editor.open(added.id());
        return added;
    }

    private GearSetup reload(GearSetup setup) {
        return book.setup(setup.id()).orElseThrow();
    }

    private List<String> variantNames(GearSetup setup) {
        return reload(setup).variants().stream().map(SetupVariant::name).collect(Collectors.toList());
    }

    @Test
    void aNewVariantIsNamedWithSomethingNothingElseIsUsing() {
        GearSetup vorkath = open(GearSetup.named("Vorkath"));

        content.addVariant(NewVariant.COPY);

        assertEquals(List.of(ContentHost.NEW_VARIANT_NAME), prompts.questions());
        assertEquals(2, variantNames(vorkath).size(), "the suggested name was taken as typed");
    }

    @Test
    void backingOutOfTheNameAddsNothing() {
        GearSetup vorkath = open(GearSetup.named("Vorkath"));
        prompts.typing(null);

        content.addVariant(NewVariant.COPY);

        assertEquals(1, variantNames(vorkath).size());
    }

    @Test
    void aCopiedVariantHoldsWhatTheShownOneDoesAndAnEmptyOneHoldsNothing() {
        GearSetup vorkath = open(GearSetup.named("Vorkath")
                .withContent(GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151))));

        content.addVariant(NewVariant.COPY, "Copy");
        content.addVariant(NewVariant.EMPTY, "Bare");

        assertEquals(List.of(SetupVariant.DEFAULT_NAME, "Copy", "Bare"), variantNames(vorkath));
        assertFalse(reload(vorkath).variants().get(1).content().isEmpty());
        assertTrue(reload(vorkath).variants().get(2).content().isEmpty());
    }

    @Test
    void aVariantFromTheGameNeedsSomebodyLoggedIn() {
        GearSetup vorkath = open(GearSetup.named("Vorkath"));
        loadout = null;

        content.addVariant(NewVariant.FROM_GAME, "Worn");

        assertEquals(1, variantNames(vorkath).size());
        assertEquals(ContentHost.NO_GAME_FOR_VARIANT, prompts.lastMessage());

        loadout = WORN;
        content.addVariant(NewVariant.FROM_GAME, "Worn");

        assertEquals(List.of(SetupVariant.DEFAULT_NAME, "Worn"), variantNames(vorkath));
        assertEquals(Optional.of(SetupItem.of(4151)),
                ((GearContent) reload(vorkath).variants().get(1).content()).equipped(EquipmentSlot.WEAPON));
    }

    @Test
    void aNameAlreadyInUseIsRefusedForBothNewAndRenamedVariants() {
        GearSetup vorkath = open(GearSetup.named("Vorkath")
                .withAddedVariant(new SetupVariant("Mage", GearContent.empty())));

        content.addVariant(NewVariant.COPY, "Mage");

        assertEquals(2, variantNames(vorkath).size());
        assertTrue(prompts.lastMessage().contains("already a variant named"), prompts.lastMessage());

        content.renameVariant(1, SetupVariant.DEFAULT_NAME);

        assertEquals("Mage", variantNames(vorkath).get(1));
    }

    @Test
    void renamingAVariantAsksWithTheNameItHasNow() {
        GearSetup vorkath = open(GearSetup.named("Vorkath")
                .withAddedVariant(new SetupVariant("Mage", GearContent.empty())));
        prompts.typing("Magic");

        content.renameVariant(1);

        assertEquals(List.of(ContentHost.VARIANT_NAME), prompts.questions());
        assertEquals("Magic", variantNames(vorkath).get(1));
    }

    @Test
    void aSetupHoldsOnlySoManyVariants() {
        GearSetup vorkath = open(GearSetup.named("Vorkath"));
        for (int i = variantNames(vorkath).size(); i < GearSetup.MAX_VARIANTS; i++) {
            content.addVariant(NewVariant.EMPTY, "Variant " + i);
        }
        assertEquals(GearSetup.MAX_VARIANTS, variantNames(vorkath).size());

        content.addVariant(NewVariant.COPY);
        assertEquals(ContentHost.MAX_VARIANTS, prompts.lastMessage());
        assertTrue(prompts.questions().isEmpty(), "it does not even ask for a name");

        content.duplicateVariant(0);
        assertEquals(ContentHost.MAX_VARIANTS, prompts.lastMessage());
        assertEquals(GearSetup.MAX_VARIANTS, variantNames(vorkath).size());
    }

    @Test
    void deletingAVariantAsksBeforeItsItemsGo() {
        GearSetup vorkath = open(GearSetup.named("Vorkath")
                .withAddedVariant(new SetupVariant("Mage", GearContent.empty())));
        prompts.answering(false);

        content.deleteVariant(1);

        assertEquals(2, variantNames(vorkath).size());
        assertTrue(prompts.questions().get(0).contains("Mage"), prompts.questions().get(0));

        prompts.answering(true);
        content.deleteVariant(1);

        assertEquals(List.of(SetupVariant.DEFAULT_NAME), variantNames(vorkath));
    }

    @Test
    void theOnlyVariantASetupHasCannotBeDeleted() {
        GearSetup vorkath = open(GearSetup.named("Vorkath"));

        content.deleteVariant(0);

        assertEquals(1, variantNames(vorkath).size());
        assertTrue(prompts.questions().isEmpty());
    }

    @Test
    void aCellNameHasToFitAndIsAskedForWithWhatItSaysNow() {
        GearSetup custom = open(GearSetup.named("Custom").withContent(CustomContent.empty(2)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151))))));
        prompts.typing("Melee gear");

        content.renameCell(CellRef.of(0, 0));

        assertEquals("Melee gear", ((CustomContent) reload(custom).content()).cell(CellRef.of(0, 0)).label());

        content.renameCell(CellRef.of(0, 0), "  ");

        assertEquals("Melee gear", ((CustomContent) reload(custom).content()).cell(CellRef.of(0, 0)).label());
        assertTrue(prompts.lastMessage().startsWith("A cell name is 1 to "), prompts.lastMessage());
    }

    @Test
    void emptyingACellAsksOnlyWhenSomethingWouldBeLost() {
        GearSetup custom = open(GearSetup.named("Custom").withContent(CustomContent.empty(2)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151))))
                .withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY))));

        content.emptyCell(CellRef.of(0, 1));

        assertTrue(prompts.questions().isEmpty(), "an empty cell holds nothing to lose");
        assertEquals(CellKind.EMPTY, ((CustomContent) reload(custom).content()).cell(CellRef.of(0, 1)).kind());

        prompts.answering(false);
        content.emptyCell(CellRef.of(0, 0));

        assertEquals(CellKind.EQUIPMENT, ((CustomContent) reload(custom).content()).cell(CellRef.of(0, 0)).kind());
        assertTrue(prompts.questions().get(0).contains("1 item(s)"), prompts.questions().get(0));
    }

    @Test
    void fillingACellFromTheGameNeedsSomebodyLoggedIn() {
        open(GearSetup.named("Custom").withContent(CustomContent.empty(2)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of()))));
        loadout = null;

        content.fillCellFromGame(CellRef.of(0, 0));

        assertEquals(ContentHost.NO_GAME_FOR_CELL, prompts.lastMessage());
    }

    @Test
    void copyingSaysHowManyAndPastingNothingSaysSo() {
        GearSetup vorkath = open(GearSetup.named("Vorkath")
                .withContent(GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(4151)))));

        content.pasteAt(SlotRef.of(GridKind.INVENTORY, 5));
        assertEquals(ContentHost.NOTHING_COPIED, prompts.lastMessage());

        content.copySlots(List.of(SlotRef.of(GridKind.INVENTORY, 0)));
        assertEquals("Copied 1 item(s)", prompts.lastMessage());
        assertEquals(List.of("contents"), backToContents);

        content.pasteAt(SlotRef.of(GridKind.INVENTORY, 5));

        assertEquals(Optional.of(SetupItem.of(4151)),
                ((GearContent) reload(vorkath).content()).inventory().slot(5));
    }

    @Test
    void syncingFromTheGameWarnsAboutWhatItOverwritesUnlessThatIsTurnedOff() {
        GearSetup vorkath = open(GearSetup.named("Vorkath"));
        prompts.answering(false);

        content.requestSync(SyncScope.GEAR);

        assertTrue(reload(vorkath).content().isEmpty(), "answering no syncs nothing");
        assertTrue(prompts.questions().get(0).contains("wearing and carrying"), prompts.questions().get(0));

        confirmBeforeSync = false;
        content.requestSync(SyncScope.GEAR);

        assertEquals(1, prompts.questions().size(), "it was not asked a second time");
        assertEquals(Optional.of(SetupItem.of(4151)),
                ((GearContent) reload(vorkath).content()).equipped(EquipmentSlot.WEAPON));
        assertEquals("Vorkath synced from the game", causes.get(causes.size() - 1));
    }

    @Test
    void eachScopeSaysWhichSideItReplaces() {
        GearSetup vorkath = GearSetup.named("Vorkath");

        assertTrue(ContentHost.warningFor(SyncScope.LEFT_SIDE, vorkath).contains("left side"));
        assertTrue(ContentHost.warningFor(SyncScope.RIGHT_SIDE, vorkath).contains("right side"));
        assertEquals("Left side of Vorkath synced", ContentHost.doneFor(SyncScope.LEFT_SIDE, vorkath));
        assertEquals("Right side of Vorkath synced", ContentHost.doneFor(SyncScope.RIGHT_SIDE, vorkath));
    }

    @Test
    void syncingNeedsSomebodyLoggedIn() {
        open(GearSetup.named("Vorkath"));
        loadout = null;
        confirmBeforeSync = false;

        content.requestSync(SyncScope.GEAR);

        assertEquals(ContentHost.NO_GAME_FOR_SYNC, prompts.lastMessage());
    }

    @Test
    void theHistoryOnThePageIsTheOneOfTheSetupOpen() {
        GearSetup vorkath = open(GearSetup.named("Vorkath"));
        confirmBeforeSync = false;
        content.requestSync(SyncScope.GEAR);

        List<SetupRevision> revisions = content.revisions(vorkath.id());

        assertEquals(1, revisions.size());
        assertFalse(reload(vorkath).content().isEmpty(), "the sync went in");

        content.restore(revisions.get(0));

        assertTrue(reload(vorkath).content().isEmpty(), "the items before the sync are back");
    }
}
