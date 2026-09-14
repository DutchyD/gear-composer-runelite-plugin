package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.account.AccountDirectory;
import dev.dutchy.runelite.gear.account.AccountEntry;
import dev.dutchy.runelite.gear.account.FixedAccount;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountsTest {

    private static final Owner MAIN = Owner.account("rsprofile.main");
    private static final Owner IRON = Owner.account("rsprofile.iron");

    private final GearSetupBook book = new GearSetupBook();
    private final AccountDirectory directory = AccountDirectory.inMemory();
    private final FixedAccount account = FixedAccount.of("rsprofile.main", "Dutchy");
    private final RecordingPrompts prompts = new RecordingPrompts();
    private final List<String> events = new ArrayList<>();

    private final SectionId section = book.sections().get(0).id();

    private Accounts open() {
        Accounts accounts = new Accounts(book, directory, account);
        accounts.shownBy(prompts, new Accounts.Listener() {
            @Override
            public void loggedIn() {
                events.add("in");
            }

            @Override
            public void loggedOut() {
                events.add("out");
            }

            @Override
            public void rosterChanged() {
                events.add("roster");
            }
        });
        return accounts;
    }

    @Test
    void theListStartsOnWhoeverIsLoggedInAndTheirNameIsRemembered() {
        Accounts accounts = open();

        assertEquals(MAIN, accounts.viewed());
        assertEquals(Optional.of("Dutchy"), directory.nameOf("rsprofile.main"));
        assertEquals(MAIN, accounts.ownerForNewSetups());
    }

    @Test
    void withNobodyLoggedInTheSharedListIsTheOneOnShow() {
        FixedAccount loggedOut = FixedAccount.loggedOut();

        Accounts accounts = new Accounts(book, directory, loggedOut);

        assertTrue(accounts.viewed().isShared());
        assertEquals(Optional.empty(), accounts.mine());
    }

    @Test
    void anOwnerIsNamedFromTheDirectoryTheGameOrItsKey() {
        directory.remember("rsprofile.iron", "Ironman");
        Accounts accounts = open();

        assertEquals(AccountEntry.SHARED_NAME, accounts.describe(Owner.shared()));
        assertEquals("Ironman", accounts.describe(IRON));
        assertEquals("Dutchy", accounts.describe(MAIN), "the live name stands in until it is remembered");
        assertTrue(accounts.describe(Owner.account("rsprofile.unseen")).startsWith(AccountEntry.UNKNOWN_NAME));
        assertEquals("Dutchy", accounts.describeViewed());
    }

    @Test
    void aSetupCanGoToTheSharedListOrAnyOtherAccount() {
        directory.remember("rsprofile.iron", "Ironman");
        Accounts accounts = open();
        GearSetup mine = book.addSetup(section, GearSetup.named("Vorkath").withOwner(MAIN));

        List<String> named = accounts.otherOwners(mine).stream().map(accounts::describe).collect(Collectors.toList());

        assertEquals(AccountEntry.SHARED_NAME, named.get(0), "shared comes first");
        assertTrue(named.contains("Ironman"));
        assertFalse(accounts.otherOwners(mine).contains(MAIN), "not where it already is");
    }

    @Test
    void movingASetupSaysWhereItWentAndDoingItTwiceSaysNothing() {
        Accounts accounts = open();
        GearSetup vorkath = book.addSetup(section, "Vorkath");
        directory.remember("rsprofile.iron", "Ironman");

        accounts.moveTo(vorkath, IRON);

        assertEquals(IRON, book.setup(vorkath.id()).orElseThrow().owner());
        assertEquals("Moved Vorkath to Ironman", prompts.lastMessage());

        accounts.moveTo(book.setup(vorkath.id()).orElseThrow(), IRON);

        assertEquals(1, prompts.messages().size(), "nothing changed, so nothing was said");
    }

    @Test
    void sharingFlipsBetweenEveryoneAndTheListOnShow() {
        Accounts accounts = open();
        GearSetup vorkath = book.addSetup(section, "Vorkath");

        accounts.toggleSharing(vorkath);

        assertEquals(MAIN, book.setup(vorkath.id()).orElseThrow().owner(), "the viewed account takes it");

        accounts.toggleSharing(book.setup(vorkath.id()).orElseThrow());

        assertTrue(book.setup(vorkath.id()).orElseThrow().owner().isShared());
        assertEquals("Shared Vorkath with all accounts", prompts.lastMessage());
    }

    @Test
    void withNobodyLoggedInAndTheSharedListOnShowThereIsNobodyToKeepItTo() {
        FixedAccount loggedOut = FixedAccount.loggedOut();
        Accounts accounts = new Accounts(book, directory, loggedOut);
        accounts.shownBy(prompts, new Accounts.Listener() {
            @Override
            public void loggedIn() {
            }

            @Override
            public void loggedOut() {
            }

            @Override
            public void rosterChanged() {
            }
        });
        GearSetup vorkath = book.addSetup(section, "Vorkath");

        accounts.toggleSharing(vorkath);

        assertTrue(book.setup(vorkath.id()).orElseThrow().owner().isShared());
        assertEquals("Log in, or open an account, to keep Vorkath to one", prompts.lastMessage());
    }

    @Test
    void anAccountIsOnlyForgottenOnceNothingIsLeftOfIt() {
        directory.remember("rsprofile.iron", "Ironman");
        Accounts accounts = open();
        book.addSetup(section, GearSetup.named("Wintertodt").withOwner(IRON));

        AccountEntry held = accounts.roster().stream().filter(entry -> entry.owner().equals(IRON)).findFirst().orElseThrow();
        assertFalse(accounts.forget(held));
        assertEquals("Ironman still holds setups", prompts.lastMessage());
        assertEquals(Optional.of("Ironman"), directory.nameOf("rsprofile.iron"));

        book.removeSetup(book.sections().get(0).setups().get(0).id());
        AccountEntry empty = accounts.roster().stream().filter(entry -> entry.owner().equals(IRON)).findFirst().orElseThrow();

        assertTrue(accounts.forget(empty));
        assertEquals(Optional.empty(), directory.nameOf("rsprofile.iron"));
    }

    @Test
    void loggingInOpensThatCharactersListWithoutBeingAsked() {
        Accounts accounts = open();
        accounts.view(Owner.shared());

        account.logIn("rsprofile.iron", "Ironman");

        assertEquals(IRON, accounts.viewed());
        assertEquals(Optional.of("Ironman"), directory.nameOf("rsprofile.iron"));
        assertEquals("in", events.get(events.size() - 1));
        assertTrue(events.contains("roster"), "a name just learnt changes the accounts screen");

        account.logOut();

        assertEquals(IRON, accounts.viewed(), "the list stays where it was");
        assertEquals("out", events.get(events.size() - 1));
    }
}
