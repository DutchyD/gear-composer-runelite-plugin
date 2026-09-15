package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The stacks the bank holds that satisfy one slot, in the order the slot prefers them: the first is
 * drawn, the rest are the other doses or charges the player could switch to.
 */
@Value
public class SlotSupply {

    /** One held stack of one family member. */
    @Value
    public static class Stack {
        ItemId id;
        int count;

        public Stack(ItemId id, int count) {
            this.id = Objects.requireNonNull(id, "id");
            if (count < 1) {
                throw new IllegalArgumentException("A held stack has at least one, got " + count);
            }
            this.count = count;
        }
    }

    public static final SlotSupply NONE = new SlotSupply(List.of());

    List<Stack> stacks;

    public SlotSupply(List<Stack> stacks) {
        this.stacks = List.copyOf(Objects.requireNonNull(stacks, "stacks"));
    }

    public static SlotSupply of(Stack... stacks) {
        return new SlotSupply(List.of(stacks));
    }

    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    /** The stack drawn in the slot. */
    public Optional<Stack> shown() {
        return stacks.stream().findFirst();
    }

    public int shownCount() {
        return shown().map(Stack::count).orElse(0);
    }

    /** Every stack across the family. */
    public int total() {
        return stacks.stream().mapToInt(Stack::count).sum();
    }

    /** The stacks the player could switch the slot to. */
    public List<Stack> others() {
        return stacks.size() <= 1 ? List.of() : stacks.subList(1, stacks.size());
    }

    public boolean hasOthers() {
        return stacks.size() > 1;
    }

    /** The same supply with the given stack drawn; unchanged when the bank does not hold it. */
    public SlotSupply showing(ItemId id) {
        Objects.requireNonNull(id, "id");
        List<Stack> reordered = new ArrayList<>(stacks);
        Optional<Stack> chosen = reordered.stream().filter(stack -> stack.id().equals(id)).findFirst();
        if (chosen.isEmpty()) {
            return this;
        }
        reordered.remove(chosen.get());
        reordered.add(0, chosen.get());
        return new SlotSupply(reordered);
    }
}
