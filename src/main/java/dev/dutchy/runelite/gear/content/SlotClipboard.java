package dev.dutchy.runelite.gear.content;

import lombok.Value;
import lombok.experimental.Accessors;
import java.util.List;
import java.util.Objects;

/** Items lifted from slots, with where they came from so equipment goes back to matching slots. */
@Value
@Accessors(fluent = true)
public class SlotClipboard {
    List<Entry> entries;


    @Value
    @Accessors(fluent = true)
    public static class Entry {
        SlotRef from;
        SetupItem item;

        public Entry(SlotRef from, SetupItem item) {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(item, "item");
            this.from = from;
            this.item = item;
        }
    }

    public SlotClipboard(List<Entry> entries) {
        entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
        this.entries = entries;
    }

    public static SlotClipboard empty() {
        return new SlotClipboard(List.of());
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }
}
