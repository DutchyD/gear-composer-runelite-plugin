package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The charges or doses an item variant carries, read from the "(n)" the game puts at the end of its name. */
public interface ItemCharges {

    Pattern TRAILING_COUNT = Pattern.compile("\\((\\d+)\\)\\s*$");

    /** Empty when the item has no charge count in its name. */
    OptionalInt chargesOf(ItemId item);

    static ItemCharges none() {
        return item -> OptionalInt.empty();
    }

    static OptionalInt parse(String name) {
        if (name == null) {
            return OptionalInt.empty();
        }
        Matcher matcher = TRAILING_COUNT.matcher(name);
        return matcher.find() ? OptionalInt.of(Integer.parseInt(matcher.group(1))) : OptionalInt.empty();
    }
}
