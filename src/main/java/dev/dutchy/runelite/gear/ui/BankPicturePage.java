package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.gear.layout.Layout;
import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** What the bank will show for a setup, drawn from the same planner the bank uses, with the parts a tutorial names. */
final class BankPicturePage extends JPanel {

    static final int PICTURE_WIDTH = 210;

    private final JLabel picture = new JLabel("Drawing the bank…", SwingConstants.CENTER);

    BankPicturePage(GearSetup setup, Function<GearSetup, CompletableFuture<BufferedImage>> renderer, Runnable onBack) {
        Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(renderer, "renderer");
        Objects.requireNonNull(onBack, "onBack");
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        picture.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        JPanel legend = Ui.column(Ui.SMALL_GAP,
                Help.describe(Ui.paragraph(HelpTopic.BANK_AMOUNTS.title() + ": held over required, red when short"), HelpTopic.BANK_AMOUNTS),
                Help.describe(Ui.paragraph(HelpTopic.BANK_PLACEHOLDERS.title() + ": faded items you do not have"), HelpTopic.BANK_PLACEHOLDERS));
        if (Layout.of(setup.content()).hasLabelledBands()) {
            legend.add(Help.describe(Ui.paragraph(HelpTopic.BANK_CELL_BANDS.title() + ": cell names on the row above each band"), HelpTopic.BANK_CELL_BANDS));
        }

        add(Ui.column(Ui.GAP, NavBar.create("In the bank: " + setup.name(), onBack),
                Help.describe(picture, HelpTopic.BANK_VIEW), legend), BorderLayout.NORTH);
        renderer.apply(setup).thenAccept(image -> picture.setIcon(new ImageIcon(ImageTransforms.scaleDownToFit(PICTURE_WIDTH, Integer.MAX_VALUE / 2).apply(image))));
    }

    boolean hasPicture() {
        return picture.getIcon() != null;
    }
}
