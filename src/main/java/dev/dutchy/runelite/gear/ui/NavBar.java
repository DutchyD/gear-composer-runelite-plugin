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
}
