package dev.dutchy.runelite.gear.bank;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SlotPoolTest {

    /** Every slot is equal to every other, so only identity can tell them apart. */
    private static final class Slot {
        private final String name;

        Slot(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Slot;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final SlotPool<Slot> pool = new SlotPool<>();
    private final Slot whip = new Slot("whip");
    private final Slot shark = new Slot("shark");
    private final Slot secondShark = new Slot("shark again");

    @Test
    void theSlotShowingAnItemGoesToThatItemAndOnlyOnce() {
        pool.add(4151, whip);

        assertSame(whip, pool.claim(4151).orElseThrow());
        assertEquals(Optional.empty(), pool.claim(4151), "a slot is claimed once");
        assertEquals(Optional.empty(), pool.claim(385), "and only for the item it shows");
    }

    @Test
    void aTemplateStaysAvailableAfterItsSlotIsClaimed() {
        pool.add(4151, whip);
        pool.claim(4151);

        assertSame(whip, pool.template(4151).orElseThrow(), "a twin still needs an appearance to copy");
        assertEquals(Optional.empty(), pool.template(385));
    }

    @Test
    void theFirstSlotOfferedForAnItemIsItsTemplate() {
        pool.add(385, shark);
        pool.add(385, secondShark);

        assertSame(shark, pool.template(385).orElseThrow());
        assertSame(shark, pool.claim(385).orElseThrow());
        assertSame(secondShark, pool.spare().orElseThrow(), "the other stack is still borrowable");
    }

    @Test
    void aClaimedSlotIsNeverBorrowedAsASpare() {
        pool.add(4151, whip);
        pool.add(385, shark);
        pool.claim(385);

        assertSame(whip, pool.spare().orElseThrow(), "the claimed slot is skipped");
        assertEquals(Optional.empty(), pool.spare(), "and nothing is left");
    }

    @Test
    void sparesComeBackInTheOrderTheyWereOffered() {
        pool.add(4151, whip);
        pool.add(385, shark);
        pool.add(385, secondShark);

        assertSame(whip, pool.spare().orElseThrow());
        assertSame(shark, pool.spare().orElseThrow());
        assertSame(secondShark, pool.spare().orElseThrow());
        assertEquals(Optional.empty(), pool.spare());
    }

    @Test
    void slotsThatCompareEqualAreStillTreatedAsDifferentSlots() {
        pool.add(385, shark);
        pool.add(385, secondShark);

        assertEquals(shark, secondShark, "the test slots deliberately compare equal");
        assertSame(shark, pool.claim(385).orElseThrow());
        assertSame(secondShark, pool.spare().orElseThrow(), "equality must not collapse them into one");
    }
}
