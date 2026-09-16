package dev.dutchy.runelite;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GearComposerPluginLauncher {

    public static void main(String[] args) throws Exception {
        //noinspection unchecked
        ExternalPluginManager.loadBuiltin(
                GearComposerPlugin.class
        );

        RuneLite.main(args);
    }
}
