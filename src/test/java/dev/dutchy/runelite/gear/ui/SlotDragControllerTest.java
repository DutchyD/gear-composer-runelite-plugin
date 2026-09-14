package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.DividerChange;
import dev.dutchy.runelite.gear.content.DropRule;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.EquipmentSlots;
import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.gear.content.SyncScope;
import dev.dutchy.runelite.libs.ui.button.DefaultItemLoader;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotDragControllerTest {

    /** Records what the page asked for and can be told to fail. */
    private static final class RecordingActions implements ContentActions {
        final List<String> calls = new ArrayList<>();
        boolean failMoves;

        @Override
        public void moveItem(SlotRef from, SlotRef to, boolean copy) {
            calls.add("move " + from.describe() + " -> " + to.describe());
            if (failMoves) {
                throw new IllegalStateException("boom");
            }
        }

        @Override
        public void editSlot(SlotRef ref) {
        }

        @Override
        public void clearSlot(SlotRef ref) {
        }

        @Override
        public void clearSlots(List<SlotRef> refs) {
        }

        @Override
        public void dropItem(ResolvedItem item, SlotRef to) {
            calls.add("drop " + item.name() + " -> " + to.describe());
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

        @Override
        public void copySlots(List<SlotRef> refs) {
        }

        @Override
        public void pasteAt(SlotRef anchor) {
        }

        @Override
        public boolean canPaste() {
            return false;
        }

        @Override
        public void sync(SyncScope scope) {
        }

        @Override
        public void showHistory() {
        }

        @Override
        public void back() {
        }

        @Override
        public void selectVariant(int index) {
        }

        @Override
        public void editVariant(int index) {
        }

        @Override
        public void addVariant(NewVariant start) {
        }

        @Override
        public void renameVariant(int index) {
        }

        @Override
        public void duplicateVariant(int index) {
        }

        @Override
        public void deleteVariant(int index) {
        }

        @Override
        public void moveVariant(int index, int to) {
        }

        @Override
        public void compareVariant(int index) {
        }

        @Override
        public void compare() {
        }

        @Override
        public void share() {
        }
    }

    private static final class WarmingSlots implements EquipmentSlots {
        final List<ItemId> warmed = new ArrayList<>();

        @Override
        public Optional<EquipmentSlot> slotOf(ItemId item) {
            return Optional.empty();
        }

        @Override
        public void warmUp(Collection<ItemId> items) {
            warmed.addAll(items);
        }
    }

    private final RecordingActions actions = new RecordingActions();
    private final WarmingSlots slots = new WarmingSlots();
    private final ItemIconFactory icons = new ItemIconFactory(new DefaultItemLoader(
            StaticItemResolver.of(ResolvedItem.of(4151, "Abyssal whip")),
            request -> new LoadedItemImage(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB))));

    private static MouseEvent mouse(Component on, int id, int x, int y) {
        return new MouseEvent(on, id, 0, InputEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1);
    }

    @Test
    void aFailedDropStillEndsTheDragSoNoPreviewLingers() throws Exception {
        actions.failMoves = true;
        onEdt(() -> {
            JPanel area = new JPanel(null);
            SlotView.Listener quiet = quietListener();
            SlotView source = new SlotView(SlotRef.of(GridKind.INVENTORY, 0), SetupItem.of(4151), icons, quiet);
            SlotView target = new SlotView(SlotRef.of(EquipmentSlot.WEAPON), icons, quiet);
            area.add(source);
            area.add(target);
            source.setBounds(0, 0, 44, 44);
            target.setBounds(100, 0, 44, 44);
            SlotDragController controller = new SlotDragController(area, point -> target.getBounds().contains(point) ? target : null,
                    new DropRule(slots), actions);

            controller.press(new SlotDragController.FromSlot(source), source, mouse(source, MouseEvent.MOUSE_PRESSED, 5, 5));
            controller.drag(source, mouse(source, MouseEvent.MOUSE_DRAGGED, 120, 10));
            assertTrue(controller.isDragging());
            assertEquals(SlotView.DropState.ALLOWED, target.dropState());

            controller.release(source, mouse(source, MouseEvent.MOUSE_RELEASED, 120, 10));

            assertFalse(controller.isDragging(), "the drag is over even though the move failed");
            assertEquals(SlotView.DropState.NONE, target.dropState(), "the highlight is gone");
            assertEquals(List.of("move Inventory slot 1 -> Weapon"), actions.calls);
            assertEquals(List.of(ItemId.of(4151)), slots.warmed, "the item was looked up ahead of the drop");
        });
    }

    private static SlotView.Listener quietListener() {
        return new SlotView.Listener() {
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
    }

    private static void onEdt(Runnable action) throws Exception {
        AtomicReference<RuntimeException> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                action.run();
            } catch (RuntimeException e) {
                failure.set(e);
            }
        });
        if (failure.get() != null) {
            throw failure.get();
        }
    }
}
