package dev.dutchy.runelite.gear.account;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** The character names seen so far, by profile key, so accounts can be told apart while logged out. */
public interface AccountDirectory {

    Map<String, String> names();

    void remember(String key, String name);

    void forget(String key);

    void addListener(Runnable listener);

    default Optional<String> nameOf(String key) {
        return Optional.ofNullable(names().get(key));
    }

    static AccountDirectory inMemory() {
        return new AccountDirectory() {
            private final Map<String, String> known = new HashMap<>();
            private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

            @Override
            public Map<String, String> names() {
                return Map.copyOf(known);
            }

            @Override
            public void remember(String key, String name) {
                known.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(name, "name"));
                listeners.forEach(Runnable::run);
            }

            @Override
            public void forget(String key) {
                if (known.remove(key) != null) {
                    listeners.forEach(Runnable::run);
                }
            }

            @Override
            public void addListener(Runnable listener) {
                listeners.add(Objects.requireNonNull(listener, "listener"));
            }
        };
    }
}
