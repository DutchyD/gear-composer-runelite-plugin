package dev.dutchy.runelite.gear.share;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.ledger.EquipmentStats;
import dev.dutchy.runelite.gear.ledger.ItemFacts;
import dev.dutchy.runelite.gear.ledger.ItemFactsSource;
import dev.dutchy.runelite.gear.ledger.Ledger;
import dev.dutchy.runelite.libs.ui.button.DefaultItemLoader;
import dev.dutchy.runelite.libs.ui.button.ItemLoader;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupShareTest {

    private static final GearSetup VORKATH = GearSetup.named("Vorkath").withContent(GearContent.empty()
            .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151))
            .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 4)).withDivider(1, "Food")));
    private static final ItemFactsSource FACTS = ItemFactsSource.fixed(Map.of(
            ItemId.of(4151), new ItemFacts(2_500_000, 0.4, new EquipmentStats(0, 82, 0, 0, 0, 0, 0, 0, 0, 0, 82, 0, 0, 0, 4)),
            ItemId.of(385), ItemFacts.unworn(900, 0.5)));

    private ItemLoader loader;
    private InMemoryClipboard clipboard;
    private List<String> saved;
    private List<BufferedImage> copied;


    @BeforeEach
    void setUp() {
        loader = new DefaultItemLoader(
                StaticItemResolver.of(ResolvedItem.of(4151, "Abyssal whip"), ResolvedItem.of(385, "Shark"), ResolvedItem.of(11791, "Staff of the dead")),
                request -> new LoadedItemImage(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB)));
        clipboard = new InMemoryClipboard();
        saved = new ArrayList<>();
        copied = new ArrayList<>();
    }

    private ShareService service() {
        ImageSink sink = new ImageSink() {
            @Override
            public String save(BufferedImage image, String fileName) {
                saved.add(fileName);
                return "the test folder";
            }

            @Override
            public void copy(BufferedImage image) {
                copied.add(image);
            }
        };
        return new ShareService(loader, FACTS, sink, clipboard);
    }

    @Test
    void markdownListsEverySlotDividerAndTheLedger() {
        String text = SetupText.markdown(VORKATH, id -> id.equals(ItemId.of(4151)) ? Optional.of("Abyssal whip") : Optional.empty(),
                Ledger.of(VORKATH.content(), FACTS));
        assertTrue(text.startsWith("**Vorkath** · Gear Layout"));
        assertTrue(text.contains("- Weapon: Abyssal whip"));
        assertTrue(text.contains("- Item 385 ×4"), "unresolved names fall back to the id");
        assertTrue(text.contains("— Food —"));
        assertTrue(text.contains("Value 2.5M"));
        assertTrue(text.contains("Weight 2.4 kg"));
    }

    @Test
    void theImageHasRoomForEveryGridDividerAndLedgerLine() throws Exception {
        LoadedSetup loaded = LoadedSetup.load(VORKATH, loader).get(5, TimeUnit.SECONDS);
        assertEquals(Optional.of("Abyssal whip"), loaded.name(ItemId.of(4151)));
        assertTrue(loaded.sprite(ItemId.of(385)).isPresent());

        BufferedImage plain = SetupImageRenderer.render(VORKATH, loaded, null);
        BufferedImage withLedger = SetupImageRenderer.render(VORKATH, loaded, Ledger.of(VORKATH.content(), FACTS));

        int columns = EquipmentSlot.COLUMNS + ItemGrid.COLUMNS;
        assertEquals(2 * SetupImageRenderer.MARGIN + columns * SetupImageRenderer.CELL + SetupImageRenderer.GAP, plain.getWidth());
        assertTrue(withLedger.getHeight() > plain.getHeight(), "the ledger adds lines");
        assertTrue(plain.getHeight() > ItemGrid.ROWS * SetupImageRenderer.CELL + SetupImageRenderer.HEADER, "the divider adds a header");
    }

    @Test
    void theServiceSavesCopiesAndCopiesText() throws Exception {
        ShareService service = service();
        assertEquals("Saved Vorkath to the test folder", service.saveImage(VORKATH, true).get(5, TimeUnit.SECONDS));
        assertEquals(List.of("gear-composer-vorkath"), saved);
        assertEquals("Copied Vorkath as an image", service.copyImage(VORKATH, false).get(5, TimeUnit.SECONDS));
        assertEquals(1, copied.size());
        assertEquals("Copied Vorkath as text", service.copyText(VORKATH, true).get(5, TimeUnit.SECONDS));
        assertTrue(clipboard.paste().orElseThrow().contains("- Weapon: Abyssal whip"));
    }
    @Test
    void customLayoutsListEachCellUnderItsPositionAndName() throws Exception {
        GearSetup trip = GearSetup.named("Trip").withContent(CustomContent.empty(1)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151))).withName("Melee"))
                .withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 4)))));
        String text = SetupText.markdown(trip, id -> Optional.of("Item"), null);
        assertTrue(text.contains("Row 1 left · Melee\n- Weapon: Item"));
        assertTrue(text.contains("Row 1 right · Inventory\n- Item ×4"));

        LoadedSetup loaded = LoadedSetup.load(trip, loader).get(5, TimeUnit.SECONDS);
        BufferedImage image = SetupImageRenderer.render(trip, loaded, null);
        assertEquals(2 * SetupImageRenderer.MARGIN + (EquipmentSlot.COLUMNS + ItemGrid.COLUMNS) * SetupImageRenderer.CELL + SetupImageRenderer.GAP, image.getWidth(), "each side is as wide as its widest block");
    }

    @Test
    void dividersReadAsHeadingsInTheTextAndAddRowsToTheImage() throws Exception {
        GearSetup divided = GearSetup.named("Trip").withContent(GearContent.empty()
                .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 4)).withDivider(0, "Food")));
        String text = SetupText.markdown(divided, id -> Optional.of("Shark"), null);
        assertTrue(text.contains("Inventory\n— Food —\n- Shark ×4"), text);

        LoadedSetup loaded = LoadedSetup.load(divided, loader).get(5, TimeUnit.SECONDS);
        BufferedImage plain = SetupImageRenderer.render(GearSetup.named("Trip").withContent(GearContent.empty()), loaded, null);
        BufferedImage withDivider = SetupImageRenderer.render(divided, loaded, null);
        assertEquals(plain.getHeight() + SetupImageRenderer.HEADER, withDivider.getHeight(), "a divider adds one header strip");
    }

}
