package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.SetupContent;
import dev.dutchy.runelite.gear.layout.*;
import dev.dutchy.runelite.gear.ledger.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** The collapsible ledger footer: value and weight tiles, a bonus table like the equipment screen, and a block per equipment cell. */
final class LedgerView extends JPanel {

    static final String TITLE = "Ledger";
    static final String UNKNOWN = "?";
    static final Color UP = new Color(120, 220, 140);
    static final Color DOWN = Ui.DANGER;
    static final Color FLAT = ColorScheme.MEDIUM_GRAY_COLOR;

    private final ItemFactsSource facts;
    private final JPanel body = Ui.column(Ui.SMALL_GAP);
    private final FlatButton toggle;
    private final List<String> lines = new ArrayList<>();
    private final SetupContent content;
    private boolean open;

    LedgerView(SetupContent content, ItemFactsSource facts, boolean open) {
        this.content = Objects.requireNonNull(content, "content");
        this.facts = Objects.requireNonNull(facts, "facts");
        this.open = open;
        this.toggle = new FlatButton(open ? ActionIcon.EXPANDED : ActionIcon.COLLAPSED, "Show or hide the ledger", this::toggleOpen);

        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(Ui.GAP, 0, 0, 0));

        JPanel header = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.OUTLINE),
                BorderFactory.createEmptyBorder(4, 2, 0, 0)));
        header.add(Ui.caption(TITLE), BorderLayout.CENTER);
        header.add(toggle, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        refresh();
    }

    boolean isOpen() {
        return open;
    }

    /** The same figures as text, one line each, for anything that cannot look at the tiles. */
    List<String> lines() {
        return List.copyOf(lines);
    }

    /** Recomputes from the facts known right now. */
    void refresh() {
        Ledger ledger = Ledger.of(content, facts);
        lines.clear();
        body.removeAll();

        boolean valueUnknown = ledger.unknownItems() == ledger.itemCount() && ledger.itemCount() > 0;
        String value = (valueUnknown ? UNKNOWN : LedgerFormat.coins(ledger.value())) + (ledger.openEnded() ? " +bank" : "");
        String weight = LedgerFormat.weight(ledger.weight());
        lines.add("Value " + value.replace(" +bank", " plus bank amounts") + "   Weight " + weight);
        addIfOpen(tiles(tile("Value", value, ledger.openEnded() ? "plus bank amounts" : null), tile("Weight", weight, null)));

        if (!ledger.stats().isZero()) {
            lines.addAll(statLines(ledger.stats()));
            addIfOpen(table(ledger.stats()));
        }
        Layout layout = Layout.of(content);
        for (LayoutBlock block : layout.blocks()) {
            block.accept(new LayoutBlock.Visitor<Void>() {
                @Override
                public Void equipment(EquipmentBlock equipment) {
                    if (equipment.countsWornBonuses() || equipment.isEmpty()) {
                        return null;
                    }
                    EquipmentStats stats = Ledger.ofEquipment(equipment.equipment(), facts).stats();
                    String where = "Row " + (equipment.band() + 1) + (equipment.side() == BankSide.LEFT ? " left" : " right");
                    lines.add(equipment.title() + " (" + where + ")");
                    lines.addAll(statLines(stats));
                    addIfOpen(block(equipment.title() + " · " + where));
                    addIfOpen(table(stats));
                    return null;
                }

                @Override
                public Void grid(GridBlock grid) {
                    return null;
                }
            });
        }
        if (ledger.hasUnknownItems()) {
            String note = ledger.unknownItems() == 1 ? "1 item has no stat data yet" : ledger.unknownItems() + " items have no stat data yet";
            lines.add(note);
            addIfOpen(Ui.note(note));
        }
        body.revalidate();
        body.repaint();
    }

    void setOpen(boolean shouldOpen) {
        open = shouldOpen;
        toggle.setIcon(open ? ActionIcon.EXPANDED : ActionIcon.COLLAPSED);
        refresh();
    }

    private void toggleOpen() {
        setOpen(!open);
    }

    private void addIfOpen(JComponent component) {
        if (open) {
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(component);
        }
    }

    private static JPanel tiles(JComponent left, JComponent right) {
        JPanel row = Ui.panel(new GridLayout(1, 2, Ui.SMALL_GAP, 0));
        row.add(left);
        row.add(right);
        return row;
    }

    /** A caption over a large figure, with a small note under it when there is one. */
    private static Card tile(String caption, String figure, String note) {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, 1));
        card.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        JLabel value = new JLabel(figure);
        value.setFont(FontManager.getRunescapeBoldFont());
        value.setForeground(ColorScheme.BRAND_ORANGE);
        card.add(Ui.caption(caption), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        if (note != null) {
            card.add(Ui.hint(note), BorderLayout.SOUTH);
        }
        return card;
    }

    /** Attack and defence by style, then the rest, each figure under its caption. */
    private static JPanel table(EquipmentStats stats) {
        Card card = new Card();
        card.setLayout(new GridLayout(0, 1, 0, 3));
        card.setBorder(BorderFactory.createEmptyBorder(4, 6, 5, 6));
        for (LedgerRows.Row row : LedgerRows.of(stats)) {
            JPanel block = Ui.panel(new BorderLayout(0, 1));
            block.add(Ui.caption(row.label()), BorderLayout.NORTH);
            JPanel cells = Ui.panel(new GridLayout(2, row.cells().size(), 2, 0));
            for (LedgerRows.Cell cell : row.cells()) {
                cells.add(centered(Ui.hint(cell.shortCaption())));
            }
            for (LedgerRows.Cell cell : row.cells()) {
                JLabel figure = new JLabel(cell.text(), SwingConstants.CENTER);
                figure.setFont(FontManager.getRunescapeSmallFont());
                figure.setForeground(colourOf(cell.tone()));
                cells.add(figure);
            }
            block.add(cells, BorderLayout.CENTER);
            card.add(block);
        }
        return card;
    }

    private static JPanel block(String title) {
        JPanel block = Ui.panel(new BorderLayout(0, 1));
        block.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 0));
        JLabel heading = new JLabel(title);
        heading.setFont(FontManager.getRunescapeSmallFont());
        heading.setForeground(ColorScheme.BRAND_ORANGE);
        block.add(heading, BorderLayout.NORTH);
        return block;
    }

    private static JLabel centered(JLabel label) {
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    static Color colourOf(LedgerRows.Tone tone) {
        switch (tone) {
            case UP:
                return UP;
            case DOWN:
                return DOWN;
            default:
                return FLAT;
        }
    }

    private static List<String> statLines(EquipmentStats stats) {
        return List.of(
                "Att st " + LedgerFormat.signed(stats.stabAttack()) + "  sl " + LedgerFormat.signed(stats.slashAttack())
                        + "  cr " + LedgerFormat.signed(stats.crushAttack()) + "  mg " + LedgerFormat.signed(stats.magicAttack())
                        + "  rg " + LedgerFormat.signed(stats.rangedAttack()),
                "Def st " + LedgerFormat.signed(stats.stabDefence()) + "  sl " + LedgerFormat.signed(stats.slashDefence())
                        + "  cr " + LedgerFormat.signed(stats.crushDefence()) + "  mg " + LedgerFormat.signed(stats.magicDefence())
                        + "  rg " + LedgerFormat.signed(stats.rangedDefence()),
                "Str " + LedgerFormat.signed(stats.strength()) + "  Rng " + LedgerFormat.signed(stats.rangedStrength())
                        + "  Mag " + LedgerFormat.signed(stats.magicDamage()) + "%  Pray " + LedgerFormat.signed(stats.prayer())
                        + (stats.attackSpeed() > 0 ? "  Speed " + stats.attackSpeed() : ""));
    }

    /** The bonuses that differ, largest first, or "no stat change". */
    static String describeDelta(EquipmentStats delta) {
        List<String> parts = new ArrayList<>();
        addIf(parts, "magic", delta.magicAttack());
        addIf(parts, "ranged", delta.rangedAttack());
        addIf(parts, "stab", delta.stabAttack());
        addIf(parts, "slash", delta.slashAttack());
        addIf(parts, "crush", delta.crushAttack());
        addIf(parts, "str", delta.strength());
        addIf(parts, "rng str", delta.rangedStrength());
        if (delta.magicDamage() != 0) {
            parts.add(LedgerFormat.signed(delta.magicDamage()) + "% mag dmg");
        }
        addIf(parts, "prayer", delta.prayer());
        return parts.isEmpty() ? "no stat change" : String.join(", ", parts.subList(0, Math.min(3, parts.size())));
    }

    private static void addIf(List<String> parts, String label, int value) {
        if (value != 0) {
            parts.add(LedgerFormat.signed(value) + " " + label);
        }
    }
}
