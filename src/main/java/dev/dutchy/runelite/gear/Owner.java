package dev.dutchy.runelite.gear;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.Optional;

/** Which account a setup belongs to; a shared setup shows on every account. */
@Value
@Accessors(fluent = true)
public class Owner {
    String accountKey;

    private static final Owner SHARED = new Owner(null);

    /** {@code accountKey} may be null, meaning shared; prefer {@link #shared()} and {@link #account(String)}. */
    public Owner(String accountKey) {
        if (accountKey != null && accountKey.isBlank()) {
            throw new IllegalArgumentException("Account key must not be blank");
        }
        this.accountKey = accountKey;
    }

    public static Owner shared() {
        return SHARED;
    }

    public static Owner account(String key) {
        return new Owner(Objects.requireNonNull(key, "key"));
    }

    public Optional<String> account() {
        return Optional.ofNullable(accountKey);
    }

    public boolean isShared() {
        return accountKey == null;
    }
}
