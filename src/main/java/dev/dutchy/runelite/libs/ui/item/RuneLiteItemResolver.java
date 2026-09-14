package dev.dutchy.runelite.libs.ui.item;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Resolves ids by reading item definitions once the game can supply them. Names are not its job;
 * chain it with a resolver that searches the item catalogue.
 *
 * <p>A task handed to the client thread that reports "not ready" is re-run on every client loop
 * iteration, so requests give up after {@link #DEFAULT_READY_TIMEOUT} rather than accumulating work
 * on the game thread.
 */
@Singleton
public final class RuneLiteItemResolver implements ItemResolver {

    private static final Logger log = LoggerFactory.getLogger(RuneLiteItemResolver.class);

    private static final String UNDEFINED_ITEM_NAME = "null";

    public static final Duration DEFAULT_READY_TIMEOUT = Duration.ofMinutes(2);

    private final Client client;
    private final ClientThread clientThread;
    private final Duration readyTimeout;

    @Inject
    public RuneLiteItemResolver(Client client, ClientThread clientThread) {
        this(client, clientThread, DEFAULT_READY_TIMEOUT);
    }

    public RuneLiteItemResolver(Client client, ClientThread clientThread, Duration readyTimeout) {
        this.client = Objects.requireNonNull(client, "client");
        this.clientThread = Objects.requireNonNull(clientThread, "clientThread");
        this.readyTimeout = Objects.requireNonNull(readyTimeout, "readyTimeout");
    }

    @Override
    public CompletableFuture<Optional<ResolvedItem>> resolve(ItemReference reference) {
        Objects.requireNonNull(reference, "reference");
        if (reference instanceof ItemReference.ById) {
            return resolveById(((ItemReference.ById) reference).id());
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    private CompletableFuture<Optional<ResolvedItem>> resolveById(ItemId id) {
        CompletableFuture<Optional<ResolvedItem>> future = new CompletableFuture<>();
        long deadline = System.nanoTime() + readyTimeout.toNanos();
        clientThread.invoke(() -> {
            if (!definitionsAvailable()) {
                if (System.nanoTime() < deadline) {
                    return false;
                }
                log.debug("Gave up resolving {}: the game never became ready", id);
                future.complete(Optional.empty());
                return true;
            }
            try {
                future.complete(toResolvedItem(id, client.getItemDefinition(id.value())));
            } catch (RuntimeException e) {
                future.completeExceptionally(e);
            }
            return true;
        });
        return future;
    }

    private boolean definitionsAvailable() {
        return client.getGameState().ordinal() >= GameState.LOGIN_SCREEN.ordinal();
    }

    private static Optional<ResolvedItem> toResolvedItem(ItemId id, ItemComposition composition) {
        if (composition == null) {
            return Optional.empty();
        }
        String name = composition.getName();
        if (name == null || name.isBlank() || UNDEFINED_ITEM_NAME.equals(name)) {
            return Optional.empty();
        }
        return Optional.of(new ResolvedItem(id, name));
    }
}
