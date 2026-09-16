package dev.dutchy.runelite.gear;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A setup: one or more variants of the same layout type, one of them selected by id so the selection
 * survives reordering. {@code iconId} may be null, meaning a lettered tile; read it through {@link #icon()}.
 */
@Value
public class GearSetup {
    SetupId id;
    String name;
    ItemId iconId;
    List<SetupVariant> variants;
    VariantId selected;
    Owner owner;
    SetupMeta meta;

    public static final int MAX_NAME_LENGTH = 48;
    public static final int MAX_VARIANTS = 8;

    public GearSetup(SetupId id, String name, ItemId iconId, SetupContent content, Owner owner, SetupMeta meta) {
        this(id, name, iconId, List.of(SetupVariant.of(Objects.requireNonNull(content, "content"))), 0, owner, meta);
    }

    /** Selects by position, for callers holding an index such as storage and the bank tabs. */
    public GearSetup(SetupId id, String name, ItemId iconId, List<SetupVariant> variants, int selectedIndex, Owner owner, SetupMeta meta) {
        this(id, name, iconId, variants, idAt(variants, selectedIndex), owner, meta);
    }

    public GearSetup(SetupId id, String name, ItemId iconId, List<SetupVariant> variants, VariantId selected, Owner owner, SetupMeta meta) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(variants, "variants");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(meta, "meta");
        Objects.requireNonNull(selected, "selected");
        requireValidVariants(variants);
        if (variants.stream().noneMatch(variant -> variant.hasId(selected))) {
            throw new IllegalArgumentException("No variant " + selected + " in this setup");
        }
        this.id = id;
        this.name = requireValidName(name);
        this.iconId = iconId;
        this.variants = List.copyOf(variants);
        this.selected = selected;
        this.owner = owner;
        this.meta = meta;
    }

    private static VariantId idAt(List<SetupVariant> variants, int index) {
        Objects.requireNonNull(variants, "variants");
        if (index < 0 || index >= variants.size()) {
            throw new IllegalArgumentException("No variant " + index + " among " + variants.size());
        }
        return variants.get(index).id();
    }

    public static GearSetup named(String name) {
        return new GearSetup(SetupId.random(), name, null, GearContent.empty(), Owner.shared(), SetupMeta.none());
    }

    public static GearSetup of(String name, ItemId icon) {
        return named(name).withIcon(icon);
    }

    public Optional<ItemId> icon() {
        return Optional.ofNullable(iconId);
    }

    /** The selected variant's contents. */
    public SetupContent content() {
        return variant().content();
    }

    /** The selected variant. */
    public SetupVariant variant() {
        return variants.get(selectedIndex());
    }

    /** Where the selected variant sits, for views and the bank tabs. */
    public int selectedIndex() {
        return indexOf(selected);
    }

    public Optional<SetupVariant> variantAt(int index) {
        return index >= 0 && index < variants.size() ? Optional.of(variants.get(index)) : Optional.empty();
    }

    public Optional<SetupVariant> variant(VariantId variantId) {
        return variants.stream().filter(variant -> variant.hasId(variantId)).findFirst();
    }

    /** The id of the variant at the position, empty when there is none there. */
    public Optional<VariantId> variantIdAt(int index) {
        return variantAt(index).map(SetupVariant::id);
    }

    /** Where the variant sits, or -1 when this setup does not hold it. */
    public int positionOf(VariantId variantId) {
        for (int i = 0; i < variants.size(); i++) {
            if (variants.get(i).hasId(variantId)) {
                return i;
            }
        }
        return -1;
    }

    private int indexOf(VariantId variantId) {
        int at = positionOf(variantId);
        if (at < 0) {
            throw new IllegalArgumentException("No variant " + variantId + " in this setup");
        }
        return at;
    }

    public boolean hasVariants() {
        return variants.size() > 1;
    }

    public boolean hasVariantNamed(String variantName) {
        return variants.stream().anyMatch(variant -> variant.isNamed(variantName));
    }

    public GearSetup withName(String newName) {
        return new GearSetup(id, newName, iconId, variants, selected, owner, meta);
    }

    public GearSetup withIcon(ItemId newIcon) {
        return new GearSetup(id, name, Objects.requireNonNull(newIcon, "newIcon"), variants, selected, owner, meta);
    }

    public GearSetup withoutIcon() {
        return new GearSetup(id, name, null, variants, selected, owner, meta);
    }

    /** Takes the other setup's icon, or lack of one. */
    public GearSetup withIconOf(GearSetup other) {
        return new GearSetup(id, name, other.iconId, variants, selected, owner, meta);
    }

    /** Replaces the selected variant's contents. */
    public GearSetup withContent(SetupContent newContent) {
        return withVariantContent(selected, newContent);
    }

    /** Replaces one variant's contents. */
    public GearSetup withVariantContent(VariantId variantId, SetupContent newContent) {
        SetupVariant found = variant(variantId).orElseThrow(() -> new IllegalArgumentException("No variant " + variantId));
        return withVariant(variantId, found.withContent(newContent));
    }

    public GearSetup withVariants(List<SetupVariant> newVariants, int newSelected) {
        return new GearSetup(id, name, iconId, newVariants, newSelected, owner, meta);
    }

    public GearSetup withVariants(List<SetupVariant> newVariants, VariantId newSelected) {
        return new GearSetup(id, name, iconId, newVariants, newSelected, owner, meta);
    }

    public GearSetup withSelectedVariant(VariantId variantId) {
        return new GearSetup(id, name, iconId, variants, variantId, owner, meta);
    }

    public GearSetup withSelectedVariant(int index) {
        return withSelectedVariant(idAt(variants, index));
    }

    /** Replaces one variant in place. A replacement under a new id keeps the selection if it had it. */
    public GearSetup withVariant(VariantId variantId, SetupVariant variant) {
        Objects.requireNonNull(variant, "variant");
        int at = indexOf(variantId);
        List<SetupVariant> updated = new ArrayList<>(variants);
        updated.set(at, variant);
        return withVariants(updated, variantId.equals(selected) ? variant.id() : selected);
    }

    public GearSetup withVariant(int index, SetupVariant variant) {
        return withVariant(idAt(variants, index), variant);
    }

    /** Adds the variant after the last; the selection stays where it was. */
    public GearSetup withAddedVariant(SetupVariant variant) {
        return withVariantInserted(variants.size(), variant);
    }

    /** Puts the variant at the position; the ones after it move down and the selection is untouched. */
    public GearSetup withVariantInserted(int index, SetupVariant variant) {
        List<SetupVariant> updated = new ArrayList<>(variants);
        updated.add(index, Objects.requireNonNull(variant, "variant"));
        return withVariants(updated, selected);
    }

    /** Moves a variant to another position; the selection is untouched. */
    public GearSetup withVariantMoved(int from, int to) {
        if (to < 0 || to >= variants.size()) {
            throw new IllegalArgumentException("No position " + to + " among " + variants.size() + " variants");
        }
        List<SetupVariant> updated = new ArrayList<>(variants);
        updated.add(to, updated.remove(from));
        return withVariants(updated, selected);
    }

    public GearSetup withoutVariant(int index) {
        return withoutVariant(idAt(variants, index));
    }

    /** Drops the variant; dropping the selected one selects the nearest remaining to its left. */
    public GearSetup withoutVariant(VariantId variantId) {
        if (variants.size() == 1) {
            throw new IllegalStateException("A setup keeps at least one variant");
        }
        int index = indexOf(variantId);
        List<SetupVariant> updated = new ArrayList<>(variants);
        updated.remove(index);
        VariantId stays = variantId.equals(selected)
                ? updated.get(Math.max(0, Math.min(index, updated.size() - 1))).id()
                : selected;
        return withVariants(updated, stays);
    }

    /** The same setup under another id, as when a stored one is read back. */
    public GearSetup withId(SetupId newId) {
        return new GearSetup(newId, name, iconId, variants, selected, owner, meta);
    }

    public GearSetup withOwner(Owner newOwner) {
        return new GearSetup(id, name, iconId, variants, selected, newOwner, meta);
    }

    public GearSetup withMeta(SetupMeta newMeta) {
        return new GearSetup(id, name, iconId, variants, selected, owner, newMeta);
    }

    public GearSetup withMeta(UnaryOperator<SetupMeta> change) {
        return withMeta(Objects.requireNonNull(change, "change").apply(meta));
    }

    /** A fresh copy under a new id, unpinned and without a hotkey, so nothing collides with the original. */
    public GearSetup copyNamed(String newName) {
        return new GearSetup(SetupId.random(), newName, iconId, variants, selected, owner,
                meta.withPinned(false).withHotkey(Hotkey.NONE));
    }

    public SetupType type() {
        return variants.get(0).type();
    }

    public boolean hasIcon() {
        return iconId != null;
    }

    public boolean isPinned() {
        return meta.pinned();
    }

    public static String requireValidName(String name) {
        Objects.requireNonNull(name, "name");
        String trimmed = name.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Setup name must not be blank");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Setup name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.strip();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_NAME_LENGTH;
    }

    private static void requireValidVariants(List<SetupVariant> variants) {
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("A setup needs at least one variant");
        }
        if (variants.size() > MAX_VARIANTS) {
            throw new IllegalArgumentException("At most " + MAX_VARIANTS + " variants, got " + variants.size());
        }
        SetupType type = variants.get(0).type();
        for (int i = 0; i < variants.size(); i++) {
            SetupVariant variant = variants.get(i);
            if (variant.type() != type) {
                throw new IllegalArgumentException("Every variant must be a " + type.displayName());
            }
            for (int j = 0; j < i; j++) {
                if (variants.get(j).isNamed(variant.name())) {
                    throw new IllegalArgumentException("Two variants are named " + variant.name());
                }
                if (variants.get(j).hasId(variant.id())) {
                    throw new IllegalArgumentException("Two variants share the id " + variant.id());
                }
            }
        }
    }
}
