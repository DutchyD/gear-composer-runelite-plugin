package dev.dutchy.runelite.gear.config;

import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public final class ConfigOnboarding implements Onboarding {

    private final GearComposerConfig config;
    private final ConfigManager configManager;

    @Inject
    public ConfigOnboarding(GearComposerConfig config, ConfigManager configManager) {
        this.config = Objects.requireNonNull(config, "config");
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    @Override
    public boolean isDismissed() {
        return config.onboardingDismissed();
    }

    @Override
    public void dismiss() {
        configManager.setConfiguration(GearComposerConfig.GROUP, GearComposerConfig.ONBOARDING_DISMISSED, true);
    }
}
