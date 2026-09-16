package dev.dutchy.runelite.gear.activation;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.bank.BankTitle;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** A short in-game notice of which setup and variant just became active, so the sidebar can stay closed. */
public final class ActivationToast extends OverlayPanel {

    static final Duration SHOW_FOR = Duration.ofSeconds(2);
    private static final Color TITLE = new Color(220, 138, 0);

    private final SetupActivator activator;
    private final Clock clock;

    private volatile Instant until = Instant.MIN;
    private volatile String title = "";
    private volatile String detail = "";

    @Inject
    public ActivationToast(SetupActivator activator, Clock clock) {
        this.activator = Objects.requireNonNull(activator, "activator");
        this.clock = Objects.requireNonNull(clock, "clock");
        setPosition(OverlayPosition.TOP_CENTER);
        setPriority(PRIORITY_HIGH);
    }

    /** Shows the current state of the activator for a moment. */
    public void announce() {
        Optional<GearSetup> current = activator.current();
        title = current.map(BankTitle::describe).orElse("Bank layout off");
        detail = current.flatMap(setup -> activator.position()).map(pos -> pos[0] + " of " + pos[1]).orElse("");
        until = clock.instant().plus(SHOW_FOR);
    }

    public boolean isShowing() {
        return clock.instant().isBefore(until);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!isShowing()) {
            return null;
        }
        panelComponent.getChildren().add(TitleComponent.builder().text(title).color(TITLE).build());
        if (!detail.isEmpty()) {
            panelComponent.getChildren().add(LineComponent.builder().left(detail).build());
        }
        return super.render(graphics);
    }
}
