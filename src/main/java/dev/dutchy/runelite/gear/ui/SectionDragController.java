package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.libs.ui.image.ImageTransforms;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;

/** Mouse-driven reordering of whole sections, with its own preview and caret. */
final class SectionDragController {

    private static final float PREVIEW_OPACITY = 0.7f;

    private final SectionListPanel list;
    private final SectionDropHandler handler;

    private SectionView source;
    private Point pressPoint;
    private Point grabOffset;
    private boolean dragging;
    private boolean dragged;
    private JLayeredPane dragLayer;
    private JLabel preview;

    SectionDragController(SectionListPanel list, SectionDropHandler handler) {
        this.list = Objects.requireNonNull(list, "list");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    /** Where inside the dragged section the pointer grabbed it, which anchors the preview. */
    Point grabOffset() {
        return grabOffset;
    }

    /** True when the gesture since the last press turned into a drag, so a click should be ignored. */
    boolean draggedSincePress() {
        return dragged;
    }

    void press(SectionView view, MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event)) {
            return;
        }
        source = view;
        pressPoint = pointInList(event);
        grabOffset = SwingUtilities.convertPoint((Component) event.getSource(), event.getPoint(), view);
        dragging = false;
        dragged = false;
    }

    void drag(SectionView view, MouseEvent event) {
        if (source != view || pressPoint == null) {
            return;
        }
        Point inList = pointInList(event);
        if (!dragging) {
            if (Math.abs(inList.y - pressPoint.y) <= DragSource.getDragThreshold()) {
                return;
            }
            dragging = true;
            dragged = true;
            addPreview();
        }
        movePreview(inList);
        list.showCaret(DropIndex.sectionInsertionIndex(list.sectionBounds(), inList.y));
    }

    void release(SectionView view, MouseEvent event) {
        if (source != view) {
            return;
        }
        if (dragging) {
            int index = DropIndex.sectionInsertionIndex(list.sectionBounds(), pointInList(event).y);
            handler.onSectionDropped(view.sectionId(), index);
        }
        stop();
    }

    void cancel() {
        stop();
        dragged = false;
    }

    private void stop() {
        list.clearCaret();
        removePreview();
        source = null;
        pressPoint = null;
        grabOffset = null;
        dragging = false;
    }

    private Point pointInList(MouseEvent event) {
        return SwingUtilities.convertPoint((Component) event.getSource(), event.getPoint(), list);
    }

    private void addPreview() {
        JRootPane rootPane = SwingUtilities.getRootPane(list);
        if (rootPane == null || source.getWidth() <= 0) {
            return;
        }
        BufferedImage image = ImageTransforms.opacity(PREVIEW_OPACITY).apply(snapshot(source));
        dragLayer = rootPane.getLayeredPane();
        preview = new JLabel(new ImageIcon(image));
        preview.setSize(source.getSize());
        dragLayer.add(preview, JLayeredPane.DRAG_LAYER);
    }

    private void movePreview(Point inList) {
        if (preview == null) {
            return;
        }
        Point inLayer = SwingUtilities.convertPoint(list, inList, dragLayer);
        preview.setLocation(inLayer.x - grabOffset.x, inLayer.y - grabOffset.y);
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

    private static BufferedImage snapshot(SectionView view) {
        BufferedImage image = new BufferedImage(Math.max(1, view.getWidth()), Math.max(1, view.getHeight()),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            view.print(g);
        } finally {
            g.dispose();
        }
        return image;
    }
}
