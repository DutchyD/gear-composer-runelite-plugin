package dev.dutchy.runelite.gear.guide;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.*;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** The small setups a tutorial walks through; tagged so leftovers can be found and removed. */
public final class SampleSetups {

    public static final String TAG = "guide-sample";

    private static final SetupItem WHIP = SetupItem.of(4151);
    private static final SetupItem HELM = SetupItem.of(10828);
    private static final SetupItem STAFF = SetupItem.of(11791);
    private static final SetupItem SHARKS = SetupItem.of(385, 4);
    private static final SetupItem PRAYER_POTIONS = SetupItem.of(2434, 2);
    private static final SetupItem COINS = SetupItem.of(995, 100_000);
    private static final SetupItem NATURE_RUNES = SetupItem.of(561, 500);
    private static final SetupItem RANARR = SetupItem.of(257);

    private SampleSetups() {
    }

    public static GearSetup of(SetupType type) {
        switch (Objects.requireNonNull(type, "type")) {
            case GEAR:
                GearContent melee = GearContent.empty()
                        .withEquipped(EquipmentSlot.WEAPON, WHIP)
                        .withEquipped(EquipmentSlot.HEAD, HELM)
                        .withInventory(ItemGrid.EMPTY.withSlot(0, SHARKS).withSlot(1, PRAYER_POTIONS).withSlot(27, COINS)
                                .withDivider(0, "Supplies").withDivider(6, "Coins"));
                return sample("Sample gear layout", melee)
                        .withVariant(0, new SetupVariant("Melee", melee))
                        .withAddedVariant(new SetupVariant("Mage", melee.withEquipped(EquipmentSlot.WEAPON, STAFF)))
                        .withSelectedVariant(0);
            case BANK:
                return sample("Sample bank layout", BankContent.empty()
                        .withLeft(ItemGrid.EMPTY.withSlot(0, SHARKS).withSlot(1, PRAYER_POTIONS).withDivider(0, "Supplies"))
                        .withRight(ItemGrid.EMPTY.withSlot(0, NATURE_RUNES).withSlot(1, RANARR)));
            default:
                return sample("Sample custom layout", CustomContent.empty(2)
                        .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, WHIP, EquipmentSlot.HEAD, HELM)).withName("Melee"))
                        .withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SHARKS).withSlot(1, PRAYER_POTIONS)).withName("Supplies"))
                        .withCell(CellRef.of(1, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, STAFF)).withName("Mage")));
        }
    }

    public static boolean isSample(GearSetup setup) {
        return Objects.requireNonNull(setup, "setup").meta().tags().contains(TAG);
    }

    /** The setup with the marker taken off, for a user who keeps it. */
    public static GearSetup kept(GearSetup sample) {
        return sample.withMeta(meta -> meta.withTags(without(meta.tags())));
    }

    private static GearSetup sample(String name, SetupContent content) {
        return GearSetup.named(name).withContent(content).withMeta(meta -> meta.withTags(Set.of(TAG)));
    }

    private static Set<String> without(Set<String> tags) {
        return tags.stream().filter(tag -> !TAG.equals(tag)).collect(Collectors.toSet());
    }
}
