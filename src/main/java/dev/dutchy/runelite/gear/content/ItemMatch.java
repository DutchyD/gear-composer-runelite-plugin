package dev.dutchy.runelite.gear.content;

/** How loosely a setup item matches what the bank holds, and which member of its family is shown first. */
public enum ItemMatch {
    /** Only this exact item id. */
    EXACT("Exact", "Only this precise item"),
    /** Any item in the same family, the fullest charges or doses first. */
    ANY_VARIANT("Any variant", "Charges, doses, degrade states and imbues all count; the fullest is shown"),
    /** Any item in the family, this one first, then the fullest. */
    PREFER_THIS("This first", "Any variant counts, but this one is shown when the bank has it"),
    /** Any item in the family, the emptiest first, to use up partial ones. */
    EMPTIEST_FIRST("Use up", "Any variant counts; the emptiest is shown so partial ones get used first");

    public static final ItemMatch DEFAULT = ANY_VARIANT;

    private final String displayName;
    private final String description;

    ItemMatch(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public boolean acceptsVariants() {
        return this != EXACT;
    }
}
