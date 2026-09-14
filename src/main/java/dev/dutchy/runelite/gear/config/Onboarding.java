package dev.dutchy.runelite.gear.config;

/** Whether the getting-started card should still be shown. */
public interface Onboarding {

    boolean isDismissed();

    void dismiss();

    /** Shows the card until dismissed within this session; for tests and previews. */
    static Onboarding inMemory() {
        return new Onboarding() {
            private boolean dismissed;

            @Override
            public boolean isDismissed() {
                return dismissed;
            }

            @Override
            public void dismiss() {
                dismissed = true;
            }
        };
    }
}
