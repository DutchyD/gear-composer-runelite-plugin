package dev.dutchy.runelite.gear.account;

import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Follows RuneLite's RuneScape profile, which changes on login and when switching characters. */
@Singleton
public final class RuneLiteAccount implements CurrentAccount {

    private final Client client;
    private final ConfigManager configManager;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private volatile String key;
    private volatile String displayName;

    @Inject
    public RuneLiteAccount(Client client, ConfigManager configManager) {
        this.client = Objects.requireNonNull(client, "client");
        this.configManager = Objects.requireNonNull(configManager, "configManager");
    }

    @Subscribe
    public void onRuneScapeProfileChanged(RuneScapeProfileChanged event) {
        refresh();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN || event.getGameState() == GameState.LOGIN_SCREEN
                || event.getGameState() == GameState.HOPPING) {
            refresh();
        }
    }

    /** The profile key and player name land a few ticks after login, so keep looking until both are known. */
    @Subscribe
    public void onGameTick(GameTick event) {
        if (client.getGameState() == GameState.LOGGED_IN && (key == null || displayName == null)) {
            refresh();
        }
    }

    @Override
    public Optional<String> key() {
        return Optional.ofNullable(key);
    }

    @Override
    public Optional<String> displayName() {
        return Optional.ofNullable(displayName);
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Re-reads who is logged in; must run on the client thread. */
    public void refresh() {
        String newKey = client.getGameState() == GameState.LOGGED_IN ? configManager.getRSProfileKey() : null;
        Player player = newKey == null ? null : client.getLocalPlayer();
        String newName = player == null ? null : player.getName();
        if (Objects.equals(newKey, key) && Objects.equals(newName, displayName)) {
            return;
        }
        key = newKey;
        displayName = newName;
        EdtDispatch.onEdt(() -> listeners.forEach(Runnable::run));
    }
}
