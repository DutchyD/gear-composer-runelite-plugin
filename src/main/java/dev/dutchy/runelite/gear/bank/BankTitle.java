package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.requirements.RequirementWatch;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

/** Writes the active setup and its variant into the bank's title, or a requirement mismatch when there is one. */
public final class BankTitle {

    private static final String WARNING_COLOUR = "ff5c5c";
    private static final String SETUP_COLOUR = "ff981f";

    private final Client client;
    private final RequirementWatch watch;
    private final ActiveSetup active;
    private final GearSetupBook book;

    @Inject
    public BankTitle(Client client, RequirementWatch watch, ActiveSetup active, GearSetupBook book) {
        this.client = Objects.requireNonNull(client, "client");
        this.watch = Objects.requireNonNull(watch, "watch");
        this.active = Objects.requireNonNull(active, "active");
        this.book = Objects.requireNonNull(book, "book");
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() != ScriptID.BANKMAIN_BUILD) {
            return;
        }
        Widget title = client.getWidget(InterfaceID.Bankmain.TITLE);
        if (title == null) {
            return;
        }
        textFor(watch.warning().orElse(null), active.current().flatMap(book::setup).orElse(null)).ifPresent(title::setText);
    }

    /**
     * A warning in red, else the setup with its variant in orange, else nothing so the game's own
     * title stays. Both may be null.
     */
    static Optional<String> textFor(String warning, GearSetup setup) {
        if (warning != null) {
            return Optional.of(coloured(WARNING_COLOUR, warning));
        }
        return Optional.ofNullable(setup).map(shown -> coloured(SETUP_COLOUR, describe(shown)));
    }

    private static String coloured(String colour, String text) {
        return "<col=" + colour + ">" + text + "</col>";
    }

    /** "Vorkath · Mage" when the setup has variants, else just its name, ready for the game's renderer. */
    public static String describe(GearSetup setup) {
        String name = GameText.escape(setup.name());
        return setup.hasVariants() ? name + " · " + GameText.escape(setup.variant().name()) : name;
    }
}
