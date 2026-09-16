package dev.dutchy.runelite.gear;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Keeps the setups an owner's list shows: an account sees its own plus the shared ones, the shared list sees shared only. */
public final class AccountFilter {

    private AccountFilter() {
    }

    public static List<GearSection> apply(List<GearSection> sections, Owner viewed) {
        Objects.requireNonNull(sections, "sections");
        Objects.requireNonNull(viewed, "viewed");
        return sections.stream()
                .map(section -> section.withSetups(section.setups().stream()
                        .filter(setup -> shows(viewed, setup.owner()))
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public static boolean shows(Owner viewed, Owner owner) {
        return owner.isShared() || owner.equals(viewed);
    }
}
