package dev.dutchy.runelite.gear.bank;

/**
 * The live bank interface, as the display can act on it: ask it to build itself again, and ask
 * whether it is on screen at all. A build is where the display's frame is written, so a redraw is
 * the only way it has of putting one there.
 */
public interface BankScreen {

    /** Asks the game to build the bank again, after which it will ask for a frame. */
    void rebuild();

    boolean isBankOpen();

    /** A screen that is never open and never redraws, for before one is wired up. */
    static BankScreen none() {
        return new BankScreen() {
            @Override
            public void rebuild() {
            }

            @Override
            public boolean isBankOpen() {
                return false;
            }
        };
    }
}
