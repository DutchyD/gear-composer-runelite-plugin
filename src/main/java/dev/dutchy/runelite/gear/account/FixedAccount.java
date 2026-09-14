package dev.dutchy.runelite.gear.account;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** An account that only changes when told to; for tests and previews. */
public final class FixedAccount implements CurrentAccount {

    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private String key;
    private String displayName;

    private FixedAccount(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public static FixedAccount loggedOut() {
        return new FixedAccount(null, null);
    }

    public static FixedAccount of(String key, String displayName) {
        return new FixedAccount(Objects.requireNonNull(key, "key"), Objects.requireNonNull(displayName, "displayName"));
    }

    public void logIn(String newKey, String newDisplayName) {
        key = Objects.requireNonNull(newKey, "newKey");
        displayName = Objects.requireNonNull(newDisplayName, "newDisplayName");
        listeners.forEach(Runnable::run);
    }

    public void logOut() {
        key = null;
        displayName = null;
        listeners.forEach(Runnable::run);
    }

    @Override
    public Optional<String> key() {
        return Optional.ofNullable(key);
    }

    @Override
    public Optional<String> displayName() {
        return Optional.ofNullable(displayName);
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }
}
