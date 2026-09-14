package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.SetupId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActiveSetupTest {

    private static final SetupId FIRST = SetupId.random();
    private static final SetupId SECOND = SetupId.random();

    private ActiveSetup active;

    @BeforeEach
    void setUp() {
        active = new ActiveSetup();
    }

    @Test
    void nothingIsActiveToStart() {
        assertTrue(active.current().isEmpty());
        assertFalse(active.isActive(FIRST));
    }

    @Test
    void togglingTurnsASetupOnAndOffAgain() {
        assertEquals(Optional.of(FIRST), active.toggle(FIRST));
        assertTrue(active.isActive(FIRST));

        assertEquals(Optional.empty(), active.toggle(FIRST));
        assertFalse(active.isActive(FIRST));
    }

    @Test
    void togglingAnotherSetupReplacesTheActiveOne() {
        active.toggle(FIRST);
        active.toggle(SECOND);

        assertTrue(active.isActive(SECOND));
        assertFalse(active.isActive(FIRST));
    }

    @Test
    void clearingTurnsEverythingOff() {
        active.toggle(FIRST);
        active.clear();
        assertTrue(active.current().isEmpty());
    }

    @Test
    void listenersSeeEveryChange() {
        List<Optional<SetupId>> seen = new ArrayList<>();
        active.addListener(() -> seen.add(active.current()));

        active.toggle(FIRST);
        active.toggle(FIRST);
        active.toggle(SECOND);
        active.clear();

        assertEquals(List.of(Optional.of(FIRST), Optional.empty(), Optional.of(SECOND), Optional.empty()), seen);
    }

    @Test
    void clearingWhenNothingIsActiveTellsNobody() {
        List<Optional<SetupId>> seen = new ArrayList<>();
        active.addListener(() -> seen.add(active.current()));
        active.clear();
        assertTrue(seen.isEmpty());
    }
}
