package dev.dutchy.runelite.gear.ledger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Optional;

/** What the game data says about one item: price in coins, weight in kilograms, and bonuses when worn. */
@Value
@Accessors(fluent = true)
public class ItemFacts {
    int price;
    double weight;
    @Getter(AccessLevel.NONE)
    EquipmentStats equipment;

    public ItemFacts(int price, double weight, EquipmentStats equipment) {
        if (price < 0) {
            throw new IllegalArgumentException("Price must not be negative, got " + price);
        }
        this.price = price;
        this.weight = weight;
        this.equipment = equipment;
    }

    public static ItemFacts unworn(int price, double weight) {
        return new ItemFacts(price, weight, null);
    }

    public Optional<EquipmentStats> equipment() {
        return Optional.ofNullable(equipment);
    }
}
