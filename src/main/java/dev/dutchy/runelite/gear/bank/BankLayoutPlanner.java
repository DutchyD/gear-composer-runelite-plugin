package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.SetupContent;
import dev.dutchy.runelite.gear.layout.Layout;

import java.util.Objects;

/** Turns setup content into the placements a bank should show; mapping a side onto real bank slots is the applier's job. */
public final class BankLayoutPlanner {

    public BankLayout plan(SetupContent content) {
        Layout layout = Layout.of(Objects.requireNonNull(content, "content"));
        return new BankLayout(layout.placements(), layout.labels(), layout.headerRows());
    }
}
