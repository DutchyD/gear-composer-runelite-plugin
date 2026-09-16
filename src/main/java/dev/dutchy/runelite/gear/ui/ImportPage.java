package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/** Shows what a file holds and asks whether to add it or replace everything with it. */
final class ImportPage extends JPanel {

    static final String ADD = "Add to my setups";
    static final String REPLACE = "Replace everything";

    private final List<GearSection> sections;

    ImportPage(String source, List<GearSection> sections, Runnable onAdd, Runnable onReplace, Runnable onCancel) {
        Objects.requireNonNull(source, "source");
        this.sections = List.copyOf(Objects.requireNonNull(sections, "sections"));
        Objects.requireNonNull(onAdd, "onAdd");
        Objects.requireNonNull(onReplace, "onReplace");
        Objects.requireNonNull(onCancel, "onCancel");

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        Help.anchor(this, HelpTopic.IMPORT_PAGE);

        int setups = sections.stream().mapToInt(GearSection::size).sum();
        JLabel summary = Ui.body(sections.size() + " section(s), " + setups + " setup(s)");
        summary.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        JLabel from = Ui.hint(source);
        from.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        JPanel rows = Ui.column(2);
        for (GearSection section : sections) {
            rows.add(Ui.hint("· " + section.name() + " (" + section.size() + ")"));
        }

        JPanel replaceRow = Ui.panel(new BorderLayout());
        replaceRow.add(new FlatButton(REPLACE, "Throw away every current setup and keep only the file's", onReplace), BorderLayout.WEST);
        replaceRow.add(new FlatButton("Cancel", "Import nothing", onCancel), BorderLayout.EAST);

        add(Ui.column(Ui.GAP,
                NavBar.create("Import", onCancel),
                from,
                summary,
                rows,
                new PrimaryButton(ADD, "Keep what you have and add these", onAdd),
                replaceRow), BorderLayout.NORTH);
    }

    List<GearSection> sections() {
        return sections;
    }
}
