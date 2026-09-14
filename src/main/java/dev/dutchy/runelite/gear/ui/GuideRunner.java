package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.guide.Guide;
import dev.dutchy.runelite.gear.guide.GuidePage;
import dev.dutchy.runelite.gear.guide.GuideProgress;
import dev.dutchy.runelite.gear.guide.GuideStep;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

/** Steps through one guide at a time: moves pages, frames elements, and looks after the tutorial's sample. */
final class GuideRunner {

    private final JComponent panel;
    private final GuideHost host;
    private final GuideProgress progress;
    private final Runnable onChange;

    private Guide guide;
    private int index;
    private SetupId sample;
    private boolean shownInBank;
    private Spotlight spotlight;
    private Callout callout;

    GuideRunner(JComponent panel, GuideHost host, GuideProgress progress, Runnable onChange) {
        this.panel = Objects.requireNonNull(panel, "panel");
        this.host = Objects.requireNonNull(host, "host");
        this.progress = Objects.requireNonNull(progress, "progress");
        this.onChange = Objects.requireNonNull(onChange, "onChange");
    }

    boolean isRunning() {
        return guide != null;
    }

    Optional<Guide> current() {
        return Optional.ofNullable(guide);
    }

    int stepIndex() {
        return index;
    }

    Optional<GuideStep> currentStep() {
        return current().map(running -> running.steps().get(index));
    }

    Optional<SetupId> sample() {
        return Optional.ofNullable(sample);
    }

    Optional<Callout> callout() {
        return Optional.ofNullable(callout);
    }

    Optional<Spotlight> spotlight() {
        return Optional.ofNullable(spotlight);
    }

    void start(Guide toRun) {
        Objects.requireNonNull(toRun, "toRun");
        if (isRunning()) {
            end();
        }
        guide = toRun;
        index = 0;
        sample = toRun.sampleType().map(host::createSample).orElse(null);
        callout = new Callout(this::back, this::next, this::end);
        spotlight = Spotlight.over(panel, callout, this::end).orElse(null);
        if (spotlight != null) {
            spotlight.bindKeys(this::back, this::next, this::end);
        }
        show();
    }

    void next() {
        if (!isRunning()) {
            return;
        }
        if (index == guide.length() - 1) {
            finish();
            return;
        }
        index++;
        show();
    }

    void back() {
        if (!isRunning() || index == 0) {
            return;
        }
        index--;
        show();
    }

    /** Leaves without credit; the sample goes with it. */
    void end() {
        if (!isRunning()) {
            return;
        }
        tearDown();
        if (sample != null) {
            host.removeSample(sample);
        }
        clear();
    }

    private void finish() {
        progress.markCompleted(guide.id());
        tearDown();
        if (sample != null) {
            if (host.askToKeepSample()) {
                host.keepSample(sample);
            } else {
                host.removeSample(sample);
            }
        }
        clear();
    }

    private void show() {
        GuideStep step = guide.steps().get(index);
        if (step.page() != GuidePage.CURRENT) {
            host.showGuidePage(step.page(), sample);
        }
        if (step.page() == GuidePage.BANK_PICTURE && sample != null && !shownInBank && host.isBankOpen()) {
            host.showInBank(sample);
            shownInBank = true;
        }
        JComponent element = step.anchored() ? host.guideAnchor(step.topic()).orElse(null) : null;
        if (spotlight != null) {
            spotlight.focusOn(element, step.topic(), index, guide.length());
        } else if (callout != null) {
            callout.show(step.topic(), index, guide.length());
        }
        onChange.run();
    }

    private void tearDown() {
        if (spotlight != null) {
            spotlight.dismiss();
        }
        if (shownInBank && sample != null) {
            host.hideFromBank(sample);
        }
        if (sample != null) {
            host.showGuidePage(GuidePage.LIST, null);
        }
    }

    private void clear() {
        guide = null;
        index = 0;
        sample = null;
        shownInBank = false;
        spotlight = null;
        callout = null;
        onChange.run();
    }
}
