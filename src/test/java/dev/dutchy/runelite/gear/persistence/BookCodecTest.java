package dev.dutchy.runelite.gear.persistence;

import com.google.gson.Gson;
import dev.dutchy.runelite.gear.ColourLabel;
import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Hotkey;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.Prayer;
import dev.dutchy.runelite.gear.Requirements;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.Spellbook;
import dev.dutchy.runelite.gear.content.BankContent;
import dev.dutchy.runelite.gear.content.CellRef;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.Divider;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.ItemMatch;
import dev.dutchy.runelite.gear.content.LayoutCell;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.content.TextAlign;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookCodecTest {

    private final BookCodec codec = new BookCodec(new Gson());

    private static List<GearSection> sampleBook() {
        SetupItem whip = SetupItem.of(4151, 1).withMatch(ItemMatch.EXACT).withNoted(true);
        SetupItem gloves = SetupItem.of(7462).withAlternative(ItemId.of(11126));
        GearSetup vorkath = GearSetup.of("Vorkath", ItemId.of(4151))
                .withContent(GearContent.empty()
                        .withEquipped(EquipmentSlot.WEAPON, whip)
                        .withEquipped(EquipmentSlot.HANDS, gloves)
                        .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(995, 10_000)).withSlot(27, SetupItem.of(385))))
                .withOwner(Owner.account("rsprofile.abc"));
        GearSetup skilling = GearSetup.named("Skilling").withContent(BankContent.empty().withLeft(ItemGrid.EMPTY.withSlot(3, SetupItem.of(1511, 28))));
        return List.of(
                new GearSection(SectionId.random(), "Bossing", List.of(vorkath)),
                new GearSection(SectionId.random(), "Other", List.of(skilling)));
    }

    @Test
    void roundTripsEverySectionSetupAndItemDetail() {
        List<GearSection> original = sampleBook();
        assertEquals(original, codec.decode(codec.encode(original)));
    }

    @Test
    void roundTripsSetupMetadata() {
        GearSetup decorated = GearSetup.named("Vorkath").withMeta(meta -> meta
                .withPinned(true)
                .withLabel(ColourLabel.BLUE)
                .withTags(Set.of("melee", "bossing"))
                .withNotes("Bring two extra brews")
                .withRequirements(Requirements.ofSpellbook(Spellbook.LUNAR))
                .withHotkey(new Hotkey(KeyEvent.VK_F5, 0)));
        List<GearSection> original = List.of(new GearSection(SectionId.random(), "S", List.of(decorated)));
        assertEquals(original, codec.decode(codec.encode(original)));
    }

    @Test
    void defaultsAreLeftOutOfTheDocument() {
        String json = codec.encode(List.of(new GearSection(SectionId.random(), "S",
                List.of(GearSetup.named("Plain").withContent(GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385))))))));
        assertFalse(json.contains("\"match\""), "the default match rule is implied");
        assertFalse(json.contains("\"alts\""));
        assertFalse(json.contains("\"owner\""), "shared setups carry no owner");
        assertTrue(json.contains("\"version\":1"));
        assertTrue(json.contains("\"type\":\"GEAR\""), "a setup is one flat document");
        assertFalse(json.contains("\"content\""));
    }

    @Test
    void anUnknownVersionIsRefused() {
        String json = codec.encode(sampleBook()).replace("\"version\":1", "\"version\":99");
        assertThrows(BookFormatException.class, () -> codec.decode(json));
    }

    @Test
    void garbageIsRefusedNotSwallowed() {
        assertThrows(BookFormatException.class, () -> codec.decode("not json"));
        assertThrows(BookFormatException.class, () -> codec.decode("{\"version\":1}"));
        assertThrows(BookFormatException.class, () -> codec.decode(
                "{\"version\":1,\"sections\":[{\"id\":\"nope\",\"name\":\"S\",\"setups\":[]}]}"));
    }

    @Test
    void invalidValuesInsideAreRefused() {
        String json = codec.encode(sampleBook()).replace("\"qty\":10000", "\"qty\":0");
        assertThrows(BookFormatException.class, () -> codec.decode(json));
    }
    @Test
    void aParentLinkRoundTripsAndOldDocumentsStayTopLevel() {
        GearSection raids = GearSection.named("Raids");
        GearSection toa = new GearSection(SectionId.random(), "ToA", List.of(GearSetup.named("Expert")), raids.id());
        List<GearSection> decoded = codec.decode(codec.encode(List.of(raids, toa)));
        assertEquals(List.of(raids, toa), decoded);
        assertTrue(decoded.get(1).isChildOf(raids.id()));
        assertFalse(codec.encode(List.of(raids)).contains("parent"), "top-level sections carry no parent field");

        String orphaned = codec.encode(List.of(toa));
        assertFalse(codec.decode(orphaned).get(0).isChild(), "a child without its parent is lifted");
    }

    @Test
    void variantsRoundTripWithTheChosenOneAndAreLeftOutWhenThereIsOnlyOne() {
        GearContent melee = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        GearContent mage = melee.withEquipped(EquipmentSlot.WEAPON, SetupItem.of(11791, 1));
        GearSetup setup = GearSetup.named("Vorkath").withContent(melee)
                .withAddedVariant(new SetupVariant("Mage", mage))
                .withSelectedVariant(1);
        GearSection section = new GearSection(SectionId.random(), "S", List.of(setup));

        String json = codec.encode(List.of(section));
        GearSetup decoded = codec.decode(json).get(0).setups().get(0);
        assertEquals(setup, decoded);
        assertEquals(mage, decoded.content(), "the chosen variant is the one read back as the contents");
        assertTrue(json.contains("\"variants\"") && json.contains("\"variant\":1"));
        String plain = codec.encode(List.of(new GearSection(SectionId.random(), "S", List.of(GearSetup.named("Plain")))));
        assertFalse(plain.contains("\"variants\"") || plain.contains("\"variant\":"), "one variant needs neither a list nor a choice");
        assertTrue(plain.contains("\"variantIds\""), "but its id is kept, so a revision still points at it after a reload");
    }

    @Test
    void aDocumentWithoutVariantsReadsAsOneDefaultVariant() {
        GearSetup decoded = codec.decode(codec.encode(sampleBook())).get(0).setups().get(0);
        assertEquals(1, decoded.variants().size());
        assertEquals(SetupVariant.DEFAULT_NAME, decoded.variant().name());
    }

    @Test
    void aCustomLayoutRoundTripsWithItsCellsAndNames() {
        CustomContent custom = CustomContent.empty(2)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151))).withName("Vorkath"))
                .withCell(CellRef.of(1, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(3, SetupItem.of(385, 4))));
        GearSection section = new GearSection(SectionId.random(), "S", List.of(GearSetup.named("Trip").withContent(custom)));
        List<GearSection> decoded = codec.decode(codec.encode(List.of(section)));
        assertEquals(custom, decoded.get(0).setups().get(0).content());
        assertTrue(codec.encode(List.of(section)).contains("\"rows\":2"));
    }

    @Test
    void quickPrayersRoundTripAndAreLeftOutWhenNoneAreSet() {
        GearSetup zulrah = GearSetup.named("Zulrah").withMeta(meta -> meta.withRequirements(
                new Requirements(Spellbook.LUNAR, Set.of(Prayer.AUGURY, Prayer.RIGOUR))));
        List<GearSection> decoded = codec.decode(codec.encode(List.of(new GearSection(SectionId.random(), "S", List.of(zulrah)))));
        assertEquals(zulrah.meta().requirements(), decoded.get(0).setups().get(0).meta().requirements());
        assertFalse(codec.encode(List.of(new GearSection(SectionId.random(), "S", List.of(GearSetup.named("Plain"))))).contains("quickPrayers"));
    }

    @Test
    void dividersRoundTripOnEveryGridAndAreLeftOutWhenAbsent() {
        GearContent gear = GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385)).withDivider(0, "Food")
                .withDivider(new Divider(2, 0, 1, "Runes", TextAlign.CENTRE, false)).withDivider(new Divider(2, 2, 3, "Pots", TextAlign.RIGHT)));
        BankContent bank = BankContent.empty().withLeft(ItemGrid.EMPTY.withDivider(2, "Runes")).withRight(ItemGrid.EMPTY.withDivider(0, "Pots"));
        CustomContent custom = CustomContent.empty(1).withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY.withDivider(1, "Food")));
        List<GearSection> decoded = codec.decode(codec.encode(List.of(new GearSection(SectionId.random(), "S", List.of(
                GearSetup.named("Gear").withContent(gear), GearSetup.named("Bank").withContent(bank), GearSetup.named("Custom").withContent(custom))))));
        assertEquals(gear, decoded.get(0).setups().get(0).content());
        assertEquals(bank, decoded.get(0).setups().get(1).content());
        assertEquals(custom, decoded.get(0).setups().get(2).content());
        assertFalse(codec.encode(List.of(new GearSection(SectionId.random(), "S", List.of(GearSetup.named("Plain"))))).contains("Dividers"));
        String json = codec.encode(List.of(new GearSection(SectionId.random(), "S", List.of(GearSetup.named("Gear").withContent(gear)))));
        assertTrue(json.contains("{\"row\":0,\"label\":\"Food\"}"), "a whole-row left divider is written as just its row and text: " + json);
        assertTrue(json.contains("\"from\":2,\"to\":3,\"label\":\"Pots\",\"align\":\"RIGHT\"}"), "an underlined divider writes no line flag");
        assertTrue(json.contains("\"label\":\"Runes\",\"align\":\"CENTRE\",\"line\":false"));
    }

    @Test
    void booksThatWroteDividersAsARowToLabelMapStillRead() {
        String json = codec.encode(List.of(new GearSection(SectionId.random(), "S", List.of(GearSetup.named("Old")
                .withContent(BankContent.empty().withLeft(ItemGrid.EMPTY.withDivider(1, "Food")))))))
                .replace("[{\"row\":1,\"label\":\"Food\"}]", "{\"1\":\"Food\"}");
        assertTrue(json.contains("\"leftDividers\":{\"1\":\"Food\"}"), json);
        ItemGrid left = ((BankContent) codec.decode(json).get(0).setups().get(0).content()).left();
        assertEquals(List.of(Divider.acrossRow(1, "Food")), left.dividers());
    }

}
