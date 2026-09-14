package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.gear.content.DividerChange;
import dev.dutchy.runelite.libs.ui.button.DefaultItemLoader;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentViewTest {

    private static final SlotView.Listener QUIET = new SlotView.Listener() {
        @Override
        public void clicked(SlotView view, MouseEvent event) {
        }

        @Override
        public void pressed(SlotView view, MouseEvent event) {
        }

        @Override
        public void dragged(SlotView view, MouseEvent event) {
        }

        @Override
        public void released(SlotView view, MouseEvent event) {
        }

        @Override
        public void edit(SlotRef ref) {
        }

        @Override
        public void clear(SlotRef ref) {
        }

        @Override
        public void fillRemaining(SlotRef from) {
        }

        @Override
        public void fillRow(SlotRef from) {
        }

        @Override
        public void editDivider(SlotRef rowSlot) {
        }

        @Override
        public void removeDivider(SlotRef rowSlot) {
        }

        @Override
        public void changeDivider(SlotRef slot, DividerChange change) {
        }
    };

    @Test
    void emptySlotsShowTheGamesGlyphForWhatGoesThereAndFilledOnesDoNot() throws Exception {
        ItemIconFactory icons = new ItemIconFactory(new DefaultItemLoader(
                StaticItemResolver.of(ResolvedItem.of(4151, "Abyssal whip")),
                request -> new LoadedItemImage(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB))));
        List<EquipmentSlot> asked = new ArrayList<>();
        EquipmentSlotArtwork artwork = (slot, onLoaded) -> {
            asked.add(slot);
            onLoaded.accept(new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB));
        };
        AtomicReference<EquipmentView> built = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> built.set(new EquipmentView(Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151)), icons, artwork, QUIET,
                ref -> ref)));
        EquipmentView view = built.get();

        EnumSet<EquipmentSlot> expected = EnumSet.allOf(EquipmentSlot.class);
        expected.remove(EquipmentSlot.WEAPON);
        assertEquals(expected, EnumSet.copyOf(asked), "every empty slot asks for its glyph, the filled one does not");
        assertFalse(view.slotFor(EquipmentSlot.WEAPON).orElseThrow().hasGlyph());
        assertTrue(view.slotFor(EquipmentSlot.HEAD).orElseThrow().hasGlyph());
        assertEquals(EquipmentSlot.values().length, view.slots().size());
    }
}
