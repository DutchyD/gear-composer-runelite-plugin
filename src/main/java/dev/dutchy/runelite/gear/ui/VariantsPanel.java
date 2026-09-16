package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** The collapsible list of a setup's variants, with a card per variant, the edited one lit, and a menu to start a new one. */
final class VariantsPanel extends JPanel {

    static final String TITLE = "Variants";
    static final String NEW_VARIANT = "New variant";

    private final GearSetup setup;
    private final int editing;
    private final VariantActions actions;
    private final List<VariantCard> cards = new ArrayList<>();
    private final JPanel body = Ui.column(Ui.SMALL_GAP);
    private final JLabel summary = Ui.hint("");
    private final FlatButton toggle;
    private final FlatButton add;
    private boolean open;

    VariantsPanel(GearSetup setup, int editing, ItemIconFactory icons, VariantActions actions, boolean open) {
        this.setup = Objects.requireNonNull(setup, "setup");
        this.editing = editing;
        this.actions = Objects.requireNonNull(actions, "actions");
        this.open = open;
        this.toggle = new FlatButton(open ? ActionIcon.EXPANDED : ActionIcon.COLLAPSED, "Show or hide the variants", this::toggleOpen);
        this.add = new FlatButton(ActionIcon.ADD, NEW_VARIANT, "Start another variant of this setup", this::showNewVariantMenu);
        add.setEnabled(setup.variants().size() < GearSetup.MAX_VARIANTS);
        Help.anchor(this, HelpTopic.VARIANTS);

        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        JPanel titles = Ui.panel(new FlowLayout(FlowLayout.LEFT, Ui.SMALL_GAP, 0));
        titles.add(Ui.caption(TITLE));
        titles.add(summary);
        JPanel tools = Help.anchor(Ui.panel(new FlowLayout(FlowLayout.RIGHT, 2, 0)), HelpTopic.VARIANT_TOOLS);
        tools.add(add);
        tools.add(toggle);
        header.add(titles, BorderLayout.CENTER);
        header.add(tools, BorderLayout.EAST);

        List<SetupVariant> variants = setup.variants();
        for (int i = 0; i < variants.size(); i++) {
            VariantCard card = new VariantCard(variants.get(i), i, variants.size(), i == editing, i == setup.selectedIndex(), icons, actions);
            cards.add(card);
            body.add(card);
        }
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        setOpen(open);
    }

    List<VariantCard> cards() {
        return List.copyOf(cards);
    }

    List<String> names() {
        return cards.stream().map(card -> card.variant().name()).collect(Collectors.toList());
    }

    /** The name on the lit card, the one the page edits. */
    String chosen() {
        return cards.stream().filter(VariantCard::isChosen).map(card -> card.variant().name()).findFirst().orElseThrow();
    }

    /** The name on the badged card, the one the bank shows. */
    String shown() {
        return cards.stream().filter(VariantCard::isShown).map(card -> card.variant().name()).findFirst().orElseThrow();
    }

    boolean isOpen() {
        return open;
    }

    boolean canAdd() {
        return add.isEnabled();
    }

    void setOpen(boolean shouldOpen) {
        open = shouldOpen;
        toggle.setIcon(open ? ActionIcon.EXPANDED : ActionIcon.COLLAPSED);
        body.setVisible(open);
        int count = setup.variants().size();
        summary.setText(open ? "· " + count : "· " + count + " · editing " + editedName());
        revalidate();
        repaint();
    }

    private String editedName() {
        return setup.variantAt(editing).map(SetupVariant::name).orElse(setup.variant().name());
    }

    ContextMenu newVariantMenu() {
        ContextMenu menu = new ContextMenu();
        for (NewVariant start : NewVariant.values()) {
            if (start.offeredFor(setup.variantAt(editing).map(SetupVariant::content).orElse(setup.content()))) {
                menu.item(start == NewVariant.FROM_GAME ? ActionIcon.SYNC : ActionIcon.ADD, start.label(), () -> actions.addVariant(start));
            }
        }
        return menu;
    }

    private void showNewVariantMenu() {
        newVariantMenu().show(add, 0, add.getHeight());
    }

    private void toggleOpen() {
        setOpen(!open);
    }
}
