package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;

/** Ways to get a setup out of the client: a picture or a text listing. */
final class SharePage extends JPanel {

    enum Action { SAVE_IMAGE, COPY_IMAGE, COPY_TEXT }

    static final String SAVE_IMAGE = "Save image";
    static final String COPY_IMAGE = "Copy image";
    static final String COPY_TEXT = "Copy as text";
    static final String WITH_LEDGER = "With ledger";
    static final String WITHOUT_LEDGER = "Items only";

    private final Chip withLedger = new Chip(WITH_LEDGER, "Add value, weight and stats", () -> setLedger(true));
    private final Chip withoutLedger = new Chip(WITHOUT_LEDGER, "Just the items", () -> setLedger(false));
    private boolean ledger;

    SharePage(GearSetup setup, ItemIconFactory icons, boolean ledgerByDefault,
              Consumer<Action> onAction, Runnable onBack) {
        Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(icons, "icons");
        Objects.requireNonNull(onAction, "onAction");
        Objects.requireNonNull(onBack, "onBack");
        this.ledger = ledgerByDefault;

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel preview = Ui.panel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        preview.add(new MiniContentView(setup, icons));

        Help.anchor(this, HelpTopic.SHARE_PAGE);
        JPanel ledgerRow = Help.anchor(Ui.panel(new GridLayout(1, 2, Ui.SMALL_GAP, 0)), HelpTopic.SHARE_LEDGER);
        ledgerRow.add(withLedger);
        ledgerRow.add(withoutLedger);
        refreshChips();

        JPanel buttons = Ui.column(Ui.SMALL_GAP,
                new PrimaryButton(SAVE_IMAGE, "Write a PNG to the screenshots folder", () -> onAction.accept(Action.SAVE_IMAGE)),
                FlatButton.outlined(ActionIcon.COPY, COPY_IMAGE, "Put the picture on the clipboard", () -> onAction.accept(Action.COPY_IMAGE)),
                FlatButton.outlined(ActionIcon.COPY, COPY_TEXT, "Put a Markdown list on the clipboard", () -> onAction.accept(Action.COPY_TEXT)));
        buttons.setBorder(BorderFactory.createEmptyBorder(Ui.SMALL_GAP, 0, 0, 0));
        Help.anchor(buttons, HelpTopic.SHARE_ACTIONS);

        add(Ui.column(Ui.GAP, NavBar.create("Share " + setup.name(), onBack), preview,
                Ui.labelled("Include", ledgerRow), buttons), BorderLayout.NORTH);
    }

    boolean withLedger() {
        return ledger;
    }

    void setLedger(boolean include) {
        ledger = include;
        refreshChips();
    }

    private void refreshChips() {
        withLedger.setChosen(ledger);
        withoutLedger.setChosen(!ledger);
    }
}
