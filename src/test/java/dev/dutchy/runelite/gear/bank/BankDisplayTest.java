package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.items.ItemVariants;
import dev.dutchy.runelite.gear.layout.BankPlacement;
import dev.dutchy.runelite.gear.layout.BankSide;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankDisplayTest {

    private static final int WHIP = 4151;
    private static final int SHARK = 385;
    private static final int SCIMITAR = 1333;
    private static final int GAME_CHILDREN = 100;

    private static final SetupItem whip = SetupItem.of(WHIP);
    private static final SetupItem shark = SetupItem.of(SHARK, 3);
    private static final SetupItem scimitar = SetupItem.of(SCIMITAR);

    private final Map<ItemId, String> names = Map.of(ItemId.of(WHIP), "Abyssal whip", ItemId.of(SHARK), "Shark",
            ItemId.of(SCIMITAR), "Rune scimitar");
    private final RecordingBankScreen screen = new RecordingBankScreen();
    private final List<Integer> chosenVariants = new ArrayList<>();
    private boolean dimSearchMisses = true;

    private final BankDisplay display = new BankDisplay(
            new BankGrid(new SlotResolver(ItemVariants.none())),
            BankContents.of(Map.of(ItemId.of(WHIP), 1, ItemId.of(SHARK), 5)),
            item -> names.getOrDefault(item, BankItemNames.unknown(item)),
            () -> dimSearchMisses);

    BankDisplayTest() {
        display.drawOn(screen);
        display.onVariantChosen(chosenVariants::add);
    }

    private static BankLayout layoutOf(SetupItem... items) {
        List<BankPlacement> placements = IntStream.range(0, items.length)
                .mapToObj(column -> new BankPlacement(BankSide.LEFT, column, 0, items[column]))
                .collect(Collectors.toList());
        return new BankLayout(placements);
    }

    private static BankSnapshot snapshotOf(BankSnapshot.Shown... shown) {
        return new BankSnapshot(List.of(shown), GAME_CHILDREN, 480);
    }

    private static BankSnapshot.Shown drawn(int child, int itemId, int quantity) {
        return new BankSnapshot.Shown(child, itemId, quantity);
    }

    @Test
    void withNoLayoutTheBankIsLeftAloneAndNothingIsPublished() {
        BankFrame frame = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        assertTrue(frame.isEmpty());
        assertEquals(0, frame.nextChild(), "no child is spoken for");
        assertFalse(display.drawnBank().active());
        assertFalse(display.isActive());
    }

    @Test
    void anItemThePlayerHoldsKeepsTheChildTheGameDrewItAtAndItsOwnMenu() {
        display.apply(layoutOf(whip));

        BankFrame frame = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        SlotFace face = frame.at(7).orElseThrow();
        assertEquals(SlotFace.Kind.OWNED, face.kind());
        assertEquals(7, face.bankIndex(), "withdraws must act on the slot the game drew it for");
        assertEquals(BankGeometry.x(0), face.x());
        assertEquals(0, face.y());
        assertFalse(face.hasName(), "the game's own name and menu are left alone");
        assertFalse(face.hasQuantity(), "a slot with no set amount keeps the game's count");
    }

    @Test
    void aSlotWithASetAmountShowsWhatItAsksForAndHidesTheGamesCount() {
        display.apply(layoutOf(shark));

        SlotFace face = display.frameFor(snapshotOf(drawn(2, SHARK, 5))).at(2).orElseThrow();

        assertEquals(3, face.quantity(), "five held covers the three asked for");
        assertTrue(face.hideQuantity());
    }

    @Test
    void aSecondCopyOfAnItemBorrowsAChildAndCopiesTheOneTheGameDrew() {
        display.apply(layoutOf(whip, whip));

        BankFrame frame = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        SlotFace twin = frame.at(GAME_CHILDREN).orElseThrow();
        assertEquals(SlotFace.Kind.TWIN, twin.kind());
        assertEquals(7, twin.templateChild(), "it copies the appearance the game drew");
        assertEquals(7, twin.bankIndex(), "and its withdraws still act on the real slot");
        assertEquals(BankGeometry.x(1), twin.x());
        assertEquals(GAME_CHILDREN + 1, frame.nextChild(), "the strip starts past the children it took");
    }

    @Test
    void aCopyPrefersASpareChildOverAppendingANewOne() {
        display.apply(layoutOf(whip, whip));

        BankFrame frame = display.frameFor(snapshotOf(drawn(7, WHIP, 1), drawn(9, SCIMITAR, 1)));

        assertEquals(SlotFace.Kind.TWIN, frame.at(9).orElseThrow().kind(), "the scimitar's child was going spare");
        assertEquals(GAME_CHILDREN, frame.nextChild(), "so no child had to be appended");
    }

    @Test
    void anItemTheBankCannotSupplyIsGhostedAndNamedWithoutAMenu() {
        display.apply(layoutOf(scimitar));

        SlotFace face = display.frameFor(snapshotOf()).at(GAME_CHILDREN).orElseThrow();

        assertEquals(SlotFace.Kind.PLACEHOLDER, face.kind());
        assertTrue(face.isPlaceholder());
        assertEquals(SlotFace.NONE, face.bankIndex(), "there is nothing to withdraw");
        assertEquals("<col=ff9040>Rune scimitar</col>", face.name());
        assertEquals(Integer.MAX_VALUE, face.quantity());
    }

    @Test
    void anItemNameWithABracketReachesTheGameEscaped() {
        BankDisplay display = new BankDisplay(
                new BankGrid(new SlotResolver(ItemVariants.none())),
                BankContents.nothing(),
                item -> "Half <of> one",
                () -> false);
        display.apply(layoutOf(scimitar));

        SlotFace face = display.frameFor(snapshotOf()).at(GAME_CHILDREN).orElseThrow();

        assertEquals("<col=ff9040>Half <lt>of<gt> one</col>", face.name());
    }

    @Test
    void fourVariantsShareTheWholeStripAndNeedNoArrows() {
        display.apply(layoutOf(whip).withVariants(new VariantTabs(List.of("Melee", "Range", "Mage", "Spec"), 0)));

        BankFrame frame = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        assertEquals(4, frame.tabs().size());
        assertTrue(frame.arrows().isEmpty(), "all four fit");
        assertEquals(BankGeometry.ITEM_START_X, frame.tabs().get(0).x());
        int rightEdge = frame.tabs().get(3).x() + frame.tabs().get(3).width();
        assertTrue(rightEdge <= 480 && rightEdge > 480 - VariantTabs.VISIBLE,
                "the four fill the width up to the right edge, give or take rounding, but ended at " + rightEdge);
        assertTrue(frame.tabs().get(0).chosen());
        assertFalse(frame.tabs().get(3).chosen());
    }

    @Test
    void moreVariantsThanFitBringTheArrowsThatCanStillScroll() {
        display.apply(layoutOf(whip).withVariants(new VariantTabs(List.of("A", "B", "C", "D", "E", "F"), 0)));

        BankFrame first = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        assertEquals(1, first.arrows().size(), "there is nowhere to scroll back to yet");
        assertTrue(first.arrows().get(0).forward());
        assertEquals(1, first.arrows().get(0).step());

        display.scrollStrip(1);
        BankFrame scrolled = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        assertEquals(2, scrolled.arrows().size(), "both ends can scroll now");
        assertEquals(List.of("B", "C", "D", "E"), scrolled.tabs().stream().map(TabFace::name).collect(Collectors.toList()));
        assertEquals(List.of(1, 2, 3, 4), scrolled.tabs().stream().map(TabFace::variant).collect(Collectors.toList()));
    }

    @Test
    void theArrowsAreDrawnWithGlyphsTheGameWillNotEat() {
        display.apply(layoutOf(whip).withVariants(new VariantTabs(List.of("A", "B", "C", "D", "E"), 4)));

        BankFrame frame = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        assertEquals(List.of("<lt>"), frame.arrows().stream().map(ArrowFace::glyph).collect(Collectors.toList()));
        assertEquals(List.of("B", "C", "D", "E"), frame.tabs().stream().map(TabFace::name).collect(Collectors.toList()),
                "the chosen variant is scrolled into view");
    }

    @Test
    void aScrollThatWouldGoNowhereIsNotEvenRedrawn() {
        display.apply(layoutOf(whip).withVariants(new VariantTabs(List.of("A", "B"), 0)));
        int before = screen.rebuilds();

        display.scrollStrip(-1);
        display.scrollStrip(0);

        assertEquals(before, screen.rebuilds());
    }

    @Test
    void aClickOnATabGoesToWhoeverIsListening() {
        display.chooseVariant(3);

        assertEquals(List.of(3), chosenVariants);
    }

    @Test
    void aDoseChoiceLastsUntilTheNextLayout() {
        SetupItem potion = SetupItem.of(SHARK);
        display.apply(layoutOf(potion));
        display.show(0, ItemId.of(SHARK));
        int redraws = screen.rebuilds();

        assertTrue(redraws > 0, "choosing redraws the bank");
        display.apply(layoutOf(potion));
        display.show(0, ItemId.of(SHARK));

        assertTrue(screen.rebuilds() > redraws);
    }

    @Test
    void aChoiceIsIgnoredWhenNoLayoutIsShown() {
        display.show(0, ItemId.of(SHARK));

        assertEquals(0, screen.rebuilds());
    }

    @Test
    void aNewLayoutIsReadFromItsTopButOnlyOnce() {
        display.apply(layoutOf(whip));

        assertTrue(display.frameFor(snapshotOf(drawn(7, WHIP, 1))).rewind());
        assertFalse(display.frameFor(snapshotOf(drawn(7, WHIP, 1))).rewind(), "a later build stays where it was");
    }

    @Test
    void theScrollAreaIsSizedForTheRowsTheLayoutNeeds() {
        BankLayout layout = layoutOf(whip);
        display.apply(layout);

        BankFrame frame = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        assertEquals(layout.rows().height(1), frame.scrollHeight());
    }

    @Test
    void whatTheOverlaysReadIsProjectedFromTheSameBuild() {
        display.apply(layoutOf(whip, scimitar));

        BankFrame frame = display.frameFor(snapshotOf(drawn(7, WHIP, 1)));
        DrawnBank bank = display.drawnBank();

        assertTrue(bank.active());
        assertEquals(frame.slots().keySet(), bank.slots().keySet(), "the same children, from the one build");
        assertEquals(7, bank.at(7).orElseThrow().bankIndex());
        assertFalse(bank.at(GAME_CHILDREN).orElseThrow().inBank(), "the scimitar is only a placeholder");
    }

    @Test
    void aLayoutTakenDownLeavesNothingForTheOverlaysToRead() {
        display.apply(layoutOf(whip));
        display.frameFor(snapshotOf(drawn(7, WHIP, 1)));

        display.clear();

        assertFalse(display.drawnBank().active());
        assertTrue(display.drawnBank().slots().isEmpty());
        assertTrue(display.frameFor(snapshotOf(drawn(7, WHIP, 1))).isEmpty());
    }

    @Test
    void itemsASearchMissesAreFadedOnlyWhileTheOptionIsOn() {
        display.apply(layoutOf(whip, shark));
        display.searching("shark");

        BankFrame faded = display.frameFor(snapshotOf(drawn(7, WHIP, 1), drawn(2, SHARK, 5)));

        assertTrue(faded.at(7).orElseThrow().opacity() > 0, "the whip is not what was typed");
        assertEquals(0, faded.at(2).orElseThrow().opacity());
        assertEquals("shark", display.drawnBank().searchQuery());

        dimSearchMisses = false;
        BankFrame plain = display.frameFor(snapshotOf(drawn(7, WHIP, 1), drawn(2, SHARK, 5)));

        assertEquals(0, plain.at(7).orElseThrow().opacity(), "the game hides the misses itself");
    }

    @Test
    void theDisplayReportsTheBankThroughTheScreenItDrawsOn() {
        assertTrue(display.isBankOpen());

        screen.close();

        assertFalse(display.isBankOpen());
    }
}
