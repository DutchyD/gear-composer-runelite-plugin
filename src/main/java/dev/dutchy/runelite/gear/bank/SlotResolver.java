package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.ItemMatch;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.items.ItemCharges;
import dev.dutchy.runelite.gear.items.ItemNotes;
import dev.dutchy.runelite.gear.items.ItemVariants;
import dev.dutchy.runelite.libs.ui.item.ItemId;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Picks the concrete stacks the bank can offer a setup item: the item's family in the order its
 * match rule prefers, then each alternative's family. The first candidate the bank holds anything
 * of supplies the slot, and every held member of that family counts towards it.
 */
@Singleton
public final class SlotResolver {

    private final ItemVariants variants;
    private final ItemNotes notes;
    private final ItemCharges charges;

    @Inject
    public SlotResolver(ItemVariants variants, ItemNotes notes, ItemCharges charges) {
        this.variants = Objects.requireNonNull(variants, "variants");
        this.notes = Objects.requireNonNull(notes, "notes");
        this.charges = Objects.requireNonNull(charges, "charges");
    }

    public SlotResolver(ItemVariants variants, ItemNotes notes) {
        this(variants, notes, ItemCharges.none());
    }

    public SlotResolver(ItemVariants variants) {
        this(variants, ItemNotes.none());
    }

    public Optional<ItemId> resolve(SetupItem item, BankContents bank) {
        return supply(item, bank).shown().map(SlotSupply.Stack::id);
    }

    /** What the bank holds for the item: the held members of the first candidate family that has any. */
    public SlotSupply supply(SetupItem item, BankContents bank) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(bank, "bank");
        for (List<ItemId> family : families(item)) {
            List<SlotSupply.Stack> held = new ArrayList<>();
            for (ItemId member : family) {
                int count = bank.count(member);
                if (count > 0) {
                    held.add(new SlotSupply.Stack(member, count));
                }
            }
            if (!held.isEmpty()) {
                return new SlotSupply(held);
            }
        }
        return SlotSupply.NONE;
    }

    /** Every id that would satisfy the item, most preferred first. */
    public List<ItemId> acceptable(SetupItem item) {
        List<ItemId> acceptable = new ArrayList<>();
        families(item).forEach(family -> family.forEach(member -> addOnce(acceptable, member)));
        return List.copyOf(acceptable);
    }

    /** The item's family and then each alternative's, every family in the order the match rule prefers. */
    private List<List<ItemId>> families(SetupItem item) {
        Objects.requireNonNull(item, "item");
        List<List<ItemId>> families = new ArrayList<>();
        List<ItemId> seen = new ArrayList<>();
        for (ItemId listed : item.candidates()) {
            ItemId candidate = notes.unnotedOf(listed).orElse(listed);
            List<ItemId> family = new ArrayList<>();
            for (ItemId member : ordered(candidate, item.match())) {
                if (!seen.contains(member)) {
                    seen.add(member);
                    family.add(member);
                }
            }
            if (!family.isEmpty()) {
                families.add(family);
            }
        }
        return families;
    }

    private List<ItemId> ordered(ItemId candidate, ItemMatch match) {
        if (!match.acceptsVariants()) {
            return List.of(candidate);
        }
        List<ItemId> family = new ArrayList<>(variants.familyOf(candidate));
        switch (match) {
            case EMPTIEST_FIRST:
                family.sort(Comparator.comparingInt(this::chargesOrMax));
                return family;
            case PREFER_THIS:
                family.sort(Comparator.comparingInt(this::chargesOrMin).reversed());
                family.remove(candidate);
                family.add(0, candidate);
                return family;
            default:
                family.sort(Comparator.comparingInt(this::chargesOrMin).reversed());
                return family;
        }
    }

    /** Members with no count sort last when the fullest go first. */
    private int chargesOrMin(ItemId id) {
        return charges.chargesOf(id).orElse(Integer.MIN_VALUE);
    }

    /** Members with no count sort last when the emptiest go first. */
    private int chargesOrMax(ItemId id) {
        return charges.chargesOf(id).orElse(Integer.MAX_VALUE);
    }

    private static void addOnce(List<ItemId> into, ItemId id) {
        if (!into.contains(id)) {
            into.add(id);
        }
    }
}
