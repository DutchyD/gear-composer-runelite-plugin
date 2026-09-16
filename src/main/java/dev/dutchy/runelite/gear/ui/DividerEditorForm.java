package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/** Edits one divider: its text, the columns it covers, and where the text sits; the strip below previews it. */
final class DividerEditorForm extends JPanel {

    interface Listener {

        void onDividerSet(SlotRef at, Divider divider);

        void onDividerRemoved(SlotRef at);

        void onCancelled();
    }

    static final String SAVE = "Save";
    static final String REMOVE = "Remove divider";

    private final SlotRef slot;
    private final int row;
    private final List<Divider> others;
    private final Listener listener;
    private final TextInput labelField = new TextInput();
    private final List<Chip> columnChips = new ArrayList<>();
    private final Map<TextAlign, Chip> alignChips = new EnumMap<>(TextAlign.class);
    private final Chip lineChip = new Chip("Underline", "Draw a line under the text, across the columns", () -> setUnderlined(true));
    private final Chip noLineChip = new Chip("No line", "Text only", () -> setUnderlined(false));
    private final PrimaryButton saveButton;
    private final JPanel previewHolder = Ui.panel(new BorderLayout());
    private int fromColumn;
    private int toColumn;
    private TextAlign align;
    private boolean underlined = true;

