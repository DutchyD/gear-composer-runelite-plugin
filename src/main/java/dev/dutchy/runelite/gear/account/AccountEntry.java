package dev.dutchy.runelite.gear.account;

import dev.dutchy.runelite.gear.Owner;
import lombok.Value;

import java.util.Objects;

/** One card on the accounts screen: an owner, what to call it, how many setups it holds, and whether it is logged in. */
@Value
public class AccountEntry {
    Owner owner;
    String name;
    int setupCount;
    boolean loggedIn;

    public static final String SHARED_NAME = "Shared with all accounts";
    public static final String UNKNOWN_NAME = "Unknown account";
    private static final int SHORT_ID_LENGTH = 6;

    public AccountEntry(Owner owner, String name, int setupCount, boolean loggedIn) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.name = Objects.requireNonNull(name, "name");
        if (setupCount < 0) {
            throw new IllegalArgumentException("Setup count must not be negative, got " + setupCount);
        }
        this.setupCount = setupCount;
        this.loggedIn = loggedIn;
    }

    public boolean isShared() {
        return owner.isShared();
    }

    /** The tail of the profile key, enough to tell two accounts with one name apart. */
    public String shortId() {
        return owner.account().map(AccountEntry::shorten).orElse("");
    }

    public static String shorten(String key) {
        return key.length() <= SHORT_ID_LENGTH ? key : key.substring(key.length() - SHORT_ID_LENGTH);
    }

    public boolean canForget() {
        return !isShared() && setupCount == 0;
    }
}
