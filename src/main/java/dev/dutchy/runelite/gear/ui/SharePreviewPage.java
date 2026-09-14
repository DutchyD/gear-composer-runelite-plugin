package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.content.SetupContentEditor;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Shows a pasted setup and asks which section to put it in. */
final class SharePreviewPage extends JPanel {

    static final String ADD = "Add setup";

    private final GearSetup setup;
    private final Map<SectionId, Chip> sectionChips = new LinkedHashMap<>();
    private SectionId chosen;

    SharePreviewPage(GearSetup setup, List<GearSection> sections, Consumer<SectionId> onAdd, Runnable onCancel) {
        this.setup = Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(sections, "sections");
        Objects.requireNonNull(onAdd, "onAdd");
        Objects.requireNonNull(onCancel, "onCancel");
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("A book always has a section");
        }
        chosen = sections.get(0).id();

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        Help.anchor(this, HelpTopic.SHARE_CODE_PAGE);

        int items = SetupContentEditor.allItems(setup.content()).size();
        JLabel summary = Ui.body(setup.name() + " · " + setup.type().displayName() + " · " + items + " item(s)");
        summary.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        JLabel tags = Ui.hint(setup.meta().tags().isEmpty() ? " " : String.join(", ", setup.meta().tags()));
        tags.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        JPanel choices = Ui.column(Ui.SMALL_GAP);
        for (GearSection section : sections) {
            Chip chip = new Chip(section.name(), "Add it to " + section.name(), () -> choose(section.id()));
            sectionChips.put(section.id(), chip);
            choices.add(chip);
        }
        refreshChips();

        add(Ui.column(Ui.GAP,
                NavBar.create("Add shared setup", onCancel),
                summary,
                tags,
                Ui.labelled("Into section", choices),
                new PrimaryButton(ADD, "Add this setup to the chosen section", () -> onAdd.accept(chosen))), BorderLayout.NORTH);
    }

    GearSetup setup() {
        return setup;
    }

    void choose(SectionId sectionId) {
        chosen = Objects.requireNonNull(sectionId, "sectionId");
        refreshChips();
    }

    private void refreshChips() {
        sectionChips.forEach((id, chip) -> chip.setChosen(id.equals(chosen)));
    }
}
