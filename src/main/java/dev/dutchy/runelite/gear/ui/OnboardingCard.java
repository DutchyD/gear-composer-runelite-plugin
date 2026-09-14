package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.guide.GuideId;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;

/** The front door: a tutorial for each layout type and a tour of the list, shown while the book is empty. */
final class OnboardingCard extends Card {

    static final String TITLE = "Get started";
    static final String DISMISS = "Got it";

    OnboardingCard(Consumer<GuideId> onStart, Runnable onDismiss) {
        Objects.requireNonNull(onStart, "onStart");
        Objects.requireNonNull(onDismiss, "onDismiss");
        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel title = new JLabel(TITLE);
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(ColorScheme.TEXT_COLOR);

        JPanel steps = Ui.column(Ui.SMALL_GAP,
                Ui.paragraph("Pick a layout type and a tutorial walks you through it. Nothing to do but read."),
                tutorial(GuideId.TUTORIAL_GEAR, "one loadout: worn gear and inventory", onStart),
                tutorial(GuideId.TUTORIAL_BANK, "two inventories side by side", onStart),
                tutorial(GuideId.TUTORIAL_CUSTOM, "your own grid of loadouts", onStart),
                FlatButton.outlined(ActionIcon.HELP, GuideId.LIST.title(), "A short tour of this page", () -> onStart.accept(GuideId.LIST)));

        JPanel footer = Ui.panel(new BorderLayout());
        footer.add(Ui.note("Every page has a ? button, and Guides… lives under the ··· menu."), BorderLayout.CENTER);
        footer.add(new FlatButton(DISMISS, "Hide this card", onDismiss), BorderLayout.EAST);

        add(title, BorderLayout.NORTH);
        add(steps, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private static FlatButton tutorial(GuideId id, String hint, Consumer<GuideId> onStart) {
        return FlatButton.outlined(ActionIcon.HELP, id.title().replace("Tutorial: ", ""), hint, () -> onStart.accept(id));
    }
}
