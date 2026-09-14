package dev.dutchy.runelite.gear.config;

import dev.dutchy.runelite.gear.ui.TileStyle;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public final class ConfigViewSettings implements ViewSettings {

    private final GearComposerConfig config;
    private final ConfigManager configManager;

    @Inject
    public ConfigViewSettings(GearComposerConfig config, ConfigManager configManager) {
        this.config = Objects.requireNonNull(config, "config");
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    @Override
    public TileStyle tileStyle() {
        return config.tileStyle();
    }

    @Override
    public void setTileStyle(TileStyle style) {
        configManager.setConfiguration(GearComposerConfig.GROUP, GearComposerConfig.TILE_STYLE, Objects.requireNonNull(style, "style"));
    }
}
