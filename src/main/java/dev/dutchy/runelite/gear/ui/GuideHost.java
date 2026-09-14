package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.content.SetupType;
import dev.dutchy.runelite.gear.guide.GuidePage;
import dev.dutchy.runelite.gear.guide.HelpTopic;

import javax.swing.*;
import java.util.Optional;

/** What a running guide asks of the sidebar: pages to show, elements to frame, and a sample to walk. */
interface GuideHost {

    /** Brings a page on screen for the sample, or for whatever is open when {@code sample} is null. */
    void showGuidePage(GuidePage page, SetupId sample);

    Optional<JComponent> guideAnchor(HelpTopic topic);

    SetupId createSample(SetupType type);

    void removeSample(SetupId sample);

    void keepSample(SetupId sample);

    boolean isBankOpen();

    /** Shows the sample in the open bank, as clicking its tile would. */
    void showInBank(SetupId sample);

    void hideFromBank(SetupId sample);

    /** Asks whether the sample should stay; only reached from the last step. */
    boolean askToKeepSample();
}
