package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Requirements;
import dev.dutchy.runelite.gear.SetupMeta;
import dev.dutchy.runelite.gear.Spellbook;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.SetupContent;
import dev.dutchy.runelite.gear.content.SetupType;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.selector.ItemSelectorFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;

final class SetupEditorForm extends JPanel {

    interface Listener {

        /**
         * The setup as edited: a fresh one for a new setup, the existing one with changes applied otherwise.
         */
        void onSaved(GearSetup draft);

        void onCancelled();
    }

    private final Listener listener;
    private final ItemPicker picker;
    private final TextInput nameField = new TextInput();
    private final List<SetupTypeButton> typeButtons = new ArrayList<>();
    private final PrimaryButton saveButton;
    private final TagEditor tagEditor;
    private final ColourPicker colourPicker;
    private final TextArea notesArea = new TextArea(3);
    private final Map<Spellbook, Chip> spellbookChips = new LinkedHashMap<>();
    private final PrayerPicker prayerPicker;
    private final SetupMeta existingMeta;
    private final GearSetup existing;

    private final Map<Integer, Chip> rowChips = new LinkedHashMap<>();
    private final JPanel rowsRow;
    private ItemId icon;
    private SetupType type;
    private Spellbook spellbook;
    private int rows;

    SetupEditorForm(String title,
                    GearSetup existing,
                    ItemIconFactory icons,
                    ItemSelectorFactory selectors,
                    SetupTypeArtwork artwork,
                    PrayerArtwork prayers,
                    Listener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
        this.existing = existing;
        this.existingMeta = existing == null ? SetupMeta.none() : existing.meta();
        this.icon = existing == null ? null : existing.iconId();
        this.type = existing == null ? SetupType.GEAR : existing.type();
        this.rows = existing != null && existing.content() instanceof CustomContent
                ? ((CustomContent) existing.content()).rows() : CustomContent.DEFAULT_ROWS;
        this.rowsRow = Ui.labelled("Rows", "each two cells wide", rowsChooser());
        this.prayerPicker = new PrayerPicker(existingMeta.requirements().quickPrayers(), Objects.requireNonNull(prayers, "prayers"), picked -> {
        });
        this.spellbook = existingMeta.requirements().spellbook();
        this.picker = new ItemPicker(icons, selectors, resolved -> chooseIcon(resolved.id()), this::clearIcon);
        this.saveButton = new PrimaryButton(existing == null ? "Create setup" : "Save changes",
                existing == null ? "Create this setup" : "Save this setup", this::save);
        this.tagEditor = new TagEditor(existingMeta.tags());
        this.colourPicker = new ColourPicker(existingMeta.label(), label -> {
        });

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel actions = Ui.panel(new BorderLayout());
        actions.setBorder(BorderFactory.createEmptyBorder(Ui.SMALL_GAP, 0, 0, 0));
        actions.add(saveButton, BorderLayout.CENTER);

        notesArea.setText(existingMeta.notes());

        Help.anchor(this, HelpTopic.EDITOR_PAGE);
        Help.anchor(rowsRow, HelpTopic.EDITOR_ROWS);
        Help.describe(saveButton, HelpTopic.EDITOR_SAVE);
        add(Ui.column(Ui.GAP,
                NavBar.create(title, this::cancel),
                Help.describe(Ui.labelled("Name", nameField()), HelpTopic.EDITOR_NAME),
                Help.describe(Ui.labelled("Layout", existing != null ? "cannot change" : null, typeChooser(existing != null, artwork)), HelpTopic.EDITOR_TYPE),
                rowsRow,
                Help.describe(Ui.labelled("Icon", "optional", picker), HelpTopic.EDITOR_ICON),
                Help.describe(Ui.labelled("Colour", colourPicker), HelpTopic.EDITOR_COLOUR),
                Help.describe(Ui.labelled("Tags", "Enter adds one", tagEditor), HelpTopic.EDITOR_TAGS),
                Help.describe(Ui.labelled("Notes", "shown on the setup", notesArea), HelpTopic.EDITOR_NOTES),
                Help.describe(Ui.labelled("Spellbook", "warns on mismatch", spellbookChooser()), HelpTopic.EDITOR_SPELLBOOK),
                Help.describe(Ui.labelled("Quick prayers", "click to pick", prayerPicker), HelpTopic.EDITOR_QUICK_PRAYERS),
                actions), BorderLayout.NORTH);

        if (existing != null) {
            nameField.setText(existing.name());
        }
        if (icon != null) {
            picker.show(icon);
        }
        refreshType();
        refreshSpellbook();
        refreshSaveEnabled();
    }

    String enteredName() {
        return nameField.getText();
    }

    void setEnteredName(String name) {
        nameField.setText(name);
    }

    Optional<ItemId> chosenIcon() {
        return Optional.ofNullable(icon);
    }

    SetupType chosenType() {
        return type;
    }

    int chosenRows() {
        return rows;
    }

    boolean isRowsChooserShowing() {
        return rowsRow.isVisible();
    }

    void chooseRows(int newRows) {
        if (newRows < CustomContent.MIN_ROWS || newRows > CustomContent.MAX_ROWS) {
            throw new IllegalArgumentException("Rows must be within [" + CustomContent.MIN_ROWS + ", " + CustomContent.MAX_ROWS + "], got " + newRows);
        }
        rows = newRows;
        refreshRows();
    }