    /**
     * @param existing the divider over the slot's column, or null to start one there
     * @param onRow    every divider above the row, so a new one can take the free columns around the slot
     */
    DividerEditorForm(SlotRef slot, Divider existing, List<Divider> onRow, Listener listener) {
        this.slot = Objects.requireNonNull(slot, "slot");
        this.listener = Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(onRow, "onRow");
        this.row = ItemGrid.row(slot.gridIndex());
        this.others = onRow.stream().filter(divider -> !divider.equals(existing)).collect(Collectors.toList());
        int column = ItemGrid.column(slot.gridIndex());
        if (existing != null) {
            fromColumn = existing.fromColumn();
            toColumn = existing.toColumn();
            align = existing.align();
            underlined = existing.underlined();
            labelField.setText(existing.label());
        } else {
            fromColumn = freeEdge(column, -1);
            toColumn = freeEdge(column, 1);
            align = TextAlign.DEFAULT;
        }
        this.saveButton = new PrimaryButton(SAVE, "Keep this divider", this::save);

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        Help.anchor(this, HelpTopic.DIVIDER_PAGE);
        Help.describe(saveButton, HelpTopic.DIVIDER_SAVE);
        labelField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });
        labelField.addActionListener(e -> save());

        JPanel actions = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        actions.add(saveButton, BorderLayout.CENTER);
        JPanel secondary = Ui.panel(new FlowLayout(FlowLayout.RIGHT, Ui.SMALL_GAP, 0));
        if (existing != null) {
            secondary.add(new FlatButton(REMOVE, "Take this divider off the row", () -> listener.onDividerRemoved(slot)));
        }
        secondary.add(new FlatButton("Cancel", "Back without changing anything", listener::onCancelled));
        actions.add(secondary, BorderLayout.EAST);

        add(Ui.column(Ui.GAP,
                NavBar.create((existing == null ? "New divider above row " : "Divider above row ") + (row + 1), listener::onCancelled),
                Help.describe(Ui.labelled("Text", "up to " + Divider.MAX_LABEL_LENGTH + " characters", labelField), HelpTopic.DIVIDER_TEXT),
                Help.describe(Ui.labelled("Columns", "click to extend or shrink", columnChooser()), HelpTopic.DIVIDER_COLUMNS),
                Help.describe(Ui.labelled("Text sits", alignChooser()), HelpTopic.DIVIDER_ALIGN),
                Help.describe(Ui.labelled("Line", lineChooser()), HelpTopic.DIVIDER_LINE),
                Ui.labelled("Preview", previewHolder),
                actions), BorderLayout.NORTH);
        refresh();
    }

    String label() {
        return labelField.getText();
    }

    void setLabel(String text) {
        labelField.setText(text);
    }

    int fromColumn() {
        return fromColumn;
    }

    int toColumn() {
        return toColumn;
    }

    void setAlign(TextAlign newAlign) {
        align = Objects.requireNonNull(newAlign, "newAlign");
        refresh();
    }

    boolean isUnderlined() {
        return underlined;
    }

    void setUnderlined(boolean withLine) {
        underlined = withLine;
        refresh();
    }

    /** Extends the span to reach the column, or shrinks it when the column is an edge of a wider span. */
    void toggleColumn(int column) {
        if (column < fromColumn) {
            fromColumn = column;
        } else if (column > toColumn) {
            toColumn = column;
        } else if (column == fromColumn && toColumn > fromColumn) {
            fromColumn++;
        } else if (column == toColumn && toColumn > fromColumn) {
            toColumn--;
        }
        refresh();
    }

    /** The divider as it would be saved, or empty while the text is not valid. */
    Optional<Divider> draft() {
        return Divider.isValidLabel(labelField.getText())
                ? Optional.of(new Divider(row, fromColumn, toColumn, labelField.getText(), align, underlined))
                : Optional.empty();
    }

    void save() {
        draft().ifPresent(divider -> listener.onDividerSet(slot, divider));
    }

    @Override
    public boolean requestFocusInWindow() {
        return labelField.requestFocusInWindow();
    }

    private int freeEdge(int column, int direction) {
        int edge = column;
        while (edge + direction >= 0 && edge + direction < ItemGrid.COLUMNS && !taken(edge + direction)) {
            edge += direction;
        }
        return edge;
    }

    private boolean taken(int column) {
        return others.stream().anyMatch(divider -> divider.spansColumn(column));
    }

    private JPanel columnChooser() {
        JPanel row = Ui.panel(new GridLayout(1, ItemGrid.COLUMNS, Ui.SMALL_GAP, 0));
        for (int column = 0; column < ItemGrid.COLUMNS; column++) {
            int index = column;
            Chip chip = new Chip(String.valueOf(column + 1), "Column " + (column + 1), () -> toggleColumn(index));
            columnChips.add(chip);
            row.add(chip);
        }
        return row;
    }

    private JPanel alignChooser() {
        JPanel row = Ui.panel(new GridLayout(1, TextAlign.values().length, Ui.SMALL_GAP, 0));
        for (TextAlign choice : TextAlign.values()) {
            Chip chip = new Chip(choice.displayName(), "Put the text at the " + choice.displayName().toLowerCase(Locale.ROOT), () -> setAlign(choice));
            alignChips.put(choice, chip);
            row.add(chip);
        }
        return row;
    }

    private JPanel lineChooser() {
        JPanel row = Ui.panel(new GridLayout(1, 2, Ui.SMALL_GAP, 0));
        row.add(lineChip);
        row.add(noLineChip);
        return row;
    }

    private void refresh() {
        lineChip.setChosen(underlined);
        noLineChip.setChosen(!underlined);
        for (int column = 0; column < columnChips.size(); column++) {
            columnChips.get(column).setChosen(column >= fromColumn && column <= toColumn);
        }
        alignChips.forEach((choice, chip) -> chip.setChosen(choice == align));
        saveButton.setEnabled(draft().isPresent());
        previewHolder.removeAll();
        List<Divider> shown = new ArrayList<>(others);
        draft().ifPresent(shown::add);
        previewHolder.add(new DividerStrip(slot.gridSibling(row * ItemGrid.COLUMNS), shown, QUIET), BorderLayout.CENTER);
        previewHolder.revalidate();
        previewHolder.repaint();
    }

    /** The preview strip is only a picture; nothing on it does anything. */
    private static final SlotView.Listener QUIET = new SlotView.Listener() {
        @Override
        public void clicked(SlotView view, MouseEvent event) {
        }

        @Override
        public void pressed(SlotView view, MouseEvent event) {
        }

        @Override
        public void dragged(SlotView view, MouseEvent event) {
        }

        @Override
        public void released(SlotView view, MouseEvent event) {
        }

        @Override
        public void edit(SlotRef ref) {
        }

        @Override
        public void clear(SlotRef ref) {
        }

        @Override
        public void fillRemaining(SlotRef from) {
        }

        @Override
        public void fillRow(SlotRef from) {
        }

        @Override
        public void editDivider(SlotRef slot) {
        }

        @Override
        public void removeDivider(SlotRef slot) {
        }

        @Override
        public void changeDivider(SlotRef slot, DividerChange change) {
        }
    };
}
