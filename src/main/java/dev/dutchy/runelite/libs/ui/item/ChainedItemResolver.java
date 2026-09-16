package dev.dutchy.runelite.libs.ui.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ChainedItemResolver implements ItemResolver {

    private final List<ItemResolver> delegates;

    public ChainedItemResolver(List<ItemResolver> delegates) {
        this.delegates = List.copyOf(Objects.requireNonNull(delegates, "delegates"));
        if (this.delegates.isEmpty()) {
            throw new IllegalArgumentException("At least one delegate resolver is required");
        }
    }

    public static ChainedItemResolver of(ItemResolver first, ItemResolver... rest) {
        ItemResolver[] all = new ItemResolver[rest.length + 1];
        all[0] = first;
        System.arraycopy(rest, 0, all, 1, rest.length);
        return new ChainedItemResolver(List.of(all));
    }

    @Override
    public CompletableFuture<Optional<ResolvedItem>> resolve(ItemReference reference) {
        Objects.requireNonNull(reference, "reference");
        return resolveFrom(0, reference);
    }

    private CompletableFuture<Optional<ResolvedItem>> resolveFrom(int index, ItemReference reference) {
        if (index >= delegates.size()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return delegates.get(index).resolve(reference).thenCompose(result ->
                result.isPresent()
                        ? CompletableFuture.completedFuture(result)
                        : resolveFrom(index + 1, reference));
    }
}
