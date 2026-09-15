package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.layout.LayoutLabel;
import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The finished appearance of one bank build: which widget child shows what, the variant tabs, the
 * scroll arrows and the scroll height. Keyed by widget child, so two owners of one child cannot be
 * expressed. A screen writes the frame and hides every child it owns that the frame does not mention.
 */
@Value
public class BankFrame {

    public static final BankFrame NONE = new BankFrame(Map.of(), List.of(), List.of(), 0, 0, false);

    Map<Integer, SlotFace> slots;
    List<TabFace> tabs;
    List<ArrowFace> arrows;
    int nextChild;
    int scrollHeight;
    boolean rewind;

    public BankFrame(Map<Integer, SlotFace> slots, List<TabFace> tabs, List<ArrowFace> arrows,
                     int nextChild, int scrollHeight, boolean rewind) {
        this.slots = Map.copyOf(Objects.requireNonNull(slots, "slots"));
        this.tabs = List.copyOf(Objects.requireNonNull(tabs, "tabs"));
        this.arrows = List.copyOf(Objects.requireNonNull(arrows, "arrows"));
        for (Integer child : this.slots.keySet()) {
            if (child >= nextChild) {
                throw new IllegalArgumentException("Child " + child + " is a slot but the strip starts at " + nextChild);
            }
        }
        this.nextChild = nextChild;
        this.scrollHeight = scrollHeight;
        this.rewind = rewind;
    }

    public boolean isEmpty() {
        return slots.isEmpty() && tabs.isEmpty();
    }

    /** The face at a widget child, empty when the frame leaves that child alone. */
    public Optional<SlotFace> at(int child) {
        return Optional.ofNullable(slots.get(child));
    }

    /** Every child the frame speaks for, in order, so a screen can walk them predictably. */
    public List<Integer> children() {
        return slots.keySet().stream().sorted().collect(Collectors.toList());
    }

    /** What the overlays read, projected from the same build so the two cannot disagree. */
    public DrawnBank drawnBank(List<LayoutLabel> labels, BankRows rows, String searchQuery) {
        Map<Integer, DrawnSlot> drawn = new HashMap<>();
        slots.forEach((child, face) -> drawn.put(child, new DrawnSlot(face.plan(), face.bankIndex())));
        return new DrawnBank(!isEmpty(), drawn, labels, rows, searchQuery);
    }
}
