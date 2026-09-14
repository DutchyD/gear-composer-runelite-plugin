package dev.dutchy.runelite.gear;

import dev.dutchy.runelite.gear.content.CellKind;
import dev.dutchy.runelite.gear.content.LayoutCell;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.layout.EquipmentBlock;
import dev.dutchy.runelite.gear.layout.GridBlock;
import dev.dutchy.runelite.gear.layout.Layout;
import dev.dutchy.runelite.gear.layout.LayoutBlock;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Every equipment set or inventory in the book that could be copied into a cell of the given kind. */
public final class CellSources {

    private CellSources() {
    }

    /** One copyable part of a setup, already shaped as the cell it would become. */
    @Value
    @Accessors(fluent = true)
    public static class Source {
        String setupName;
        String partName;
        LayoutCell cell;

        public Source(String setupName, String partName, LayoutCell cell) {
            this.setupName = Objects.requireNonNull(setupName, "setupName");
            this.partName = Objects.requireNonNull(partName, "partName");
            this.cell = Objects.requireNonNull(cell, "cell");
        }

        public String label() {
            return setupName + " · " + partName;
        }
    }

    public static List<Source> of(List<GearSection> sections, CellKind kind, SetupId exclude) {
        Objects.requireNonNull(sections, "sections");
        Objects.requireNonNull(kind, "kind");
        List<Source> sources = new ArrayList<>();
        for (GearSection section : sections) {
            for (GearSetup setup : section.setups()) {
                if (setup.id().equals(exclude)) {
                    continue;
                }
                for (SetupVariant variant : setup.variants()) {
                    String setupName = setup.hasVariants() ? setup.name() + " (" + variant.name() + ")" : setup.name();
                    for (LayoutBlock block : Layout.of(variant.content()).blocks()) {
                        partOf(block, kind).ifPresent(cell -> sources.add(new Source(setupName, block.title(), cell)));
                    }
                }
            }
        }
        return List.copyOf(sources);
    }

    private static Optional<LayoutCell> partOf(LayoutBlock block, CellKind kind) {
        if (block.isEmpty()) {
            return Optional.empty();
        }
        return block.accept(new LayoutBlock.Visitor<>() {
            @Override
            public Optional<LayoutCell> equipment(EquipmentBlock equipment) {
                return kind == CellKind.EQUIPMENT ? Optional.of(LayoutCell.equipment(equipment.equipment())) : Optional.empty();
            }

            @Override
            public Optional<LayoutCell> grid(GridBlock grid) {
                return kind == CellKind.INVENTORY ? Optional.of(LayoutCell.inventory(grid.grid())) : Optional.empty();
            }
        });
    }
}
