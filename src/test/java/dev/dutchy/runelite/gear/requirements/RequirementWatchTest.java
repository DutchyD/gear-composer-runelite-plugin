package dev.dutchy.runelite.gear.requirements;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.Prayer;
import dev.dutchy.runelite.gear.Requirements;
import dev.dutchy.runelite.gear.Spellbook;
import dev.dutchy.runelite.gear.bank.ActiveSetup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequirementWatchTest {

    private final GearSetupBook book = new GearSetupBook();
    private final ActiveSetup active = new ActiveSetup();
    private final FixedSpellbook spellbook = new FixedSpellbook(Spellbook.STANDARD);
    private final FixedQuickPrayers quickPrayers = new FixedQuickPrayers(Set.of());
    private final RequirementWatch watch = new RequirementWatch(book, active, spellbook, quickPrayers);

    @Test
    void warnsOnlyWhenTheActiveSetupNeedsAnotherSpellbook() {
        GearSetup zulrah = book.addSetup(book.sections().get(0).id(), "Zulrah");
        book.changeMeta(zulrah.id(), meta -> meta.withRequirements(Requirements.ofSpellbook(Spellbook.LUNAR)));
        assertTrue(watch.warning().isEmpty(), "nothing active yet");

        active.toggle(zulrah.id());
        assertEquals(Optional.of("Zulrah needs Lunar, you are on Standard"), watch.warning());

        spellbook.set(Spellbook.LUNAR);
        assertTrue(watch.warning().isEmpty());

        spellbook.logOut();
        assertTrue(watch.warning().isEmpty(), "logged out means no warning");
    }

    @Test
    void listenersHearEveryRelevantChange() {
        List<Integer> heard = new ArrayList<>();
        watch.addListener(() -> heard.add(1));
        GearSetup setup = book.addSetup(book.sections().get(0).id(), "A");
        active.toggle(setup.id());
        spellbook.set(Spellbook.ANCIENT);
        assertEquals(3, heard.size());
    }
    @Test
    void warnsWhenTheQuickPrayersDifferAndJoinsBothWarnings() {
        GearSetup zulrah = book.addSetup(book.sections().get(0).id(), "Zulrah");
        book.changeMeta(zulrah.id(), meta -> meta.withRequirements(new Requirements(Spellbook.LUNAR, Set.of(Prayer.AUGURY, Prayer.RIGOUR))));
        active.toggle(zulrah.id());
        quickPrayers.set(Set.of(Prayer.PIETY));

        assertEquals(Optional.of("Quick prayers differ: set Rigour, Augury"), watch.quickPrayerWarning());
        assertEquals(Optional.of("Zulrah needs Lunar, you are on Standard · Quick prayers differ: set Rigour, Augury"), watch.warning());

        spellbook.set(Spellbook.LUNAR);
        quickPrayers.set(Set.of(Prayer.RIGOUR, Prayer.AUGURY));
        assertTrue(watch.warning().isEmpty(), "a matching selection is quiet");

        quickPrayers.set(Set.of(Prayer.RIGOUR, Prayer.AUGURY, Prayer.PROTECT_FROM_MAGIC));
        assertTrue(watch.quickPrayerWarning().isPresent(), "an extra prayer is a difference too");

        quickPrayers.logOut();
        assertTrue(watch.warning().isEmpty());
    }

}
