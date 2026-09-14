package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.account.AccountDirectory;
import dev.dutchy.runelite.gear.account.AccountEntry;
import dev.dutchy.runelite.gear.account.AccountRoster;
import dev.dutchy.runelite.gear.account.CurrentAccount;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Whose setups are on show, and who each setup belongs to. The logged-in character's name is
 * remembered as soon as it is known, so their setups still read as theirs when they are logged out.
 */
public final class Accounts {

    /** What the sidebar does when who is logged in, or what they are called, changes. */
    interface Listener {

        void loggedIn();

        void loggedOut();

        /** A name was learnt or forgotten, so the accounts screen has something else to show. */
        void rosterChanged();
    }

    private final GearSetupBook book;
    private final AccountDirectory directory;
    private final CurrentAccount account;

    private Prompts prompts = SilentPrompts.INSTANCE;
    private Listener listener = new Listener() {
        @Override
        public void loggedIn() {
        }

        @Override
        public void loggedOut() {
        }

        @Override
        public void rosterChanged() {
        }
    };

    private Owner viewed;

    @Inject
    public Accounts(GearSetupBook book, AccountDirectory directory, CurrentAccount account) {
        this.book = Objects.requireNonNull(book, "book");
        this.directory = Objects.requireNonNull(directory, "directory");
        this.account = Objects.requireNonNull(account, "account");
        this.viewed = mine().orElse(Owner.shared());
        rememberCurrent();
    }

    /** Wired by the sidebar, after which the module follows logins and name changes itself. */
    void shownBy(Prompts newPrompts, Listener newListener) {
        this.prompts = Objects.requireNonNull(newPrompts, "newPrompts");
        this.listener = Objects.requireNonNull(newListener, "newListener");
        account.addListener(this::refresh);
        directory.addListener(() -> listener.rosterChanged());
    }

    /** The logged-in character, when there is one. */
    public Optional<Owner> mine() {
        return account.key().map(Owner::account);
    }

    public Owner viewed() {
        return viewed;
    }

    public void view(Owner owner) {
        viewed = Objects.requireNonNull(owner, "owner");
    }

    /** New setups belong to whoever's list is on show. */
    public Owner ownerForNewSetups() {
        return viewed;
    }

    /** Every account with setups, plus the shared one. */
    public List<AccountEntry> roster() {
        return AccountRoster.of(book.sections(), directory, account);
    }

    public String describeViewed() {
        return describe(viewed);
    }

    /** The remembered name, the live one for whoever is logged in, or a shortened key. */
    public String describe(Owner owner) {
        if (owner.isShared()) {
            return AccountEntry.SHARED_NAME;
        }
        String key = owner.account().orElseThrow();
        return directory.nameOf(key)
                .or(() -> account.key().filter(key::equals).flatMap(unused -> account.displayName()))
                .orElse(AccountEntry.UNKNOWN_NAME + " " + AccountEntry.shorten(key));
    }

    /** Where a setup could go instead, shared first. */
    public List<Owner> otherOwners(GearSetup setup) {
        List<Owner> owners = new ArrayList<>();
        owners.add(Owner.shared());
        roster().stream().map(AccountEntry::owner).filter(owner -> !owner.isShared()).forEach(owners::add);
        owners.remove(setup.owner());
        return List.copyOf(owners);
    }

    /** An account is only forgotten once nothing is left of it; true when it was. */
    public boolean forget(AccountEntry entry) {
        if (!entry.canForget()) {
            prompts.say(entry.name() + " still holds setups");
            return false;
        }
        entry.owner().account().ifPresent(directory::forget);
        prompts.say("Forgot " + entry.name());
        return true;
    }

    public void moveTo(GearSetup setup, Owner owner) {
        if (setup.owner().equals(owner)) {
            return;
        }
        book.setOwner(setup.id(), owner);
        prompts.announce(owner.isShared()
                ? "Shared " + setup.name() + " with all accounts"
                : "Moved " + setup.name() + " to " + describe(owner));
    }

    /** Flips between shared and the account whose list is on show, or the logged-in one when that is shared. */
    public void toggleSharing(GearSetup setup) {
        if (!setup.owner().isShared()) {
            moveTo(setup, Owner.shared());
            return;
        }
        Optional<Owner> target = viewed.isShared() ? mine() : Optional.of(viewed);
        if (target.isEmpty()) {
            prompts.say("Log in, or open an account, to keep " + setup.name() + " to one");
            return;
        }
        moveTo(setup, target.get());
    }

    /** Login opens that character's list; logout leaves nothing of theirs on show. */
    public void refresh() {
        rememberCurrent();
        Optional<Owner> mine = mine();
        if (mine.isPresent()) {
            viewed = mine.get();
            listener.loggedIn();
        } else {
            listener.loggedOut();
        }
    }

    private void rememberCurrent() {
        account.key().ifPresent(key -> account.displayName().ifPresent(name -> directory.remember(key, name)));
    }
}
