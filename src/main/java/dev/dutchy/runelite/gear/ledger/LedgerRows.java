package dev.dutchy.runelite.gear.ledger;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** The worn bonuses laid out the way the game's equipment screen does: attack and defence by style, then the rest. */
public final class LedgerRows {

    public enum Tone { UP, DOWN, FLAT }

    @Value
    @Accessors(fluent = true)
    public static class Cell {
        String caption;
        String shortCaption;
        String text;
        Tone tone;

        public Cell(String caption, String shortCaption, String text, Tone tone) {
            this.caption = Objects.requireNonNull(caption, "caption");
            this.shortCaption = Objects.requireNonNull(shortCaption, "shortCaption");
            this.text = Objects.requireNonNull(text, "text");
            this.tone = Objects.requireNonNull(tone, "tone");
        }
    }

    @Value
    @Accessors(fluent = true)
    public static class Row {
        String label;
        List<Cell> cells;

        public Row(String label, List<Cell> cells) {
            this.label = Objects.requireNonNull(label, "label");
            this.cells = List.copyOf(Objects.requireNonNull(cells, "cells"));
        }
    }

    public static final List<String> STYLES = List.of("Stab", "Slash", "Crush", "Magic", "Range");

    private LedgerRows() {
    }

    public static List<Row> of(EquipmentStats stats) {
        Objects.requireNonNull(stats, "stats");
        return List.of(
                new Row("Attack", List.of(cell("Stab", "Stab", stats.stabAttack()), cell("Slash", "Slash", stats.slashAttack()), cell("Crush", "Crush", stats.crushAttack()),
                        cell("Magic", "Magic", stats.magicAttack()), cell("Range", "Range", stats.rangedAttack()))),
                new Row("Defence", List.of(cell("Stab", "Stab", stats.stabDefence()), cell("Slash", "Slash", stats.slashDefence()), cell("Crush", "Crush", stats.crushDefence()),
                        cell("Magic", "Magic", stats.magicDefence()), cell("Range", "Range", stats.rangedDefence()))),
                new Row("Other", List.of(cell("Strength", "Str", stats.strength()), cell("Ranged str", "Rng", stats.rangedStrength()),
                        new Cell("Magic dmg", "Mag", String.format(Locale.ROOT, "%+.1f%%", stats.magicDamage()).replace("+0.0%", "0%"), tone(stats.magicDamage())),
                        cell("Prayer", "Pray", stats.prayer()),
                        new Cell("Speed", "Spd", stats.attackSpeed() > 0 ? String.valueOf(stats.attackSpeed()) : "–", Tone.FLAT))));
    }

    private static Cell cell(String caption, String shortCaption, int value) {
        return new Cell(caption, shortCaption, value > 0 ? "+" + value : String.valueOf(value), tone(value));
    }

    private static Tone tone(double value) {
        return value > 0 ? Tone.UP : value < 0 ? Tone.DOWN : Tone.FLAT;
    }
}
