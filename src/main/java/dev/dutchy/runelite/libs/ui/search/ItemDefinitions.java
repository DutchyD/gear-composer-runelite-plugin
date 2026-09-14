package dev.dutchy.runelite.libs.ui.search;

/** Which item definitions belong in a searchable catalogue. */
public final class ItemDefinitions {

    private static final String UNDEFINED_NAME = "null";
    private static final int NONE = -1;

    private ItemDefinitions() {
    }

    /**
     * An item earns a place only if it could sit in a bank, which the game marks by giving it a
     * placeholder. That drops the copies used by minigames and other closed content, which share a
     * name with the real item and cannot be banked.
     *
     * <p>Noted and placeholder ids are dropped as well: each duplicates a real item under another id.
     * Unused ids in the cache report a blank or "null" name.
     */
    public static boolean isBankable(String name, int note, int placeholderTemplateId, int placeholderId) {
        if (note != NONE || placeholderTemplateId != NONE) {
            return false;
        }
        if (placeholderId == NONE) {
            return false;
        }
        return name != null && !name.isBlank() && !UNDEFINED_NAME.equals(name);
    }
}
