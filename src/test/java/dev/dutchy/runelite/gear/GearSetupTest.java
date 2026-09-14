package dev.dutchy.runelite.gear;

import dev.dutchy.runelite.gear.content.BankContent;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.content.VariantId;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GearSetupTest {

    private static final GearContent MELEE = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
    private static final GearContent MAGE = MELEE.withEquipped(EquipmentSlot.WEAPON, SetupItem.of(11791));

    @Test
    void aSetupStartsWithOneDefaultVariantThatItsContentsReadAndWrite() {
        GearSetup setup = GearSetup.named("Vorkath").withContent(MELEE);

        assertFalse(setup.hasVariants());
        assertEquals(SetupVariant.DEFAULT_NAME, setup.variant().name());
        assertEquals(MELEE, setup.content());
        assertEquals(MAGE, setup.withContent(MAGE).content());
        assertTrue(setup.variantAt(1).isEmpty());
    }

    @Test
    void addingAVariantKeepsTheChoiceAndContentChangesStayWithTheChosenOne() {
        GearSetup setup = GearSetup.named("Vorkath").withContent(MELEE).withAddedVariant(new SetupVariant("Mage", MAGE));

        assertTrue(setup.hasVariants());
        assertEquals(0, setup.selectedIndex(), "adding a variant does not switch to it");
        assertEquals(MELEE, setup.content());
        assertEquals(MAGE, setup.variantAt(1).orElseThrow().content());
        GearSetup edited = setup.withContent(GearContent.empty());
        assertEquals(MAGE, edited.variants().get(1).content(), "the other variant is untouched");
        assertEquals(MAGE, setup.withSelectedVariant(1).content());
        VariantId mage = setup.variantIdAt(1).orElseThrow();
        assertEquals(GearContent.empty(), setup.withVariantContent(mage, GearContent.empty()).variant(mage).orElseThrow().content());
        assertThrows(IllegalArgumentException.class, () -> setup.withVariantContent(VariantId.random(), MAGE));
        assertTrue(setup.hasVariantNamed(" mage "));
        assertEquals(List.of("Default", "Magic"), setup.withVariant(1, setup.variantAt(1).orElseThrow().withName("Magic")).variants().stream()
                .map(SetupVariant::name).collect(Collectors.toList()));
    }

    @Test
    void removingAVariantMovesTheSelectionToTheNearestOneLeft() {
        GearSetup setup = GearSetup.named("Vorkath").withContent(MELEE)
                .withAddedVariant(new SetupVariant("Mage", MAGE))
                .withAddedVariant(new SetupVariant("Range", MELEE))
                .withSelectedVariant(2);

        assertEquals(1, setup.withoutVariant(0).selectedIndex(), "removing an earlier variant shifts the chosen index");
        assertEquals("Range", setup.withoutVariant(0).variant().name());
        assertEquals("Mage", setup.withoutVariant(2).variant().name(), "removing the chosen last variant chooses the one before it");
        assertEquals("Mage", setup.withSelectedVariant(0).withoutVariant(0).variant().name(), "removing the chosen first variant chooses the next");
        assertThrows(IllegalStateException.class, () -> GearSetup.named("Solo").withoutVariant(0));
    }

    @Test
    void theSelectionFollowsItsVariantThroughInsertsMovesAndRenames() {
        GearSetup setup = GearSetup.named("Vorkath").withContent(MELEE)
                .withAddedVariant(new SetupVariant("Mage", MAGE))
                .withSelectedVariant(1);
        VariantId chosen = setup.selected();

        assertEquals("Mage", setup.withVariantInserted(0, new SetupVariant("First", MELEE)).variant().name(),
                "an insert before the selection does not move it");
        assertEquals(chosen, setup.withVariantMoved(1, 0).selected(), "nor does a move");
        assertEquals(0, setup.withVariantMoved(1, 0).selectedIndex(), "though its position changes");
        assertEquals(chosen, setup.withVariant(chosen, setup.variant().withName("Magic")).selected(), "nor a rename");
        assertEquals("Default", setup.withoutVariant(chosen).variant().name(), "deleting the selected one picks a neighbour");
        assertEquals(chosen, setup.withoutVariant(0).selected(), "deleting another leaves it selected");
    }

    @Test
    void variantsMustShareATypeHaveDistinctNamesAndStayWithinTheLimit() {
        GearSetup setup = GearSetup.named("Vorkath").withContent(MELEE);

        assertThrows(IllegalArgumentException.class, () -> setup.withAddedVariant(new SetupVariant("Bank", BankContent.empty())));
        assertThrows(IllegalArgumentException.class, () -> setup.withAddedVariant(new SetupVariant("default", MAGE)));
        assertThrows(IllegalArgumentException.class, () -> setup.withSelectedVariant(1));
        assertThrows(IllegalArgumentException.class, () -> new SetupVariant(" ", MELEE));
        GearSetup full = setup;
        for (int i = 1; i < GearSetup.MAX_VARIANTS; i++) {
            full = full.withAddedVariant(new SetupVariant("V" + i, MELEE));
        }
        GearSetup atLimit = full;
        assertThrows(IllegalArgumentException.class, () -> atLimit.withAddedVariant(new SetupVariant("One more", MELEE)));
    }

    @Test
    void variantsCanBeInsertedAndMovedWithTheChoiceFollowingTheVariant() {
        GearSetup setup = GearSetup.named("Vorkath").withContent(MELEE)
                .withAddedVariant(new SetupVariant("Mage", MAGE))
                .withVariantInserted(1, new SetupVariant("Range", MELEE));

        assertEquals(List.of("Default", "Range", "Mage"), names(setup));
        assertEquals("Default", setup.variant().name(), "an inserted variant is not chosen");
        assertEquals("Mage", setup.withSelectedVariant(2).withVariantInserted(0, new SetupVariant("First", MELEE)).variant().name(),
                "inserting before the chosen one keeps the choice on it");
        GearSetup moved = setup.withVariantMoved(1, 2);
        assertEquals(List.of("Default", "Mage", "Range"), names(moved));
        assertEquals("Default", moved.variant().name());
        assertEquals("Default", setup.withVariantMoved(0, 2).variant().name(), "the choice follows the moved variant");
        assertThrows(IllegalArgumentException.class, () -> setup.withVariantMoved(0, 3));
    }

    private static List<String> names(GearSetup setup) {
        return setup.variants().stream().map(SetupVariant::name).collect(Collectors.toList());
    }

    @Test
    void aCopyKeepsEveryVariantAndTheChoice() {
        GearSetup setup = GearSetup.named("Vorkath").withContent(MELEE).withAddedVariant(new SetupVariant("Mage", MAGE)).withSelectedVariant(1);
        GearSetup copy = setup.copyNamed("Vorkath (2)");

        assertEquals(setup.variants(), copy.variants());
        assertEquals(1, copy.selectedIndex());
    }
}
