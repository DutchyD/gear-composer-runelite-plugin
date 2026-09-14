package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupVariant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameTextTest {

    @Test
    void anglesBecomeTheGamesOwnEscapes() {
        assertEquals("<lt>", GameText.escape("<"));
        assertEquals("<gt>", GameText.escape(">"));
        assertEquals("a<lt>b<gt>c", GameText.escape("a<b>c"));
    }

    @Test
    void plainTextIsLeftExactlyAsTyped() {
        assertEquals("Vorkath", GameText.escape("Vorkath"));
        assertEquals("", GameText.escape(""));
        assertEquals("Tank / DPS · 5", GameText.escape("Tank / DPS · 5"));
    }

    @Test
    void aColourTagAPlayerTypedIsShownRatherThanObeyed() {
        assertEquals("<lt>col=ff0000<gt>Mage", GameText.escape("<col=ff0000>Mage"),
                "the tag has to arrive at the renderer as visible text");
    }

    @Test
    void escapingDoesNotEscapeItsOwnOutput() {
        assertEquals("<lt>lt<gt>", GameText.escape("<lt>"), "one pass over the input, never over the replacement");
    }

    @Test
    void theBankTitleEscapesBothTheSetupAndTheVariant() {
        GearSetup setup = GearSetup.named("A<B")
                .withAddedVariant(new SetupVariant("C>D", GearContent.empty()));

        assertEquals("A<lt>B · C<gt>D", BankTitle.describe(setup.withSelectedVariant(1)));
        assertEquals("A<lt>B", BankTitle.describe(GearSetup.named("A<B")), "one variant means just the name");
    }
}
