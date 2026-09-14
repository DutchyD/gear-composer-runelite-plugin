package dev.dutchy.runelite.gear.account;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dev.dutchy.runelite.gear.config.GearComposerConfig;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Known account names kept as a small JSON object in the plugin's config. */
@Singleton
public final class ConfigAccountDirectory implements AccountDirectory {

    private final ConfigManager configManager;
    private final Gson gson;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    @Inject
    public ConfigAccountDirectory(ConfigManager configManager, Gson gson) {
        this.configManager = Objects.requireNonNull(configManager, "configManager");
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    @Override
    public Map<String, String> names() {
        return parse(gson, configManager.getConfiguration(GearComposerConfig.GROUP, GearComposerConfig.KNOWN_ACCOUNTS));
    }

    @Override
    public void remember(String key, String name) {
        Map<String, String> known = new LinkedHashMap<>(names());
        if (name.equals(known.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(name, "name")))) {
            return;
        }
        store(known);
    }

    @Override
    public void forget(String key) {
        Map<String, String> known = new LinkedHashMap<>(names());
        if (known.remove(key) != null) {
            store(known);
        }
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /** A damaged value is treated as empty rather than failing every read. */
    static Map<String, String> parse(Gson gson, String stored) {
        if (stored == null || stored.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, String> parsed = gson.fromJson(stored, new TypeToken<Map<String, String>>() {
            }.getType());
            return parsed == null ? Map.of() : Map.copyOf(parsed);
        } catch (JsonSyntaxException e) {
            return Map.of();
        }
    }

    private void store(Map<String, String> known) {
        configManager.setConfiguration(GearComposerConfig.GROUP, GearComposerConfig.KNOWN_ACCOUNTS, gson.toJson(known));
        listeners.forEach(Runnable::run);
    }
}
