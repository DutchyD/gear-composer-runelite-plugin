package dev.dutchy.runelite.gear.ui;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/** Read-only text that wraps to the width it is given instead of trailing off in an ellipsis. */
final class WrappedText extends JTextArea {

    private static final int FALLBACK_WIDTH = 200;

    WrappedText(String text, Font font, Color colour) {
        super(text);
        setFont(font);
        setForeground(colour);
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setBorder(null);
        setMargin(new Insets(0, 0, 0, 0));
        setAlignmentX(LEFT_ALIGNMENT);
        ((DefaultCaret) getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    }

    /** A change of text must never drag the sidebar around to show this component. */
    @Override
    public void scrollRectToVisible(Rectangle rectangle) {
    }

    /** Wraps at the parent's width, so the height is right the moment the parent has been laid out. */
    @Override
    public Dimension getPreferredSize() {
        int width = getParent() != null && getParent().getWidth() > 0
                ? getParent().getWidth() - getParent().getInsets().left - getParent().getInsets().right
                : getWidth() > 0 ? getWidth() : FALLBACK_WIDTH;
        setSize(new Dimension(Math.max(1, width), Short.MAX_VALUE));
        Dimension preferred = super.getPreferredSize();
        return new Dimension(width, preferred.height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(1, getPreferredSize().height);
    }
}
