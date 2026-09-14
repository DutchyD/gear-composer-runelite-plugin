package dev.dutchy.runelite.gear.guide;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

/** One stop of a guide: a topic to explain, which page it lives on, and whether the topic's element is spotlit. */
@Value
@Accessors(fluent = true)
public class GuideStep {
    HelpTopic topic;
    GuidePage page;
    boolean anchored;

    public GuideStep(HelpTopic topic, GuidePage page, boolean anchored) {
        this.topic = Objects.requireNonNull(topic, "topic");
        this.page = Objects.requireNonNull(page, "page");
        this.anchored = anchored;
    }

    /** Spotlights the element tagged with the topic on the page already showing. */
    public static GuideStep at(HelpTopic topic) {
        return new GuideStep(topic, GuidePage.CURRENT, true);
    }

    public static GuideStep at(HelpTopic topic, GuidePage page) {
        return new GuideStep(topic, page, true);
    }

    /** Tells rather than points: the callout sits on its own. */
    public static GuideStep tell(HelpTopic topic, GuidePage page) {
        return new GuideStep(topic, page, false);
    }
}
