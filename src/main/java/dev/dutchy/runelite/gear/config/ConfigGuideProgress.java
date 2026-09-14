package dev.dutchy.runelite.gear.config;

import dev.dutchy.runelite.gear.guide.GuideId;
import dev.dutchy.runelite.gear.guide.GuideProgress;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Completed guides kept as a comma-separated list in the plugin's config. */
@Singleton
public final class ConfigGuideProgress implements GuideProgress {

    private static final String SEPARATOR = ",";

    private final ConfigManager configManager;

    @Inject
    public ConfigGuideProgress(ConfigManager configManager) {
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    @Override
    public Set<GuideId> completed() {
        return parse(configManager.getConfiguration(GearComposerConfig.GROUP, GearComposerConfig.COMPLETED_GUIDES));
    }

    @Override
    public void markCompleted(GuideId id) {
        Set<GuideId> done = completed();
        done.add(Objects.requireNonNull(id, "id"));
        configManager.setConfiguration(GearComposerConfig.GROUP, GearComposerConfig.COMPLETED_GUIDES, serialise(done));
    }

    /** Names that no longer match a guide are dropped rather than failing the whole list. */
    static Set<GuideId> parse(String stored) {
        Set<GuideId> done = EnumSet.noneOf(GuideId.class);
        if (stored == null || stored.isBlank()) {
            return done;
        }
        for (String name : stored.split(SEPARATOR)) {
            try {
                done.add(GuideId.valueOf(name.strip()));
            } catch (IllegalArgumentException ignored) {
                // a guide that no longer exists is simply forgotten
            }
        }
        return done;
    }

    static String serialise(Set<GuideId> done) {
        return done.stream().sorted().map(GuideId::name).collect(Collectors.joining(SEPARATOR));
    }
}
