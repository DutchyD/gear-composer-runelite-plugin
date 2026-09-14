package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.SetupContent;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.gear.ledger.EquipmentStats;
import dev.dutchy.runelite.gear.ledger.Ledger;
import dev.dutchy.runelite.gear.ledger.LedgerFormat;
import dev.dutchy.runelite.gear.ledger.SetupDiff;
import dev.dutchy.runelite.libs.ui.icon.ItemIcon;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** Two setups side by side: what to pick, then every slot that differs and how the ledger moves. */
final class DiffPage extends JPanel {

    static final String WORN_NOW = "Worn now";
    static final String NO_CHANGES = "Nothing differs";
    static final String NO_CANDIDATES = "No other setup of this type to compare with";
    private static final int ICON = 24;

    /** Compare against another setup, another variant of this one, or what the player wears right now when the content is null. */
    @lombok.Value
    @lombok.experimental.Accessors(fluent = true)
    static class Target {
        String name;
        @lombok.Getter(lombok.AccessLevel.NONE)
        SetupContent content;
        @lombok.Getter(lombok.AccessLevel.NONE)
        GearSetup setup;

        static Target setup(GearSetup setup) {
            Objects.requireNonNull(setup, "setup");
            return new Target(setup.name(), setup.content(), setup);
        }

        static Target variant(SetupVariant variant) {
            Objects.requireNonNull(variant, "variant");
            return new Target(variant.name(), variant.content(), null);
        }

        static Target live() {
            return new Target(WORN_NOW, null, null);
        }

        Optional<SetupContent> content() {
            return Optional.ofNullable(content);
        }

        Optional<GearSetup> setup() {
            return Optional.ofNullable(setup);
        }

        boolean isLive() {
            return content == null;
        }
    }

    private final ItemIconFactory icons;
    private final JPanel rows = Ui.column(Ui.SMALL_GAP);
    private final JLabel heading = Ui.body("");
    private final List<Chip> chips = new ArrayList<>();
    private final List<String> rowTexts = new ArrayList<>();
    private final Consumer<Target> onPick;
    private Target chosen;

