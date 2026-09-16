package dev.dutchy.runelite.gear.requirements;

import dev.dutchy.runelite.gear.Prayer;
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
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/** Follows the game's quick-prayer selection bits. */
@Singleton
public final class RuneLiteQuickPrayers implements CurrentQuickPrayers {

    private final Client client;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private volatile Set<Prayer> selected;

    @Inject
    public RuneLiteQuickPrayers(Client client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == VarbitID.QUICKPRAYER_SELECTED) {
            refresh();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        refresh();
    }

    /** Must run on the client thread. */
    public void refresh() {
        Set<Prayer> now = client.getGameState() == GameState.LOGGED_IN
                ? Set.copyOf(Prayer.fromQuickPrayerBits(client.getVarbitValue(VarbitID.QUICKPRAYER_SELECTED)))
                : null;
        if (Objects.equals(now, selected)) {
            return;
        }
        selected = now;
        EdtDispatch.onEdt(() -> listeners.forEach(Runnable::run));
    }

    @Override
    public Optional<Set<Prayer>> selected() {
        return Optional.ofNullable(selected);
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }
}
