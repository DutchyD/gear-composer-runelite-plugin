package dev.dutchy.runelite.gear.share;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dev.dutchy.runelite.gear.GearSetup;

import dev.dutchy.runelite.gear.persistence.BookFormatException;
import dev.dutchy.runelite.gear.persistence.ContentCodec;
import dev.dutchy.runelite.gear.persistence.SetupDocument;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * One setup as a short pasteable string: a version tag, then gzip-compressed JSON in URL-safe
 * base64. Pins, hotkeys and ownership are personal and are not shared.
 */
@Singleton
public final class ShareCodec {

    public static final String PREFIX = "gc1:";

    private final Gson gson;

    @Inject
    public ShareCodec(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    public String encode(GearSetup setup) {
        Objects.requireNonNull(setup, "setup");
        ShareDto dto = SetupDocument.write(setup, new ShareDto());
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(gzip(gson.toJson(dto)));
    }

    /** Whether the text looks like one of ours, before trying to decode it. */
    public static boolean looksLikeCode(String text) {
        return text != null && text.strip().startsWith(PREFIX);
    }

    /** @throws BookFormatException when the text is not a share code this version understands */
    public GearSetup decode(String code) {
        String trimmed = Objects.requireNonNull(code, "code").strip();
        if (!trimmed.startsWith(PREFIX)) {
            throw new BookFormatException("Not a Gear Composer share code");
        }
        ShareDto dto;
        try {
            byte[] packed = Base64.getUrlDecoder().decode(trimmed.substring(PREFIX.length()));
            dto = gson.fromJson(gunzip(packed), ShareDto.class);
        } catch (IllegalArgumentException | IOException | JsonParseException e) {
            throw new BookFormatException("The share code is damaged", e);
        }
        if (dto == null || !dto.hasName()) {
            throw new BookFormatException("The share code is incomplete");
        }
        if (dto.content != null) {
            dto.adoptContent(dto.content);
        }
        try {
            return dto.toSetup();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BookFormatException("The share code holds invalid values: " + e.getMessage(), e);
        }
    }

    private static byte[] gzip(String text) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("In-memory compression failed", e);
        }
        return bytes.toByteArray();
    }

    private static String gunzip(byte[] packed) throws IOException {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(packed))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Early codes nested the items under "content"; they still decode. */
    static final class ShareDto extends SetupDocument {
        ContentCodec.ContentDto content;
    }
}
