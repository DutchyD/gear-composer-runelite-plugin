package dev.dutchy.runelite.libs.ui.search;

@FunctionalInterface
public interface ItemIndexProvider {

    /** May be empty while the catalogue is still loading. */
    ItemIndex index();

    /** True once the index holds at least one item. */
    default boolean warmUp() {
        return !index().isEmpty();
    }
}
