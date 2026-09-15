package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupOrder;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import lombok.Value;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;

final class SectionView extends JPanel {

    @Value
    static class Config {
        boolean deletable;
        boolean collapsed;
        boolean canMoveUp;
        boolean canMoveDown;
        boolean nested;
        boolean canAddChild;
        boolean canUnnest;
        List<GearSection> nestTargets;
        boolean hasChildren;

        Config(boolean deletable, boolean collapsed, boolean canMoveUp, boolean canMoveDown,
               boolean nested, boolean canAddChild, boolean canUnnest, List<GearSection> nestTargets, boolean hasChildren) {
            this.deletable = deletable;
            this.collapsed = collapsed;
            this.canMoveUp = canMoveUp;
            this.canMoveDown = canMoveDown;
            this.nested = nested;
            this.canAddChild = canAddChild;
            this.canUnnest = canUnnest;
            this.nestTargets = List.copyOf(Objects.requireNonNull(nestTargets, "nestTargets"));
            this.hasChildren = hasChildren;
        }
    }

    static final int CHILD_INDENT = 14;

    static final String NEW_SETUP = "New setup";
    static final String NEW_FROM_GAME = "New from game";
    static final String RENAME = "Rename";
    static final String COLLAPSE_OTHERS = "Collapse others";
    static final String INSERT_ABOVE = "Insert section above";
    static final String INSERT_BELOW = "Insert section below";
    static final String EXPORT = "Export section…";
    static final String DELETE = "Delete section";
    static final String NEW_SUBSECTION = "New sub-section";
    static final String UNNEST = "Move to top level";
    static final String NEST_PREFIX = "Move under ";

    private final SectionId sectionId;
    private final String sectionName;
    private final SetupGrid grid;
    private final FlatButton toggleButton;
    private final BulkSelector bulk;
    private final BulkTarget target;
    private final JPanel headerPanel;
    private final JPanel titleArea = Ui.panel(new BorderLayout());
    private final JPanel title;
    private final SectionActions actions;
    private final Config config;
    private final TextInput renameField = new TextInput();

    private boolean collapsed;
    private boolean renaming;

