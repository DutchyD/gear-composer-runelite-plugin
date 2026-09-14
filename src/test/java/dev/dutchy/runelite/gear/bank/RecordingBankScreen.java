package dev.dutchy.runelite.gear.bank;

/** A bank screen that only counts what it was asked to do, so a test can watch the display act. */
final class RecordingBankScreen implements BankScreen {

    private int rebuilds;
    private boolean open = true;

    @Override
    public void rebuild() {
        rebuilds++;
    }

    @Override
    public boolean isBankOpen() {
        return open;
    }

    void close() {
        open = false;
    }

    int rebuilds() {
        return rebuilds;
    }
}
