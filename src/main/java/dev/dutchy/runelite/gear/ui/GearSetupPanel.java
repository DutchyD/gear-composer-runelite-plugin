package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.*;
import dev.dutchy.runelite.gear.account.AccountEntry;
import dev.dutchy.runelite.gear.bank.ActiveSetup;
import dev.dutchy.runelite.gear.config.GearComposerConfig;
import dev.dutchy.runelite.gear.config.Onboarding;
import dev.dutchy.runelite.gear.config.ViewSettings;
import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.guide.*;
import dev.dutchy.runelite.gear.history.SetupHistory;
import dev.dutchy.runelite.gear.history.SetupRevision;
import dev.dutchy.runelite.gear.layout.GridBlock;
import dev.dutchy.runelite.gear.layout.Layout;
import dev.dutchy.runelite.gear.ledger.ItemFactsSource;
import dev.dutchy.runelite.gear.ledger.Ledger;
import dev.dutchy.runelite.gear.ledger.SetupDiff;
import dev.dutchy.runelite.gear.persistence.Backup;
import dev.dutchy.runelite.gear.player.PlayerItems;
import dev.dutchy.runelite.gear.requirements.RequirementWatch;
import dev.dutchy.runelite.gear.share.ShareService;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.selector.ItemSelectorFactory;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Reordering is disabled while a search filter is active. */
public final class GearSetupPanel extends PluginPanel
        implements GearSetupBookListener, SetupActions, SectionActions, SetupDropHandler, ContentActions, CellActions, GuideHost {
    private static final String LIST_CARD = "list";
    private static final String HOME_CARD = "home";
    static final String ALL_ACCOUNTS = "All accounts";
    private static final String EDITOR_CARD = "editor";
    private static final String CONTENT_CARD = "content";
    private static final String SLOT_CARD = "slot";
    private static final String DIVIDER_CARD = "divider";
    private static final String HISTORY_CARD = "history";
    private static final String HOTKEY_CARD = "hotkey";
    private static final String IMPORT_CARD = "import";
    private static final String BACKUPS_CARD = "backups";
    static final String IMPORT_FILE = "Import file…";
    static final String EXPORT_ALL = "Export all…";
    static final String BACKUPS = "Backups…";
    static final String PASTE_SHARE_CODE = "Paste share code…";
    private static final String SHARE_CARD = "share";
    private static final String DIFF_CARD = "diff";
    private static final String SHARE_OUT_CARD = "shareout";
    private static final String CELL_SOURCE_CARD = "cellsource";
    private static final String GUIDES_CARD = "guides";
    private static final String BANK_PICTURE_CARD = "bankpicture";
    static final String GUIDES = "Guides…";

    private final GearSetupBook book;
    private final ItemIconFactory icons;
    private final ItemSelectorFactory selectors;
    private final SetupFilter filter;
    private final SetupTypeArtwork artwork;
    private final ActiveSetup activeSetup;
    private final PlayerItems playerItems;
    private final GearComposerConfig config;
    private final Onboarding onboarding;
    private final UndoHistory undo;
    private final DropRule dropRule;
    private final Clock clock;
    private final RequirementWatch requirements;
    private final ViewSettings view;
    private final HoverPreview preview;
    private final ItemFactsSource facts;
    private final ShareService shareService;
    private final GuideProgress guideProgress;
    private final PrayerArtwork prayerArtwork;
    private final EquipmentSlotArtwork slotArtwork;
    private final GuideRunner guides;
    private final OpenSetupEditor editor;
    private final BulkDelete bulkDelete;
    private final Transfer transfer;
    private final GuideSamples samples;
    private final Accounts accounts;
    private final SectionCommands sectionCommands;
    private final SetupCommands setupCommands;
    private final ContentHost content;
    private UnaryOperator<ContentViewState> pendingView = UnaryOperator.identity();
    private final FlatButton styleButton = new FlatButton(ActionIcon.GRID, "", this::cycleTileStyle);
    private final WrappedText banner = Ui.paragraph("");
    private SectionId pendingRename;

    private final PageStack pages;
    private final SectionListPanel sections = new SectionListPanel();
    private final SetupDragController drags;
    private final SectionDragController sectionDrags;
    private final IconTextField searchField = new IconTextField();
    private final WrappedText status = Ui.paragraph("");
    private final FlatButton undoButton = new FlatButton("Undo", "Take back the last change (Ctrl+Z)", this::undo);
    private final Timer undoOffer = new Timer(0, e -> undoButton.setVisible(false));
    private final JPanel viewedLine = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
    private final JPanel searchLine = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
    private final FlatButton bulkButton = new FlatButton(ActionIcon.TRASH, "Select several setups or sections to delete", this::toggleBulkMode);
    private final JLabel viewedName = new JLabel();

    private final JPanel bulkBar = new JPanel(new BorderLayout(4, 0));
    private final JLabel bulkCount = new JLabel();

    private SetupId selected;

    @Inject
    public GearSetupPanel(GearSetupBook book,
                          SetupFilter filter,
                          ActiveSetup activeSetup,
                          PlayerItems playerItems,
                          GearComposerConfig config,
                          Onboarding onboarding,
                          UndoHistory undo,
                          RequirementWatch requirements,
                          GuideProgress guideProgress,
                          ShareService shareService,
                          SetupHistory history,
                          BulkDelete bulkDelete,
                          Transfer transfer,
                          GuideSamples samples,
                          Accounts accounts,
                          SectionCommands sectionCommands,
                          SetupCommands setupCommands,
                          PageParts parts) {
        this(book, filter, activeSetup, playerItems, config, onboarding, undo, requirements, guideProgress, shareService,
                history, bulkDelete, transfer, samples, accounts, sectionCommands, setupCommands, parts, SwingPrompts::over);
    }

    /** {@code prompts} is how the modules ask and report; a test hands over one that never opens a dialog. */
    GearSetupPanel(GearSetupBook book,
                   SetupFilter filter,
                   ActiveSetup activeSetup,
                   PlayerItems playerItems,
                   GearComposerConfig config,
                   Onboarding onboarding,
                   UndoHistory undo,
                   RequirementWatch requirements,
                   GuideProgress guideProgress,
                   ShareService shareService,
                   SetupHistory history,
                   BulkDelete bulkDelete,
                   Transfer transfer,
                   GuideSamples samples,
                   Accounts accounts,
                   SectionCommands sectionCommands,
                   SetupCommands setupCommands,
                   PageParts parts,
                   Function<GearSetupPanel, Prompts> prompts) {
        this.book = Objects.requireNonNull(book, "book");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.activeSetup = Objects.requireNonNull(activeSetup, "activeSetup");
        this.playerItems = Objects.requireNonNull(playerItems, "playerItems");
        this.config = Objects.requireNonNull(config, "config");
        this.onboarding = Objects.requireNonNull(onboarding, "onboarding");
        this.undo = Objects.requireNonNull(undo, "undo");
        this.requirements = Objects.requireNonNull(requirements, "requirements");
        this.guideProgress = Objects.requireNonNull(guideProgress, "guideProgress");
        this.shareService = Objects.requireNonNull(shareService, "shareService");
        this.bulkDelete = Objects.requireNonNull(bulkDelete, "bulkDelete");
        this.transfer = Objects.requireNonNull(transfer, "transfer");
        this.samples = Objects.requireNonNull(samples, "samples");
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.sectionCommands = Objects.requireNonNull(sectionCommands, "sectionCommands");
        this.setupCommands = Objects.requireNonNull(setupCommands, "setupCommands");
        Prompts asked = Objects.requireNonNull(prompts, "prompts").apply(this);
        Objects.requireNonNull(parts, "parts");
        this.icons = parts.icons();
        this.selectors = parts.selectors();
        this.artwork = parts.typeArtwork();
        this.prayerArtwork = parts.prayerArtwork();
        this.slotArtwork = parts.slotArtwork();
        this.facts = parts.facts();
        this.view = parts.view();
        this.dropRule = parts.dropRule();
        this.clock = parts.clock();
        this.guides = new GuideRunner(this, this, guideProgress, this::refreshGuideState);
        this.preview = new HoverPreview(parts.icons());

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(Ui.GAP, Ui.GAP, Ui.GAP, Ui.GAP));

        drags = new SetupDragController(sections, this::nearestGrid, this::sectionViewAt, this::springOpen, this);
        sectionDrags = new SectionDragController(sections, this::onSectionDropped);

        pages = new PageStack(LIST_CARD, listCard(), this::guideButton, name -> refreshListChrome());
        editor = new OpenSetupEditor(book, Objects.requireNonNull(history, "history"), new OpenSetupEditor.Listener() {
            @Override
            public void changed(String cause, UnaryOperator<ContentViewState> focus) {
                pendingView = focus;
                announce(cause);
                showContentCard();
            }

            @Override
            public void gone() {
                showListCard();
            }
        });

        bulkDelete.shownBy(asked, new BulkDelete.Listener() {
            @Override
            public void modeChanged() {
                bulkBar.setVisible(bulkDelete.isOn());
                rebuild();
            }

            @Override
            public void selectionChanged() {
                refreshBulkHighlights();
                updateBulkBar();
            }
        }, this::visibleTargets);
        transfer.shownBy(asked, new Transfer.Pages() {
            @Override
            public void showImport(String source, List<GearSection> imported) {
                showImportPage(source, imported);
            }

            @Override
            public void showBackups(List<Backup> backups) {
                showBackupsPage(backups);
            }

            @Override
            public void showShared(GearSetup shared) {
                showSharedPage(shared);
            }

            @Override
            public void backToList() {
                showListCard();
            }
        }, this::ownerForNewSetups);
        samples.shownBy(asked, this::refreshActiveHighlights);
        sectionCommands.shownBy(asked, new SectionCommands.Listener() {
            @Override
            public void renameStarted(SectionId sectionId) {
                pendingRename = sectionId;
                rebuild();
            }

            @Override
            public void foldingChanged() {
                rebuild();
            }
        }, () -> sectionViews().stream().map(SectionView::sectionId).collect(Collectors.toList()));
        setupCommands.shownBy(asked, this::followActiveSetup, this::ownerForNewSetups);
        accounts.shownBy(asked, new Accounts.Listener() {
            @Override
            public void loggedIn() {
                if (isHomeShowing() || isListShowing()) {
                    showListCard();
                } else {
                    refreshAfterAccountChange();
                }
            }

            @Override
            public void loggedOut() {
                if (isHomeShowing() || isListShowing()) {
                    showHome();
                } else {
                    refreshAfterAccountChange();
                }
            }

            @Override
            public void rosterChanged() {
                SwingUtilities.invokeLater(() -> {
                    if (isHomeShowing()) {
                        showHome();
                    }
                });
            }
        });

        content = new ContentHost(editor, history, playerItems, config::confirmBeforeSync);
        content.shownBy(asked, this::returnToContents);

        add(header(), BorderLayout.NORTH);
        add(pages.component(), BorderLayout.CENTER);
        add(statusRow(), BorderLayout.SOUTH);
        installShortcuts();

        book.addChangeListener(this);
        activeSetup.addListener(() -> SwingUtilities.invokeLater(this::followActiveSetup));
        requirements.addListener(() -> SwingUtilities.invokeLater(this::refreshBanner));
        facts.addListener(() -> contentPanel().ifPresent(SetupContentPanel::refreshLedger));
        refreshViewedLine();
        refreshBanner();
        rebuild();
        if (accounts.mine().isEmpty()) {
            showHome();
        }
    }

    private String bannerMessage = "";

    boolean isBannerShowing() {
        return banner.isVisible();
    }

    String bannerText() {
        return bannerMessage;
    }

    /** Wraps onto a second line rather than clipping in the narrow sidebar. */
    private void refreshBanner() {
        Optional<String> warning = requirements.warning();
        bannerMessage = warning.orElse("");
        banner.setVisible(warning.isPresent());
        banner.setText(bannerMessage);
        banner.revalidate();
    }

    private WrappedText bannerLine() {
        banner.setFont(FontManager.getRunescapeSmallFont());
        banner.setForeground(ColorScheme.TEXT_COLOR);
        banner.setOpaque(true);
        banner.setBackground(Highlights.danger(ColorScheme.DARK_GRAY_COLOR));
        banner.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        banner.setMargin(new Insets(3, 8, 3, 8));
        banner.setVisible(false);
        return banner;
    }

    /** Opens the list of one owner's setups, from the accounts screen. */
    void viewOwner(Owner owner) {
        accounts.view(owner);
        showListCard();
    }

    boolean isHomeShowing() {
        return pages.isShowing(HOME_CARD);
    }

    /** The accounts screen: the home while logged out, and the way to any account's setups. */
    void showHome() {
        Runnable backToMine = accounts.mine().map(owner -> (Runnable) () -> viewOwner(owner)).orElse(null);
        JComponent frontDoor = book.isEmpty() && !onboarding.isDismissed() && !samples.anyTutorialCompleted()
                ? new OnboardingCard(this::startStandaloneGuide, this::dismissOnboarding) : null;
        pages.replace(HOME_CARD, new AccountsPage(accounts.roster(), entry -> viewOwner(entry.owner()), this::forgetAccount, backToMine, frontDoor), GuideId.HOME);
        pages.show(HOME_CARD);
    }

    Optional<AccountsPage> accountsPage() {
        return pages.visible(AccountsPage.class);
    }

    void forgetAccount(AccountEntry entry) {
        if (accounts.forget(entry)) {
            showHome();
        }
    }

    String describeViewed() {
        return accounts.describeViewed();
    }

    boolean isUndoOffered() {
        return undoButton.isVisible();
    }

    boolean isOnboardingShowing() {
        return Stream.of(sections.getComponents()).anyMatch(OnboardingCard.class::isInstance);
    }

    void dismissOnboarding() {
        onboarding.dismiss();
        rebuild();
    }

    void undo() {
        if (undo.undo()) {
            say("Undid the last change");
        }
    }

    void redo() {
        if (undo.redo()) {
            say("Redid the change");
        }
    }

    private void refreshAfterAccountChange() {
        refreshViewedLine();
        rebuild();
    }

    private void followActiveSetup() {
        selected = activeSetup.current().orElse(null);
        refreshActiveHighlights();
    }

    /** Reports a change to the book and offers to take it back. */
    void announce(String message) {
        status.setText(message);
        undoButton.setVisible(undo.canUndo());
        undoOffer.setInitialDelay(Math.max(1, config.undoToastSeconds()) * 1000);
        undoOffer.restart();
    }

    /** Reports something that did not change the book. */
    void say(String message) {
        status.setText(message);
        undoButton.setVisible(false);
        undoOffer.stop();
    }

    private JPanel statusRow() {
        status.setFont(FontManager.getRunescapeSmallFont());
        status.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        status.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        undoButton.setVisible(false);
        undoOffer.setRepeats(false);

        JPanel row = Help.describe(Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0)), HelpTopic.STATUS_LINE);
        row.setBorder(BorderFactory.createEmptyBorder(Ui.GAP, 0, 0, 0));
        row.add(status, BorderLayout.CENTER);
        row.add(undoButton, BorderLayout.EAST);
        return row;
    }

    private void installShortcuts() {
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo", this::undo);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo", this::redo);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "redo", this::redo);
    }

    private void bind(KeyStroke key, String name, Runnable action) {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private JPanel viewedLineRow() {
        viewedName.setFont(FontManager.getRunescapeSmallFont());
        viewedName.setForeground(ColorScheme.TEXT_COLOR);
        viewedLine.add(FlatButton.outlined(ActionIcon.GRID, ALL_ACCOUNTS, "Every account's setups, and the shared ones", this::showHome), BorderLayout.EAST);
        refreshViewedLine();
        return viewedLine;
    }

    private void refreshViewedLine() {
        Component old = ((BorderLayout) viewedLine.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (old != null) {
            viewedLine.remove(old);
        }
        viewedName.setText(describeViewed());
        JPanel who = Ui.panel(new FlowLayout(FlowLayout.LEFT, Ui.SMALL_GAP, 0));
        who.add(new AccountBadge(accounts.viewed(), describeViewed()));
        who.add(viewedName);
        viewedLine.add(who, BorderLayout.CENTER);
        viewedLine.revalidate();
        viewedLine.repaint();
    }

    public Optional<SetupId> selectedSetup() {
        return Optional.ofNullable(selected);
    }

    public void search(String query) {
        searchField.setText(Objects.requireNonNull(query, "query"));
    }

    SectionDragController sectionDrags() {
        return sectionDrags;
    }

    List<SectionView> sectionViews() {
        return Stream.of(sections.getComponents())
                .filter(SectionView.class::isInstance)
                .map(SectionView.class::cast)
                .collect(Collectors.toList());
    }

    boolean isEditorShowing() {
        return pages.visible(SetupEditorForm.class).isPresent();
    }

    boolean isListShowing() {
        return pages.isShowing(LIST_CARD);
    }

    Optional<SlotEditorForm> slotEditorForm() {
        return pages.visible(SlotEditorForm.class);
    }

    Optional<SetupContentPanel> contentPanel() {
        return pages.visible(SetupContentPanel.class);
    }

    Optional<SetupEditorForm> editorForm() {
        return pages.visible(SetupEditorForm.class);
    }

    private JPanel header() {
        JPanel title = Ui.panel(new BorderLayout());

        JLabel heading = new JLabel("Gear Composer");
        heading.setFont(FontManager.getRunescapeBoldFont());
        heading.setForeground(ColorScheme.BRAND_ORANGE);

        JPanel buttons = Ui.panel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        FlatButton add = Help.describe(new FlatButton(ActionIcon.ADD, "New setup", this::createSetup), HelpTopic.NEW_SETUP);
        Help.describe(bulkButton, HelpTopic.BULK_DELETE);
        FlatButton bulk = bulkButton;
        add.setPreferredSize(new Dimension(24, 24));
        bulk.setPreferredSize(new Dimension(24, 24));
        FlatButton more = new FlatButton(ActionIcon.MORE, "Import, export and backups", () -> {
        });
        more.setPreferredSize(new Dimension(24, 24));
        more.addActionListener(e -> overflowMenu().show(more, 0, more.getHeight()));
        Help.describe(more, HelpTopic.OVERFLOW_MENU);
        styleButton.setPreferredSize(new Dimension(24, 24));
        Help.anchor(styleButton, HelpTopic.TILE_STYLE);
        refreshStyleButton();
        buttons.add(add);
        buttons.add(styleButton);
        buttons.add(more);
        buttons.add(bulk);

        title.add(heading, BorderLayout.WEST);
        title.add(buttons, BorderLayout.EAST);

        searchField.setIcon(IconTextField.Icon.SEARCH);
        Help.describe(searchField, HelpTopic.SEARCH);
        searchField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchField.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchField.setPreferredSize(new Dimension(100, Ui.FIELD_HEIGHT));
        searchField.addClearListener(this::rebuild);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                rebuild();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                rebuild();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                rebuild();
            }
        });

        searchLine.add(searchField, BorderLayout.CENTER);
        searchLine.add(guideButton(GuideId.LIST), BorderLayout.EAST);
        JPanel searchRow = Ui.panel(new BorderLayout(0, Ui.SMALL_GAP));
        searchRow.add(searchLine, BorderLayout.NORTH);
        searchRow.add(Ui.column(Ui.SMALL_GAP, bulkBar(), bannerLine()), BorderLayout.SOUTH);

        JPanel header = Ui.panel(new BorderLayout(0, Ui.GAP));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, Ui.GAP, 0));
        header.add(title, BorderLayout.NORTH);
        header.add(Help.describe(viewedLineRow(), HelpTopic.VIEWED_ACCOUNT), BorderLayout.CENTER);
        refreshListChrome();
        header.add(searchRow, BorderLayout.SOUTH);
        return header;
    }

    private JPanel bulkBar() {
        bulkBar.setBackground(Highlights.danger(ColorScheme.DARK_GRAY_COLOR));
        bulkBar.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));
        bulkBar.setVisible(false);
        bulkCount.setFont(FontManager.getRunescapeSmallFont());
        bulkCount.setForeground(ColorScheme.TEXT_COLOR);

        JPanel actions = Ui.panel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        actions.add(new FlatButton("Delete", "Delete everything selected", bulkDelete::deleteSelected));
        actions.add(new FlatButton("Cancel", "Leave bulk delete", () -> bulkDelete.set(false)));

        bulkBar.add(bulkCount, BorderLayout.WEST);
        bulkBar.add(actions, BorderLayout.EAST);
        return bulkBar;
    }

    ContextMenu overflowMenu() {
        return new ContextMenu()
                .item(ActionIcon.MOVE_DOWN, IMPORT_FILE, transfer::importFile)
                .item(ActionIcon.MOVE_UP, EXPORT_ALL, transfer::exportAll)
                .item(ActionIcon.CLOCK, BACKUPS, transfer::showBackups)
                .item(ActionIcon.COPY, PASTE_SHARE_CODE, transfer::pasteShareCode)
                .item(ActionIcon.HELP, GUIDES, this::showGuides);
    }

    TileStyle tileStyle() {
        return view.tileStyle();
    }

    void cycleTileStyle() {
        setTileStyle(view.tileStyle().next());
    }

    void setTileStyle(TileStyle style) {
        view.setTileStyle(style);
        refreshStyleButton();
        rebuild();
    }

    private void refreshStyleButton() {
        styleButton.setToolTipText("Setup list: " + view.tileStyle().displayName() + ". Click for " + view.tileStyle().next().displayName().toLowerCase());
    }

    @Override
    public void copyShareCode(GearSetup setup) {
        transfer.copyShareCode(setup);
    }

    private void showSharedPage(GearSetup shared) {
        pages.replace(SHARE_CARD, new SharePreviewPage(shared, book.sections(),
                sectionId -> transfer.addShared(shared, sectionId), this::showListCard), GuideId.SHARE_CODE);
        pages.show(SHARE_CARD);
    }

    @Override
    public void selectVariant(int index) {
        editor.selectVariant(index);
    }

    @Override
    public void editVariant(int index) {
        editor.editVariant(index);
    }

    @Override
    public void selectVariant(GearSetup setup, int index) {
        setupCommands.showVariant(setup, index);
    }

    @Override
    public void addVariant(GearSetup setup) {
        editContents(setup);
        addVariant(NewVariant.COPY);
    }

    @Override
    public void addVariant(NewVariant start) {
        content.addVariant(start);
    }

    @Override
    public void renameVariant(int index) {
        content.renameVariant(index);
    }

    @Override
    public void duplicateVariant(int index) {
        content.duplicateVariant(index);
    }

    @Override
    public void deleteVariant(int index) {
        content.deleteVariant(index);
    }

    @Override
    public void moveVariant(int index, int to) {
        editor.moveVariant(index, to);
    }

    @Override
    public void compareVariant(int index) {
        editor.setup().ifPresent(setup -> setup.variantAt(index).ifPresent(variant -> {
            compare(setup);
            diffPage().ifPresent(page -> page.choose(DiffPage.Target.variant(variant)));
        }));
    }

    @Override
    public void compare() {
        editor.setup().ifPresent(this::compare);
    }

    @Override
    public void share() {
        editor.setup().ifPresent(this::share);
    }

    /** The subject is the variant on the page, so a card's compare sets its variant against the edited one. */
    @Override
    public void compare(GearSetup opened) {
        editor.open(opened.id());
        GearSetup setup = opened.withSelectedVariant(editor.editingIndex());
        List<GearSetup> others = book.sections().stream()
                .flatMap(section -> section.setups().stream())
                .filter(other -> !other.id().equals(setup.id()) && SetupDiff.comparable(setup.content(), other.content()))
                .collect(Collectors.toList());
        boolean liveOffered = setup.content() instanceof GearContent;
        DiffPage page = new DiffPage(setup, others, liveOffered, icons, target -> compareWith(setup, target), this::showContentCard);
        pages.replace(DIFF_CARD, page, GuideId.COMPARE);
        pages.show(DIFF_CARD);
        if (!others.isEmpty()) {
            page.choose(DiffPage.Target.setup(others.get(0)));
        } else if (liveOffered) {
            page.choose(DiffPage.Target.live());
        }
    }

    private void compareWith(GearSetup subject, DiffPage.Target target) {
        if (target.content().isPresent()) {
            showDiff(subject, target.name(), target.content().get());
            return;
        }
        playerItems.capture(loadout -> showDiff(subject, DiffPage.WORN_NOW, new GearContent(loadout.equipment(), loadout.inventory())),
                () -> say("Log in to compare with what you wear."));
    }

    private void showDiff(GearSetup subject, String otherName, SetupContent other) {
        Optional<DiffPage> page = diffPage();
        if (page.isEmpty()) {
            return;
        }
        facts.warmUp(Ledger.itemsOf(other));
        page.get().show(subject.name(), otherName, SetupDiff.between(subject.content(), other),
                Ledger.of(subject.content(), facts), Ledger.of(other, facts));
    }

    Optional<DiffPage> diffPage() {
        return pages.visible(DiffPage.class);
    }

    @Override
    public void share(GearSetup setup) {
        editor.open(setup.id());
        facts.warmUp(Ledger.itemsOf(setup.content()));
        SharePage page = new SharePage(setup, icons, config.shareLedger(),
                action -> runShare(setup, action), this::showContentCard);
        pages.replace(SHARE_OUT_CARD, page, GuideId.SHARE);
        pages.show(SHARE_OUT_CARD);
    }

    private void runShare(GearSetup setup, SharePage.Action action) {
        boolean withLedger = sharePage().map(SharePage::withLedger).orElse(config.shareLedger());
        say("Preparing " + setup.name() + "…");
        CompletableFuture<String> outcome;
        switch (action) {
            case SAVE_IMAGE:
                outcome = shareService.saveImage(setup, withLedger);
                break;
            case COPY_IMAGE:
                outcome = shareService.copyImage(setup, withLedger);
                break;
            default:
                outcome = shareService.copyText(setup, withLedger);
                break;
        }
        outcome.whenComplete((message, error) -> SwingUtilities.invokeLater(() ->
                say(error == null ? message : "Sharing failed: " + error.getMessage())));
    }

    Optional<SharePage> sharePage() {
        return pages.visible(SharePage.class);
    }

    @Override
    public void setCellKind(CellRef ref, CellKind kind) {
        editor.setCellKind(ref, kind);
    }

    @Override
    public void fillCellFromSetup(CellRef ref) {
        Optional<LayoutCell> cell = editor.cellAt(ref);
        if (cell.isEmpty() || cell.get().isBlank()) {
            return;
        }
        List<CellSources.Source> sources = CellSources.of(book.sections(), cell.get().kind(), editor.id().orElse(null));
        pages.replace(CELL_SOURCE_CARD, new CellSourcePage(ref, sources, source -> {
            editor.fillCell(ref, source.cell(), source.setupName());
            returnToContents();
        }, this::showContentCard), GuideId.CELL_SOURCE);
        pages.show(CELL_SOURCE_CARD);
    }

    Optional<CellSourcePage> cellSourcePage() {
        return pages.visible(CellSourcePage.class);
    }

    @Override
    public void fillCellFromGame(CellRef ref) {
        content.fillCellFromGame(ref);
    }

    @Override
    public void renameCell(CellRef ref) {
        content.renameCell(ref);
    }

    @Override
    public void clearCell(CellRef ref) {
        editor.clearCell(ref);
    }

    @Override
    public void emptyCell(CellRef ref) {
        content.emptyCell(ref);
    }

    // --- guides ---

    GuideRunner guides() {
        return guides;
    }

    /** From a page's ? button: explains what is on screen right now. */
    void startGuide(GuideId id) {
        guides.start(Guides.of(id));
    }

    /** From the Guides list or the front door: opens the page on a sample so there is always something to point at. */
    void startStandaloneGuide(GuideId id) {
        guides.start(Guides.standalone(id));
    }

    public void endGuide() {
        guides.end();
    }

    void showGuides() {
        pages.replace(GUIDES_CARD, new GuidesPage(guideProgress, guide -> startStandaloneGuide(guide.id()), this::showListCard));
        pages.show(GUIDES_CARD);
    }

    Optional<GuidesPage> guidesPage() {
        return pages.visible(GuidesPage.class);
    }

    /** A page's ? button, dotted until that page's guide has been completed once. */
    GuideButton guideButton(GuideId id) {
        return new GuideButton(() -> startGuide(id), !guideProgress.isCompleted(id));
    }

    private void refreshGuideState() {
        if (!guides.isRunning() && isListShowing()) {
            rebuild();
        }
    }

    private void showBankPicture(GearSetup setup) {
        pages.replace(BANK_PICTURE_CARD, new BankPicturePage(setup, sample -> shareService.render(sample, false), this::showListCard));
        pages.show(BANK_PICTURE_CARD);
    }

    Optional<BankPicturePage> bankPicturePage() {
        return pages.visible(BankPicturePage.class);
    }

    @Override
    public void showGuidePage(GuidePage page, SetupId sample) {
        Optional<GearSetup> setup = Optional.ofNullable(sample).flatMap(book::setup);
        switch (page) {
            case HOME:
                showHome();
                break;
            case LIST:
                showListCard();
                break;
            case EDITOR:
                setup.ifPresent(this::edit);
                break;
            case CONTENTS:
                setup.ifPresent(this::editContents);
                break;
            case SLOT:
                setup.ifPresent(found -> {
                    editor.open(found.id());
                    editSlot(firstSlotOf(editor.content().orElse(found.content())));
                });
                break;
            case DIVIDER:
                setup.ifPresent(found -> {
                    editor.open(found.id());
                    firstGridSlotOf(editor.content().orElse(found.content())).ifPresentOrElse(this::editDivider, () -> editContents(found));
                });
                break;
            case BANK_PICTURE:
                setup.ifPresent(this::showBankPicture);
                break;
            case COMPARE:
                setup.ifPresent(this::compare);
                break;
            case SHARE:
                setup.ifPresent(this::share);
                break;
            case HISTORY:
                setup.ifPresent(found -> {
                    editor.open(found.id());
                    showHistory();
                });
                break;
            case HOTKEY:
                setup.ifPresent(this::chooseHotkey);
                break;
            case IMPORT:
                setup.ifPresent(found -> transfer.offerImport("a sample file", List.of(new GearSection(SectionId.random(), "Sample section", List.of(found)))));
                break;
            case BACKUPS:
                transfer.showBackups();
                break;
            case SHARE_CODE:
                setup.ifPresent(this::showSharedPage);
                break;
            case CELL_SOURCE:
                setup.ifPresent(found -> {
                    editor.open(found.id());
                    fillCellFromSetup(CellRef.of(0, 1));
                });
                break;
            default:
                break;
        }
    }

    private static SlotRef firstSlotOf(SetupContent content) {
        if (content instanceof BankContent) {
            return SlotRef.of(GridKind.LEFT, 0);
        }
        if (content instanceof CustomContent) {
            return SlotRef.in(CellRef.of(0, 0), SlotRef.of(EquipmentSlot.WEAPON));
        }
        return SlotRef.of(EquipmentSlot.WEAPON);
    }

    /** The first slot of the first item grid, where a divider guide can start. */
    private static Optional<SlotRef> firstGridSlotOf(SetupContent content) {
        return Layout.of(content).blocks().stream()
                .filter(block -> block instanceof GridBlock)
                .map(block -> ((GridBlock) block).ref(0))
                .findFirst();
    }

    @Override
    public Optional<JComponent> guideAnchor(HelpTopic topic) {
        if (topic == HelpTopic.CELL || topic == HelpTopic.EMPTY_CELL) {
            contentPanel().ifPresent(page -> page.showRowWithCell(topic == HelpTopic.EMPTY_CELL));
        }
        return Help.find(this, topic);
    }

    @Override
    public SetupId createSample(SetupType type) {
        return samples.create(type);
    }

    @Override
    public void removeSample(SetupId sample) {
        samples.remove(sample);
    }

    @Override
    public void keepSample(SetupId sample) {
        samples.keep(sample);
    }

    @Override
    public boolean isBankOpen() {
        return samples.isBankOpen();
    }

    @Override
    public void showInBank(SetupId sample) {
        samples.showInBank(sample);
    }

    @Override
    public void hideFromBank(SetupId sample) {
        samples.hideFromBank(sample);
    }

    @Override
    public boolean askToKeepSample() {
        return samples.askToKeep();
    }

    Optional<SharePreviewPage> sharePreviewPage() {
        return pages.visible(SharePreviewPage.class);
    }

    @Override
    public void sortSection(SectionId sectionId, SetupOrder order) {
        sectionCommands.sort(sectionId, order);
    }

    @Override
    public void exportSection(SectionId sectionId) {
        transfer.exportSection(sectionId);
    }

    private void showImportPage(String source, List<GearSection> imported) {
        pages.replace(IMPORT_CARD, new ImportPage(source, imported,
                () -> transfer.addImported(imported),
                () -> transfer.replaceWithImported(source, imported),
                this::showListCard), GuideId.IMPORT);
        pages.show(IMPORT_CARD);
    }

    Optional<ImportPage> importPage() {
        return pages.visible(ImportPage.class);
    }

    private void showBackupsPage(List<Backup> backups) {
        pages.replace(BACKUPS_CARD, new BackupsPage(backups, clock.getZone(), transfer::restoreBackup, this::showListCard), GuideId.BACKUPS);
        pages.show(BACKUPS_CARD);
    }

    Optional<BackupsPage> backupsPage() {
        return pages.visible(BackupsPage.class);
    }

    private FlatButton newSectionButton() {
        FlatButton button = Help.describe(new FlatButton(ActionIcon.ADD, "New section", "Create a new section", this::createSection), HelpTopic.NEW_SECTION);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    BulkDelete bulkDelete() {
        return bulkDelete;
    }

    void toggleBulkMode() {
        bulkDelete.toggle();
    }

    Transfer transfer() {
        return transfer;
    }

    ContentHost content() {
        return content;
    }

    SetupCommands setupCommands() {
        return setupCommands;
    }

    boolean isBulkMode() {
        return bulkDelete.isOn();
    }

    private List<BulkTarget> visibleTargets() {
        List<BulkTarget> order = new ArrayList<>();
        for (SectionView view : sectionViews()) {
            order.add(BulkTarget.of(view.sectionId()));
            if (!view.isCollapsed()) {
                view.grid().tiles().forEach(tile -> order.add(BulkTarget.of(tile.setup().id())));
            }
        }
        return order;
    }

    private void refreshActiveHighlights() {
        for (SetupTile tile : allTiles()) {
            tile.setSelected(activeSetup.isActive(tile.setup().id()));
        }
    }

    /** Every tile on the list page, pinned ones included; a setup can appear twice. */
    List<SetupTile> allTiles() {
        List<SetupTile> tiles = new ArrayList<>();
        for (Component component : sections.getComponents()) {
            if (component instanceof PinnedRow) {
                tiles.addAll(((PinnedRow) component).grid().tiles());
            } else if (component instanceof SectionView) {
                tiles.addAll(((SectionView) component).grid().tiles());
            }
        }
        return tiles;
    }

    private void refreshBulkHighlights() {
        for (SectionView view : sectionViews()) {
            view.refreshBulkHighlight();
            view.grid().tiles().forEach(SetupTile::refreshBulkHighlight);
        }
    }

    private void updateBulkBar() {
        bulkCount.setText(bulkDelete.countText());
    }

    private JPanel listCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ColorScheme.DARK_GRAY_COLOR);
        card.add(Help.anchor(sections, HelpTopic.LIST_PAGE), BorderLayout.NORTH);
        return card;
    }

    @Override
    public void onBookChanged(GearSetupBook changed) {
        rebuild();
    }

    private void rebuild() {
        preview.hide();
        sections.removeAll();
        String query = searchField.getText();
        boolean searching = !query.isBlank();
        List<GearSection> inScope = AccountFilter.apply(book.sections(), accounts.viewed());
        List<GearSection> visible = filter.apply(inScope, query);
        sectionCommands.dropMissingFolds();
        bulkDelete.dropMissing();

        if (visible.isEmpty()) {
            sections.add(message("No setups match \"" + query.strip() + "\""));
        } else {
            if (book.isEmpty() && !onboarding.isDismissed() && !samples.anyTutorialCompleted()) {
                sections.add(new OnboardingCard(this::startStandaloneGuide, this::dismissOnboarding));
            } else if (book.isEmpty()) {
                sections.add(message("No setups yet. Use + to create one."));
            } else if (inScope.stream().allMatch(GearSection::isEmpty)) {
                sections.add(message("Nothing for " + describeViewed() + " yet."));
            }
            boolean deletable = !bulkDelete.isOn() && book.sectionCount() > 1;
            boolean interactive = !searching && !bulkDelete.isOn();
            List<GearSetup> pinned = inScope.stream().flatMap(section -> section.setups().stream()).filter(GearSetup::isPinned).collect(Collectors.toList());
            if (!pinned.isEmpty() && !searching && !bulkDelete.isOn()) {
                sections.add(new PinnedRow(pinned, icons, this, view.tileStyle(), bulkDelete.isOn() ? HoverPreview.none() : preview));
            }
            for (GearSection section : visible) {
                boolean hiddenUnderParent = !searching && section.parent().filter(sectionCommands::isFolded).isPresent();
                if (hiddenUnderParent) {
                    continue;
                }
                List<GearSection> peers = SectionTree.peersOf(visible, section.id());
                int among = peers.indexOf(section);
                boolean childless = !SectionTree.hasChildren(book.sections(), section.id());
                List<GearSection> nestTargets = interactive && !section.isChild() && childless
                        ? SectionTree.topLevel(book.sections()).stream().filter(other -> !other.id().equals(section.id())).collect(Collectors.toList())
                        : List.of();
                SectionView.Config config = new SectionView.Config(deletable,
                        !searching && sectionCommands.isFolded(section.id()),
                        interactive && among > 0,
                        interactive && among < peers.size() - 1,
                        !searching && section.isChild(),
                        interactive && !section.isChild(),
                        interactive && section.isChild(),
                        nestTargets,
                        !childless);
                sections.add(new SectionView(section, config, icons, this, this,
                        interactive ? drags : null, interactive ? sectionDrags : null,
                        bulkDelete.isOn() ? bulkDelete : null, view.tileStyle(), bulkDelete.isOn() ? HoverPreview.none() : preview));
            }
            if (searching) {
                sections.add(message("Reordering is off while searching"));
            } else if (!bulkDelete.isOn()) {
                sections.add(newSectionButton());
            }
        }
        sections.revalidate();
        sections.repaint();
        refreshBulkHighlights();
        refreshActiveHighlights();
        updateBulkBar();
        if (pendingRename != null) {
            SectionId toRename = pendingRename;
            pendingRename = null;
            sectionViews().stream().filter(view -> view.sectionId().equals(toRename)).findFirst().ifPresent(SectionView::startRename);
        }
        if (isListShowing()) {
            pages.show(LIST_CARD);
        } else if (isHomeShowing()) {
            showHome();
        }
    }

    private JLabel message(String text) {
        JLabel label = Ui.hint(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        return label;
    }

    void createSetup() {
        addSetupTo(book.sections().get(0).id());
    }

    private Owner ownerForNewSetups() {
        return accounts.ownerForNewSetups();
    }

    void toggleSharing(GearSetup setup) {
        accounts.toggleSharing(setup);
    }

    @Override
    public List<Owner> otherOwners(GearSetup setup) {
        return accounts.otherOwners(setup);
    }

    @Override
    public String describeOwner(Owner owner) {
        return accounts.describe(owner);
    }

    @Override
    public void moveTo(GearSetup setup, Owner owner) {
        accounts.moveTo(setup, owner);
    }

    static final String NEW_SECTION_NAME = SectionCommands.NEW_SECTION_NAME;

    void createSection() {
        sectionCommands.create();
    }

    @Override
    public void insertSectionAbove(SectionId sectionId) {
        sectionCommands.insertAbove(sectionId);
    }

    @Override
    public void insertSectionBelow(SectionId sectionId) {
        sectionCommands.insertBelow(sectionId);
    }

    @Override
    public void collapseOthers(SectionId sectionId) {
        sectionCommands.foldOthers(sectionId);
    }

    @Override
    public void duplicate(GearSetup setup) {
        setupCommands.duplicate(setup);
    }

    @Override
    public void togglePin(GearSetup setup) {
        setupCommands.togglePin(setup);
    }

    @Override
    public void setLabel(GearSetup setup, ColourLabel label) {
        setupCommands.setLabel(setup, label);
    }

    @Override
    public void chooseHotkey(GearSetup setup) {
        HotkeyCapturePage page = new HotkeyCapturePage(setup, hotkey -> setHotkey(setup, hotkey), this::showListCard);
        pages.replace(HOTKEY_CARD, page, GuideId.HOTKEY);
        pages.show(HOTKEY_CARD);
        page.requestFocusInWindow();
    }

    Optional<HotkeyCapturePage> hotkeyPage() {
        return pages.visible(HotkeyCapturePage.class);
    }

    void setHotkey(GearSetup setup, Hotkey hotkey) {
        setupCommands.setHotkey(setup, hotkey);
        showListCard();
    }

    @Override
    public void addSetupFromGame(SectionId sectionId) {
        playerItems.capture(loadout -> showEditor(new SetupEditorForm("New from game", null, icons, selectors, artwork, prayerArtwork, new SetupEditorForm.Listener() {
            @Override
            public void onSaved(GearSetup draft) {
                SyncScope scope = draft.type() == SetupType.GEAR ? SyncScope.GEAR : SyncScope.LEFT_SIDE;
                SetupContent content = LoadoutSync.apply(draft.content(), scope, loadout);
                setupCommands.addFromGame(sectionId, draft.withContent(content));
                closeEditor();
            }

            @Override
            public void onCancelled() {
                closeEditor();
            }
        })), () -> say("Log in to make a setup from the game."));
    }

    @Override
    public void addSetupTo(SectionId sectionId) {
        showEditor(new SetupEditorForm("New setup", null, icons, selectors, artwork, prayerArtwork, new SetupEditorForm.Listener() {
            @Override
            public void onSaved(GearSetup draft) {
                setupCommands.add(sectionId, draft);
                closeEditor();
            }

            @Override
            public void onCancelled() {
                closeEditor();
            }
        }));
    }

    @Override
    public void edit(GearSetup setup) {
        showEditor(new SetupEditorForm("Edit setup", setup, icons, selectors, artwork, prayerArtwork, new SetupEditorForm.Listener() {
            @Override
            public void onSaved(GearSetup draft) {
                setupCommands.applyEdit(setup.id(), draft);
                closeEditor();
            }

            @Override
            public void onCancelled() {
                closeEditor();
            }
        }));
    }

    /** The owner line and the search belong to the list; the accounts screen shows neither. */
    private void refreshListChrome() {
        boolean list = !isHomeShowing();
        viewedLine.setVisible(list);
        searchLine.setVisible(list);
        bulkButton.setVisible(list);
    }

    void showListCard() {
        editor.close();
        pages.show(LIST_CARD);
        refreshViewedLine();
        rebuild();
    }

    @Override
    public void editContents(GearSetup setup) {
        editor.open(setup.id());
        showContentCard();
    }

    /** Rebuilds the contents page for the open setup, keeping where the previous one was looking. */
    private void showContentCard() {
        Optional<GearSetup> setup = editor.setup();
        if (setup.isEmpty()) {
            showListCard();
            return;
        }
        dropRule.warmUp(SetupContentEditor.allItems(setup.get().content()).stream().map(SetupItem::id).collect(Collectors.toList()));
        setup.get().variants().forEach(variant -> facts.warmUp(Ledger.itemsOf(variant.content())));
        ContentViewState view = pendingView.apply(pages.any(SetupContentPanel.class).map(SetupContentPanel::viewState).orElse(ContentViewState.initial()));
        pendingView = UnaryOperator.identity();
        SetupContentPanel page = new SetupContentPanel(setup.get(), editor.editingIndex(), icons, slotArtwork, selectors, dropRule, this, this, facts, view);
        pages.replace(CONTENT_CARD, page, Guides.contentsGuideFor(setup.get().type()));
        pages.show(CONTENT_CARD);
    }

    /** Back to the contents page unless a change already put it on show. */
    private void returnToContents() {
        if (!pages.isShowing(CONTENT_CARD)) {
            showContentCard();
        }
    }

    void requestSync(SyncScope scope) {
        content.requestSync(scope);
    }

    void applySync(SyncScope scope) {
        content.applySync(scope);
    }

    @Override
    public void editSlot(SlotRef ref) {
        Optional<SetupContent> content = editor.content();
        if (content.isEmpty()) {
            showListCard();
            return;
        }
        SlotEditorForm.Listener slotListener = new SlotEditorForm.Listener() {
                    @Override
                    public void onSlotSet(SlotRef slot, SetupItem item) {
                        editor.setItem(slot, item);
                        returnToContents();
                    }

                    @Override
                    public void onSlotCleared(SlotRef slot) {
                        editor.clearSlot(slot);
                        returnToContents();
                    }

                    @Override
                    public void onCancelled() {
                        showContentCard();
                    }
                };
        SlotEditorForm form = SetupContentEditor.itemAt(content.get(), ref)
                .map(existing -> SlotEditorForm.forItem(ref, existing, icons, selectors, slotListener))
                .orElseGet(() -> SlotEditorForm.forEmptySlot(ref, icons, selectors, slotListener));
        pages.replace(SLOT_CARD, form, GuideId.SLOT);
        pages.show(SLOT_CARD);
    }

    @Override
    public void clearSlot(SlotRef ref) {
        editor.clearSlot(ref);
    }

    @Override
    public void clearSlots(List<SlotRef> refs) {
        editor.clearSlots(refs);
    }

    @Override
    public void moveItem(SlotRef from, SlotRef to, boolean copy) {
        editor.moveItem(from, to, copy);
    }

    @Override
    public void dropItem(ResolvedItem item, SlotRef to) {
        editor.setItem(to, new SetupItem(item.id()));
    }

    @Override
    public void fillRemaining(SlotRef from) {
        editor.fillRemaining(from);
    }

    @Override
    public void fillRow(SlotRef from) {
        editor.fillRow(from);
    }

    @Override
    public void editDivider(SlotRef slot) {
        if (editor.content().isEmpty()) {
            showListCard();
            return;
        }
        DividerEditorForm form = new DividerEditorForm(slot, editor.dividerAt(slot).orElse(null), editor.dividersAbove(slot), new DividerEditorForm.Listener() {
            @Override
            public void onDividerSet(SlotRef at, Divider divider) {
                setDivider(at, divider);
                returnToContents();
            }

            @Override
            public void onDividerRemoved(SlotRef at) {
                removeDivider(at);
                returnToContents();
            }

            @Override
            public void onCancelled() {
                showContentCard();
            }
        });
        pages.replace(DIVIDER_CARD, form, GuideId.DIVIDER);
        pages.show(DIVIDER_CARD);
    }

    Optional<DividerEditorForm> dividerForm() {
        return pages.visible(DividerEditorForm.class);
    }

    void setDivider(SlotRef slot, Divider divider) {
        editor.setDivider(slot, divider);
    }

    @Override
    public void removeDivider(SlotRef slot) {
        editor.removeDivider(slot);
    }

    @Override
    public void changeDivider(SlotRef slot, DividerChange change) {
        editor.changeDivider(slot, change);
    }

    @Override
    public void copySlots(List<SlotRef> refs) {
        content.copySlots(refs);
    }

    @Override
    public void pasteAt(SlotRef anchor) {
        content.pasteAt(anchor);
    }

    @Override
    public boolean canPaste() {
        return editor.canPaste();
    }

    @Override
    public void sync(SyncScope scope) {
        requestSync(scope);
    }

    @Override
    public void back() {
        showListCard();
    }

    @Override
    public void showHistory() {
        Optional<GearSetup> setup = editor.setup();
        if (setup.isEmpty()) {
            showListCard();
            return;
        }
        HistoryPage historyPage = new HistoryPage(setup.get(), content.revisions(setup.get().id()), clock, this::restoreRevision, this::showContentCard);
        pages.replace(HISTORY_CARD, historyPage, GuideId.HISTORY);
        pages.show(HISTORY_CARD);
    }

    Optional<HistoryPage> historyPage() {
        return pages.visible(HistoryPage.class);
    }

    void restoreRevision(SetupRevision revision) {
        content.restore(revision);
    }

    private void showEditor(SetupEditorForm form) {
        pages.replace(EDITOR_CARD, form, GuideId.EDITOR);
        pages.showOver(EDITOR_CARD);
        form.requestFocusInWindow();
    }

    /** Leaves the editor for the page it was opened from; a content page whose setup is gone falls back to the list. */
    private void closeEditor() {
        if (CONTENT_CARD.equals(pages.beneath()) && editor.setup().isPresent()) {
            showContentCard();
            return;
        }
        showListCard();
    }

    @Override
    public void activate(GearSetup setup) {
        setupCommands.activate(setup);
    }

    @Override
    public void delete(GearSetup setup) {
        setupCommands.delete(setup);
    }

    @Override
    public void toggleSection(SectionId sectionId) {
        sectionCommands.toggleFolded(sectionId);
    }

    boolean isCollapsed(SectionId sectionId) {
        return sectionCommands.isFolded(sectionId);
    }

    @Override
    public void moveSectionUp(SectionId sectionId) {
        sectionCommands.moveUp(sectionId);
    }

    @Override
    public void moveSectionDown(SectionId sectionId) {
        sectionCommands.moveDown(sectionId);
    }

    void onSectionDropped(SectionId sectionId, int targetIndex) {
        sectionCommands.dropped(sectionId, targetIndex);
    }

    @Override
    public void addSubSection(SectionId parentId) {
        sectionCommands.addSub(parentId);
    }

    @Override
    public void nestSection(SectionId sectionId, SectionId parentId) {
        sectionCommands.nest(sectionId, parentId);
    }

    @Override
    public void unnestSection(SectionId sectionId) {
        sectionCommands.unnest(sectionId);
    }

    private SectionView sectionViewAt(Point point) {
        for (SectionView view : sectionViews()) {
            Rectangle bounds = view.getBounds();
            if (point.y >= bounds.y && point.y < bounds.y + bounds.height) {
                return view;
            }
        }
        return null;
    }

    private void springOpen(SectionView view) {
        sectionCommands.unfold(view.sectionId());
        view.expand();
    }

    @Override
    public void renameSection(SectionId sectionId, String newName) {
        sectionCommands.rename(sectionId, newName);
    }

    @Override
    public void deleteSection(SectionId sectionId) {
        sectionCommands.delete(sectionId);
    }

    private SetupGrid nearestGrid(Point point) {
        SetupGrid nearest = null;
        int smallestDistance = Integer.MAX_VALUE;
        for (SectionView view : sectionViews()) {
            if (view.isCollapsed()) {
                continue;
            }
            Rectangle bounds = view.getBounds();
            int distance = point.y < bounds.y ? bounds.y - point.y
                    : point.y >= bounds.y + bounds.height ? point.y - (bounds.y + bounds.height) : 0;
            if (distance < smallestDistance) {
                smallestDistance = distance;
                nearest = view.grid();
            }
        }
        return nearest;
    }

    @Override
    public void onSetupDropped(SetupId setupId, SectionId targetSection, int targetIndex) {
        setupCommands.moveToSection(setupId, targetSection, targetIndex);
    }
}
