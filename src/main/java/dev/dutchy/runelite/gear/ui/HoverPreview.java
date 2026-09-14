package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/** Shows a setup's contents beside its tile after the cursor rests on it for a moment. */
final class HoverPreview {

    static final int DELAY_MILLIS = 450;
    private static final int GAP = 6;

    private final ItemIconFactory icons;
    private final Timer timer = new Timer(DELAY_MILLIS, e -> open());

    private JComponent anchor;
    private GearSetup pending;
    private Popup popup;

    HoverPreview(ItemIconFactory icons) {
        this.icons = Objects.requireNonNull(icons, "icons");
        timer.setRepeats(false);
    }

    /** A preview that never shows, for bulk mode and tests. */
    static HoverPreview none() {
        return new HoverPreview();
    }

    private HoverPreview() {
        this.icons = null;
        timer.setRepeats(false);
    }

    void arm(JComponent tile, GearSetup setup) {
        if (icons == null) {
            return;
        }
        hide();
        anchor = Objects.requireNonNull(tile, "tile");
        pending = Objects.requireNonNull(setup, "setup");
        timer.restart();
    }

    void hide() {
        timer.stop();
        pending = null;
        anchor = null;
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    boolean isShowing() {
        return popup != null;
    }

    boolean isArmed() {
        return pending != null;
    }

    private void open() {
        if (pending == null || anchor == null || !anchor.isShowing() || GraphicsEnvironment.isHeadless()) {
            return;
        }
        MiniContentView view = new MiniContentView(pending, icons);
        Dimension size = view.getPreferredSize();
        Point origin = anchor.getLocationOnScreen();
        Rectangle screen = anchor.getGraphicsConfiguration().getBounds();
        int x = origin.x + anchor.getWidth() + GAP;
        if (x + size.width > screen.x + screen.width) {
            x = origin.x - size.width - GAP;
        }
        int y = Math.min(origin.y, screen.y + screen.height - size.height - GAP);
        popup = PopupFactory.getSharedInstance().getPopup(SwingUtilities.getWindowAncestor(anchor), view, x, Math.max(screen.y, y));
        popup.show();
    }
}
