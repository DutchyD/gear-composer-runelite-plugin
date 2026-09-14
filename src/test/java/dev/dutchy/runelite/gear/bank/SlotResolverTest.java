package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.ItemMatch;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.items.ItemCharges;
import dev.dutchy.runelite.gear.items.ItemNotes;
import dev.dutchy.runelite.gear.items.ItemVariants;
import dev.dutchy.runelite.gear.items.StaticItemCharges;
import dev.dutchy.runelite.gear.items.StaticItemNotes;
import dev.dutchy.runelite.gear.items.StaticItemVariants;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotResolverTest {

    private static final int RING_8 = 2552;
    private static final int RING_7 = 2554;
    private static final int RING_6 = 2556;
    private static final int GLOVES = 7462;
    private static final int BRACELET = 11126;

    private final ItemVariants variants = new StaticItemVariants().family(RING_8, RING_7, RING_6);
    private final SlotResolver resolver = new SlotResolver(variants);

    private static BankContents holding(int... ids) {
        Set<ItemId> held = Set.of(Arrays.stream(ids).mapToObj(ItemId::of).toArray(ItemId[]::new));
        return held::contains;
    }

    @Test
    void theItemItselfWinsWhenTheBankHasIt() {
        assertEquals(Optional.of(ItemId.of(RING_8)), resolver.resolve(SetupItem.of(RING_8), holding(RING_8, RING_6)));
    }

    @Test
    void aVariantSatisfiesTheSlotByDefault() {
        assertEquals(Optional.of(ItemId.of(RING_6)), resolver.resolve(SetupItem.of(RING_8), holding(RING_6)));
    }

    @Test
    void anExactSlotIgnoresVariants() {
        SetupItem exact = SetupItem.of(RING_8).withMatch(ItemMatch.EXACT);
        assertEquals(Optional.empty(), resolver.resolve(exact, holding(RING_6)));
    }

    @Test
    void alternativesComeAfterEveryVariantOfTheItem() {
        SetupItem gloves = SetupItem.of(RING_8).withAlternative(ItemId.of(GLOVES)).withAlternative(ItemId.of(BRACELET));
        assertEquals(List.of(ItemId.of(RING_8), ItemId.of(RING_7), ItemId.of(RING_6), ItemId.of(GLOVES), ItemId.of(BRACELET)),
                resolver.acceptable(gloves));
        assertEquals(Optional.of(ItemId.of(BRACELET)), resolver.resolve(gloves, holding(BRACELET)));
        assertEquals(Optional.of(ItemId.of(RING_6)), resolver.resolve(gloves, holding(BRACELET, RING_6)),
                "a variant of the item beats an alternative");
    }

    @Test
    void nothingAcceptableMeansAPlaceholder() {
        assertEquals(Optional.empty(), resolver.resolve(SetupItem.of(RING_8), BankContents.nothing()));
    }

    @Test
    void aNotedSlotIsSatisfiedByTheUnnotedStackTheBankHolds() {
        SlotResolver notes = new SlotResolver(ItemVariants.none(), new StaticItemNotes().note(527, 526));
        SetupItem notedBones = SetupItem.of(527).withNoted(true);
        assertEquals(Optional.of(ItemId.of(526)), notes.resolve(notedBones, holding(526)));
        assertEquals(Optional.of(ItemId.of(526)), notes.resolve(SetupItem.of(527), holding(526)), "a noted id always means the real item");
    }
    @Test
    void theFullestChargesInAFamilyAreShownFirst() {
        int can0 = 5331;
        int can3 = 5335;
        int can8 = 5340;
        ItemVariants cans = new StaticItemVariants().family(can0, can3, can8);
        ItemCharges charges = new StaticItemCharges().charged(can3, 3).charged(can8, 8);
        SlotResolver charged = new SlotResolver(cans, ItemNotes.none(), charges);

        assertEquals(List.of(ItemId.of(can8), ItemId.of(can3), ItemId.of(can0)), charged.acceptable(SetupItem.of(can3)));
        assertEquals(Optional.of(ItemId.of(can8)), charged.resolve(SetupItem.of(can3), holding(can0, can3, can8)));
        assertEquals(Optional.of(ItemId.of(can0)), charged.resolve(SetupItem.of(can3), holding(can0)), "an empty one still beats a placeholder");
        assertEquals(Optional.empty(), charged.resolve(SetupItem.of(can3).withMatch(ItemMatch.EXACT), holding(can8)), "exact still means exact");
    }

    @Test
    void aSupplyHoldsEveryStackOfTheWinningFamilyInOrderOfPreference() {
        int can0 = 5331;
        int can3 = 5335;
        int can8 = 5340;
        ItemVariants cans = new StaticItemVariants().family(can0, can3, can8);
        ItemCharges charges = new StaticItemCharges().charged(can3, 3).charged(can8, 8);
        SlotResolver charged = new SlotResolver(cans, ItemNotes.none(), charges);
        BankContents bank = BankContents.of(Map.of(ItemId.of(can3), 6, ItemId.of(can0), 2, ItemId.of(GLOVES), 1));

        SlotSupply supply = charged.supply(SetupItem.of(can8).withAlternative(ItemId.of(GLOVES)), bank);
        assertEquals(List.of(ItemId.of(can3), ItemId.of(can0)), ids(supply), "only the family that won, not the alternative");
        assertEquals(8, supply.total());
        assertEquals(6, supply.shownCount());
        assertEquals(List.of(ItemId.of(can0)), ids(new SlotSupply(supply.others())));
        assertEquals(List.of(ItemId.of(can0), ItemId.of(can3)), ids(supply.showing(ItemId.of(can0))), "the player can put another stack in front");
        assertEquals(supply, supply.showing(ItemId.of(can8)), "a stack the bank lacks changes nothing");
        assertEquals(SlotSupply.NONE, charged.supply(SetupItem.of(can8), BankContents.nothing()));
        assertEquals(List.of(ItemId.of(GLOVES)), ids(charged.supply(SetupItem.of(can8).withAlternative(ItemId.of(GLOVES)),
                BankContents.of(Map.of(ItemId.of(GLOVES), 1)))), "an alternative supplies the slot when the family is gone");
    }

    @Test
    void eachRuleOrdersTheFamilyItsOwnWay() {
        int can0 = 5331;
        int can3 = 5335;
        int can8 = 5340;
        ItemVariants cans = new StaticItemVariants().family(can0, can3, can8);
        ItemCharges charges = new StaticItemCharges().charged(can3, 3).charged(can8, 8);
        SlotResolver charged = new SlotResolver(cans, ItemNotes.none(), charges);

        assertEquals(List.of(ItemId.of(can8), ItemId.of(can3), ItemId.of(can0)), charged.acceptable(SetupItem.of(can3).withMatch(ItemMatch.ANY_VARIANT)));
        assertEquals(List.of(ItemId.of(can3), ItemId.of(can8), ItemId.of(can0)), charged.acceptable(SetupItem.of(can3).withMatch(ItemMatch.PREFER_THIS)),
                "this first, then the fullest");
        assertEquals(List.of(ItemId.of(can3), ItemId.of(can8), ItemId.of(can0)), charged.acceptable(SetupItem.of(can3).withMatch(ItemMatch.EMPTIEST_FIRST)),
                "the emptiest counted one first; an uncounted member still comes last");
        assertEquals(List.of(ItemId.of(can3)), charged.acceptable(SetupItem.of(can3).withMatch(ItemMatch.EXACT)));
    }

    private static List<ItemId> ids(SlotSupply supply) {
        return supply.stacks().stream().map(SlotSupply.Stack::id).collect(Collectors.toList());
    }

    @Test
    void chargesAreReadFromTheTrailingCount() {
        assertEquals(OptionalInt.of(8), ItemCharges.parse("Watering can(8)"));
        assertEquals(OptionalInt.of(4), ItemCharges.parse("Prayer potion(4)"));
        assertEquals(OptionalInt.empty(), ItemCharges.parse("Watering can"));
        assertEquals(OptionalInt.empty(), ItemCharges.parse("Rune pouch (l)"));
        assertEquals(OptionalInt.empty(), ItemCharges.parse(null));
    }

}
