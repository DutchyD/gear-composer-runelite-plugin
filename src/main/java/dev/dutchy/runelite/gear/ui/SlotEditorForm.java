package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.ItemMatch;
import dev.dutchy.runelite.gear.content.QuantityText;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;
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

final class SlotEditorForm extends JPanel {

    interface Listener {

        void onSlotSet(SlotRef ref, SetupItem item);

        void onSlotCleared(SlotRef ref);

        void onCancelled();
    }

    private final SlotRef ref;
    private final Listener listener;
    private final ItemPicker picker;
    private final TextInput quantityField = new TextInput();
    private final AmountPresets presets = new AmountPresets(this::setEnteredQuantity);
    private final PrimaryButton saveButton;
    private final FlatButton clearButton;
    private final ItemIconFactory icons;
    private final Map<ItemMatch, Chip> matchChips = new EnumMap<>(ItemMatch.class);
    private final Chip asItemChip = new Chip("Item", "Withdraw the item itself", () -> setNoted(false));
    private final Chip asNoteChip = new Chip("Note", "Withdraw it as a bank note", () -> setNoted(true));
    private boolean noted;
    private final JPanel alternativeRows = Ui.column(Ui.SMALL_GAP);
    private final ItemPicker alternativePicker;
    private final FlatButton addAlternativeButton;

    private ItemId item;
    private ItemMatch match;
    private final List<ItemId> alternatives = new ArrayList<>();

    /** An editor for a slot that is still empty. */
    static SlotEditorForm forEmptySlot(SlotRef ref, ItemIconFactory icons, ItemSelectorFactory selectors, Listener listener) {
        return new SlotEditorForm(ref, null, icons, selectors, listener);
    }

    /** An editor pre-filled with the slot's current item. */
    static SlotEditorForm forItem(SlotRef ref, SetupItem existing, ItemIconFactory icons, ItemSelectorFactory selectors, Listener listener) {
        return new SlotEditorForm(ref, Objects.requireNonNull(existing, "existing"), icons, selectors, listener);
    }

    private SlotEditorForm(SlotRef ref,
                           SetupItem existing,
                           ItemIconFactory icons,
                           ItemSelectorFactory selectors,
                           Listener listener) {
        this.ref = Objects.requireNonNull(ref, "ref");
        this.listener = Objects.requireNonNull(listener, "listener");
        this.icons = Objects.requireNonNull(icons, "icons");
        this.item = existing == null ? null : existing.id();
        this.match = existing == null ? ItemMatch.DEFAULT : existing.match();
        this.noted = existing != null && existing.noted();
        if (existing != null) {
            alternatives.addAll(existing.alternatives());
        }
        this.picker = new ItemPicker(icons, selectors, resolved -> chooseItem(resolved.id()), this::forgetItem);
        this.alternativePicker = new ItemPicker(icons, selectors, resolved -> addAlternative(resolved.id()), () -> {
        });
        this.addAlternativeButton = FlatButton.outlined(ActionIcon.ADD, "Add alternative", "Accept another item in this slot", this::showAlternativePicker);
        this.saveButton = new PrimaryButton("Save", "Put this item in the slot", this::save);
        this.clearButton = new FlatButton("Empty slot", "Remove the item from this slot", this::clear);

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        Help.anchor(this, HelpTopic.SLOT_PAGE);
        Help.describe(saveButton, HelpTopic.SLOT_SAVE);
        add(Ui.column(Ui.GAP,
                NavBar.create(ref.describe(), this::cancel),
                Help.describe(Ui.labelled("Item", picker), HelpTopic.SLOT_ITEM),
                Help.describe(Ui.labelled("Match", "how the bank is searched", matchChooser()), HelpTopic.SLOT_MATCH),
                Help.describe(Ui.labelled("Withdraw as", "notes for bulk trips", notedChooser()), HelpTopic.SLOT_NOTED),
                Help.describe(Ui.labelled("Also accept", "in order of preference", alternativesSection()), HelpTopic.SLOT_ALTERNATIVES),
                Help.describe(Ui.labelled("Amount", "blank follows the bank", Ui.column(Ui.SMALL_GAP, presets, quantityField())), HelpTopic.SLOT_AMOUNT),
                actions()), BorderLayout.NORTH);

        if (item != null) {
            picker.show(item);
        }
        quantityField.setText(existing != null && existing.hasQuantity() ? String.valueOf(existing.quantity().orElse(0)) : "");
        clearButton.setEnabled(existing != null);
        refreshMatch();
        refreshNoted();
        refreshAlternatives();
        refreshAmount();
    }

    boolean isNoted() {
        return noted;
    }

    void setNoted(boolean asNote) {
        noted = asNote;
        refreshNoted();
    }

    private JPanel notedChooser() {
        JPanel row = Ui.panel(new GridLayout(1, 2, Ui.SMALL_GAP, 0));
        row.add(asItemChip);
        row.add(asNoteChip);
        return row;
    }

