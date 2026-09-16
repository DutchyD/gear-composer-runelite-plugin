package dev.dutchy.runelite.gear.account;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Owner;

import java.util.*;

/** Every owner worth a card: shared first, then every remembered or encountered account by name. */
public final class AccountRoster {

    private AccountRoster() {
    }

    public static List<AccountEntry> of(List<GearSection> sections, AccountDirectory directory, CurrentAccount current) {
        Objects.requireNonNull(sections, "sections");
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(current, "current");
        Map<String, Integer> counts = new HashMap<>();
        int shared = 0;
        for (GearSection section : sections) {
            for (GearSetup setup : section.setups()) {
                Optional<String> key = setup.owner().account();
                if (key.isPresent()) {
                    counts.merge(key.get(), 1, Integer::sum);
                } else {
                    shared++;
                }
            }
        }
        Map<String, String> names = new HashMap<>(directory.names());
        current.key().ifPresent(key -> names.putIfAbsent(key, current.displayName().orElse(AccountEntry.UNKNOWN_NAME)));
        counts.keySet().forEach(key -> names.putIfAbsent(key, AccountEntry.UNKNOWN_NAME));

        List<AccountEntry> entries = new ArrayList<>();
        entries.add(new AccountEntry(Owner.shared(), AccountEntry.SHARED_NAME, shared, false));
        names.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<String, String> e) -> e.getValue().toLowerCase(Locale.ROOT)).thenComparing(Map.Entry::getKey))
                .forEach(entry -> entries.add(new AccountEntry(Owner.account(entry.getKey()), entry.getValue(),
                        counts.getOrDefault(entry.getKey(), 0), current.key().filter(entry.getKey()::equals).isPresent())));
        return List.copyOf(entries);
    }
}
