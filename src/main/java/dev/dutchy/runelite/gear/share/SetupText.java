package dev.dutchy.runelite.gear.share;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.gear.layout.*;
import dev.dutchy.runelite.gear.ledger.Ledger;
import dev.dutchy.runelite.gear.ledger.LedgerFormat;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** A setup as a Markdown post: one list per block, dividers as headings, the ledger as a footer. */
public final class SetupText {

    private SetupText() {
    }

    /** {@code ledger} is the footer of totals, null to leave it off. */
    public static String markdown(GearSetup setup, Function<ItemId, Optional<String>> names, Ledger ledger) {
        Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(names, "names");
        StringBuilder out = new StringBuilder();
        out.append("**").append(setup.name()).append("** · ").append(setup.type().displayName()).append('\n');
        for (LayoutBlock block : Layout.of(setup.content()).blocks()) {
            if (block.isEmpty()) {
                continue;
            }
            String title = block.accept(new LayoutBlock.Visitor<>() {
                @Override
                public String equipment(EquipmentBlock equipment) {
                    return equipment.title();
                }

                @Override
                public String grid(GridBlock grid) {
                    return grid.title();
                }
            });
            out.append('\n').append(titleFor(block, title)).append('\n');
            appendSlots(out, block, names);
        }
        Optional.ofNullable(ledger).ifPresent(totals -> {
            out.append("\nValue ").append(LedgerFormat.coins(totals.value()));
            if (totals.openEnded()) {
                out.append(" plus bank amounts");
            }
            out.append(" · Weight ").append(LedgerFormat.weight(totals.weight())).append('\n');
        });
        return out.toString();
    }

    /** Custom cells are named after their place in the grid as well as their label. */
    private static String titleFor(LayoutBlock block, String title) {
        return block.labelRow().isPresent()
                ? "Row " + (block.band() + 1) + " " + (block.side() == BankSide.LEFT ? "left" : "right") + " · " + title
                : title;
    }

    private static void appendSlots(StringBuilder out, LayoutBlock block, Function<ItemId, Optional<String>> names) {
        Optional<ItemGrid> grid = block instanceof GridBlock ? Optional.of(((GridBlock) block).grid()) : Optional.empty();
        int index = 0;
        for (LayoutBlock.Slot slot : block.slots()) {
            if (grid.isPresent() && index % ItemGrid.COLUMNS == 0) {
                grid.get().dividersAbove(index / ItemGrid.COLUMNS).forEach(divider -> out.append("— ").append(divider.label()).append(" —\n"));
            }
            index++;
            if (slot.item().isEmpty()) {
                continue;
            }
            String prefix = block instanceof EquipmentBlock
                    ? ((SlotRef.Equipment) unwrap(slot.ref())).slot().displayName() + ": " : "";
            out.append("- ").append(prefix).append(describe(slot.item().get(), names)).append('\n');
        }
    }

    private static SlotRef unwrap(SlotRef ref) {
        if (ref instanceof SlotRef.Cell) {
            return ((SlotRef.Cell) ref).inner();
        }
        return ref;
    }

    private static String describe(SetupItem item, Function<ItemId, Optional<String>> names) {
        String name = names.apply(item.id()).orElse("Item " + item.id().value());
        String amount = item.quantity().isPresent() ? " ×" + item.quantity().getAsInt() : "";
        String note = item.noted() ? " (noted)" : "";
        return name + amount + note;
    }
}
