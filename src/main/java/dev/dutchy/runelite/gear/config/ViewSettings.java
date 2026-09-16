package dev.dutchy.runelite.gear.config;

import dev.dutchy.runelite.gear.ui.TileStyle;

/** How the player wants the setup list drawn; remembered between sessions. */
public interface ViewSettings {

    TileStyle tileStyle();

    void setTileStyle(TileStyle style);

    /** Forgets nothing between runs; for tests and previews. */
    static ViewSettings inMemory(TileStyle initial) {
        return new ViewSettings() {
            private TileStyle style = initial;

            @Override
            public TileStyle tileStyle() {
                return style;
            }

            @Override
            public void setTileStyle(TileStyle newStyle) {
                style = newStyle;
            }
        };
    }
}
