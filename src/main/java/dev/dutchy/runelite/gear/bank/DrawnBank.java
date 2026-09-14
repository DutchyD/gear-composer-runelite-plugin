package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.layout.LayoutLabel;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** What the bank shows right now: each drawn slot by bank child index, the labels, where the rows sit, and the search in force. Safe to read from any thread. */
@Value
@Accessors(fluent = true)
public class DrawnBank {

    public static final DrawnBank NONE = new DrawnBank(false, Map.of(), List.of(), BankRows.plain(), "");

    boolean active;
    Map<Integer, DrawnSlot> slots;
    List<LayoutLabel> labels;
    BankRows rows;
    String searchQuery;

    public DrawnBank(boolean active, Map<Integer, DrawnSlot> slots, List<LayoutLabel> labels, BankRows rows, String searchQuery) {
        this.active = active;
        this.slots = Map.copyOf(Objects.requireNonNull(slots, "slots"));
        this.labels = List.copyOf(Objects.requireNonNull(labels, "labels"));
        this.rows = Objects.requireNonNull(rows, "rows");
        this.searchQuery = Objects.requireNonNull(searchQuery, "searchQuery");
    }

    /** What is drawn in the bank child at {@code childIndex}, if it is one of ours. */
    public Optional<DrawnSlot> at(int childIndex) {
        return Optional.ofNullable(slots.get(childIndex));
    }

    /** Whether the shown layout asks for anything as a note. */
    public boolean wantsNotes() {
        return slots.values().stream().anyMatch(slot -> slot.plan().item().noted());
    }
}
