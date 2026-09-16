package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.SetupId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Which setup the bank is currently showing, if any. Safe to use from any thread: the bank close
 * handler clears it from the client thread while the panel and hotkeys toggle it from the EDT.
 */
public final class ActiveSetup {

    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private final Object lock = new Object();

    private SetupId active;

    public Optional<SetupId> current() {
        synchronized (lock) {
            return Optional.ofNullable(active);
        }
    }

    public boolean isActive(SetupId id) {
        Objects.requireNonNull(id, "id");
        synchronized (lock) {
            return id.equals(active);
        }
    }

    /** Turns the setup on, or off again when it is already the active one. Returns the new state. */
    public Optional<SetupId> toggle(SetupId id) {
        Objects.requireNonNull(id, "id");
        Optional<SetupId> now;
        synchronized (lock) {
            active = id.equals(active) ? null : id;
            now = Optional.ofNullable(active);
        }
        notifyListeners();
        return now;
    }

    public void clear() {
        synchronized (lock) {
            if (active == null) {
                return;
            }
            active = null;
        }
        notifyListeners();
    }

    /** Listeners run right after every change, outside the lock; read {@link #current()} for the new state. */
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
