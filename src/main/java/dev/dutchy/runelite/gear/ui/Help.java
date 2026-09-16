package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.guide.HelpTopic;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/** Ties a component to its help topic: the rich tooltip now, and a spotlight anchor for the guides. */
final class Help {

    private static final String ANCHOR = "gearcomposer.help";

    private Help() {
    }

    /** Sets the topic's tooltip and marks the component as the topic's anchor. */
    static <T extends JComponent> T describe(T component, HelpTopic topic) {
        anchor(component, topic);
        component.setToolTipText(topic.tooltip());
        return component;
    }

    /** Marks the anchor only, for components that keep a tooltip of their own. */
    static <T extends JComponent> T anchor(T component, HelpTopic topic) {
        Objects.requireNonNull(component, "component").putClientProperty(ANCHOR, Objects.requireNonNull(topic, "topic"));
        return component;
    }

    /** The first showing component under {@code root} tagged with the topic. */
    static Optional<JComponent> find(Container root, HelpTopic topic) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(topic, "topic");
        for (Component child : root.getComponents()) {
            if (child instanceof JComponent && topic.equals(((JComponent) child).getClientProperty(ANCHOR)) && child.isVisible()) {
                return Optional.of((JComponent) child);
            }
            if (child instanceof Container && child.isVisible()) {
                Optional<JComponent> found = find((Container) child, topic);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }
}
