package dev.dutchy.runelite.libs.ui.search;

import java.util.Locale;

public final class ItemNames {

    private ItemNames() {
    }

    public static String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
