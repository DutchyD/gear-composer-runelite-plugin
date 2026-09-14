package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupVariant;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankTitleTest {

    private static final GearSetup VORKATH = GearSetup.named("Vorkath").withAddedVariant(new SetupVariant("Mage", GearContent.empty())).withSelectedVariant(1);

    @Test
    void theTitleNamesTheSetupAndItsVariantUnlessAWarningTakesPrecedence() {
        assertEquals(Optional.of("<col=ff981f>Vorkath · Mage</col>"), BankTitle.textFor(null, VORKATH));
        assertEquals(Optional.of("<col=ff981f>Solo</col>"), BankTitle.textFor(null, GearSetup.named("Solo")));
        assertEquals(Optional.of("<col=ff5c5c>Wrong spellbook</col>"), BankTitle.textFor("Wrong spellbook", VORKATH));
        assertEquals(Optional.empty(), BankTitle.textFor(null, null), "no setup leaves the game's title alone");
    }
}
