package dev.dutchy.runelite.gear.bank;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

/**
 * What the bank shows at the moment a build starts, as the display needs to see it. The screen reads
 * this off the widget tree; a test hands one over directly.
 */
@Value
@Accessors(fluent = true)
public class BankSnapshot {

    public static final BankSnapshot NONE = new BankSnapshot(List.of(), 0, 0);

    List<Shown> shown;
    int firstSpareChild;
    int width;

    public BankSnapshot(List<Shown> shown, int firstSpareChild, int width) {
        this.shown = List.copyOf(Objects.requireNonNull(shown, "shown"));
        this.firstSpareChild = firstSpareChild;
        this.width = width;
    }

    /** One item the game already drew, at the widget child that is also its bank slot. */
    @Value
    @Accessors(fluent = true)
    public static class Shown {
        int child;
        int itemId;
        int quantity;
    }
}
