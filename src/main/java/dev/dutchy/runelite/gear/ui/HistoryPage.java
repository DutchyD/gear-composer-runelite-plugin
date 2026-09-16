package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.gear.history.SetupRevision;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** Earlier contents of one setup, every variant together, newest first, each with a way back to it. */
final class HistoryPage extends JPanel {

    static final String RESTORE = "Restore";
    static final String EMPTY = "No earlier versions yet";

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM");

    private final List<SetupRevision> revisions;

    HistoryPage(GearSetup setup, List<SetupRevision> revisions, Clock clock, Consumer<SetupRevision> onRestore, Runnable onBack) {
        Objects.requireNonNull(setup, "setup");
        this.revisions = List.copyOf(Objects.requireNonNull(revisions, "revisions"));
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(onRestore, "onRestore");

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        Help.anchor(this, HelpTopic.HISTORY_PAGE);

        JPanel rows = Ui.column(Ui.SMALL_GAP);
        if (revisions.isEmpty()) {
            rows.add(Ui.hint(EMPTY));
        }
        for (SetupRevision revision : revisions) {
            rows.add(row(setup, revision, clock, onRestore));
        }

        WrappedText hint = Ui.note("A restore is listed here too.");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        add(Ui.column(Ui.GAP, NavBar.create("History of " + setup.name(), onBack), hint, rows), BorderLayout.NORTH);
    }

    List<SetupRevision> revisions() {
        return revisions;
    }

    private static Card row(GearSetup setup, SetupRevision revision, Clock clock, Consumer<SetupRevision> onRestore) {
        Card card = new Card();
        card.setLayout(new BorderLayout(Ui.SMALL_GAP, 0));
        card.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel when = Ui.hint(describe(revision.at(), clock));
        when.setPreferredSize(new Dimension(56, 18));
        JLabel cause = Ui.body(revision.cause());
        Component what = variantName(setup, revision)
                .map(name -> (Component) Ui.column(0, cause, Ui.hint(name)))
                .orElse(cause);

        card.add(when, BorderLayout.WEST);
        card.add(what, BorderLayout.CENTER);
        card.add(new FlatButton(RESTORE, "Put these items back", () -> onRestore.accept(revision)), BorderLayout.EAST);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    /** Which variant a revision came from, when the setup has more than one to tell apart. */
    private static Optional<String> variantName(GearSetup setup, SetupRevision revision) {
        if (!setup.hasVariants()) {
            return Optional.empty();
        }
        return revision.variant().flatMap(setup::variant).map(SetupVariant::name);
    }

    private static String describe(Instant at, Clock clock) {
        ZoneId zone = clock.getZone();
        Instant now = clock.instant();
        if (Duration.between(at, now).toHours() < 24 && at.atZone(zone).toLocalDate().equals(now.atZone(zone).toLocalDate())) {
            return TIME.format(at.atZone(zone));
        }
        return DATE.format(at.atZone(zone));
    }
}
