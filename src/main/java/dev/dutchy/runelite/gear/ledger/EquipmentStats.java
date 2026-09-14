package dev.dutchy.runelite.gear.ledger;

import lombok.Value;
import lombok.experimental.Accessors;

/** The summed equipment bonuses the game shows on the worn-equipment screen. */
@Value
@Accessors(fluent = true)
public class EquipmentStats {
    int stabAttack;
    int slashAttack;
    int crushAttack;
    int magicAttack;
    int rangedAttack;
    int stabDefence;
    int slashDefence;
    int crushDefence;
    int magicDefence;
    int rangedDefence;
    int strength;
    int rangedStrength;
    double magicDamage;
    int prayer;
    int attackSpeed;

    public static final EquipmentStats ZERO = new EquipmentStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    /** Sums every bonus; attack speed comes from whichever side carries a weapon. */
    public EquipmentStats plus(EquipmentStats other) {
        return new EquipmentStats(
                stabAttack + other.stabAttack, slashAttack + other.slashAttack, crushAttack + other.crushAttack,
                magicAttack + other.magicAttack, rangedAttack + other.rangedAttack,
                stabDefence + other.stabDefence, slashDefence + other.slashDefence, crushDefence + other.crushDefence,
                magicDefence + other.magicDefence, rangedDefence + other.rangedDefence,
                strength + other.strength, rangedStrength + other.rangedStrength, magicDamage + other.magicDamage,
                prayer + other.prayer, attackSpeed != 0 ? attackSpeed : other.attackSpeed);
    }

    public EquipmentStats minus(EquipmentStats other) {
        return new EquipmentStats(
                stabAttack - other.stabAttack, slashAttack - other.slashAttack, crushAttack - other.crushAttack,
                magicAttack - other.magicAttack, rangedAttack - other.rangedAttack,
                stabDefence - other.stabDefence, slashDefence - other.slashDefence, crushDefence - other.crushDefence,
                magicDefence - other.magicDefence, rangedDefence - other.rangedDefence,
                strength - other.strength, rangedStrength - other.rangedStrength, magicDamage - other.magicDamage,
                prayer - other.prayer, attackSpeed - other.attackSpeed);
    }

    public boolean isZero() {
        return equals(ZERO);
    }
}