    private void refreshNoted() {
        asItemChip.setChosen(!noted);
        asNoteChip.setChosen(noted);
    }

    ItemMatch chosenMatch() {
        return match;
    }

    void setMatch(ItemMatch newMatch) {
        match = Objects.requireNonNull(newMatch, "newMatch");
        refreshMatch();
    }

    List<ItemId> alternatives() {
        return List.copyOf(alternatives);
    }

    void addAlternative(ItemId alternative) {
        Objects.requireNonNull(alternative, "alternative");
        boolean isPrimary = alternative.equals(item);
        if (isPrimary || alternatives.contains(alternative) || alternatives.size() >= SetupItem.MAX_ALTERNATIVES) {
            alternativePicker.clear();
            return;
        }
        alternatives.add(alternative);
        alternativePicker.clear();
        alternativePicker.setVisible(false);
        refreshAlternatives();
    }

    void removeAlternative(ItemId alternative) {
        if (alternatives.remove(alternative)) {
            refreshAlternatives();
        }
    }

    Optional<ItemId> chosenItem() {
        return Optional.ofNullable(item);
    }

    OptionalInt enteredQuantity() {
        return enteredAmount();
    }

    void setEnteredQuantity(String text) {
        quantityField.setText(text);
    }

    boolean isSaveEnabled() {
        return saveButton.isEnabled();
    }

    void chooseItem(ItemId newItem) {
        item = Objects.requireNonNull(newItem, "newItem");
        picker.show(newItem);
        refreshSaveEnabled();
    }

    void save() {
        if (item == null || !isQuantityUsable()) {
            return;
        }
        listener.onSlotSet(ref, new SetupItem(item, enteredAmount().orElse(SetupItem.BANK_AMOUNT), match, alternatives, noted));
    }

    void clear() {
        listener.onSlotCleared(ref);
    }

    void cancel() {
        listener.onCancelled();
    }

    @Override
    public boolean requestFocusInWindow() {
        return picker.requestFocusInWindow();
    }

    /** One chip per rule, the rules that accept variants together on the top row. */
    private JPanel matchChooser() {
        JPanel rows = Ui.panel(new GridLayout(2, 2, Ui.SMALL_GAP, Ui.SMALL_GAP));
        for (ItemMatch rule : List.of(ItemMatch.ANY_VARIANT, ItemMatch.PREFER_THIS, ItemMatch.EMPTIEST_FIRST, ItemMatch.EXACT)) {
            Chip chip = new Chip(rule.displayName(), rule.description(), () -> setMatch(rule));
            matchChips.put(rule, chip);
            rows.add(chip);
        }
        return rows;
    }

    private JPanel alternativesSection() {
        alternativePicker.setVisible(false);
        JPanel footer = Ui.panel(new BorderLayout());
        footer.add(addAlternativeButton, BorderLayout.WEST);
        return Ui.column(Ui.SMALL_GAP, alternativeRows, alternativePicker, footer);
    }

    private void showAlternativePicker() {
        alternativePicker.setVisible(true);
        alternativePicker.requestFocusInWindow();
        revalidate();
    }

    private void refreshMatch() {
        matchChips.forEach((rule, chip) -> chip.setChosen(rule == match));
    }

    private void refreshAlternatives() {
        alternativeRows.removeAll();
        int rank = 1;
        for (ItemId alternative : alternatives) {
            AlternativeRow row = new AlternativeRow(rank++, alternative, icons, this::removeAlternative);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            alternativeRows.add(row);
        }
        alternativeRows.setVisible(!alternatives.isEmpty());
        addAlternativeButton.setEnabled(alternatives.size() < SetupItem.MAX_ALTERNATIVES);
        alternativeRows.revalidate();
        alternativeRows.repaint();
        revalidate();
        repaint();
    }

    private void forgetItem() {
        item = null;
        refreshSaveEnabled();
    }

    private boolean isQuantityUsable() {
        return quantityField.getText().isBlank() || enteredAmount().isPresent();
    }

    Optional<String> chosenPreset() {
        return presets.chosen();
    }

    private OptionalInt enteredAmount() {
        return QuantityText.parse(quantityField.getText());
    }

    private void refreshAmount() {
        presets.reflect(quantityField.getText());
        refreshSaveEnabled();
    }

    private void refreshSaveEnabled() {
        saveButton.setEnabled(item != null && isQuantityUsable());
    }

    private JPanel actions() {
        JPanel row = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        row.setBorder(BorderFactory.createEmptyBorder(Ui.SMALL_GAP, 0, 0, 0));
        row.add(saveButton, BorderLayout.CENTER);
        row.add(clearButton, BorderLayout.EAST);
        return row;
    }

    private TextInput quantityField() {
        quantityField.setToolTipText("How many to withdraw, such as 250, 10K, 2.5M or MAX; blank takes whatever the bank holds");
        quantityField.addActionListener(e -> save());
        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshAmount();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshAmount();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshAmount();
            }
        });
        return quantityField;
    }
}
