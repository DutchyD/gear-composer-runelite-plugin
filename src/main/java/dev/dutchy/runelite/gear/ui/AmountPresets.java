package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.QuantityText;
import dev.dutchy.runelite.gear.content.SetupItem;
import lombok.Value;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** Quick picks for an amount field; whichever one matches the field's value is lit, however it got there. */
final class AmountPresets extends JPanel {

    static final String BANK = "BANK";

    @Value
    private static class Preset {
        String text;
        int value;

    }

    private static final List<Preset> PRESETS = List.of(
            new Preset(BANK, SetupItem.BANK_AMOUNT),
            new Preset("1", 1),
            new Preset("100", 100),
            new Preset("1K", 1_000),
            new Preset("10K", 10_000),
            new Preset("100K", 100_000),
            new Preset("1M", 1_000_000),
            new Preset(QuantityText.MAX_TEXT, QuantityText.MAX));

    private static final int COLUMNS = 4;

    private final List<Chip> chips = new ArrayList<>();

    AmountPresets(Consumer<String> onPick) {
        Objects.requireNonNull(onPick, "onPick");
        setLayout(new GridLayout(0, COLUMNS, Ui.SMALL_GAP, Ui.SMALL_GAP));
        setOpaque(false);
        for (Preset preset : PRESETS) {
            String typed = preset.value() == SetupItem.BANK_AMOUNT ? "" : preset.text();
            Chip chip = new Chip(preset.text(), tooltip(preset), () -> onPick.accept(typed));
            chips.add(chip);
            add(chip);
        }
        reflect("");
    }

    /** Lights the preset whose value the text means, if any. */
    void reflect(String text) {
        boolean blank = text == null || text.isBlank();
        int value = QuantityText.parse(text).orElse(SetupItem.BANK_AMOUNT);
        for (int i = 0; i < chips.size(); i++) {
            int presetValue = PRESETS.get(i).value();
            chips.get(i).setChosen(presetValue == SetupItem.BANK_AMOUNT ? blank : presetValue == value);
        }
    }

    Optional<String> chosen() {
        return chips.stream().filter(Chip::isChosen).map(Chip::getText).findFirst();
    }

    private static String tooltip(Preset preset) {
        if (preset.value() == SetupItem.BANK_AMOUNT) {
            return "Take whatever the bank holds";
        }
        if (preset.value() == QuantityText.MAX) {
            return "The largest stack the game allows";
        }
        return "Withdraw " + preset.value();
    }
}
