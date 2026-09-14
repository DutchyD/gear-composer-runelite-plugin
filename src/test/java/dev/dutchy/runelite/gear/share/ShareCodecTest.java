package dev.dutchy.runelite.gear.share;

import com.google.gson.Gson;
import dev.dutchy.runelite.gear.ColourLabel;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Hotkey;
import dev.dutchy.runelite.gear.Requirements;
import dev.dutchy.runelite.gear.Spellbook;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.persistence.BookFormatException;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShareCodecTest {

    private final ShareCodec codec = new ShareCodec(new Gson());

    private static GearSetup sample() {
        return GearSetup.of("Vorkath", ItemId.of(4151))
                .withContent(GearContent.empty()
                        .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151, 1).withAlternative(ItemId.of(4153)))
                        .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(995, 10_000)).withSlot(1, SetupItem.of(526).withNoted(true))))
                .withMeta(meta -> meta
                        .withPinned(true)
                        .withHotkey(new Hotkey(KeyEvent.VK_F5, 0))
                        .withLabel(ColourLabel.RED)
                        .withTags(Set.of("melee"))
                        .withNotes("Bring brews")
                        .withRequirements(Requirements.ofSpellbook(Spellbook.LUNAR)));
    }

    @Test
    void roundTripsItemsAndSharedMetadataUnderANewId() {
        GearSetup original = sample();
        String code = codec.encode(original);
        assertTrue(code.startsWith(ShareCodec.PREFIX));
        assertTrue(ShareCodec.looksLikeCode("  " + code + "\n"));

        GearSetup decoded = codec.decode(code);
        assertNotEquals(original.id(), decoded.id());
        assertEquals(original.name(), decoded.name());
        assertEquals(original.icon(), decoded.icon());
        assertEquals(original.content(), decoded.content());
        assertEquals(ColourLabel.RED, decoded.meta().label());
        assertEquals(Set.of("melee"), decoded.meta().tags());
        assertEquals("Bring brews", decoded.meta().notes());
        assertEquals(Spellbook.LUNAR, decoded.meta().requirements().spellbook());
        assertFalse(decoded.isPinned(), "pins are personal");
        assertFalse(decoded.meta().hasHotkey(), "hotkeys are personal");
        assertTrue(decoded.owner().isShared());
    }

    @Test
    void earlyCodesWithTheItemsNestedUnderContentStillDecode() throws IOException {
        String json = "{\"name\":\"Old\",\"icon\":4151,\"content\":{\"type\":\"GEAR\",\"equipment\":{\"WEAPON\":{\"id\":4151}},\"inventory\":{\"0\":{\"id\":385,\"qty\":4}}},\"tags\":[\"old\"]}";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
            out.write(json.getBytes(StandardCharsets.UTF_8));
        }
        String code = ShareCodec.PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray());

        GearSetup decoded = codec.decode(code);
        assertEquals("Old", decoded.name());
        assertEquals(SetupItem.of(4151), ((GearContent) decoded.content()).equipped(EquipmentSlot.WEAPON).orElseThrow());
        assertEquals(SetupItem.of(385, 4), ((GearContent) decoded.content()).inventory().slot(0).orElseThrow());
        assertEquals(Set.of("old"), decoded.meta().tags());
    }

    @Test
    void codesAreShort() {
        assertTrue(codec.encode(sample()).length() < 400);
    }

    @Test
    void garbageIsRefused() {
        assertThrows(BookFormatException.class, () -> codec.decode("hello"));
        assertThrows(BookFormatException.class, () -> codec.decode(ShareCodec.PREFIX + "not-base64!!"));
        assertThrows(BookFormatException.class, () -> codec.decode(ShareCodec.PREFIX + Base64.getUrlEncoder().encodeToString("plain".getBytes())));
        assertFalse(ShareCodec.looksLikeCode(null));
    }
}
