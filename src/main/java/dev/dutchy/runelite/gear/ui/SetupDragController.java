package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.libs.ui.image.ImageTransforms;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/** Mouse-driven drag that draws its own preview and caret. */
final class SetupDragController {

    private static final float PREVIEW_OPACITY = 0.75f;

    private static final int SPRING_DELAY_MILLIS = 550;

    private final JComponent dropArea;
    private final Function<Point, SetupGrid> gridAt;
    private final Function<Point, SectionView> sectionAt;
    private final Consumer<SectionView> onSpringOpen;
    private final SetupDropHandler handler;
    private final Timer springTimer;

    private SectionView springTarget;

    private SetupTile source;
    private Point pressPoint;
    private boolean dragging;
    private JLayeredPane dragLayer;
    private JLabel preview;
    private SetupGrid caretGrid;

    SetupDragController(JComponent dropArea,
                        Function<Point, SetupGrid> gridAt,
                        Function<Point, SectionView> sectionAt,
                        Consumer<SectionView> onSpringOpen,
                        SetupDropHandler handler) {
        this.dropArea = Objects.requireNonNull(dropArea, "dropArea");
        this.gridAt = Objects.requireNonNull(gridAt, "gridAt");
        this.sectionAt = Objects.requireNonNull(sectionAt, "sectionAt");
        this.onSpringOpen = Objects.requireNonNull(onSpringOpen, "onSpringOpen");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.springTimer = new Timer(SPRING_DELAY_MILLIS, e -> springOpen());
        this.springTimer.setRepeats(false);
    }

    void press(SetupTile tile, MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event)) {
            return;
        }
        source = tile;
        pressPoint = event.getPoint();
        dragging = false;
    }

    void drag(SetupTile tile, MouseEvent event) {
        if (source != tile || pressPoint == null) {
            return;
        }
        if (!dragging) {
            if (event.getPoint().distance(pressPoint) <= DragSource.getDragThreshold()) {
                return;
            }
            dragging = true;
            addPreview();
        }
        Point inDropArea = SwingUtilities.convertPoint(tile, event.getPoint(), dropArea);
        movePreview(tile, event);
        showCaret(inDropArea);
        updateSpring(inDropArea);
    }

    private void updateSpring(Point inDropArea) {
        SectionView hovered = sectionAt.apply(inDropArea);
        if (hovered == null || !hovered.isCollapsed()) {
            springTarget = null;
            springTimer.stop();
            return;
        }
        if (springTarget != hovered) {
            springTarget = hovered;
            springTimer.restart();
        }
    }

    private void springOpen() {
        if (dragging && springTarget != null && springTarget.isCollapsed()) {
            onSpringOpen.accept(springTarget);
        }
        springTarget = null;
    }

    void release(SetupTile tile, MouseEvent event) {
        if (source != tile) {
            return;
        }
        if (dragging) {
            Point inDropArea = SwingUtilities.convertPoint(tile, event.getPoint(), dropArea);
            SetupGrid target = gridAt.apply(inDropArea);
            if (target != null) {
                Point inGrid = SwingUtilities.convertPoint(dropArea, inDropArea, target);
                handler.onSetupDropped(tile.setup().id(), target.sectionId(), target.insertionIndexAt(inGrid));
            }
        }
        cancel();
    }

    void cancel() {
        springTimer.stop();
        springTarget = null;
        clearCaret();
        removePreview();
        source = null;
        pressPoint = null;
        dragging = false;
    }

    private void showCaret(Point inDropArea) {
        SetupGrid target = gridAt.apply(inDropArea);
        if (target == null) {
            clearCaret();
            return;
        }
        if (caretGrid != null && caretGrid != target) {
            caretGrid.clearCaret();
        }
        caretGrid = target;
        target.showCaret(target.insertionIndexAt(SwingUtilities.convertPoint(dropArea, inDropArea, target)));
    }

    private void clearCaret() {
        if (caretGrid != null) {
            caretGrid.clearCaret();
            caretGrid = null;
        }
    }

    private void addPreview() {
        JRootPane rootPane = SwingUtilities.getRootPane(dropArea);
        if (rootPane == null || source.getWidth() <= 0) {
            return;
        }
        BufferedImage image = ImageTransforms.opacity(PREVIEW_OPACITY).apply(source.snapshot());
        dragLayer = rootPane.getLayeredPane();
        preview = new JLabel(new ImageIcon(image));
        preview.setSize(source.getSize());
        dragLayer.add(preview, JLayeredPane.DRAG_LAYER);
    }

    private void movePreview(SetupTile tile, MouseEvent event) {
        if (preview == null) {
            return;
        }
        Point inLayer = SwingUtilities.convertPoint(tile, event.getPoint(), dragLayer);
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
