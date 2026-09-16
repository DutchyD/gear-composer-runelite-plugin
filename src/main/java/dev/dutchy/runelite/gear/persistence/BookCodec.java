package dev.dutchy.runelite.gear.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dev.dutchy.runelite.gear.*;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.content.VariantId;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/** Turns the book into a versioned JSON document and back, refusing anything it does not understand. */
@Singleton
public final class BookCodec {

    public static final int VERSION = 1;

    private final Gson gson;

    @Inject
    public BookCodec(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    public String encode(List<GearSection> sections) {
        Objects.requireNonNull(sections, "sections");
        BookDto book = new BookDto();
        book.version = VERSION;
        book.sections = sections.stream().map(BookCodec::toDto).collect(Collectors.toList());
        return gson.toJson(book);
    }

    public List<GearSection> decode(String json) {
        Objects.requireNonNull(json, "json");
        BookDto book;
        try {
            book = gson.fromJson(json, BookDto.class);
        } catch (JsonParseException e) {
            throw new BookFormatException("The stored book is not valid JSON", e);
        }
        if (book == null || book.sections == null) {
            throw new BookFormatException("The stored book has no sections");
        }
        if (book.version != VERSION) {
            throw new BookFormatException("Unsupported book version " + book.version + ", expected " + VERSION);
        }
        try {
            return SectionTree.normalised(book.sections.stream().map(BookCodec::fromDto).collect(Collectors.toList()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BookFormatException("The stored book holds invalid values: " + e.getMessage(), e);
        }
    }

    private static SectionDto toDto(GearSection section) {
        SectionDto dto = new SectionDto();
        dto.id = section.id().value().toString();
        dto.name = section.name();
        dto.parent = section.parent().map(parent -> parent.value().toString()).orElse(null);
        dto.setups = section.setups().stream().map(BookCodec::toDto).collect(Collectors.toList());
        return dto;
    }

    private static GearSection fromDto(SectionDto dto) {
        List<GearSetup> setups = new ArrayList<>();
        if (dto.setups != null) {
            dto.setups.forEach(setup -> setups.add(fromDto(setup)));
        }
        SectionId parent = dto.parent == null ? null : new SectionId(UUID.fromString(dto.parent));
        return new GearSection(new SectionId(UUID.fromString(ContentCodec.require(dto.id, "section id"))),
                ContentCodec.require(dto.name, "section name"), setups, parent);
    }

    private static SetupDto toDto(GearSetup setup) {
        SetupDto dto = SetupDocument.write(setup, new SetupDto());
        dto.id = setup.id().value().toString();
        dto.owner = setup.owner().accountKey();
        dto.pinned = setup.meta().pinned() ? Boolean.TRUE : null;
        dto.hotkey = setup.meta().hasHotkey() ? setup.meta().hotkey().serialize() : null;
        dto.variantIds = setup.variants().stream().map(variant -> variant.id().value().toString()).collect(Collectors.toList());
        return dto;
    }

    private static GearSetup fromDto(SetupDto dto) {
        Owner owner = dto.owner == null ? Owner.shared() : Owner.account(dto.owner);
        Hotkey hotkey = dto.hotkey == null ? Hotkey.NONE : Hotkey.parse(dto.hotkey).orElse(Hotkey.NONE);
        SetupMeta meta = dto.meta().withPinned(Boolean.TRUE.equals(dto.pinned)).withHotkey(hotkey);
        return withVariantIds(dto.toSetup(), dto.variantIds)
                .withId(new SetupId(UUID.fromString(ContentCodec.require(dto.id, "setup id"))))
                .withOwner(owner)
                .withMeta(meta);
    }

    /** Puts back the variant ids the book keeps, so pointers such as history survive a reload. */
    private static GearSetup withVariantIds(GearSetup setup, List<String> ids) {
        if (ids == null || ids.size() != setup.variants().size()) {
            return setup;
        }
        int selected = setup.selectedIndex();
        List<SetupVariant> restored = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            restored.add(setup.variants().get(i).withId(new VariantId(UUID.fromString(ids.get(i)))));
        }
        return setup.withVariants(restored, selected);
    }

    static final class BookDto {
        int version;
        List<SectionDto> sections;
    }

    static final class SectionDto {
        String id;
        String name;
        String parent;
        List<SetupDto> setups;
    }

    /** The personal fields a book keeps on top of the shared document. */
    static final class SetupDto extends SetupDocument {
        String id;
        String owner;
        Boolean pinned;
        String hotkey;
        List<String> variantIds;
    }
}
