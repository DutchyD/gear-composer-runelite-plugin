package dev.dutchy.runelite.gear.bank;

/**
 * Whether the bank is showing one of the Bank Tags plugin's tags, and the way to put it away. A tag
 * filters the build, so the two cannot share a bank: the layout stands down while one is open.
 */
public interface BankTagState {

    boolean isTagOpen();

    /** Closes the open tag, if there is one, and lets the bank build itself again. */
    void closeTag();

    /** No tag is ever open, for a bank the plugin is not laying out. */
    static BankTagState none() {
        return new BankTagState() {
            @Override
            public boolean isTagOpen() {
                return false;
            }

            @Override
            public void closeTag() {
            }
        };
    }
}
