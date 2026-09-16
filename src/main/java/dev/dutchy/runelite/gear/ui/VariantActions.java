package dev.dutchy.runelite.gear.ui;

/** What a view of the open setup's variants can ask of its host. Indexes are positions in the setup's variant list. */
interface VariantActions {

    /** Makes the variant the setup's chosen one, so the bank shows it; the page stays on what it edits. */
    void selectVariant(int index);

    /** Opens the variant on the page for editing, leaving the bank's choice alone. */
    void editVariant(int index);

    /** Asks for a name and adds a variant holding the chosen start, then opens it for editing. */
    void addVariant(NewVariant start);

    void renameVariant(int index);

    /** Inserts a copy right after the variant. */
    void duplicateVariant(int index);

    void deleteVariant(int index);

    /** Moves the variant one place up or down the list. */
    void moveVariant(int index, int to);
}
