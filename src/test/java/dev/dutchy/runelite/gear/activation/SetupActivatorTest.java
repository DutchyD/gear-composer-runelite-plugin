package dev.dutchy.runelite.gear.activation;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.bank.ActiveSetup;
import dev.dutchy.runelite.gear.bank.BankGeometry;
import dev.dutchy.runelite.gear.bank.BankLayout;
import dev.dutchy.runelite.gear.bank.BankLayoutApplier;
import dev.dutchy.runelite.gear.bank.BankLayoutPlanner;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupVariant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupActivatorTest {

    private final GearSetupBook book = new GearSetupBook();
    private final ActiveSetup active = new ActiveSetup();
    private final List<BankLayout> applied = new ArrayList<>();
    private final List<Boolean> cleared = new ArrayList<>();
    private final SetupActivator activator = new SetupActivator(book, active, new BankLayoutPlanner(), new BankLayoutApplier() {
        @Override
        public void apply(BankLayout layout) {
            applied.add(layout);
        }

        @Override
        public void clear() {
            cleared.add(Boolean.TRUE);
        }
    });

    private List<GearSetup> three() {
        SectionId section = book.sections().get(0).id();
        return List.of(
                book.addSetup(section, "A"),
                book.addSetup(section, "B"),
                book.addSetup(section, "C"));
    }

    @Test
    void togglingShowsAndThenHidesASetup() {
        GearSetup a = three().get(0);
        assertEquals(Optional.of(a), activator.toggle(a.id()));
        assertEquals(1, applied.size());
        assertTrue(applied.get(0).variants().isEmpty(), "one variant means no tabs");
        assertEquals(0, applied.get(0).rows().itemY(0));
        assertEquals(Optional.empty(), activator.toggle(a.id()));
        assertEquals(1, cleared.size());
    }

    @Test
    void nextAndPreviousWalkTheBookAndWrap() {
        List<GearSetup> setups = three();
        assertEquals(Optional.of(setups.get(0)), activator.next(), "starts at the first");
        assertEquals(Optional.of(setups.get(1)), activator.next());
        assertEquals(Optional.of(setups.get(0)), activator.previous());
        assertEquals(Optional.of(setups.get(2)), activator.previous(), "wraps around");
        assertEquals(List.of(3, 3), List.of(activator.position().orElseThrow()[0], activator.position().orElseThrow()[1]));
    }

    @Test
    void clearingTakesTheLayoutDownOnce() {
        GearSetup a = three().get(0);
        activator.toggle(a.id());
        activator.clear();
        activator.clear();
        assertEquals(1, cleared.size());
        assertTrue(activator.current().isEmpty());
    }

    @Test
    void theBankFollowsTheActiveSetupThroughEditsVariantsAndDeletion() {
        List<GearSetup> setups = three();
        GearSetup a = setups.get(0);
        activator.toggle(a.id());
        assertEquals(1, applied.size());

        book.change(setups.get(1).id(), other -> other.withName("Renamed"));
        book.changeMeta(a.id(), meta -> meta.withPinned(true));
        assertEquals(1, applied.size(), "changes that leave the items alone do not redraw");

        GearContent geared = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        book.updateSetupContent(a.id(), geared);
        assertEquals(2, applied.size(), "an edit redraws");
        assertEquals(SetupItem.of(4151), applied.get(1).placements().get(0).item());

        book.change(a.id(), current -> current.withAddedVariant(new SetupVariant("Mage", geared.withEquipped(EquipmentSlot.WEAPON, SetupItem.of(11791)))));
        assertEquals(3, applied.size(), "a new variant redraws the tabs");
        assertEquals(SetupItem.of(4151), applied.get(2).placements().get(0).item(), "while the shown one stays");
        book.change(a.id(), current -> current.withSelectedVariant(1));
        assertEquals(SetupItem.of(11791), applied.get(3).placements().get(0).item(), "choosing another variant redraws");

        book.change(a.id(), current -> current.withVariant(1, current.variantAt(1).orElseThrow().withName("Magic")));
        assertEquals(5, applied.size(), "renaming a variant redraws the tabs");
        assertEquals(List.of("Default", "Magic"), applied.get(4).variants().names());
        book.change(a.id(), current -> current.withAddedVariant(new SetupVariant("Copy", geared)));
        assertEquals(6, applied.size(), "a new variant redraws even when its items match");
        assertEquals(1, applied.get(5).variants().chosen(), "and the choice stays where it was");

        book.removeSetup(a.id());
        assertEquals(1, cleared.size(), "deleting the active setup takes the layout down");
        assertTrue(activator.current().isEmpty());
        book.updateSetupContent(setups.get(1).id(), geared);
        assertEquals(6, applied.size(), "nothing follows once nothing is active");
    }

    @Test
    void variantsAreSwitchedOnTheActiveSetupAndListenersHearEverySwitchButNotEdits() {
        List<Integer> heard = new ArrayList<>();
        activator.addListener(() -> heard.add(applied.size()));
        GearContent melee = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        GearSetup a = three().get(0);
        book.change(a.id(), current -> current.withContent(melee)
                .withAddedVariant(new SetupVariant("Mage", melee.withEquipped(EquipmentSlot.WEAPON, SetupItem.of(11791))))
                .withAddedVariant(new SetupVariant("Range", melee.withEquipped(EquipmentSlot.WEAPON, SetupItem.of(861))))
                .withSelectedVariant(0));
        assertEquals(Optional.empty(), activator.nextVariant().map(GearSetup::name), "nothing is active yet");

        activator.toggle(a.id());
        assertEquals(List.of("Default", "Mage", "Range"), applied.get(applied.size() - 1).variants().names(), "the bank gets a tab per variant");
        assertEquals("Mage", activator.nextVariant().orElseThrow().variant().name());
        assertEquals(SetupItem.of(11791), applied.get(applied.size() - 1).placements().get(0).item(), "the bank follows");
        assertEquals(1, applied.get(applied.size() - 1).variants().chosen(), "with the chosen tab lit");
        assertEquals(BankGeometry.VARIANT_STRIP_HEIGHT, applied.get(applied.size() - 1).rows().itemY(0), "and the items moved down under the tabs");
        assertEquals("Default", activator.previousVariant().orElseThrow().variant().name());
        assertEquals("Range", activator.previousVariant().orElseThrow().variant().name(), "stepping wraps");
        assertEquals("Mage", activator.selectVariant(1).orElseThrow().variant().name());
        assertEquals("Mage", activator.selectVariant(7).orElseThrow().variant().name(), "an unknown variant is ignored");
        assertEquals(5, heard.size(), "the toggle and four switches, not the redraws they caused");

        book.updateSetupContent(a.id(), GearContent.empty());
        assertEquals(5, heard.size(), "an edit redraws quietly");
        activator.clear();
        assertEquals(6, heard.size());
    }

    @Test
    void anUnknownSetupChangesNothing() {
        assertEquals(Optional.empty(), activator.toggle(SetupId.random()));
        assertTrue(applied.isEmpty());
        assertEquals(Optional.empty(), activator.next(), "an empty book has nothing to step to");
    }
}