    SectionView(GearSection section,
                Config config,
                ItemIconFactory icons,
                SetupActions setupActions,
                SectionActions sectionActions,
                SetupDragController setupDrags,
                SectionDragController sectionDrags,
                BulkSelector bulk,
                TileStyle style,
                HoverPreview preview) {
        Objects.requireNonNull(section, "section");
        this.config = Objects.requireNonNull(config, "config");
        this.actions = Objects.requireNonNull(sectionActions, "sectionActions");
        this.sectionId = section.id();
        this.sectionName = section.name();
        this.collapsed = config.collapsed();
        this.bulk = bulk;
        this.target = BulkTarget.of(section.id());
        this.grid = new SetupGrid(section.id(), section.setups(), icons, setupActions, setupDrags, bulk, style, preview, !config.hasChildren());
        this.toggleButton = new FlatButton(toggleIcon(), toggleTooltip(), () -> sectionActions.toggleSection(sectionId));
        this.title = titleOf(section);

        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(config.nested()
                ? BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(0, CHILD_INDENT - 2, Ui.GAP, 0),
                        BorderFactory.createMatteBorder(0, 2, 0, 0, Ui.OUTLINE))
                : BorderFactory.createEmptyBorder(0, 0, Ui.GAP, 0));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        this.headerPanel = header(section, sectionDrags);
        add(headerPanel, BorderLayout.NORTH);
        if (!collapsed) {
            add(grid, BorderLayout.CENTER);
        }
    }

    SectionId sectionId() {
        return sectionId;
    }

    SetupGrid grid() {
        return grid;
    }

    boolean isRenaming() {
        return renaming;
    }

    boolean isNested() {
        return config.nested();
    }

    void refreshBulkHighlight() {
        headerPanel.setBackground(bulk != null && bulk.isSelected(target)
                ? Highlights.danger(ColorScheme.DARK_GRAY_COLOR)
                : ColorScheme.DARK_GRAY_COLOR);
        headerPanel.setOpaque(true);
        headerPanel.repaint();
    }

    boolean isCollapsed() {
        return collapsed;
    }

    /** Shows the grid in place, without rebuilding the panel. */
    void expand() {
        if (!collapsed) {
            return;
        }
        collapsed = false;
        add(grid, BorderLayout.CENTER);
        toggleButton.setIcon(toggleIcon());
        toggleButton.setToolTipText(toggleTooltip());
        revalidate();
        repaint();
    }

    /** Swaps the name for a field; Enter keeps the new name, Escape drops it. */
    void startRename() {
        if (renaming || bulk != null) {
            return;
        }
        renaming = true;
        renameField.setText(sectionName);
        titleArea.removeAll();
        titleArea.add(renameField, BorderLayout.CENTER);
        titleArea.revalidate();
        titleArea.repaint();
        renameField.requestFocusInWindow();
        renameField.selectAll();
    }

    void commitRename() {
        if (!renaming) {
            return;
        }
        String newName = renameField.getText();
        stopRenaming();
        if (GearSection.isValidName(newName) && !newName.strip().equals(sectionName)) {
            actions.renameSection(sectionId, newName);
        }
    }

    void cancelRename() {
        stopRenaming();
    }

    ContextMenu contextMenu() {
        ContextMenu menu = new ContextMenu()
                .item(ActionIcon.ADD, NEW_SETUP, () -> actions.addSetupTo(sectionId))
                .item(ActionIcon.SYNC, NEW_FROM_GAME, () -> actions.addSetupFromGame(sectionId))
                .item(ActionIcon.RENAME, RENAME, this::startRename)
                .item(ActionIcon.COLLAPSED, COLLAPSE_OTHERS, () -> actions.collapseOthers(sectionId))
                .item(ActionIcon.MOVE_UP, INSERT_ABOVE, () -> actions.insertSectionAbove(sectionId))
                .item(ActionIcon.MOVE_DOWN, INSERT_BELOW, () -> actions.insertSectionBelow(sectionId))
                .item(ActionIcon.COPY, EXPORT, () -> actions.exportSection(sectionId));
        for (SetupOrder order : SetupOrder.values()) {
            menu.item(ActionIcon.MOVE_DOWN, order.displayName(), () -> actions.sortSection(sectionId, order));
        }
        if (config.canAddChild()) {
            menu.item(ActionIcon.ADD, NEW_SUBSECTION, () -> actions.addSubSection(sectionId));
        }
        for (GearSection target : config.nestTargets()) {
            menu.item(ActionIcon.MOVE_DOWN, NEST_PREFIX + target.name(), () -> actions.nestSection(sectionId, target.id()));
        }
        if (config.canUnnest()) {
            menu.item(ActionIcon.MOVE_UP, UNNEST, () -> actions.unnestSection(sectionId));
        }
        if (config.deletable()) {
            menu.divider().danger(DELETE, () -> actions.deleteSection(sectionId));
        }
        return menu;
    }

    private ContextMenu addMenu() {
        return new ContextMenu()
                .item(ActionIcon.ADD, NEW_SETUP, () -> actions.addSetupTo(sectionId))
                .item(ActionIcon.SYNC, NEW_FROM_GAME, () -> actions.addSetupFromGame(sectionId));
    }

    private void stopRenaming() {
        renaming = false;
        titleArea.removeAll();
        titleArea.add(title, BorderLayout.CENTER);
        titleArea.revalidate();
        titleArea.repaint();
    }

    private ActionIcon toggleIcon() {
        return collapsed ? ActionIcon.COLLAPSED : ActionIcon.EXPANDED;
    }

    private String toggleTooltip() {
        return (collapsed ? "Expand " : "Collapse ") + sectionName;
    }

    private JPanel titleOf(GearSection section) {
        JLabel name = new JLabel(section.name());
        name.setFont(FontManager.getRunescapeBoldFont());
        name.setForeground(ColorScheme.TEXT_COLOR);
        JLabel count = Ui.hint(String.valueOf(section.size()));
        JPanel panel = Ui.panel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setToolTipText("Drag to reorder, click to expand or collapse, double-click to rename, right-click for more");
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(name);
        panel.add(count);
        return panel;
    }

    private JPanel header(GearSection section, SectionDragController drags) {
        JPanel header = new JPanel(new BorderLayout(2, 0));
        header.setBackground(ColorScheme.DARK_GRAY_COLOR);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.OUTLINE),
                BorderFactory.createEmptyBorder(0, 0, 2, 0)));

        titleArea.add(title, BorderLayout.CENTER);
        renameField.addActionListener(e -> commitRename());
        renameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelRename();
                }
            }
        });
        renameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitRename();
            }
        });

        JPanel buttons = Ui.panel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if (bulk == null && config.canMoveUp()) {
            buttons.add(new FlatButton(ActionIcon.MOVE_UP, "Move " + section.name() + " up",
                    () -> actions.moveSectionUp(sectionId)));
        }
        if (bulk == null && config.canMoveDown()) {
            buttons.add(new FlatButton(ActionIcon.MOVE_DOWN, "Move " + section.name() + " down",
                    () -> actions.moveSectionDown(sectionId)));
        }
        if (bulk == null) {
            FlatButton add = Help.anchor(new FlatButton(ActionIcon.ADD, "Add a setup to " + section.name(), () -> {
            }), HelpTopic.SECTION_ADD);
            add.addActionListener(e -> addMenu().show(add, 0, add.getHeight()));
            buttons.add(add);
            FlatButton more = new FlatButton(ActionIcon.MORE, "More for " + section.name(), () -> {
            });
            more.addActionListener(e -> contextMenu().show(more, 0, more.getHeight()));
            buttons.add(more);
        }

        Help.anchor(header, HelpTopic.SECTION_HEADER);
        header.add(toggleButton, BorderLayout.WEST);
        header.add(titleArea, BorderLayout.CENTER);
        header.add(buttons, BorderLayout.EAST);

        if (bulk != null) {
            MouseAdapter picker = bulkHandle();
            header.addMouseListener(picker);
            title.addMouseListener(picker);
            title.setToolTipText("Click to select, shift click to select a range");
        } else {
            MouseAdapter handle = dragHandle(drags);
            header.addMouseListener(handle);
            header.addMouseMotionListener(handle);
            title.addMouseListener(handle);
            title.addMouseMotionListener(handle);
        }
        for (Component child : title.getComponents()) {
            for (MouseListener listener : title.getMouseListeners()) {
                child.addMouseListener(listener);
            }
            for (MouseMotionListener listener : title.getMouseMotionListeners()) {
                child.addMouseMotionListener(listener);
            }
        }
        return header;
    }

    private MouseAdapter bulkHandle() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                bulk.select(target, e.isShiftDown());
            }
        };
    }

    private MouseAdapter dragHandle(SectionDragController drags) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (maybeShowMenu(e)) {
                    return;
                }
                if (drags != null) {
                    drags.press(SectionView.this, e);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drags != null) {
                    drags.drag(SectionView.this, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (maybeShowMenu(e)) {
                    if (drags != null) {
                        drags.cancel();
                    }
                    return;
                }
                if (drags != null) {
                    drags.release(SectionView.this, e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || (drags != null && drags.draggedSincePress())) {
                    return;
                }
                if (e.getClickCount() == 2) {
                    startRename();
                } else if (e.getClickCount() == 1) {
                    actions.toggleSection(sectionId);
                }
            }
        };
    }

    private boolean maybeShowMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return false;
        }
        Component source = e.getComponent();
        contextMenu().show(source, e.getX(), e.getY());
        return true;
    }
}
