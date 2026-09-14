package dev.dutchy.runelite.gear.persistence;

import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

/** Keeps the book in RuneLite's configuration, which follows the player's RuneLite account. */
@Singleton
public final class ConfigBookStore implements BookStore {

    static final String GROUP = "gearcomposer";
    static final String KEY = "book";
    static final String QUARANTINE_PREFIX = "book.unreadable.";

    private final ConfigManager configManager;
    private final Clock clock;

    @Inject
    public ConfigBookStore(ConfigManager configManager) {
        this(configManager, Clock.systemUTC());
    }

    ConfigBookStore(ConfigManager configManager, Clock clock) {
        this.configManager = Objects.requireNonNull(configManager, "configManager");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public Optional<String> read() {
        String value = configManager.getConfiguration(GROUP, KEY);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    @Override
    public void write(String encoded) {
        configManager.setConfiguration(GROUP, KEY, Objects.requireNonNull(encoded, "encoded"));
    }

    @Override
    public void quarantine(String encoded) {
        configManager.setConfiguration(GROUP, QUARANTINE_PREFIX + clock.millis(), Objects.requireNonNull(encoded, "encoded"));
    }
}
