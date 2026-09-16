package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.gear.persistence.Backup;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Automatic backups, newest first, each restorable. */
final class BackupsPage extends JPanel {

    static final String RESTORE = "Restore";
    static final String EMPTY = "No backups yet; one is written after every change.";

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("d MMM HH:mm");

    private final List<Backup> backups;

    BackupsPage(List<Backup> backups, ZoneId zone, Consumer<Backup> onRestore, Runnable onBack) {
        this.backups = List.copyOf(Objects.requireNonNull(backups, "backups"));
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(onRestore, "onRestore");

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        Help.anchor(this, HelpTopic.BACKUPS_PAGE);

        JPanel rows = Ui.column(Ui.SMALL_GAP);
        if (backups.isEmpty()) {
            rows.add(Ui.hint(EMPTY));
        }
        for (Backup backup : backups) {
            rows.add(row(backup, zone, onRestore));
        }

        WrappedText hint = Ui.note("Restoring replaces everything; a backup of today's setups is written first.");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        add(Ui.column(Ui.GAP, NavBar.create("Backups", onBack), hint, rows), BorderLayout.NORTH);
    }

    List<Backup> backups() {
        return backups;
    }

    private static Card row(Backup backup, ZoneId zone, Consumer<Backup> onRestore) {
        Card card = new Card();
        card.setLayout(new BorderLayout(Ui.SMALL_GAP, 0));
        card.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Ui.body(STAMP.format(backup.at().atZone(zone))), BorderLayout.CENTER);
        card.add(new FlatButton(RESTORE, "Go back to this copy", () -> onRestore.accept(backup)), BorderLayout.EAST);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }
}
