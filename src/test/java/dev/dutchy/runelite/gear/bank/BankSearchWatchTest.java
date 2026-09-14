package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.activation.SetupActivator;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPreFired;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankSearchWatchTest {

    private final GearSetupBook book = new GearSetupBook();
    private final ActiveSetup active = new ActiveSetup();
    private final List<Boolean> cleared = new ArrayList<>();
    private final SetupActivator activator = new SetupActivator(book, active, new BankLayoutPlanner(), new BankLayoutApplier() {
        @Override
        public void apply(BankLayout layout) {
        }

        @Override
        public void clear() {
            cleared.add(Boolean.TRUE);
        }
    });
    private final BankSearchWatch watch = new BankSearchWatch(activator);

    @Test
    void openingTheSearchTurnsTheShownSetupOff() {
        SetupId id = book.addSetup("Vorkath").id();
        activator.show(id);
        assertTrue(activator.current().isPresent());

        watch.onScriptPreFired(new ScriptPreFired(ScriptID.BANKMAIN_SEARCH_TOGGLE));

        assertTrue(activator.current().isEmpty());
        assertEquals(1, cleared.size());
    }

    @Test
    void otherScriptsAndAnIdleBankAreLeftAlone() {
        watch.onScriptPreFired(new ScriptPreFired(ScriptID.BANKMAIN_SEARCH_TOGGLE));
        watch.onScriptPreFired(new ScriptPreFired(ScriptID.BANKMAIN_BUILD));
        assertTrue(cleared.isEmpty());

        activator.show(book.addSetup("Vorkath").id());
        watch.onScriptPreFired(new ScriptPreFired(ScriptID.BANKMAIN_BUILD));
        assertTrue(activator.current().isPresent());
    }
}
