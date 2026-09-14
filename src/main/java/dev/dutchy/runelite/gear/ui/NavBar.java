package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;

/** Back button and title, pinned to the top of every page that is not the setup list. */
final class NavBar {

    private static final int HEIGHT = 24;
    // A square button is as wide as the bar is tall.
    @SuppressWarnings("SuspiciousNameCombination")
    private static final Dimension SQUARE = new Dimension(HEIGHT, HEIGHT);
    private static final String NAV_BAR = "gearcomposer.navbar";

    private NavBar() {
    }

    static JPanel create(String title, Runnable onBack) {
        JPanel bar = Ui.panel(new BorderLayout(4, 0));
        bar.putClientProperty(NAV_BAR, Boolean.TRUE);
        bar.setPreferredSize(new Dimension(0, HEIGHT));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

        JLabel heading = new JLabel(title);
        heading.setFont(FontManager.getRunescapeBoldFont());
        heading.setForeground(ColorScheme.BRAND_ORANGE);
        heading.setToolTipText(title);

        FlatButton back = new FlatButton(ActionIcon.BACK, "Back", onBack);
        back.setPreferredSize(SQUARE);

        bar.add(back, BorderLayout.WEST);
        bar.add(heading, BorderLayout.CENTER);
        return bar;
    }

    /** Puts the guide button on the right of the page's nav bar, if the page has one. */
    static void attachGuide(Container page, JComponent guide) {
        for (Component child : page.getComponents()) {
            if (child instanceof JPanel && Boolean.TRUE.equals(((JPanel) child).getClientProperty(NAV_BAR))) {
                JPanel bar = (JPanel) child;
                Component existing = ((BorderLayout) bar.getLayout()).getLayoutComponent(BorderLayout.EAST);
                if (existing == null) {
                    bar.add(guide, BorderLayout.EAST);
                } else {
                    bar.remove(existing);
                    JPanel east = Ui.panel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                    east.add(existing);
                    east.add(guide);
                    bar.add(east, BorderLayout.EAST);
                }
                bar.revalidate();
                return;
            }
            if (child instanceof Container) {
                attachGuide((Container) child, guide);
            }
        }
    }

}
