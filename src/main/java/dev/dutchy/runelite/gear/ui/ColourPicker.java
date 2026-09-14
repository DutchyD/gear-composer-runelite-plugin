package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.ColourLabel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Every colour label in a row, with the current one ringed. */
final class ColourPicker extends JPanel {

    private final List<ColourSwatch> swatches = new ArrayList<>();
    private ColourLabel chosen;

    ColourPicker(ColourLabel initial, Consumer<ColourLabel> onPick) {
        Objects.requireNonNull(onPick, "onPick");
        this.chosen = Objects.requireNonNull(initial, "initial");
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
        setOpaque(false);
        for (ColourLabel label : ColourLabel.values()) {
            ColourSwatch swatch = new ColourSwatch(label, label == initial, () -> {
                choose(label);
                onPick.accept(label);
            });
            swatches.add(swatch);
            add(swatch);
        }
    }

    ColourLabel chosen() {
        return chosen;
    }

    void choose(ColourLabel label) {
        chosen = Objects.requireNonNull(label, "label");
        swatches.forEach(swatch -> swatch.setChosen(swatch.label() == label));
    }
}
