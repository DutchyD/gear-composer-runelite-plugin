package dev.dutchy.runelite.libs.ui.image;

@FunctionalInterface
public interface ItemImageProvider {

    ItemImage imageFor(ItemImageRequest request);
}
