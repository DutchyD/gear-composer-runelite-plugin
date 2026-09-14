package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.guide.HelpTopic;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/** The card a guide speaks through: title, sentence, step counter, and Back, Next, End. */
final class Callout extends Card {

    static final int WIDTH = 206;
    private static final int TEXT_WIDTH = WIDTH - 20;
    static final String BACK = "Back";
    static final String NEXT = "Next";
    static final String FINISH = "Finish";
    static final String END = "End";

    private final JLabel title = new JLabel();
    private final JTextArea body = new JTextArea();
    private final JLabel counter = Ui.hint("");
    private final FlatButton back;
    private final PrimaryButton next;

    Callout(Runnable onBack, Runnable onNext, Runnable onEnd) {
        Objects.requireNonNull(onBack, "onBack");
        Objects.requireNonNull(onNext, "onNext");
        Objects.requireNonNull(onEnd, "onEnd");
        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        setOutline(ColorScheme.BRAND_ORANGE);
        setRadius(Ui.RADIUS);

        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(ColorScheme.BRAND_ORANGE);
        body.setFont(FontManager.getRunescapeSmallFont());
        body.setForeground(ColorScheme.TEXT_COLOR);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setFocusable(false);
        body.setOpaque(false);
        body.setBorder(null);

        back = new FlatButton(BACK, "Previous step (Left)", onBack);
        next = new PrimaryButton(NEXT, "Next step (Right or Enter)", onNext);
        FlatButton end = new FlatButton(END, "Leave the guide (Escape)", onEnd);
        JPanel buttons = Ui.panel(new FlowLayout(FlowLayout.RIGHT, Ui.SMALL_GAP, 0));
        buttons.add(end);
        buttons.add(back);
        buttons.add(next);

        JPanel footer = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        footer.add(counter, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);

        add(title, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    void show(HelpTopic topic, int index, int total) {
        title.setText(topic.title());
        body.setText(topic.body());
        body.setSize(new Dimension(TEXT_WIDTH, Short.MAX_VALUE));
        counter.setText((index + 1) + " / " + total);
        back.setEnabled(index > 0);
        next.setText(index == total - 1 ? FINISH : NEXT);
        setSize(getPreferredSize());
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        return new Dimension(WIDTH, preferred.height);
    }

    String titleText() {
        return title.getText();
    }

    String counterText() {
        return counter.getText();
    }

}
