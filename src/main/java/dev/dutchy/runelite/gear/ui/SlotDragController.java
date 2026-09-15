package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.DropRule;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import lombok.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Drags an item from a slot or a search result onto another slot, previewing the drop as allowed or refused. */
final class SlotDragController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SlotDragController.class);
    private static final float PREVIEW_OPACITY = 0.75f;

    /** What is being dragged: an existing slot, or a fresh item from search. */
    interface Payload {
        SetupItem item();
    }

    @Value
    static class FromSlot implements Payload {
        SlotView view;

        @Override
        public SetupItem item() {
            return view.item().orElseThrow();
        }
    }

    @Value
    static class FromSearch implements Payload {
        ResolvedItem resolved;

        @Override
        public SetupItem item() {
            return new SetupItem(resolved.id());
        }
    }

    private final JComponent dropArea;
    private final Function<Point, SlotView> slotAt;
    private final DropRule dropRule;
    private final ContentActions actions;

    private Payload payload;
    private JComponent origin;
    private Point pressPoint;
    private boolean dragging;
    private JLayeredPane dragLayer;
    private JLabel preview;
    private SlotView target;

    SlotDragController(JComponent dropArea, Function<Point, SlotView> slotAt, DropRule dropRule, ContentActions actions) {
        this.dropArea = Objects.requireNonNull(dropArea, "dropArea");
        this.slotAt = Objects.requireNonNull(slotAt, "slotAt");
        this.dropRule = Objects.requireNonNull(dropRule, "dropRule");
        this.actions = Objects.requireNonNull(actions, "actions");
    }

    boolean isDragging() {
        return dragging;
    }

    void press(Payload newPayload, JComponent from, MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event)) {
            return;
        }
        dropRule.warmUp(List.of(newPayload.item().id()));
        payload = newPayload;
        origin = from;
        pressPoint = event.getPoint();
        dragging = false;
    }

    void drag(JComponent from, MouseEvent event) {
        if (origin != from || pressPoint == null) {
            return;
        }
        if (!dragging) {
            if (event.getPoint().distance(pressPoint) <= DragSource.getDragThreshold()) {
                return;
            }
            dragging = true;
            addPreview();
        }
        movePreview(event);
        Point inDropArea = SwingUtilities.convertPoint(from, event.getPoint(), dropArea);
        highlight(slotAt.apply(inDropArea));
    }

    void release(JComponent from, MouseEvent event) {
        if (origin != from) {
            return;
        }
        try {
            if (dragging) {
                Point inDropArea = SwingUtilities.convertPoint(from, event.getPoint(), dropArea);
                SlotView over = slotAt.apply(inDropArea);
                if (over != null && dropRule.allows(payload.item(), over.ref())) {
                    drop(over.ref(), event.isControlDown());
                }
            }
        } catch (RuntimeException e) {
            log.warn("Dropping an item failed", e);
        } finally {
            cancel();
        }
    }

    /** Whether the drag would be accepted at the slot; used for tests and the highlight. */
    boolean allows(SlotRef ref) {
        return payload != null && dropRule.allows(payload.item(), ref);
    }

    private void drop(SlotRef to, boolean copy) {
        if (payload instanceof FromSlot) {
            actions.moveItem(((FromSlot) payload).view().ref(), to, copy);
        } else if (payload instanceof FromSearch) {
            actions.dropItem(((FromSearch) payload).resolved(), to);
        }
    }

    void cancel() {
        highlight(null);
        removePreview();
        payload = null;
        origin = null;
        pressPoint = null;
        dragging = false;
    }

    private void highlight(SlotView over) {
        if (target != null && target != over) {
            target.setDropState(SlotView.DropState.NONE);
        }
        target = over;
        if (over != null) {
            boolean self = payload instanceof FromSlot && ((FromSlot) payload).view() == over;
            over.setDropState(self ? SlotView.DropState.NONE
                    : allows(over.ref()) ? SlotView.DropState.ALLOWED : SlotView.DropState.REFUSED);
        }
    }

    private void addPreview() {
        JRootPane rootPane = SwingUtilities.getRootPane(dropArea);
        if (rootPane == null || origin.getWidth() <= 0 || origin.getHeight() <= 0) {
            return;
        }
        BufferedImage image = new BufferedImage(origin.getWidth(), origin.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            origin.print(g);
        } finally {
            g.dispose();
        }
        dragLayer = rootPane.getLayeredPane();
        preview = new JLabel(new ImageIcon(ImageTransforms.opacity(PREVIEW_OPACITY).apply(image)));
        preview.setSize(origin.getSize());
        dragLayer.add(preview, JLayeredPane.DRAG_LAYER);
    }

    private void movePreview(MouseEvent event) {
        if (preview == null) {
            return;
        }
        Point inLayer = SwingUtilities.convertPoint(origin, event.getPoint(), dragLayer);
        preview.setLocation(inLayer.x - pressPoint.x, inLayer.y - pressPoint.y);
        dragLayer.repaint();
    }

    private void removePreview() {
        if (preview != null && dragLayer != null) {
            dragLayer.remove(preview);
            dragLayer.repaint();
        }
        preview = null;
        dragLayer = null;
    }
}
