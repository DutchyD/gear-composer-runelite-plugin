package dev.dutchy.runelite.gear.bank;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

/** A slot as it is currently drawn in the bank: the plan behind it and where the drawn item really sits. */
@Value
@Accessors(fluent = true)
public class DrawnSlot {
    BankSlotPlan plan;
    int bankIndex;

    public DrawnSlot(BankSlotPlan plan, int bankIndex) {
        this.plan = Objects.requireNonNull(plan, "plan");
        this.bankIndex = bankIndex;
    }

    /** How many the bank holds across the whole family, which is what the slot reports. */
    public int held() {
        return plan.supply().total();
    }

    /** How many are in the drawn stack alone, which is what a withdraw can reach. */
    public int shownHeld() {
        return plan.supply().shownCount();
    }

    /** How many the setup asks for, none when it takes whatever the bank holds. */
    public int required() {
        return plan.item().quantity().orElse(0);
    }

    /** True when the setup asks for more than the bank has across the family. */
    public boolean isShort() {
        return held() < required();
    }

    /** True when the family covers the need but the drawn stack alone does not. */
    public boolean isShownShort() {
        return !isShort() && shownHeld() < required();
    }

    public boolean inBank() {
        return bankIndex >= 0;
    }
}
