package dev.dutchy.runelite.gear.bank;

/** Where overlays and menu swaps read the drawn bank from; the applier is the live one, tests hand over a fixed snapshot. */
@FunctionalInterface
public interface DrawnBankSource {

    DrawnBank drawnBank();
}
