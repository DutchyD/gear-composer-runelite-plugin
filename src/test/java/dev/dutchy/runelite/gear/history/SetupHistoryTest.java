package dev.dutchy.runelite.gear.history;

import java.util.stream.Collectors;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.content.VariantId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupHistoryTest {

    private final GearSetupBook book = new GearSetupBook();
    private final InMemoryHistoryStore store = new InMemoryHistoryStore();
    private final SetupHistory history = new SetupHistory(book, store, Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneOffset.UTC));
    private final GearSetup setup = book.addSetup(book.sections().get(0).id(), "Vorkath");

    @Test
    void aRevisionNamesItsVariantAndRestoringGoesBackToThatVariant() {
        GearContent melee = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        GearContent mage = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(11791));
        book.change(setup.id(), current -> current.withContent(melee).withAddedVariant(new SetupVariant("Mage", mage)));
        GearSetup withMage = book.setup(setup.id()).orElseThrow();
        VariantId first = withMage.variantIdAt(0).orElseThrow();
        VariantId second = withMage.variantIdAt(1).orElseThrow();

        history.applyContent(setup.id(), second, GearContent.empty(), "Emptied Mage");
        SetupRevision revision = history.revisions(setup.id()).get(0);
        assertEquals(Optional.of(second), revision.variant(), "the revision knows which variant it came from");
        assertEquals(mage, revision.content());

        book.change(setup.id(), current -> current.withSelectedVariant(0));
        history.restore(setup.id(), revision);
        GearSetup restored = book.setup(setup.id()).orElseThrow();
        assertEquals(mage, restored.variant(second).orElseThrow().content(), "restoring goes back to its own variant");
        assertEquals(melee, restored.variant(first).orElseThrow().content(), "and leaves the shown one alone");
    }

    @Test
    void aRevisionFromBeforeVariantsHadIdsRestoresIntoTheShownVariant() {
        GearContent melee = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        SetupRevision legacy = new SetupRevision(Instant.parse("2026-09-08T09:00:00Z"), "Older edit", melee);

        assertEquals(Optional.empty(), legacy.variant());
        history.restore(setup.id(), legacy);
        assertEquals(melee, book.setup(setup.id()).orElseThrow().content());
    }

    @Test
    void eachContentChangeRecordsWhatCameBefore() {
        GearContent withWhip = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        history.applyContent(setup.id(), withWhip, "Edited Weapon");
        history.applyContent(setup.id(), withWhip, "Edited Weapon again");

        List<SetupRevision> revisions = history.revisions(setup.id());
        assertEquals(1, revisions.size(), "an identical write is not a revision");
        assertEquals("Edited Weapon", revisions.get(0).cause());
        assertEquals(GearContent.empty(), revisions.get(0).content());
        assertEquals(withWhip, book.setup(setup.id()).orElseThrow().content());
    }

    @Test
    void restoringBringsOldItemsBackAndIsItselfRecorded() {
        GearContent withWhip = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        history.applyContent(setup.id(), withWhip, "Sync from game");

        history.restore(setup.id(), history.revisions(setup.id()).get(0));

        assertTrue(book.setup(setup.id()).orElseThrow().content().isEmpty());
        assertEquals(List.of("Restored from history", "Sync from game"),
                history.revisions(setup.id()).stream().map(SetupRevision::cause).collect(Collectors.toList()));
    }

    @Test
    void onlyTheNewestRevisionsAreKept() {
        for (int i = 0; i < HistoryStore.MAX_REVISIONS + 3; i++) {
            history.applyContent(setup.id(), GearContent.empty().withEquipped(EquipmentSlot.RING, SetupItem.of(2550 + i)), "Edit " + i);
        }
        List<SetupRevision> revisions = history.revisions(setup.id());
        assertEquals(HistoryStore.MAX_REVISIONS, revisions.size());
        assertEquals("Edit " + (HistoryStore.MAX_REVISIONS + 2), revisions.get(0).cause(), "newest first");
    }
@Test
    void everyVariantKeepsItsOwnTenRevisions() {
        book.change(setup.id(), current -> current.withAddedVariant(new SetupVariant("Mage", GearContent.empty())));
        GearSetup withMage = book.setup(setup.id()).orElseThrow();
        VariantId melee = withMage.variantIdAt(0).orElseThrow();
        VariantId mage = withMage.variantIdAt(1).orElseThrow();
        for (int i = 0; i < HistoryStore.MAX_REVISIONS + 3; i++) {
            history.applyContent(setup.id(), melee, GearContent.empty().withEquipped(EquipmentSlot.RING, SetupItem.of(2550 + i)), "Melee edit " + i);
        }
        history.applyContent(setup.id(), mage, GearContent.empty().withEquipped(EquipmentSlot.RING, SetupItem.of(2552)), "Mage edit");

        List<SetupRevision> revisions = history.revisions(setup.id());
        assertEquals(HistoryStore.MAX_REVISIONS + 1, revisions.size(), "a busy variant does not push another out");
        assertEquals(1, revisions.stream().filter(revision -> revision.variant().equals(Optional.of(mage))).count());
        assertEquals("Mage edit", revisions.get(0).cause(), "newest first across the whole setup");
    }
}
