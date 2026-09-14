package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.guide.HelpTopic;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.Optional;

/**
 * Dims the visible part of the sidebar, cuts a bright hole around one element, and parks the callout
 * beside it. Follows scrolling and resizing, swallows clicks underneath, and gives up when the sidebar
 * is hidden so it can never be left covering something else.
 */
final class Spotlight extends JComponent {

    private static final Color DIM = new Color(0, 0, 0, 150);
    private static final Color RING = new Color(255, 152, 31);
    private static final int PADDING = 4;
    private static final int GAP = 8;
    /** An element taller than this share of the view is the page itself: no hole, the callout sits centred. */
    private static final double PAGE_SHARE = 0.6;

    private final JComponent panel;
    private final Callout callout;
    private final JLayeredPane layer;
    private final Runnable onLost;
    private final HierarchyListener showing = this::onHierarchyChanged;
    private final ComponentAdapter resized = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            refit();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            onLost.run();
        }
    };
    private final ChangeListener scrolled = e -> refit();
    private JViewport viewport;
    private JComponent element;
    private Rectangle hole;

    private Spotlight(JComponent panel, JLayeredPane layer, Callout callout, Runnable onLost) {
        this.panel = panel;
        this.layer = layer;
        this.callout = callout;
        this.onLost = onLost;
        setLayout(null);
        setOpaque(false);
        setFocusable(true);
        add(callout);
        MouseAdapter swallow = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        };
        addMouseListener(swallow);
        addMouseMotionListener(swallow);
        addMouseWheelListener(swallow);
    }

    /** Empty when the panel is not in a window, as in tests; the guide still steps, just without a picture. */
    static Optional<Spotlight> over(JComponent panel, Callout callout, Runnable onLost) {
        JRootPane root = SwingUtilities.getRootPane(Objects.requireNonNull(panel, "panel"));
        if (root == null || root.getLayeredPane() == null) {
            return Optional.empty();
        }
        Spotlight spotlight = new Spotlight(panel, root.getLayeredPane(), callout, Objects.requireNonNull(onLost, "onLost"));
        root.getLayeredPane().add(spotlight, JLayeredPane.POPUP_LAYER);
        spotlight.watch();
        spotlight.fitPanel();
        spotlight.requestFocusInWindow();
        return Optional.of(spotlight);
    }

    void bindKeys(Runnable onBack, Runnable onNext, Runnable onEnd) {
        bind(KeyEvent.VK_ESCAPE, "end", onEnd);
        bind(KeyEvent.VK_LEFT, "back", onBack);
        bind(KeyEvent.VK_RIGHT, "next", onNext);
        bind(KeyEvent.VK_ENTER, "enter", onNext);
        bind(KeyEvent.VK_SPACE, "space", onNext);
    }

    /**
     * Frames the element, scrolled into view first, or centres the callout when there is nothing to
     * frame, which is what a null {@code target} asks for.
     */
    void focusOn(JComponent target, HelpTopic topic, int index, int total) {
        callout.show(topic, index, total);
        element = target;
        if (element != null) {
            element.scrollRectToVisible(new Rectangle(0, 0, element.getWidth(), element.getHeight()));
        }
        refit();
    }

    void dismiss() {
        unwatch();
        layer.remove(this);
        layer.repaint();
    }

    Optional<Rectangle> hole() {
        return Optional.ofNullable(hole);
    }

    Rectangle calloutBounds() {
        return callout.getBounds();
    }

    private void watch() {
        panel.addHierarchyListener(showing);
        panel.addComponentListener(resized);
        viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, panel);
        if (viewport != null) {
            viewport.addChangeListener(scrolled);
        }
    }

    private void unwatch() {
        panel.removeHierarchyListener(showing);
        panel.removeComponentListener(resized);
        if (viewport != null) {
            viewport.removeChangeListener(scrolled);
        }
    }

    private void onHierarchyChanged(HierarchyEvent event) {
        if (isOutOfView(panel)) {
            onLost.run();
        } else {
            refit();
        }
    }

    /** Hidden itself or under something hidden; unlike isShowing this also answers before the window is up. */
    private static boolean isOutOfView(Component component) {
        if (component.getParent() == null) {
            return true;
        }
        for (Component c = component; c != null; c = c.getParent()) {
            if (!c.isVisible()) {
                return true;
            }
        }
        return false;
    }

    private void refit() {
        fitPanel();
        hole = element == null ? null : frame(element);
        placeCallout();
        repaint();
    }

    /** Covers only what the user can see of the panel, so the callout always lands on screen. */
    private void fitPanel() {
        Rectangle visible = panel.getVisibleRect();
        Point origin = SwingUtilities.convertPoint(panel, visible.x, visible.y, layer);
        setBounds(origin.x, origin.y, visible.width, visible.height);
    }

    private Rectangle frame(JComponent target) {
        if (isOutOfView(target)) {
            return null;
        }
        Rectangle bounds = SwingUtilities.convertRectangle(target.getParent(), target.getBounds(), this);
        bounds.grow(PADDING, PADDING);
        Rectangle clipped = bounds.intersection(new Rectangle(0, 0, getWidth(), getHeight()));
        if (clipped.isEmpty() || clipped.height > getHeight() * PAGE_SHARE) {
            return null;
        }
        return clipped;
    }

    private void placeCallout() {
        int width = Math.min(Callout.WIDTH, Math.max(120, getWidth() - 2 * GAP));
        int height = Math.min(callout.getPreferredSize().height, Math.max(60, getHeight() - 2 * GAP));
        int x = Math.max(GAP, (getWidth() - width) / 2);
        int y;
        if (hole == null) {
            y = Math.max(GAP, (getHeight() - height) / 2);
        } else if (hole.y + hole.height + GAP + height <= getHeight()) {
            y = hole.y + hole.height + GAP;
        } else if (hole.y - GAP - height >= 0) {
            y = hole.y - GAP - height;
        } else {
            y = Math.max(GAP, getHeight() - height - GAP);
        }
        callout.setBounds(x, y, width, height);
        callout.revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isOutOfView(panel)) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Area shade = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
            if (hole != null) {
                shade.subtract(new Area(new RoundRectangle2D.Double(hole.x, hole.y, hole.width, hole.height, Ui.RADIUS, Ui.RADIUS)));
            }
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(DIM);
            g2.fill(shade);
            if (hole != null) {
                g2.setColor(RING);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(hole.x, hole.y, hole.width - 1, hole.height - 1, Ui.RADIUS, Ui.RADIUS));
            }
        } finally {
            g2.dispose();
        }
    }

    private void bind(int keyCode, String name, Runnable action) {
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }
}