    boolean isSaveEnabled() {
        return saveButton.isEnabled();
    }

    TagEditor tags() {
        return tagEditor;
    }

    ColourPicker colour() {
        return colourPicker;
    }

    @SuppressWarnings("SameParameterValue")
    void setNotes(String notes) {
        notesArea.setText(notes);
    }

    PrayerPicker prayerPicker() {
        return prayerPicker;
    }

    void chooseSpellbook(Spellbook newSpellbook) {
        spellbook = Objects.requireNonNull(newSpellbook, "newSpellbook");
        refreshSpellbook();
    }

    void chooseType(SetupType newType) {
        type = Objects.requireNonNull(newType, "newType");
        refreshType();
    }

    void chooseIcon(ItemId newIcon) {
        icon = Objects.requireNonNull(newIcon, "newIcon");
        picker.show(newIcon);
    }

    void clearIcon() {
        icon = null;
        picker.clear();
    }

    /**
     * What the form would save right now; pin and hotkey are kept as they were.
     */
    SetupMeta meta() {
        return existingMeta
                .withLabel(colourPicker.chosen())
                .withTags(tagEditor.tags())
                .withNotes(clamp(notesArea.getText()))
                .withRequirements(new Requirements(spellbook, prayerPicker.chosen()));
    }

    void save() {
        if (!GearSetup.isValidName(nameField.getText())) {
            return;
        }
        tagEditor.addFromField();
        listener.onSaved(draft());
    }

    /**
     * The setup as it would be saved.
     */
    GearSetup draft() {
        GearSetup base = existing == null
                ? GearSetup.named(nameField.getText()).withContent(newContent())
                : existing.withName(nameField.getText());
        if (existing != null && existing.content() instanceof CustomContent) {
            base = base.withContent(((CustomContent) existing.content()).withRows(rows));
        }
        GearSetup withIcon = icon == null ? base.withoutIcon() : base.withIcon(icon);
        return withIcon.withMeta(meta());
    }

    void cancel() {
        listener.onCancelled();
    }

    @Override
    public boolean requestFocusInWindow() {
        return nameField.requestFocusInWindow();
    }

    /** Notes are kept to what a setup may hold, so a paste cannot overflow the field. */
    private static String clamp(String text) {
        String stripped = text.strip();
        return stripped.length() <= SetupMeta.MAX_NOTES ? stripped : stripped.substring(0, SetupMeta.MAX_NOTES);
    }

    /** A custom layout starts with the chosen number of rows; the other types have a fixed shape. */
    private SetupContent newContent() {
        return type == SetupType.CUSTOM ? CustomContent.empty(rows) : SetupContent.empty(type);
    }

    private JPanel rowsChooser() {
        JPanel grid = Ui.panel(new GridLayout(1, CustomContent.MAX_ROWS, Ui.SMALL_GAP, 0));
        for (int count = CustomContent.MIN_ROWS; count <= CustomContent.MAX_ROWS; count++) {
            int chosen = count;
            Chip chip = new Chip(String.valueOf(count), count == 1 ? "One row of two cells" : count + " rows of two cells", () -> chooseRows(chosen));
            rowChips.put(count, chip);
            grid.add(chip);
        }
        return grid;
    }

    private void refreshRows() {
        rowChips.forEach((count, chip) -> chip.setChosen(count == rows));
        rowsRow.setVisible(type == SetupType.CUSTOM);
    }

    private JPanel typeChooser(boolean locked, SetupTypeArtwork artwork) {
        if (locked) {
            return Ui.column(0, SetupTypeButton.locked(type, artwork));
        }
        List<SetupTypeButton> buttons = new ArrayList<>();
        for (SetupType candidate : SetupType.values()) {
            buttons.add(new SetupTypeButton(candidate, artwork, this::chooseType));
        }
        typeButtons.addAll(buttons);
        return Ui.column(Ui.SMALL_GAP, buttons.toArray(SetupTypeButton[]::new));
    }

    private JPanel spellbookChooser() {
        JPanel grid = Ui.panel(new GridLayout(0, 3, Ui.SMALL_GAP, Ui.SMALL_GAP));
        for (Spellbook candidate : Spellbook.values()) {
            addSpellbookChip(grid, candidate, candidate.displayName(),
                    candidate.isRequirement() ? "Warn unless you are on " + candidate.displayName() : "No spellbook requirement");
        }
        return grid;
    }

    private void addSpellbookChip(JPanel grid, Spellbook value, String text, String tooltip) {
        Chip chip = new Chip(text, tooltip, () -> chooseSpellbook(value));
        spellbookChips.put(value, chip);
        grid.add(chip);
    }

    private TextInput nameField() {
        nameField.setToolTipText("What to call this setup");
        nameField.addActionListener(e -> save());
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshSaveEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshSaveEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshSaveEnabled();
            }
        });
        return nameField;
    }

    private void refreshType() {
        typeButtons.forEach(button -> button.setChosen(button.type() == type));
        refreshRows();
    }

    private void refreshSpellbook() {
        spellbookChips.forEach((value, chip) -> chip.setChosen(value == spellbook));
    }

    private void refreshSaveEnabled() {
        saveButton.setEnabled(GearSetup.isValidName(nameField.getText()));
    }

}
