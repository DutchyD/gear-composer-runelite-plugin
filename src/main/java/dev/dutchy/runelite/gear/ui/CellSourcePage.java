package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.CellSources;
import dev.dutchy.runelite.gear.content.CellRef;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Picks which setup's equipment or inventory to copy into a cell. */
final class CellSourcePage extends JPanel {

    static final String NOTHING_TO_COPY = "No other setup has anything of this kind yet";

    private final List<CellSources.Source> sources;

    CellSourcePage(CellRef target, List<CellSources.Source> sources, Consumer<CellSources.Source> onPick, Runnable onBack) {
        Objects.requireNonNull(target, "target");
        this.sources = List.copyOf(Objects.requireNonNull(sources, "sources"));
        Objects.requireNonNull(onPick, "onPick");
        Objects.requireNonNull(onBack, "onBack");

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        Help.anchor(this, HelpTopic.CELL_SOURCE_PAGE);

        JPanel rows = Ui.column(Ui.SMALL_GAP);
        rows.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        if (sources.isEmpty()) {
            rows.add(Ui.hint(NOTHING_TO_COPY));
        }
        for (CellSources.Source source : sources) {
            rows.add(FlatButton.outlined(ActionIcon.COPY, source.label(), "Copy into " + target.describe(), () -> onPick.accept(source)));
        }
        add(Ui.column(Ui.GAP, NavBar.create("Fill " + target.describe(), onBack),
                Ui.note("The items are copied; later edits to the source stay there."), rows), BorderLayout.NORTH);
    }

    List<CellSources.Source> sources() {
        return sources;
    }
}