    /** Other variants of the subject come first, then setups of the same shape, then what is worn. */
    DiffPage(GearSetup subject, List<GearSetup> others, boolean liveOffered, ItemIconFactory icons,
             Consumer<Target> onPick, Runnable onBack) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(others, "others");
        List<Target> targets = new ArrayList<>();
        subject.variants().stream().filter(variant -> variant != subject.variant()).map(Target::variant).forEach(targets::add);
        others.stream().map(Target::setup).forEach(targets::add);
        this.icons = Objects.requireNonNull(icons, "icons");
        this.onPick = Objects.requireNonNull(onPick, "onPick");
        Objects.requireNonNull(onBack, "onBack");

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        Help.anchor(this, HelpTopic.COMPARE_PAGE);
        JPanel picker = Help.anchor(Ui.panel(new FlowLayout(FlowLayout.LEFT, Ui.SMALL_GAP, Ui.SMALL_GAP)), HelpTopic.COMPARE_TARGETS);
        for (Target target : targets) {
            Chip chip = Chip.sized(target.name(), "Compare with " + target.name(), () -> pick(target, onPick));
            chips.add(chip);
            picker.add(chip);
        }
        if (liveOffered) {
            Chip live = Chip.sized(WORN_NOW, "Compare with what you are wearing and carrying", () -> pick(Target.live(), onPick));
            chips.add(live);
            picker.add(live);
        }
        if (chips.isEmpty()) {
            picker.add(Ui.hint(NO_CANDIDATES));
        }
        heading.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        rows.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        add(Ui.column(Ui.GAP, NavBar.create("Compare " + subject.name(), onBack),
                Ui.labelled("Against", picker), heading, rows), BorderLayout.NORTH);
    }

    Optional<Target> chosen() {
        return Optional.ofNullable(chosen);
    }

    List<String> rowTexts() {
        return List.copyOf(rowTexts);
    }

    /** Shows a computed comparison; the header names both sides. */
    void show(String subjectName, String otherName, SetupDiff diff, Ledger before, Ledger after) {
        heading.setText(subjectName + " vs " + otherName);
        rows.removeAll();
        rowTexts.clear();
        if (diff.isEmpty()) {
            rows.add(Ui.hint(NO_CHANGES));
            rowTexts.add(NO_CHANGES);
        }
        for (SetupDiff.SlotChange change : diff.changes()) {
            rows.add(row(change));
        }
        rows.add(ledgerLine("Value", LedgerFormat.coins(before.value()), LedgerFormat.coins(after.value()),
                LedgerFormat.coinsDelta(after.value() - before.value())));
        rows.add(ledgerLine("Weight", LedgerFormat.weight(before.weight()), LedgerFormat.weight(after.weight()),
                LedgerFormat.weightDelta(after.weight() - before.weight())));
        EquipmentStats delta = after.stats().minus(before.stats());
        if (!delta.isZero()) {
            rows.add(ledgerLine("Stats", "", "", LedgerView.describeDelta(delta)));
        }
        rows.revalidate();
        rows.repaint();
    }

    /** Marks a target as chosen and asks for its comparison, as clicking its chip would. */
    void choose(Target target) {
        pick(target, onPick);
    }

    private void pick(Target target, Consumer<Target> onPick) {
        chosen = target;
        for (Chip chip : chips) {
            chip.setChosen(false);
        }
        int index = target.isLive() ? chips.size() - 1 : indexOf(target);
        if (index >= 0 && index < chips.size()) {
            chips.get(index).setChosen(true);
        }
        onPick.accept(target);
    }

    private int indexOf(Target target) {
        for (int i = 0; i < chips.size(); i++) {
            if (chips.get(i).getText().equals(target.name())) {
                return i;
            }
        }
        return -1;
    }

    private JComponent row(SetupDiff.SlotChange change) {
        Card card = new Card();
        card.setLayout(new BorderLayout(Ui.SMALL_GAP, 0));
        card.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel slot = Ui.hint(change.ref().describe());
        slot.setPreferredSize(new Dimension(78, ICON));

        JPanel sides = Ui.panel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        sides.add(sprite(change.before().orElse(null)));
        sides.add(Ui.hint("→"));
        sides.add(sprite(change.after().orElse(null)));

        String delta = change.quantityDelta().isPresent()
                ? LedgerFormat.signed(change.quantityDelta().getAsInt())
                : change.after().isPresent() && change.before().isEmpty() ? "added"
                : change.after().isEmpty() ? "removed" : "";
        JLabel deltaLabel = Ui.hint(delta);
        deltaLabel.setForeground(delta.startsWith("-") || delta.equals("removed") ? Ui.DANGER : ColorScheme.BRAND_ORANGE);
        rowTexts.add(change.ref().describe() + " " + delta);

        card.add(slot, BorderLayout.WEST);
        card.add(sides, BorderLayout.CENTER);
        card.add(deltaLabel, BorderLayout.EAST);
        card.setToolTipText(change.ref().describe());
        return card;
    }

    /** {@code item} is the side's item, null when that side has none. */
    private JComponent sprite(SetupItem item) {
        if (item == null) {
            JLabel empty = Ui.hint("—");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setPreferredSize(new Dimension(ICON, ICON));
            return empty;
        }
        ItemIcon icon = icons.icon(ICON, ICON);
        icon.setResolvedListener(resolved -> icon.setToolTipText(resolved.name() + quantity(item)));
        icon.setItem(ItemReference.byId(item.id()));
        icon.setToolTipText("Item " + item.id().value() + quantity(item));
        return icon;
    }

    private static String quantity(SetupItem item) {
        return item.quantity().isPresent() ? " ×" + item.quantity().getAsInt() : "";
    }

    private static JComponent ledgerLine(String label, String before, String after, String delta) {
        JPanel line = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        String middle = before.isEmpty() ? "" : before + " → " + after;
        line.add(Ui.hint(label), BorderLayout.WEST);
        line.add(Ui.body(middle), BorderLayout.CENTER);
        JLabel change = Ui.body(delta.isEmpty() ? "" : "(" + delta + ")");
        change.setForeground(delta.startsWith("-") ? Ui.DANGER : ColorScheme.BRAND_ORANGE);
        line.add(change, BorderLayout.EAST);
        return line;
    }
}
