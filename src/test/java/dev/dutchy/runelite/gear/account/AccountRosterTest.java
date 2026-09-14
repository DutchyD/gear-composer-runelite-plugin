package dev.dutchy.runelite.gear.account;

import com.google.gson.Gson;
import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.SectionId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountRosterTest {

    private static List<GearSection> book() {
        return List.of(new GearSection(SectionId.random(), "S", List.of(
                GearSetup.named("Mine").withOwner(Owner.account("rsprofile.main")),
                GearSetup.named("Mine too").withOwner(Owner.account("rsprofile.main")),
                GearSetup.named("Mystery").withOwner(Owner.account("rsprofile.gone")),
                GearSetup.named("Everyone").withOwner(Owner.shared()))));
    }

    @Test
    void sharedComesFirstThenAccountsByNameWithCountsAndTheLoggedInMark() {
        AccountDirectory directory = AccountDirectory.inMemory();
        directory.remember("rsprofile.main", "Dutchy");
        directory.remember("rsprofile.alt", "Alt");
        List<AccountEntry> roster = AccountRoster.of(book(), directory, FixedAccount.of("rsprofile.main", "Dutchy"));

        assertEquals(List.of(AccountEntry.SHARED_NAME, "Alt", "Dutchy", AccountEntry.UNKNOWN_NAME),
                roster.stream().map(AccountEntry::name).collect(Collectors.toList()));
        assertEquals(List.of(1, 0, 2, 1), roster.stream().map(AccountEntry::setupCount).collect(Collectors.toList()));
        assertTrue(roster.get(2).loggedIn());
        assertFalse(roster.get(1).loggedIn());
        assertEquals("e.main", roster.get(2).shortId());
        assertTrue(roster.get(1).canForget(), "an empty account can be forgotten");
        assertFalse(roster.get(2).canForget());
        assertFalse(roster.get(0).canForget(), "the shared card stays");
    }

    @Test
    void aLoggedInAccountNotYetRememberedStillGetsACardUnderItsName() {
        List<AccountEntry> roster = AccountRoster.of(List.of(), AccountDirectory.inMemory(), FixedAccount.of("rsprofile.new", "Fresh"));
        assertEquals("Fresh", roster.get(1).name());
        assertEquals(1, roster.get(0).setupCount() + roster.get(1).setupCount() + 1);
    }

    @Test
    void theConfigDirectoryReadsDamagedValuesAsEmpty() {
        Gson gson = new Gson();
        assertEquals(Map.of("rsprofile.a", "Alpha"), ConfigAccountDirectory.parse(gson, "{\"rsprofile.a\":\"Alpha\"}"));
        assertTrue(ConfigAccountDirectory.parse(gson, null).isEmpty());
        assertTrue(ConfigAccountDirectory.parse(gson, "not json").isEmpty());
        assertTrue(ConfigAccountDirectory.parse(gson, "  ").isEmpty());
    }
}
