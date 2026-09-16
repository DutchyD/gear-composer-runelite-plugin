package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.account.AccountEntry;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** The home screen while logged out: every account the plugin knows, two to a row, and the shared setups. */
final class AccountsPage extends JPanel {

    static final String TITLE = "Accounts";
    static final String BACK_TO_MINE = "Back to my setups";
    private static final int COLUMNS = 2;

    private final List<AccountCardView> cards = new ArrayList<>();

    AccountsPage(List<AccountEntry> entries, Consumer<AccountEntry> onOpen, Consumer<AccountEntry> onForget,
                 Runnable onBackToMine, JComponent frontDoor) {
        Objects.requireNonNull(entries, "entries");
        Objects.requireNonNull(onOpen, "onOpen");
        Objects.requireNonNull(onForget, "onForget");
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        Help.anchor(this, HelpTopic.HOME_PAGE);

        JLabel title = new JLabel(TITLE);
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(ColorScheme.BRAND_ORANGE);
        JPanel head = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        head.add(title, BorderLayout.CENTER);

        JPanel grid = Ui.panel(new GridLayout(0, COLUMNS, Ui.SMALL_GAP, Ui.SMALL_GAP));
        for (AccountEntry entry : entries) {
            AccountCardView card = new AccountCardView(entry, onOpen, onForget);
            if (cards.isEmpty()) {
                Help.anchor(card, HelpTopic.ACCOUNT_CARD);
            }
            cards.add(card);
            grid.add(card);
        }

        JPanel column = Ui.column(Ui.GAP, head,
                Ui.note("Click an account to manage its setups, logged in or not. Names are remembered from each login."), grid);
        if (onBackToMine != null) {
            column.add(FlatButton.outlined(ActionIcon.BACK, BACK_TO_MINE, "The list for the character logged in now", onBackToMine));
        }
        if (frontDoor != null) {
            column.add(frontDoor);
        }
        column.setBorder(BorderFactory.createEmptyBorder(0, 0, Ui.GAP, 0));
        add(column, BorderLayout.NORTH);
    }

    List<AccountCardView> cards() {
        return List.copyOf(cards);
    }
}
