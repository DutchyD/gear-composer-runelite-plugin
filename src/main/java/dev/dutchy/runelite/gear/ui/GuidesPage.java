package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.guide.Guide;
import dev.dutchy.runelite.gear.guide.GuideProgress;
import dev.dutchy.runelite.gear.guide.Guides;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** The tutorials, one per layout type, with a tick beside the ones already completed. */
final class GuidesPage extends JPanel {

    static final String TITLE = "Guides";

    private final List<Guide> guides;

    GuidesPage(GuideProgress progress, Consumer<Guide> onStart, Runnable onBack) {
        Objects.requireNonNull(progress, "progress");
        Objects.requireNonNull(onStart, "onStart");
        Objects.requireNonNull(onBack, "onBack");
        this.guides = Guides.all().stream().filter(guide -> guide.id().isTutorial()).collect(Collectors.toList());

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel tutorials = Ui.column(Ui.SMALL_GAP);
        for (Guide guide : guides) {
            boolean done = progress.isCompleted(guide.id());
            tutorials.add(FlatButton.outlined(done ? ActionIcon.CHECK : ActionIcon.HELP, guide.title().replace("Tutorial: ", ""),
                    (done ? "Completed. " : "") + guide.length() + " steps", () -> onStart.accept(guide)));
        }
        add(Ui.column(Ui.GAP, NavBar.create(TITLE, onBack),
                Ui.note("A tutorial builds a sample setup and walks it from the editor to the bank. Nothing to do but read; Escape leaves at any time."),
                Ui.labelled("Tutorials", "one per layout type", tutorials),
                Ui.note("For a closer look at any single page, press the ? on that page.")), BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Ui.GAP, 0));
    }

    List<Guide> guides() {
        return guides;
    }
}
