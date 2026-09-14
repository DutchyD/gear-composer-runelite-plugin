package dev.dutchy.runelite.libs.ui.item;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class StaticItemResolver implements ItemResolver {

    private final Map<ItemId, ResolvedItem> byId;
    private final Map<String, ResolvedItem> byName;
    private final ItemNameMatcher matcher;

    public StaticItemResolver(Collection<ResolvedItem> items) {
        this(items, new BestNameMatch());
    }

    public StaticItemResolver(Collection<ResolvedItem> items, ItemNameMatcher matcher) {
        Objects.requireNonNull(items, "items");
        this.matcher = Objects.requireNonNull(matcher, "matcher");
        this.byId = new HashMap<>();
        this.byName = new HashMap<>();
        for (ResolvedItem item : items) {
            byId.put(item.id(), item);
            byName.putIfAbsent(item.name().toLowerCase(Locale.ROOT), item);
        }
    }

    public static StaticItemResolver of(ResolvedItem... items) {
        return new StaticItemResolver(List.of(items));
    }

    @Override
    public CompletableFuture<Optional<ResolvedItem>> resolve(ItemReference reference) {
        Objects.requireNonNull(reference, "reference");
        Optional<ResolvedItem> result = reference instanceof ItemReference.ById
                ? Optional.ofNullable(byId.get(((ItemReference.ById) reference).id()))
                : resolveName(((ItemReference.ByName) reference).name());
        return CompletableFuture.completedFuture(result);
    }

    private Optional<ResolvedItem> resolveName(String name) {
        ResolvedItem exact = byName.get(name.toLowerCase(Locale.ROOT));
        if (exact != null) {
            return Optional.of(exact);
        }
        return matcher.bestMatch(name, List.copyOf(byId.values()));
    }
}
