package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.ColourLabel;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.Hotkey;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.activation.SetupActivator;
import dev.dutchy.runelite.gear.bank.ActiveSetup;
import dev.dutchy.runelite.gear.bank.BankLayout;
import dev.dutchy.runelite.gear.bank.BankLayoutApplier;
import dev.dutchy.runelite.gear.bank.BankLayoutPlanner;
import dev.dutchy.runelite.gear.content.CellRef;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.LayoutCell;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.history.InMemoryHistoryStore;
import dev.dutchy.runelite.gear.history.SetupHistory;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupCommandsTest {

    private static final Owner MINE = Owner.account("rsprofile.main");

    private final GearSetupBook book = new GearSetupBook();
    private final SetupHistory history = new SetupHistory(book, new InMemoryHistoryStore(),
            Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneOffset.UTC));
    private final ActiveSetup activeSetup = new ActiveSetup();
    private final List<BankLayout> applied = new ArrayList<>();
    private final RecordingPrompts prompts = new RecordingPrompts();
    private int marks;

    private final SectionId section = book.sections().get(0).id();

    private final SetupCommands setups = new SetupCommands(book, history,
            new SetupActivator(book, activeSetup, new BankLayoutPlanner(), new BankLayoutApplier() {
                @Override
                public void apply(BankLayout layout) {
                    applied.add(layout);
                }

                @Override
                public void clear() {
                }
            }));

    SetupCommandsTest() {
        setups.shownBy(prompts, () -> marks++, () -> MINE);
    }

    private GearSetup add(String name) {
        return book.addSetup(section, name);
    }

    private List<String> names() {
        return book.sections().stream().flatMap(each -> each.setups().stream())
                .map(GearSetup::name).collect(Collectors.toList());
    }

    @Test
    void showingASetupInTheBankAndStoppingAgainBothMarkTheList() {
        GearSetup vorkath = add("Vorkath");

        setups.activate(vorkath);

        assertTrue(activeSetup.isActive(vorkath.id()));
        assertEquals(1, applied.size(), "and it is drawn there");
        assertEquals("Showing Vorkath in the bank", prompts.lastMessage());

        setups.activate(vorkath);

        assertFalse(activeSetup.isActive(vorkath.id()));
        assertEquals("Stopped showing Vorkath", prompts.lastMessage());
        assertEquals(2, marks);
    }

    @Test
    void deletingAsksFirstAndTakesTheHistoryWithIt() {
        GearSetup vorkath = add("Vorkath");
        history.applyContent(vorkath.id(), GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151)), "Edited");
        prompts.answering(false);

        setups.delete(vorkath);

        assertEquals(List.of("Vorkath"), names(), "answering no keeps it");

        prompts.answering(true);
        setups.delete(vorkath);

        assertTrue(names().isEmpty());
        assertTrue(history.revisions(vorkath.id()).isEmpty());
        assertEquals("Deleted Vorkath", prompts.lastMessage());
    }

    @Test
    void duplicatingSaysWhatTheCopyIsCalled() {
        setups.duplicate(add("Vorkath"));

        assertEquals(2, names().size());
        assertTrue(prompts.lastMessage().startsWith("Duplicated as "), prompts.lastMessage());
    }

    @Test
    void onlySoManySetupsCanBePinnedAtOnce() {
        for (int i = 0; i < SetupCommands.MAX_PINNED; i++) {
            setups.togglePin(add("Setup " + i));
        }
        assertEquals(SetupCommands.MAX_PINNED, book.pinnedSetups().size());

        GearSetup extra = add("One too many");
        setups.togglePin(extra);

        assertFalse(book.setup(extra.id()).orElseThrow().isPinned());
        assertEquals("Up to " + SetupCommands.MAX_PINNED + " setups can be pinned", prompts.lastMessage());
    }

    @Test
    void aPinnedSetupCanBeUnpinnedAgain() {
        GearSetup vorkath = add("Vorkath");

        setups.togglePin(vorkath);
        assertEquals("Pinned Vorkath", prompts.lastMessage());

        setups.togglePin(book.setup(vorkath.id()).orElseThrow());

        assertFalse(book.setup(vorkath.id()).orElseThrow().isPinned());
        assertEquals("Unpinned Vorkath", prompts.lastMessage());
    }

    @Test
    void aColourCanBePutOnASetupAndTakenOffAgain() {
        GearSetup vorkath = add("Vorkath");

        setups.setLabel(vorkath, ColourLabel.RED);
        assertEquals(ColourLabel.RED, book.setup(vorkath.id()).orElseThrow().meta().label());
        assertEquals("Coloured Vorkath", prompts.lastMessage());

        setups.setLabel(vorkath, ColourLabel.NONE);
        assertEquals("Removed the colour from Vorkath", prompts.lastMessage());
    }

    @Test
    void aHotkeyBelongsToOneSetupOnly() {
        GearSetup vorkath = add("Vorkath");
        GearSetup zulrah = add("Zulrah");
        Hotkey key = new Hotkey(KeyEvent.VK_F1, 0);

        setups.setHotkey(vorkath, key);
        setups.setHotkey(zulrah, key);

        assertEquals(Hotkey.NONE, book.setup(vorkath.id()).orElseThrow().meta().hotkey(), "the first binding gave way");
        assertEquals(key, book.setup(zulrah.id()).orElseThrow().meta().hotkey());
        assertTrue(prompts.lastMessage().startsWith("Zulrah is now on "), prompts.lastMessage());

        setups.setHotkey(zulrah, Hotkey.NONE);

        assertEquals("Removed the hotkey from Zulrah", prompts.lastMessage());
    }

    @Test
    void switchingVariantSaysWhatTheBankShowsNow() {
        GearSetup vorkath = book.addSetup(section, GearSetup.named("Vorkath")
                .withAddedVariant(new SetupVariant("Mage", GearContent.empty())));

        setups.showVariant(vorkath, 1);

        assertEquals(1, book.setup(vorkath.id()).orElseThrow().selectedIndex());
        assertEquals("Vorkath now shows Mage", prompts.lastMessage());

        setups.showVariant(book.setup(vorkath.id()).orElseThrow(), 7);

        assertEquals(1, book.setup(vorkath.id()).orElseThrow().selectedIndex(), "there is no seventh variant");
    }

    @Test
    void aNewSetupBelongsToWhoeverIsLookingAtTheList() {
        GearSetup added = setups.add(section, GearSetup.named("Vorkath"));

        assertEquals(MINE, book.setup(added.id()).orElseThrow().owner());
        assertEquals("Created Vorkath", prompts.lastMessage());

        setups.addFromGame(section, GearSetup.named("From game"));

        assertEquals("Created From game from the game", prompts.lastMessage());
    }

    @Test
    void anEditTakesTheNameAndIconWithoutTouchingTheItems() {
        GearSetup vorkath = book.addSetup(section, GearSetup.named("Vorkath")
                .withContent(GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151))));

        setups.applyEdit(vorkath.id(), vorkath.withName("Vorkath alt"));

        GearSetup saved = book.setup(vorkath.id()).orElseThrow();
        assertEquals("Vorkath alt", saved.name());
        assertFalse(saved.content().isEmpty(), "the items are edited elsewhere");
        assertEquals("Saved Vorkath alt", prompts.lastMessage());
    }

    @Test
    void droppingRowsOffACustomLayoutIsAskedAboutFirst() {
        CustomContent threeRows = CustomContent.empty(3)
                .withCell(CellRef.of(2, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151))));
        GearSetup custom = book.addSetup(section, GearSetup.named("Custom").withContent(threeRows));
        GearSetup fewer = custom.withContent(((CustomContent) custom.content()).withRows(1));
        prompts.answering(false);

        setups.applyEdit(custom.id(), fewer);

        assertEquals(3, ((CustomContent) book.setup(custom.id()).orElseThrow().content()).rows(), "nothing was dropped");

        prompts.answering(true);
        setups.applyEdit(custom.id(), fewer);

        assertEquals(1, ((CustomContent) book.setup(custom.id()).orElseThrow().content()).rows());
    }

    @Test
    void aSetupCanBeMovedIntoAnotherSection() {
        GearSetup vorkath = add("Vorkath");
        SectionId skilling = book.addSection("Skilling").id();

        setups.moveToSection(vorkath.id(), skilling, 0);

        assertTrue(book.sections().get(0).setups().isEmpty());
        assertEquals(List.of("Vorkath"), book.sections().get(1).setups().stream().map(GearSetup::name).collect(Collectors.toList()));
    }
}
