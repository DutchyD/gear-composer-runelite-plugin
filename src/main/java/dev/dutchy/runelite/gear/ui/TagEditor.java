package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SetupMeta;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/** Tag chips with a remove cross, plus a field that adds one on Enter. */
final class TagEditor extends JPanel {

    private final Set<String> tags = new TreeSet<>();
    private final JPanel chips = Ui.panel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    private final TextInput field = new TextInput();

    TagEditor(Set<String> initial) {
        Objects.requireNonNull(initial, "initial");
        tags.addAll(SetupMeta.normalizeTags(initial));
        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setOpaque(false);
        field.setToolTipText("Type a tag and press Enter");
        field.addActionListener(e -> addFromField());
        add(chips, BorderLayout.NORTH);
        add(field, BorderLayout.CENTER);
        refresh();
    }

    Set<String> tags() {
        return Set.copyOf(tags);
    }

    boolean add(String tag) {
        Set<String> candidate = new LinkedHashSet<>(tags);
        candidate.add(tag);
        try {
            Set<String> normalized = SetupMeta.normalizeTags(candidate);
            if (normalized.size() == tags.size()) {
                return false;
            }
            tags.clear();
            tags.addAll(normalized);
        } catch (IllegalArgumentException e) {
            return false;
        }
        refresh();
        return true;
    }

    void remove(String tag) {
        if (tags.remove(tag)) {
            refresh();
        }
    }

    @SuppressWarnings("SameParameterValue")
    void setFieldText(String text) {
        field.setText(text);
    }

    void addFromField() {
        if (add(field.getText())) {
            field.setText("");
        }
    }

    private void refresh() {
        chips.removeAll();
        for (String tag : tags) {
            chips.add(new FlatButton(ActionIcon.DELETE, tag, "Remove " + tag, () -> remove(tag)));
        }
        chips.setVisible(!tags.isEmpty());
        field.setEnabled(tags.size() < SetupMeta.MAX_TAGS);
        chips.revalidate();
        chips.repaint();
        revalidate();
    }
}
