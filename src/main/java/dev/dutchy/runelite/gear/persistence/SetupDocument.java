package dev.dutchy.runelite.gear.persistence;

import dev.dutchy.runelite.gear.*;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * One setup as a flat JSON document: name, icon, the selected variant's contents at the top level, every variant
 * when there is more than one, and the meta that travels with it. Pins, hotkeys, ids and ownership are personal
 * to a book, so documents that need them extend this one.
 */
public class SetupDocument extends ContentCodec.ContentDto {
    String name;
    Integer icon;
    List<VariantDto> variants;
    Integer variant;
    String label;
    List<String> tags;
    String notes;
    String spellbook;
    List<String> quickPrayers;

    public SetupDocument() {
    }

    public static final class VariantDto extends ContentCodec.ContentDto {
        String name;
    }

    /** Fills the document from the setup, leaving fields a subclass adds alone. */
    public static <D extends SetupDocument> D write(GearSetup setup, D document) {
        Objects.requireNonNull(setup, "setup");
        ContentCodec.write(setup.content(), document);
        document.name = setup.name();
        document.icon = setup.icon().map(ItemId::value).orElse(null);
        if (setup.hasVariants()) {
            document.variants = setup.variants().stream().map(SetupDocument::toDto).collect(Collectors.toList());
            document.variant = setup.selectedIndex();
        }
        SetupMeta meta = setup.meta();
        document.label = meta.label().isNone() ? null : meta.label().name();
        document.tags = meta.tags().isEmpty() ? null : List.copyOf(meta.tags());
        document.notes = meta.hasNotes() ? meta.notes() : null;
        document.spellbook = meta.requirements().spellbook().isRequirement() ? meta.requirements().spellbook().name() : null;
        document.quickPrayers = meta.requirements().hasQuickPrayers()
                ? meta.requirements().quickPrayers().stream().sorted().map(Prayer::name).collect(Collectors.toList()) : null;
        return document;
    }

    public boolean hasName() {
        return name != null;
    }

    /** Copies content fields from an older document that nested them under their own object. */
    public void adoptContent(ContentCodec.ContentDto content) {
        type = content.type;
        equipment = content.equipment;
        inventory = content.inventory;
        left = content.left;
        right = content.right;
        inventoryDividers = content.inventoryDividers;
        leftDividers = content.leftDividers;
        rightDividers = content.rightDividers;
        rows = content.rows;
        cells = content.cells;
    }

    /** A fresh setup under a new id; the caller adds whatever personal fields it keeps. */
    public GearSetup toSetup() {
        GearSetup setup = GearSetup.named(ContentCodec.require(name, "setup name"));
        if (variants == null || variants.isEmpty()) {
            setup = setup.withContent(ContentCodec.fromDto(this));
        } else {
            List<SetupVariant> read = new ArrayList<>();
            for (VariantDto dto : variants) {
                read.add(new SetupVariant(ContentCodec.require(dto.name, "variant name"), ContentCodec.fromDto(dto)));
            }
            setup = setup.withVariants(read, variant == null ? 0 : variant);
        }
        if (icon != null) {
            setup = setup.withIcon(ItemId.of(icon));
        }
        return setup.withMeta(meta());
    }

    /** The shared part of the meta; personal flags are left at their defaults. */
    public SetupMeta meta() {
        Requirements requirements = new Requirements(spellbook == null ? Spellbook.ANY : Spellbook.valueOf(spellbook), prayersFrom(quickPrayers));
        return SetupMeta.none()
                .withLabel(label == null ? ColourLabel.NONE : ColourLabel.valueOf(label))
                .withTags(tags == null ? Set.of() : new HashSet<>(tags))
                .withNotes(notes == null ? "" : notes)
                .withRequirements(requirements);
    }

    public static Set<Prayer> prayersFrom(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }
        Set<Prayer> prayers = EnumSet.noneOf(Prayer.class);
        names.forEach(name -> prayers.add(Prayer.valueOf(name)));
        return prayers;
    }

    private static VariantDto toDto(SetupVariant variant) {
        VariantDto dto = ContentCodec.write(variant.content(), new VariantDto());
        dto.name = variant.name();
        return dto;
    }
}
