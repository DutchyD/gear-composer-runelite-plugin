package dev.dutchy.runelite.gear.requirements;

import dev.dutchy.runelite.gear.Spellbook;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
public final class RuneLiteSpellbook implements CurrentSpellbook {

    private final Client client;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private volatile Spellbook current;

    @Inject
    public RuneLiteSpellbook(Client client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == VarbitID.SPELLBOOK) {
            refresh();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        refresh();
    }

    /** Must run on the client thread. */
    public void refresh() {
        Spellbook now = client.getGameState() == GameState.LOGGED_IN
                ? Spellbook.fromGameValue(client.getVarbitValue(VarbitID.SPELLBOOK)).orElse(null)
                : null;
        if (now == current) {
            return;
        }
        current = now;
        EdtDispatch.onEdt(() -> listeners.forEach(Runnable::run));
    }

    @Override
    public Optional<Spellbook> current() {
        return Optional.ofNullable(current);
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }
}
