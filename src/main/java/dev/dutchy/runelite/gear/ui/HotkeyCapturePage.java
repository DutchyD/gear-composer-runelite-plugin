package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Hotkey;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.function.Consumer;

/** Waits for one key press and hands it back as the setup's hotkey. */
final class HotkeyCapturePage extends JPanel {

    static final String WAITING = "Press a key…";

    private final Card well = new Card();
    private final JLabel current;
    private final Consumer<Hotkey> onChosen;

    /** {@code onChosen} receives {@link Hotkey#NONE} when the key is cleared. */
    HotkeyCapturePage(GearSetup setup, Consumer<Hotkey> onChosen, Runnable onCancel) {
        Objects.requireNonNull(setup, "setup");
        this.onChosen = Objects.requireNonNull(onChosen, "onChosen");
        Objects.requireNonNull(onCancel, "onCancel");

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        Help.anchor(this, HelpTopic.HOTKEY_PAGE);

        current = new JLabel(setup.meta().hasHotkey() ? setup.meta().hotkey().describe() : WAITING, SwingConstants.CENTER);
        current.setFont(FontManager.getRunescapeBoldFont());
        current.setForeground(ColorScheme.BRAND_ORANGE);

        well.setLayout(new BorderLayout());
        well.setPreferredSize(new Dimension(0, 60));
        well.setFocusable(true);
        well.setToolTipText("Click here, then press the key");
        well.add(current, BorderLayout.CENTER);
        well.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                capture(e);
            }
        });
        well.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                well.setOutline(ColorScheme.BRAND_ORANGE);
            }

            @Override
            public void focusLost(FocusEvent e) {
                well.setOutline(Ui.OUTLINE);
            }
        });

        JPanel actions = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        actions.add(new FlatButton("Clear", "Remove the hotkey", () -> onChosen.accept(Hotkey.NONE)), BorderLayout.WEST);
        actions.add(new FlatButton("Cancel", "Keep things as they are", onCancel), BorderLayout.EAST);

        WrappedText hint = Ui.note("Needs game focus. Escape cancels.");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        add(Ui.column(Ui.GAP,
                NavBar.create("Hotkey for " + setup.name(), onCancel),
                well,
                hint,
                actions), BorderLayout.NORTH);
    }

    /** Feeds a key press as if the well had focus; used by tests. */
    void capture(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            return;
        }
        if (isModifier(code)) {
            current.setText("…");
            return;
        }
        Hotkey hotkey = Hotkey.of(event);
        current.setText(hotkey.describe());
        onChosen.accept(hotkey);
    }

    @Override
    public boolean requestFocusInWindow() {
        return well.requestFocusInWindow();
    }

    private static boolean isModifier(int keyCode) {
        return keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_ALT
                || keyCode == KeyEvent.VK_META || keyCode == KeyEvent.VK_ALT_GRAPH || keyCode == KeyEvent.VK_UNDEFINED;
    }
}
