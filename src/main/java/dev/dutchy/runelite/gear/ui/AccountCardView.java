package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.account.AccountEntry;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Consumer;

/** One account on the accounts screen: badge over name, short id and setup count; click opens its list, right-click for more. */
final class AccountCardView extends Card {

    static final int HEIGHT = 96;
    static final String OPEN = "Open";
    static final String FORGET = "Forget account";

    private final AccountEntry entry;
    private final Consumer<AccountEntry> onOpen;
    private final Consumer<AccountEntry> onForget;
    private boolean hovered;

    AccountCardView(AccountEntry entry, Consumer<AccountEntry> onOpen, Consumer<AccountEntry> onForget) {
        this.entry = Objects.requireNonNull(entry, "entry");
        this.onOpen = Objects.requireNonNull(onOpen, "onOpen");
        this.onForget = Objects.requireNonNull(onForget, "onForget");
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        setPreferredSize(new Dimension(0, HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(entry.isShared() ? "Setups every account can use" : entry.name() + " · " + entry.shortId()
                + (entry.canForget() ? ". Right-click to forget." : ""));

        JPanel badge = Ui.panel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badge.add(new AccountBadge(entry.owner(), entry.name()));

        JLabel name = new JLabel(entry.isShared() ? "Shared" : entry.name(), SwingConstants.CENTER);
        name.setFont(FontManager.getRunescapeBoldFont());
        name.setForeground(ColorScheme.TEXT_COLOR);
        JLabel detail = Ui.hint(entry.isShared() ? "all accounts" : entry.shortId() + (entry.loggedIn() ? " · here" : ""));
        detail.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel count = Ui.body(entry.setupCount() == 1 ? "1 setup" : entry.setupCount() + " setups");
        count.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel text = Ui.column(0, name, detail, count);
        add(badge, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    onOpen.accept(entry);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                refresh();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                refresh();
            }
        };
        addMouseListener(mouse);
        refresh();
    }

    AccountEntry entry() {
        return entry;
    }

    ContextMenu contextMenu() {
        ContextMenu menu = new ContextMenu().item(ActionIcon.GRID, OPEN, () -> onOpen.accept(entry));
        if (entry.canForget()) {
            menu.divider().danger(FORGET, () -> onForget.accept(entry));
        }
        return menu;
    }

    private void maybeShowMenu(MouseEvent e) {
        if (e.isPopupTrigger() && !entry.isShared()) {
            contextMenu().show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void refresh() {
        setBackground(hovered ? Ui.SURFACE_HOVER : Ui.SURFACE);
        setOutline(entry.loggedIn() ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE);
        repaint();
    }
}
