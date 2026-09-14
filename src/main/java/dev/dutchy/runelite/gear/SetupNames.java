package dev.dutchy.runelite.gear;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Picks a name for a copy that does not clash with its neighbours: "Vorkath (2)", "Vorkath (3)". */
public final class SetupNames {

    private static final Pattern NUMBERED = Pattern.compile("^(.*?) \\((\\d+)\\)$");

    private SetupNames() {
    }

    public static String copyOf(String original, Collection<String> taken) {
        return copyOf(original, taken, GearSetup.MAX_NAME_LENGTH);
    }

    /** The same for names with another length limit, such as a variant's. */
    public static String copyOf(String original, Collection<String> taken, int maxLength) {
        Objects.requireNonNull(original, "original");
        Objects.requireNonNull(taken, "taken");
        Matcher matcher = NUMBERED.matcher(original);
        String base = matcher.matches() ? matcher.group(1) : original;
        for (int number = 2; ; number++) {
            String candidate = fit(base, " (" + number + ")", maxLength);
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    private static String fit(String base, String suffix, int maxLength) {
        int room = maxLength - suffix.length();
        return (base.length() > room ? base.substring(0, room).stripTrailing() : base) + suffix;
    }
}
