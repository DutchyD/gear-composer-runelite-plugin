package dev.dutchy.runelite.gear.ui;

import com.google.gson.Gson;
import dev.dutchy.runelite.gear.CellSources;
import dev.dutchy.runelite.gear.ColourLabel;
import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.Hotkey;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.Prayer;
import dev.dutchy.runelite.gear.Requirements;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupFilter;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.SetupMeta;
import dev.dutchy.runelite.gear.SetupOrder;
import dev.dutchy.runelite.gear.Spellbook;
import dev.dutchy.runelite.gear.UndoHistory;
import dev.dutchy.runelite.gear.account.AccountDirectory;
import dev.dutchy.runelite.gear.account.AccountEntry;
import dev.dutchy.runelite.gear.account.FixedAccount;
import dev.dutchy.runelite.gear.activation.SetupActivator;
import dev.dutchy.runelite.gear.bank.ActiveSetup;
import dev.dutchy.runelite.gear.bank.BankLayout;
import dev.dutchy.runelite.gear.bank.BankLayoutApplier;
import dev.dutchy.runelite.gear.bank.BankLayoutPlanner;
import dev.dutchy.runelite.gear.config.GearComposerConfig;
import dev.dutchy.runelite.gear.config.Onboarding;
import dev.dutchy.runelite.gear.config.ViewSettings;
import dev.dutchy.runelite.gear.content.BankContent;
import dev.dutchy.runelite.gear.content.CellKind;
import dev.dutchy.runelite.gear.content.CellRef;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.Divider;
import dev.dutchy.runelite.gear.content.DividerChange;
import dev.dutchy.runelite.gear.content.DropRule;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.ItemMatch;
import dev.dutchy.runelite.gear.content.LayoutCell;
import dev.dutchy.runelite.gear.content.Loadout;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupType;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.gear.content.SyncScope;
import dev.dutchy.runelite.gear.content.TextAlign;
import dev.dutchy.runelite.gear.guide.Guide;
import dev.dutchy.runelite.gear.guide.GuideId;
import dev.dutchy.runelite.gear.guide.GuidePage;
import dev.dutchy.runelite.gear.guide.GuideProgress;
import dev.dutchy.runelite.gear.guide.GuideStep;
import dev.dutchy.runelite.gear.guide.Guides;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.gear.guide.SampleSetups;
import dev.dutchy.runelite.gear.history.InMemoryHistoryStore;
import dev.dutchy.runelite.gear.history.SetupHistory;
import dev.dutchy.runelite.gear.layout.BankSide;
import dev.dutchy.runelite.gear.ledger.EquipmentStats;
import dev.dutchy.runelite.gear.ledger.ItemFacts;
import dev.dutchy.runelite.gear.ledger.ItemFactsSource;
import dev.dutchy.runelite.gear.persistence.Backup;
import dev.dutchy.runelite.gear.persistence.BackupStore;
import dev.dutchy.runelite.gear.persistence.BookCodec;
import dev.dutchy.runelite.gear.persistence.BookFiles;
import dev.dutchy.runelite.gear.player.PlayerItems;
import dev.dutchy.runelite.gear.requirements.FixedQuickPrayers;
import dev.dutchy.runelite.gear.requirements.FixedSpellbook;
import dev.dutchy.runelite.gear.requirements.RequirementWatch;
import dev.dutchy.runelite.gear.share.ImageSink;
import dev.dutchy.runelite.gear.share.InMemoryClipboard;
import dev.dutchy.runelite.gear.share.ShareCodec;
import dev.dutchy.runelite.gear.share.ShareService;
import dev.dutchy.runelite.gear.transfer.BookTransfer;
import dev.dutchy.runelite.gear.transfer.FileDialogs;
import dev.dutchy.runelite.libs.ui.button.DefaultItemLoader;
import dev.dutchy.runelite.libs.ui.button.ItemButtonFactory;
import dev.dutchy.runelite.libs.ui.button.ItemLoader;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import dev.dutchy.runelite.libs.ui.search.CachingItemIndex;
import dev.dutchy.runelite.libs.ui.search.FuzzyItemSearch;
import dev.dutchy.runelite.libs.ui.search.ItemCatalog;
import dev.dutchy.runelite.libs.ui.search.ItemRanker;
import dev.dutchy.runelite.libs.ui.search.SubsequenceNameScorer;
import dev.dutchy.runelite.libs.ui.selector.ItemSelector;
import dev.dutchy.runelite.libs.ui.selector.ItemSelectorFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GearSetupPanelTest {

    private static final ResolvedItem WHIP = ResolvedItem.of(4151, "Abyssal whip");
    private static final Loadout WORN_AND_CARRIED = new Loadout(
            Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151, 1), EquipmentSlot.RING, SetupItem.of(2550, 1)),
            ItemGrid.EMPTY.withSlot(0, SetupItem.of(995, 12_000)).withSlot(27, SetupItem.of(385, 1)));

    private GearSetupBook book;
    private SectionId firstSection;
    private GearSetupPanel panel;
    private List<BankLayout> appliedLayouts;
    private ActiveSetup activeSetup;
    private List<Boolean> cleared;
    private Loadout loadout;
    private FixedAccount account;
    private Onboarding onboarding;
    private UndoHistory history;
    private SetupHistory setupHistory;
    private SetupActivator activator;
    private FixedSpellbook spellbook;
    private FixedQuickPrayers quickPrayers;
    private AccountDirectory directory;
    private List<String> backupDocuments;
    private Path nextOpenFile;
    private Path nextSaveFile;
    private InMemoryClipboard clipboard;
    private ItemFactsSource facts;
    private ShareService shareService;
    private GuideProgress guideProgress;
    private List<String> savedImages;
    private List<BufferedImage> copiedImages;
    private RecordingPrompts prompts;

    @BeforeEach
    void setUp() throws Exception {
        StaticItemResolver resolver = StaticItemResolver.of(WHIP);
        ItemLoader loader = new DefaultItemLoader(resolver,
                request -> new LoadedItemImage(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB)));
        ItemSelectorFactory selectors = new ItemSelectorFactory(
                new FuzzyItemSearch(new CachingItemIndex(ItemCatalog.of(List.of(WHIP))),
                        new ItemRanker(new SubsequenceNameScorer()), resolver, Runnable::run),
                new ItemButtonFactory(loader));

        book = new GearSetupBook();
        firstSection = book.sections().get(0).id();
        appliedLayouts = new ArrayList<>();
        cleared = new ArrayList<>();
        loadout = WORN_AND_CARRIED;
        activeSetup = new ActiveSetup();
        PlayerItems playerItems = (onCaptured, onUnavailable) ->
                Optional.ofNullable(loadout).ifPresentOrElse(onCaptured, onUnavailable);
        account = FixedAccount.of("rsprofile.main", "Dutchy");
        onboarding = Onboarding.inMemory();
        history = new UndoHistory(book);
        setupHistory = new SetupHistory(book, new InMemoryHistoryStore(), Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneOffset.UTC));
        BankLayoutApplier applier = new BankLayoutApplier() {
            @Override
            public void apply(BankLayout layout) {
                appliedLayouts.add(layout);
            }

            @Override
            public void clear() {
                cleared.add(Boolean.TRUE);
            }
        };
        activator = new SetupActivator(book, activeSetup, new BankLayoutPlanner(), applier);
        spellbook = new FixedSpellbook(Spellbook.STANDARD);
        quickPrayers = new FixedQuickPrayers(Set.of());
        directory = AccountDirectory.inMemory();
        backupDocuments = new ArrayList<>();
        BookCodec codec = new BookCodec(new Gson());
        BackupStore backups = new BackupStore() {
            @Override
            public void save(String encoded) {
                backupDocuments.add(0, encoded);
            }

            @Override
            public List<Backup> list() {
                List<Backup> list = new ArrayList<>();
                for (int i = 0; i < backupDocuments.size(); i++) {
                    list.add(new Backup(Path.of("backup-" + i + ".json"), Instant.parse("2026-09-08T10:00:00Z").minusSeconds(i)));
                }
                return list;
            }

            @Override
            public String read(Backup backup) {
                return backupDocuments.get(Integer.parseInt(backup.file().toString().replaceAll("\\D", "")));
            }
        };
        BookTransfer transfer = new BookTransfer(new BookFiles(codec), backups, codec);
        clipboard = new InMemoryClipboard();
        guideProgress = GuideProgress.inMemory();
        facts = ItemFactsSource.fixed(Map.of(
                ItemId.of(4151), new ItemFacts(2_500_000, 0.4, new EquipmentStats(0, 82, 0, 0, 0, 0, 0, 0, 0, 0, 82, 0, 0, 0, 4)),
                ItemId.of(385), ItemFacts.unworn(900, 0.5),
                ItemId.of(995), ItemFacts.unworn(1, 0)));
        savedImages = new ArrayList<>();
        copiedImages = new ArrayList<>();
        ImageSink sink = new ImageSink() {
            @Override
            public String save(BufferedImage image, String fileName) {
                savedImages.add(fileName);
                return "the test folder";
            }

            @Override
            public void copy(BufferedImage image) {
                copiedImages.add(image);
            }
        };
        shareService = new ShareService(loader, facts, sink, clipboard);
        FileDialogs dialogs = new FileDialogs() {
            @Override
            public Optional<Path> chooseFileToOpen() {
                return Optional.ofNullable(nextOpenFile);
            }

            @Override
            public Optional<Path> chooseFileToSave(String suggestedName) {
                return Optional.ofNullable(nextSaveFile);
            }
        };
        GearComposerConfig config = new GearComposerConfig() {
        };
        prompts = new RecordingPrompts();
        panel = onEdt(() -> new GearSetupPanel(book, new SetupFilter(new SubsequenceNameScorer()), activeSetup, playerItems,
                config, onboarding, history, new RequirementWatch(book, activeSetup, spellbook, quickPrayers), guideProgress,
                shareService, setupHistory,
                new BulkDelete(book, setupHistory),
                new Transfer(book, transfer, dialogs, new ShareCodec(new Gson()), clipboard),
                new GuideSamples(book, setupHistory, activator, activeSetup, applier, guideProgress),
                new Accounts(book, directory, account),
                new SectionCommands(book),
                new SetupCommands(book, setupHistory, activator),
                new PageParts(new ItemIconFactory(loader), selectors, (type, onLoaded) -> onLoaded.accept(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)),
                        PrayerArtwork.none(), EquipmentSlotArtwork.none(), facts, ViewSettings.inMemory(TileStyle.GRID),
                        new DropRule(item -> item.equals(ItemId.of(4151)) ? Optional.of(EquipmentSlot.WEAPON) : Optional.empty()),
                        Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneOffset.UTC)),
                open -> prompts.reportingTo(open::say, open::announce)));
    }

    @Test
    void emptyBookShowsTheGettingStartedCardUntilDismissed() throws Exception {
        assertTrue(panel.isOnboardingShowing());
        assertTrue(labels().contains(OnboardingCard.TITLE));
        assertEquals(1, panel.sectionViews().size());

        onEdt(() -> {
            panel.dismissOnboarding();
            return null;
        });

        assertFalse(panel.isOnboardingShowing());
        assertTrue(onboarding.isDismissed());
        assertTrue(labels().contains("No setups yet. Use + to create one."));
    }

    @Test
    void everyChangeCanBeUndoneAndRedoneFromThePanel() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            book.removeSetup(setup.id());
            return null;
        });
        assertTrue(tileNames().isEmpty());

        onEdt(() -> {
            panel.undo();
            return null;
        });
        assertEquals(List.of("Vorkath"), tileNames());

        onEdt(() -> {
            panel.redo();
            return null;
        });
        assertTrue(tileNames().isEmpty());
    }

    @Test
    void changesMadeThroughThePanelOfferUndoInTheStatusLine() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        assertFalse(panel.isUndoOffered());

        onEdt(() -> {
            panel.editContents(setup);
            panel.applySync(SyncScope.GEAR);
            return null;
        });

        assertTrue(panel.isUndoOffered());
        onEdt(() -> {
            panel.undo();
            return null;
        });
        assertTrue(book.setup(setup.id()).orElseThrow().content().isEmpty(), "the sync was taken back");
    }

    @Test
    void theListFollowsTheViewedOwnerAndLogoutOpensTheAccountsScreen() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            panel.editorForm().orElseThrow().setEnteredName("Mine");
            panel.editorForm().orElseThrow().save();
            return null;
        });
        assertEquals(Owner.account("rsprofile.main"), book.sections().get(0).setups().get(0).owner());
        assertEquals("Dutchy", panel.describeViewed());

        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Iron only").withContent(GearContent.empty()).withOwner(Owner.account("rsprofile.iron"))));
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Everyone").withContent(GearContent.empty()).withOwner(Owner.shared())));
        assertEquals(List.of("Mine", "Everyone"), tileNames());

        onEdt(() -> {
            panel.viewOwner(Owner.account("rsprofile.iron"));
            return null;
        });
        assertEquals(List.of("Iron only", "Everyone"), tileNames(), "another account's list shows its setups plus the shared ones");
        onEdt(() -> {
            panel.viewOwner(Owner.shared());
            return null;
        });
        assertEquals(List.of("Everyone"), tileNames(), "the shared list shows shared setups only");

        onEdt(() -> {
            panel.showListCard();
            account.logOut();
            return null;
        });
        assertTrue(panel.isHomeShowing(), "logging out opens the accounts screen");
        onEdt(() -> {
            account.logIn("rsprofile.iron", "Ironman");
            return null;
        });
        assertFalse(panel.isHomeShowing());
        assertEquals("Ironman", panel.describeViewed(), "logging in opens that character's list");
        assertEquals(Optional.of("Ironman"), directory.nameOf("rsprofile.iron"), "and remembers the name");
    }

    @Test
    void theAccountsScreenListsEveryKnownOwnerWithCountsAndForgetsEmptyOnes() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Iron only").withContent(GearContent.empty()).withOwner(Owner.account("rsprofile.iron"))));
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Everyone").withContent(GearContent.empty()).withOwner(Owner.shared())));
        directory.remember("rsprofile.iron", "Ironman");
        directory.remember("rsprofile.old", "Retired");
        onEdt(() -> {
            panel.showHome();
            return null;
        });
        AccountsPage page = panel.accountsPage().orElseThrow();
        List<String> names = page.cards().stream().map(card -> card.entry().name()).collect(Collectors.toList());
        assertEquals(List.of(AccountEntry.SHARED_NAME, "Dutchy", "Ironman", "Retired"), names, "shared first, then accounts by name");
        AccountCardView mine = page.cards().get(1);
        assertTrue(mine.entry().loggedIn());
        assertEquals(0, mine.entry().setupCount());
        assertEquals(1, page.cards().get(0).entry().setupCount());
        assertEquals("e.iron", page.cards().get(2).entry().shortId());
        assertTrue(buttonTexts().contains(AccountsPage.BACK_TO_MINE), "logged in, the screen offers the way back");

        AccountCardView retired = page.cards().get(3);
        assertTrue(retired.entry().canForget());
        assertTrue(onEdt(() -> retired.contextMenu().entryTexts()).contains(AccountCardView.FORGET));
        assertFalse(onEdt(() -> page.cards().get(2).contextMenu().entryTexts()).contains(AccountCardView.FORGET), "an account with setups cannot be forgotten");
        onEdt(() -> {
            panel.forgetAccount(retired.entry());
            return null;
        });
        assertTrue(directory.nameOf("rsprofile.old").isEmpty());
        assertEquals(3, panel.accountsPage().orElseThrow().cards().size());

        onEdt(() -> {
            chipOrButton(panel.accountsPage().orElseThrow(), AccountsPage.BACK_TO_MINE).doClick();
            return null;
        });
        assertFalse(panel.isHomeShowing());
        assertEquals("Dutchy", panel.describeViewed());
    }

    @Test
    void setupsMoveBetweenOwnersFromTheTileMenu() throws Exception {
        directory.remember("rsprofile.iron", "Ironman");
        addSetups("Vorkath");
        GearSetup shared = book.sections().get(0).setups().get(0);
        assertTrue(shared.owner().isShared());
        List<String> entries = onEdt(() -> panel.sectionViews().get(0).grid().tiles().get(0).contextMenu().entryTexts());
        assertTrue(entries.contains(SetupTile.moveTo("Dutchy")));
        assertTrue(entries.contains(SetupTile.moveTo("Ironman")));
        assertFalse(entries.contains(SetupTile.SHARE_WITH_ALL), "already shared");

        onEdt(() -> {
            panel.toggleSharing(shared);
            return null;
        });
        GearSetup owned = book.setup(shared.id()).orElseThrow();
        assertEquals(Owner.account("rsprofile.main"), owned.owner(), "the viewed account takes it");
        entries = onEdt(() -> panel.sectionViews().get(0).grid().tiles().get(0).contextMenu().entryTexts());
        assertTrue(entries.contains(SetupTile.SHARE_WITH_ALL));
        assertFalse(entries.contains(SetupTile.moveTo("Dutchy")));

        onEdt(() -> {
            panel.moveTo(owned, Owner.account("rsprofile.iron"));
            return null;
        });
        assertEquals(Owner.account("rsprofile.iron"), book.setup(shared.id()).orElseThrow().owner());
        assertTrue(tileNames().isEmpty(), "moved away from the list on show");
        assertTrue(labels().contains("Moved Vorkath to Ironman"));
    }


    @Test
    void theSlotEditorKeepsMatchRuleAndAlternatives() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(EquipmentSlot.HANDS));
            return null;
        });
        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        assertEquals(ItemMatch.ANY_VARIANT, form.chosenMatch(), "variants match by default");

        onEdt(() -> {
            form.chooseItem(ItemId.of(7462));
            form.setMatch(ItemMatch.EXACT);
            form.addAlternative(ItemId.of(11126));
            form.addAlternative(ItemId.of(7462));
            form.addAlternative(ItemId.of(11126));
            form.addAlternative(ItemId.of(2552));
            form.removeAlternative(ItemId.of(2552));
            form.save();
            return null;
        });

        SetupItem stored = ((GearContent) book.setup(setup.id()).orElseThrow().content()).equipped(EquipmentSlot.HANDS).orElseThrow();
        assertEquals(ItemMatch.EXACT, stored.match());
        assertEquals(List.of(ItemId.of(11126)), stored.alternatives());

        onEdt(() -> {
            panel.editSlot(SlotRef.of(EquipmentSlot.HANDS));
            return null;
        });
        SlotEditorForm reopened = panel.slotEditorForm().orElseThrow();
        assertEquals(ItemMatch.EXACT, reopened.chosenMatch());
        assertEquals(List.of(ItemId.of(11126)), reopened.alternatives());
    }

    @Test
    void rendersATilePerSetupInGridOrder() throws Exception {
        addSetups("Vorkath", "Zulrah", "Jad");
        assertEquals(List.of("Vorkath", "Zulrah", "Jad"), tileNames());
        assertEquals(4, SetupGrid.COLUMNS, "the grid the user asked for is four wide");
    }

    @Test
    void showsEachSectionSeparately() throws Exception {
        SectionId skilling = onEdt(() -> book.addSection("Skilling").id());
        onEdt(() -> book.addSetup(firstSection, "Vorkath"));
        onEdt(() -> book.addSetup(skilling, "Wintertodt"));

        assertEquals(2, panel.sectionViews().size());
        assertEquals(List.of("Vorkath"), namesIn(0));
        assertEquals(List.of("Wintertodt"), namesIn(1));
    }

    @Test
    void searchNarrowsTheGridToMatchingSetups() throws Exception {
        addSetups("Vorkath", "Zulrah");
        onEdt(() -> {
            panel.search("vork");
            return null;
        });
        assertEquals(List.of("Vorkath"), tileNames());
    }

    @Test
    void searchWithNoMatchesSaysSo() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.search("nothing");
            return null;
        });
        assertTrue(labels().contains("No setups match \"nothing\""));
        assertTrue(panel.sectionViews().isEmpty());
    }

    @Test
    void reorderingIsOffWhileSearchingSoDropsCannotLandWrong() throws Exception {
        addSetups("Vorkath", "Zulrah");
        onEdt(() -> {
            panel.search("vork");
            return null;
        });
        assertTrue(labels().contains("Reordering is off while searching"));

        onEdt(() -> {
            panel.search("");
            return null;
        });
        assertFalse(labels().contains("Reordering is off while searching"));
    }

    @Test
    void plusOpensTheEditorAndSavingAddsTheSetup() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });
        assertTrue(panel.isEditorShowing());

        SetupEditorForm form = panel.editorForm().orElseThrow();
        onEdt(() -> {
            form.setEnteredName("Vorkath");
            form.save();
            return null;
        });

        assertEquals(1, book.setupCount());
        assertEquals(List.of("Vorkath"), tileNames());
        assertFalse(panel.isEditorShowing(), "returns to the list after saving");
    }

    @Test
    void theEditorRefusesABlankName() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });
        SetupEditorForm form = panel.editorForm().orElseThrow();

        onEdt(() -> {
            form.setEnteredName("   ");
            form.save();
            return null;
        });

        assertFalse(form.isSaveEnabled());
        assertEquals(0, book.setupCount());
    }

    @Test
    void anIconCanBeChosenAndCleared() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });
        SetupEditorForm form = panel.editorForm().orElseThrow();

        onEdt(() -> {
            form.setEnteredName("Vorkath");
            form.chooseIcon(ItemId.of(4151));
            return null;
        });
        assertEquals(Optional.of(ItemId.of(4151)), form.chosenIcon());

        onEdt(() -> {
            form.clearIcon();
            form.save();
            return null;
        });
        assertEquals(Optional.empty(), book.sections().get(0).setups().get(0).icon());
    }

    @Test
    void pickingASearchResultSetsTheIconAndClearingTheSearchRemovesIt() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });
        SetupEditorForm form = panel.editorForm().orElseThrow();
        ItemSelector selector = find(form, ItemSelector.class).orElseThrow();

        onEdt(() -> {
            selector.setQuery("whip");
            selector.selectHighlighted();
            return null;
        });
        assertEquals(Optional.of(ItemId.of(4151)), form.chosenIcon());
        assertEquals("Abyssal whip", selector.query(), "the search field shows what was chosen");

        onEdt(() -> {
            selector.clear();
            return null;
        });
        assertEquals(Optional.empty(), form.chosenIcon(), "clearing the search also drops the icon");
    }

    @Test
    void anExistingIconShowsUpInTheSearchField() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.of("Vorkath", ItemId.of(4151))));
        GearSetup existing = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.edit(existing);
            return null;
        });
        flushEdt();

        SetupEditorForm form = panel.editorForm().orElseThrow();
        ItemSelector selector = find(form, ItemSelector.class).orElseThrow();
        assertEquals(Optional.of(ItemId.of(4151)), form.chosenIcon());
        assertEquals("Abyssal whip", selector.query());
    }

    @Test
    void clearingTheSlotSearchForgetsTheItemAndBlocksSaving() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(EquipmentSlot.WEAPON));
            return null;
        });
        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        ItemSelector selector = find(form, ItemSelector.class).orElseThrow();

        onEdt(() -> {
            form.chooseItem(ItemId.of(4151));
            return null;
        });
        assertTrue(form.isSaveEnabled());

        onEdt(() -> {
            selector.clear();
            return null;
        });
        assertEquals(Optional.empty(), form.chosenItem());
        assertFalse(form.isSaveEnabled());
    }

    @Test
    void theListEndsWithANewSectionButtonExceptWhileSearchingOrBulkDeleting() throws Exception {
        addSetups("Vorkath");
        assertTrue(buttonTexts().contains("New section"));

        onEdt(() -> {
            panel.search("vork");
            return null;
        });
        assertFalse(buttonTexts().contains("New section"), "no section changes while a filter is on");

        onEdt(() -> {
            panel.search("");
            panel.bulkDelete().set(true);
            return null;
        });
        assertFalse(buttonTexts().contains("New section"), "no section changes while picking what to delete");

        onEdt(() -> {
            panel.bulkDelete().set(false);
            return null;
        });
        assertTrue(buttonTexts().contains("New section"));
    }

    @Test
    void amountPresetsFollowTheFieldHoweverItWasFilled() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 0));
            return null;
        });
        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        assertEquals(Optional.of("BANK"), form.chosenPreset(), "an empty amount means the bank amount");

        onEdt(() -> {
            form.setEnteredQuantity("1000");
            return null;
        });
        assertEquals(Optional.of("1K"), form.chosenPreset(), "a typed amount lights the matching preset");

        onEdt(() -> {
            form.setEnteredQuantity("max");
            return null;
        });
        assertEquals(Optional.of("MAX"), form.chosenPreset());

        onEdt(() -> {
            form.setEnteredQuantity("7");
            return null;
        });
        assertEquals(Optional.empty(), form.chosenPreset());

        onEdt(() -> {
            chip(form, "100K").doClick();
            return null;
        });
        assertEquals(100_000, form.enteredQuantity().getAsInt());
        assertEquals(Optional.of("100K"), form.chosenPreset());

        onEdt(() -> {
            chip(form, "BANK").doClick();
            return null;
        });
        assertTrue(form.enteredQuantity().isEmpty());
        assertEquals(Optional.of("BANK"), form.chosenPreset());
    }

    @Test
    void suffixedAndMaxAmountsAreSaved() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 0));
            panel.slotEditorForm().orElseThrow().chooseItem(ItemId.of(995));
            panel.slotEditorForm().orElseThrow().setEnteredQuantity("2.5M");
            panel.slotEditorForm().orElseThrow().save();
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 1));
            panel.slotEditorForm().orElseThrow().chooseItem(ItemId.of(995));
            panel.slotEditorForm().orElseThrow().setEnteredQuantity("MAX");
            panel.slotEditorForm().orElseThrow().save();
            return null;
        });

        GearContent content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(2_500_000, content.inventory().slot(0).orElseThrow().quantity().orElseThrow());
        assertEquals(Integer.MAX_VALUE, content.inventory().slot(1).orElseThrow().quantity().orElseThrow());
    }

    @Test
    void bulkSelectionIsMarkedApartFromTheActiveSetup() throws Exception {
        addSetups("Vorkath", "Zulrah");
        GearSetup vorkath = book.sections().get(0).setups().get(0);
        GearSetup zulrah = book.sections().get(0).setups().get(1);

        onEdt(() -> {
            panel.activate(zulrah);
            panel.bulkDelete().set(true);
            panel.bulkDelete().select(BulkTarget.of(vorkath.id()), false);
            return null;
        });

        List<SetupTile> tiles = panel.sectionViews().get(0).grid().tiles();
        assertTrue(tiles.get(0).isMarkedForDeletion());
        assertFalse(tiles.get(0).isSelected());
        assertFalse(tiles.get(1).isMarkedForDeletion());
        assertTrue(tiles.get(1).isSelected(), "the active setup keeps its own highlight");
    }

    private static Chip chip(Container container, String text) {
        for (Component child : container.getComponents()) {
            if (child instanceof Chip && text.equals(((Chip) child).getText())) {
                return (Chip) child;
            }
            if (child instanceof Container) {
                Container nested = (Container) child;
                Chip found = chipOrNull(nested, text);
                if (found != null) {
                    return found;
                }
            }
        }
        throw new AssertionError("No preset " + text);
    }

    private static Chip chipOrNull(Container container, String text) {
        try {
            return chip(container, text);
        } catch (AssertionError e) {
            return null;
        }
    }

    @Test
    void syncingAGearSetupTakesWhatThePlayerWearsAndCarries() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withEquipped(EquipmentSlot.HEAD, SetupItem.of(1163)))));
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.editContents(setup);
            panel.applySync(SyncScope.GEAR);
            return null;
        });

        GearContent content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(WORN_AND_CARRIED.equipment(), content.equipment());
        assertEquals(WORN_AND_CARRIED.inventory(), content.inventory());
        assertTrue(panel.contentPanel().isPresent(), "the page stays open and shows the new items");
        assertEquals(Optional.of(SetupItem.of(995, 12_000)),
                panel.contentPanel().orElseThrow().slots().stream()
                        .filter(slot -> slot.ref().equals(SlotRef.of(GridKind.INVENTORY, 0)))
                        .findFirst().orElseThrow().item());
        assertTrue(labels().contains("Vorkath synced from the game"));
    }

    @Test
    void syncingABankSideFillsOnlyThatSideFromTheInventory() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Skilling").withContent(BankContent.empty().withRight(ItemGrid.EMPTY.withSlot(2, SetupItem.of(1163))))));
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.editContents(setup);
            panel.applySync(SyncScope.LEFT_SIDE);
            return null;
        });

        BankContent content = (BankContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(WORN_AND_CARRIED.inventory(), content.left());
        assertEquals(Optional.of(SetupItem.of(1163)), content.right().slot(2), "the right side is untouched");
    }

    @Test
    void syncingWhileLoggedOutChangesNothingAndSaysWhy() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        loadout = null;

        onEdt(() -> {
            panel.editContents(setup);
            panel.applySync(SyncScope.GEAR);
            return null;
        });

        assertTrue(book.setup(setup.id()).orElseThrow().content().isEmpty());
        assertTrue(labels().contains("Log in to sync from the game."));
    }

    @Test
    void gearSetupsOfferOneSyncButtonAndBankSetupsOnePerSide() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        assertEquals(1, buttonTexts().stream().filter(SetupContentPanel.SYNC_GEAR::equals).count());
        assertFalse(buttonTexts().contains(SetupContentPanel.SYNC_SIDE));

        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Skilling").withContent(BankContent.empty())));
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(1));
            return null;
        });
        assertEquals(2, buttonTexts().stream().filter(SetupContentPanel.SYNC_SIDE::equals).count());
        assertFalse(buttonTexts().contains(SetupContentPanel.SYNC_GEAR));
    }

    @Test
    void slotMenusOfferWhatMakesSenseForTheSlot() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(4151))))));
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        List<SlotView> slots = panel.contentPanel().orElseThrow().slots();
        SlotView filled = slots.stream().filter(slot -> slot.item().isPresent()).findFirst().orElseThrow();
        SlotView empty = slots.stream().filter(slot -> slot.item().isEmpty()).findFirst().orElseThrow();

        assertEquals(List.of(SlotView.CHANGE_ITEM, SlotView.FILL_REMAINING, SlotView.FILL_ROW, SlotView.ADD_DIVIDER, SlotView.DELETE_ITEM),
                onEdt(() -> filled.contextMenu().entryTexts()));
        assertEquals(List.of(SlotView.ADD_ITEM), onEdt(() -> empty.contextMenu().entryTexts()), "an equipment slot offers no divider");
    }

    @Test
    void deletingFromTheSlotMenuEmptiesTheSlotAndStaysOnThePage() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(3, SetupItem.of(4151))))));
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            return null;
        });
        SlotView filled = panel.contentPanel().orElseThrow().slots().stream()
                .filter(slot -> slot.item().isPresent()).findFirst().orElseThrow();

        onEdt(() -> {
            filled.contextMenu().invoke(SlotView.DELETE_ITEM);
            return null;
        });

        GearContent content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertTrue(content.inventory().isEmpty());
        assertTrue(panel.contentPanel().isPresent());
        assertTrue(panel.contentPanel().orElseThrow().slots().stream().allMatch(slot -> slot.item().isEmpty()));
    }

    @Test
    void choosingChangeOrAddFromTheSlotMenuOpensTheSlotEditor() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        SlotView empty = panel.contentPanel().orElseThrow().slots().get(0);

        onEdt(() -> {
            empty.contextMenu().invoke(SlotView.ADD_ITEM);
            return null;
        });

        assertTrue(panel.slotEditorForm().isPresent());
    }

    @Test
    void theTileMenuOffersContentsRenameAndDelete() throws Exception {
        addSetups("Vorkath");
        SetupTile tile = panel.sectionViews().get(0).grid().tiles().get(0);
        assertEquals(List.of("Edit contents", SetupTile.VARIANTS, SetupTile.DUPLICATE, "Rename", SetupTile.PIN, SetupTile.SET_HOTKEY, SetupTile.COPY_SHARE_CODE, SetupTile.COMPARE, SetupTile.SHARE, SetupTile.moveTo("Dutchy"), "Delete"),
                onEdt(() -> tile.contextMenu().entryTexts()));
    }

    @Test
    void itemsCanBeMovedSwappedAndCopiedBetweenSlots() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(4151)).withSlot(1, SetupItem.of(995, 5))))));
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.moveItem(SlotRef.of(GridKind.INVENTORY, 0), SlotRef.of(GridKind.INVENTORY, 1), false);
            return null;
        });
        GearContent swapped = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(Optional.of(SetupItem.of(995, 5)), swapped.inventory().slot(0));
        assertEquals(Optional.of(SetupItem.of(4151)), swapped.inventory().slot(1));

        onEdt(() -> {
            panel.moveItem(SlotRef.of(GridKind.INVENTORY, 1), SlotRef.of(EquipmentSlot.WEAPON), true);
            return null;
        });
        GearContent copied = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(Optional.of(SetupItem.of(4151)), copied.equipped(EquipmentSlot.WEAPON));
        assertEquals(Optional.of(SetupItem.of(4151)), copied.inventory().slot(1), "a copy leaves the source");
        assertTrue(panel.contentPanel().isPresent(), "the page stays open");
    }

    @Test
    void theDropRuleKeepsAWhipOutOfTheHeadSlot() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(4151))))));
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        SetupContentPanel content = panel.contentPanel().orElseThrow();
        SlotView whip = content.slots().stream().filter(slot -> slot.item().isPresent()).findFirst().orElseThrow();
        MouseEvent press = new MouseEvent(whip, MouseEvent.MOUSE_PRESSED, 0, InputEvent.BUTTON1_DOWN_MASK, 5, 5, 1, false, MouseEvent.BUTTON1);
        onEdt(() -> {
            content.pressed(whip, press);
            return null;
        });
        assertTrue(content.drags().allows(SlotRef.of(EquipmentSlot.WEAPON)));
        assertFalse(content.drags().allows(SlotRef.of(EquipmentSlot.HEAD)));
        assertTrue(content.drags().allows(SlotRef.of(GridKind.INVENTORY, 7)));
        onEdt(() -> {
            content.drags().cancel();
            return null;
        });
    }

    @Test
    void aSearchResultCanBeDroppedIntoASlot() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.contentPanel().orElseThrow().toggleFinder();
            return null;
        });
        assertTrue(panel.contentPanel().orElseThrow().isFinderShowing());

        onEdt(() -> {
            panel.dropItem(WHIP, SlotRef.of(GridKind.INVENTORY, 3));
            return null;
        });
        GearContent content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(Optional.of(SetupItem.of(4151)), content.inventory().slot(3));

        onEdt(() -> {
            SetupContentPanel page = panel.contentPanel().orElseThrow();
            page.select(SlotRef.of(GridKind.INVENTORY, 9), false, false);
            page.finder().setQuery("whip");
            page.finder().selectHighlighted();
            return null;
        });
        content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(Optional.of(SetupItem.of(4151)), content.inventory().slot(9), "clicking a result fills the one selected slot");
        flushEdt();
        assertTrue(panel.contentPanel().orElseThrow().isFinderShowing(), "the finder survives the edit");
        assertEquals(1, panel.contentPanel().orElseThrow().finder().resultCount(), "the result stays put to be dragged");
        assertEquals("whip", panel.contentPanel().orElseThrow().finder().query(), "the query is kept as typed");
    }

    @Test
    void selectedSlotsCanBeCopiedPastedIntoAnotherSetupAndCleared() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Source").withContent(GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385)).withSlot(1, SetupItem.of(385)).withSlot(2, SetupItem.of(995, 100))))));
        addSetups("Target");
        GearSetup source = book.sections().get(0).setups().get(0);
        GearSetup target = book.sections().get(0).setups().get(1);

        onEdt(() -> {
            panel.editContents(source);
            SetupContentPanel page = panel.contentPanel().orElseThrow();
            page.select(SlotRef.of(GridKind.INVENTORY, 0), false, false);
            page.select(SlotRef.of(GridKind.INVENTORY, 2), true, false);
            assertEquals(3, page.selectedSlots().size());
            page.copySelection();
            return null;
        });
        assertTrue(panel.canPaste());

        onEdt(() -> {
            panel.editContents(target);
            SetupContentPanel page = panel.contentPanel().orElseThrow();
            page.select(SlotRef.of(GridKind.INVENTORY, 10), false, false);
            page.pasteSelection();
            return null;
        });
        GearContent pasted = (GearContent) book.setup(target.id()).orElseThrow().content();
        assertEquals(Optional.of(SetupItem.of(385)), pasted.inventory().slot(10));
        assertEquals(Optional.of(SetupItem.of(995, 100)), pasted.inventory().slot(12));

        onEdt(() -> {
            SetupContentPanel page = panel.contentPanel().orElseThrow();
            page.select(SlotRef.of(GridKind.INVENTORY, 10), false, false);
            page.select(SlotRef.of(GridKind.INVENTORY, 12), false, true);
            page.deleteSelection();
            return null;
        });
        GearContent cleared = (GearContent) book.setup(target.id()).orElseThrow().content();
        assertTrue(cleared.inventory().slot(10).isEmpty());
        assertEquals(Optional.of(SetupItem.of(385)), cleared.inventory().slot(11), "only selected slots were emptied");
    }

    @Test
    void quickFillOffersOnlyOnGridSlotsWithAnItemAndFillsTheRest() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151)).withInventory(ItemGrid.EMPTY.withSlot(20, SetupItem.of(385))))));
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            return null;
        });
        SetupContentPanel page = panel.contentPanel().orElseThrow();
        SlotView weapon = page.slots().stream().filter(slot -> slot.ref().equals(SlotRef.of(EquipmentSlot.WEAPON))).findFirst().orElseThrow();
        SlotView shark = page.slots().stream().filter(slot -> slot.ref().equals(SlotRef.of(GridKind.INVENTORY, 20))).findFirst().orElseThrow();
        assertFalse(onEdt(() -> weapon.contextMenu().entryTexts()).contains(SlotView.FILL_REMAINING));
        assertTrue(onEdt(() -> shark.contextMenu().entryTexts()).contains(SlotView.FILL_REMAINING));

        onEdt(() -> {
            shark.contextMenu().invoke(SlotView.FILL_REMAINING);
            return null;
        });
        GearContent content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(8, content.inventory().filledSlots());
        assertTrue(content.inventory().slot(19).isEmpty());
    }

    @Test
    void theHistoryPageListsEarlierContentsAndRestoresThem() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.dropItem(WHIP, SlotRef.of(GridKind.INVENTORY, 0));
            panel.dropItem(ResolvedItem.of(385, "Shark"), SlotRef.of(GridKind.INVENTORY, 1));
            panel.showHistory();
            return null;
        });
        HistoryPage page = panel.historyPage().orElseThrow();
        assertEquals(2, page.revisions().size());
        assertTrue(labels().contains("Set Inventory slot 2"), "the newest cause is listed");

        onEdt(() -> {
            panel.restoreRevision(page.revisions().get(1));
            return null;
        });
        assertTrue(book.setup(setup.id()).orElseThrow().content().isEmpty(), "the very first state is back");
        assertTrue(panel.contentPanel().isPresent());
        assertEquals(3, setupHistory.revisions(setup.id()).size(), "the restore itself was recorded");
    }

    @Test
    void theHistoryPageNamesTheVariantEachChangeCameFromAndRestoringOpensIt() throws Exception {
        GearContent melee = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(melee)
                .withAddedVariant(new SetupVariant("Mage", melee))
                .withSelectedVariant(0)));
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editVariant(1);
            panel.editSlot(SlotRef.of(EquipmentSlot.HEAD));
            panel.slotEditorForm().orElseThrow().chooseItem(ItemId.of(1163));
            panel.slotEditorForm().orElseThrow().save();
            panel.showHistory();
            return null;
        });
        HistoryPage page = panel.historyPage().orElseThrow();

        assertEquals(1, page.revisions().size());
        assertTrue(labels().contains("Mage"), "the row says which variant changed");

        onEdt(() -> {
            panel.editVariant(0);
            panel.showHistory();
            panel.restoreRevision(panel.historyPage().orElseThrow().revisions().get(0));
            return null;
        });
        GearSetup restored = book.setup(setup.id()).orElseThrow();

        assertEquals(melee, restored.variants().get(1).content(), "the restore went back into Mage");
        assertEquals("Mage", panel.contentPanel().orElseThrow().variants().chosen(), "and Mage is what the page is editing");
    }

    @Test
    void notesAndRequirementsShowOnTheContentsPage() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            book.changeMeta(setup.id(), meta -> meta.withNotes("Bring two extra brews")
                    .withRequirements(Requirements.ofSpellbook(Spellbook.LUNAR)));
            panel.editContents(book.setup(setup.id()).orElseThrow());
            return null;
        });
        assertTrue(labels().contains("Bring two extra brews"));
        assertTrue(labels().contains("Lunar spellbook"));
    }

    @Test
    void pinnedSetupsShowInTheirOwnRowUpToTheCap() throws Exception {
        addSetups("Vorkath", "Zulrah");
        GearSetup vorkath = book.sections().get(0).setups().get(0);
        assertTrue(onEdt(() -> panel.sectionViews().get(0).grid().tiles().get(0).contextMenu().entryTexts()).contains(SetupTile.PIN));

        onEdt(() -> {
            panel.togglePin(vorkath);
            return null;
        });
        assertTrue(book.setup(vorkath.id()).orElseThrow().isPinned());
        assertTrue(labels().contains(PinnedRow.CAPTION));
        assertEquals(List.of("Vorkath"), pinnedNames());

        onEdt(() -> {
            panel.togglePin(book.setup(vorkath.id()).orElseThrow());
            return null;
        });
        assertFalse(labels().contains(PinnedRow.CAPTION));

        for (int i = 0; i < PinnedRow.CAPACITY; i++) {
            GearSetup extra = onEdt(() -> book.addSetup(firstSection, "Extra"));
            onEdt(() -> {
                panel.togglePin(extra);
                return null;
            });
        }
        onEdt(() -> {
            panel.togglePin(book.setup(vorkath.id()).orElseThrow());
            return null;
        });
        assertFalse(book.setup(vorkath.id()).orElseThrow().isPinned(), "the ninth pin is refused");
        assertEquals(PinnedRow.CAPACITY, pinnedNames().size());
    }

    @Test
    void aPinnedSetupIsHighlightedInBothPlacesWhenActive() throws Exception {
        addSetups("Vorkath", "Zulrah");
        GearSetup vorkath = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.togglePin(vorkath);
            panel.activate(book.setup(vorkath.id()).orElseThrow());
            return null;
        });

        List<SetupTile> vorkathTiles = panel.allTiles().stream().filter(tile -> tile.setup().id().equals(vorkath.id())).collect(Collectors.toList());
        assertEquals(2, vorkathTiles.size(), "once pinned, once in its section");
        assertTrue(vorkathTiles.stream().allMatch(SetupTile::isSelected));

        onEdt(() -> {
            panel.activate(book.setup(vorkath.id()).orElseThrow());
            return null;
        });
        assertTrue(panel.allTiles().stream().noneMatch(SetupTile::isSelected));
    }

    @Test
    void aColourCanBePickedFromTheTileMenu() throws Exception {
        addSetups("Vorkath");
        GearSetup vorkath = book.sections().get(0).setups().get(0);
        ContextMenu menu = onEdt(() -> panel.sectionViews().get(0).grid().tiles().get(0).contextMenu());
        assertEquals(List.of(ColourLabel.values()), menu.swatchLabels());

        onEdt(() -> {
            menu.pickSwatch(ColourLabel.RED);
            return null;
        });
        assertEquals(ColourLabel.RED, book.setup(vorkath.id()).orElseThrow().meta().label());
    }

    @Test
    void aHotkeyIsCapturedAndTakenAwayFromAnySetupThatHadIt() throws Exception {
        addSetups("Vorkath", "Zulrah");
        GearSetup vorkath = book.sections().get(0).setups().get(0);
        GearSetup zulrah = book.sections().get(0).setups().get(1);
        onEdt(() -> {
            panel.chooseHotkey(vorkath);
            return null;
        });
        HotkeyCapturePage page = panel.hotkeyPage().orElseThrow();
        onEdt(() -> {
            page.capture(new KeyEvent(page, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_F5, KeyEvent.CHAR_UNDEFINED));
            return null;
        });
        Hotkey f5 = new Hotkey(KeyEvent.VK_F5, 0);
        assertEquals(f5, book.setup(vorkath.id()).orElseThrow().meta().hotkey());
        assertTrue(panel.isListShowing());

        onEdt(() -> {
            panel.setHotkey(zulrah, f5);
            return null;
        });
        assertFalse(book.setup(vorkath.id()).orElseThrow().meta().hasHotkey(), "a key belongs to one setup");
        assertEquals(f5, book.setup(zulrah.id()).orElseThrow().meta().hotkey());
    }

    @Test
    void duplicateAndNewFromGameCreateFilledSetups() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151)))));
        GearSetup vorkath = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.duplicate(vorkath);
            return null;
        });
        assertEquals(List.of("Vorkath", "Vorkath (2)"), tileNames());
        assertEquals(vorkath.content(), book.sections().get(0).setups().get(1).content());

        onEdt(() -> {
            panel.addSetupFromGame(firstSection);
            panel.editorForm().orElseThrow().setEnteredName("Worn now");
            panel.editorForm().orElseThrow().save();
            return null;
        });
        GearSetup fromGame = book.sections().get(0).setups().get(2);
        assertEquals(WORN_AND_CARRIED.inventory(), ((GearContent) fromGame.content()).inventory());
    }

    @Test
    void sectionsAreRenamedInlineAndInsertedFromTheMenu() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.createSection();
            return null;
        });
        SectionView fresh = panel.sectionViews().get(1);
        assertTrue(fresh.isRenaming(), "a new section starts in rename mode");

        onEdt(() -> {
            fresh.cancelRename();
            panel.renameSection(fresh.sectionId(), "Slayer");
            return null;
        });
        assertEquals("Slayer", book.sections().get(1).name());

        ContextMenu menu = onEdt(() -> panel.sectionViews().get(1).contextMenu());
        assertTrue(menu.entryTexts().containsAll(List.of(SectionView.NEW_SETUP, SectionView.NEW_FROM_GAME, SectionView.RENAME,
                SectionView.COLLAPSE_OTHERS, SectionView.INSERT_ABOVE, SectionView.INSERT_BELOW, SectionView.EXPORT, SectionView.DELETE)));

        onEdt(() -> {
            panel.insertSectionAbove(book.sections().get(1).id());
            return null;
        });
        assertEquals(List.of(GearSetupBook.DEFAULT_SECTION_NAME, GearSetupPanel.NEW_SECTION_NAME, "Slayer"),
                book.sections().stream().map(GearSection::name).collect(Collectors.toList()));

        onEdt(() -> {
            panel.collapseOthers(book.sections().get(2).id());
            return null;
        });
        assertTrue(panel.sectionViews().get(0).isCollapsed());
        assertFalse(panel.sectionViews().get(2).isCollapsed());
    }

    @Test
    void exportImportAndBackupsRoundTripThroughTheOverflowMenu(@org.junit.jupiter.api.io.TempDir Path directory) throws Exception {
        addSetups("Vorkath");
        Path file = directory.resolve("out.json");
        nextSaveFile = (file);
        onEdt(() -> {
            panel.transfer().exportAll();
            return null;
        });
        assertTrue(Files.exists(file));

        onEdt(() -> {
            book.removeSetup(book.sections().get(0).setups().get(0).id());
            return null;
        });
        nextOpenFile = (file);
        onEdt(() -> {
            panel.transfer().importFile();
            return null;
        });
        ImportPage page = panel.importPage().orElseThrow();
        assertEquals(1, page.sections().size());
        onEdt(() -> {
            chipOrButton(page, ImportPage.ADD).doClick();
            return null;
        });
        assertEquals(2, book.sectionCount(), "added alongside what was there");
        assertEquals(List.of("Vorkath"), tileNames());

        backupDocuments.add(0, new BookCodec(new Gson()).encode(List.of(new GearSection(SectionId.random(), "From backup", List.of(GearSetup.named("Old"))))));
        onEdt(() -> {
            panel.transfer().showBackups();
            return null;
        });
        BackupsPage backups = panel.backupsPage().orElseThrow();
        assertEquals(1, backups.backups().size());
        onEdt(() -> {
            panel.transfer().restoreBackup(backups.backups().get(0));
            return null;
        });
        assertEquals(List.of("From backup"), book.sections().stream().map(GearSection::name).collect(Collectors.toList()));
        assertEquals(List.of("Old"), tileNames());
    }

    @Test
    void theBannerWarnsWhenTheActiveSetupNeedsAnotherSpellbook() throws Exception {
        addSetups("Zulrah");
        GearSetup zulrah = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            book.changeMeta(zulrah.id(), meta -> meta.withRequirements(Requirements.ofSpellbook(Spellbook.LUNAR)));
            panel.activate(book.setup(zulrah.id()).orElseThrow());
            return null;
        });
        flushEdt();
        assertTrue(panel.isBannerShowing());
        assertEquals("Zulrah needs Lunar, you are on Standard", panel.bannerText());

        onEdt(() -> {
            spellbook.set(Spellbook.LUNAR);
            return null;
        });
        flushEdt();
        assertFalse(panel.isBannerShowing());
    }

    private List<String> pinnedNames() {
        for (Component child : panel.getComponents()) {
            List<String> found = pinnedNamesIn(child);
            if (found != null) {
                return found;
            }
        }
        return List.of();
    }

    private static List<String> pinnedNamesIn(Component component) {
        if (component instanceof PinnedRow) {
            PinnedRow row = (PinnedRow) component;
            return row.grid().tiles().stream().map(tile -> tile.setup().name()).collect(Collectors.toList());
        }
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                List<String> found = pinnedNamesIn(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static AbstractButton chipOrButton(Container container, String text) {
        for (Component child : container.getComponents()) {
            if (child instanceof AbstractButton && text.equals(((AbstractButton) child).getText())) {
                return (AbstractButton) child;
            }
            if (child instanceof Container) {
                Container nested = (Container) child;
                try {
                    return chipOrButton(nested, text);
                } catch (AssertionError ignored) {
                    // keep looking
                }
            }
        }
        throw new AssertionError("No button " + text);
    }

    @Test
    void theEditorSavesColourTagsNotesAndRequirements() throws Exception {
        addSetups("Zulrah");
        GearSetup zulrah = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            book.changeMeta(zulrah.id(), meta -> meta.withPinned(true));
            panel.edit(book.setup(zulrah.id()).orElseThrow());
            return null;
        });
        SetupEditorForm form = panel.editorForm().orElseThrow();
        onEdt(() -> {
            form.colour().choose(ColourLabel.GREEN);
            form.tags().add("Mage");
            form.tags().setFieldText("bossing");
            form.setNotes("Bring two extra brews");
            form.chooseSpellbook(Spellbook.LUNAR);
            form.save();
            return null;
        });

        SetupMeta meta = book.setup(zulrah.id()).orElseThrow().meta();
        assertEquals(ColourLabel.GREEN, meta.label());
        assertEquals(List.of("bossing", "mage"), List.copyOf(meta.tags()));
        assertEquals("Bring two extra brews", meta.notes());
        assertEquals(Spellbook.LUNAR, meta.requirements().spellbook());
        assertTrue(meta.pinned(), "the editor leaves pinning alone");

        onEdt(() -> {
            panel.search("tag:mage");
            return null;
        });
        assertEquals(List.of("Zulrah"), tileNames());
    }

    @Test
    void editingAnExistingSetupPrefillsAndUpdatesIt() throws Exception {
        addSetups("Vorkath");
        GearSetup existing = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.edit(existing);
            return null;
        });
        SetupEditorForm form = panel.editorForm().orElseThrow();
        assertEquals("Vorkath", form.enteredName());

        onEdt(() -> {
            form.setEnteredName("Vorkath range");
            form.save();
            return null;
        });

        assertEquals("Vorkath range", book.setup(existing.id()).orElseThrow().name());
        assertEquals(1, book.setupCount(), "edited, not duplicated");
    }

    @Test
    void droppingASetupMovesItInTheBook() throws Exception {
        addSetups("Vorkath", "Zulrah", "Jad");
        SetupId vorkath = book.sections().get(0).setups().get(0).id();

        onEdt(() -> {
            panel.onSetupDropped(vorkath, firstSection, 2);
            return null;
        });

        assertEquals(List.of("Zulrah", "Jad", "Vorkath"), tileNames());
    }

    @Test
    void activatingASetupSelectsItsTile() throws Exception {
        addSetups("Vorkath", "Zulrah");
        GearSetup zulrah = book.sections().get(0).setups().get(1);

        onEdt(() -> {
            panel.activate(zulrah);
            return null;
        });

        assertEquals(Optional.of(zulrah.id()), panel.selectedSetup());
    }

    @Test
    void togglingASectionHidesItsGrid() throws Exception {
        addSetups("Vorkath", "Zulrah");
        assertFalse(panel.sectionViews().get(0).isCollapsed());

        onEdt(() -> {
            panel.toggleSection(firstSection);
            return null;
        });

        SectionView collapsedView = panel.sectionViews().get(0);
        assertTrue(collapsedView.isCollapsed());
        assertNull(collapsedView.grid().getParent(), "the grid is not shown while collapsed");
        assertTrue(panel.isCollapsed(firstSection));
    }

    @Test
    void togglingTwiceExpandsAgain() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.toggleSection(firstSection);
            panel.toggleSection(firstSection);
            return null;
        });

        assertFalse(panel.sectionViews().get(0).isCollapsed());
        assertEquals(List.of("Vorkath"), tileNames());
    }

    @Test
    void collapsedSectionsStayCollapsedWhenTheBookChanges() throws Exception {
        SectionId skilling = onEdt(() -> book.addSection("Skilling").id());
        onEdt(() -> {
            panel.toggleSection(firstSection);
            return null;
        });

        onEdt(() -> book.addSetup(skilling, "Wintertodt"));

        assertTrue(panel.sectionViews().get(0).isCollapsed());
        assertFalse(panel.sectionViews().get(1).isCollapsed());
    }

    @Test
    void searchingExpandsCollapsedSectionsSoMatchesStayVisible() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.toggleSection(firstSection);
            return null;
        });
        assertTrue(panel.sectionViews().get(0).isCollapsed());

        onEdt(() -> {
            panel.search("vork");
            return null;
        });
        assertFalse(panel.sectionViews().get(0).isCollapsed());

        onEdt(() -> {
            panel.search("");
            return null;
        });
        assertTrue(panel.sectionViews().get(0).isCollapsed());
    }

    @Test
    void deletingASectionForgetsThatItWasCollapsed() throws Exception {
        SectionId skilling = onEdt(() -> book.addSection("Skilling").id());
        onEdt(() -> {
            panel.toggleSection(skilling);
            return null;
        });
        assertTrue(panel.isCollapsed(skilling));

        onEdt(() -> {
            book.removeSection(skilling);
            return null;
        });

        assertFalse(panel.isCollapsed(skilling));
    }

    @Test
    void arrowsReorderSections() throws Exception {
        SectionId skilling = onEdt(() -> book.addSection("Skilling").id());

        onEdt(() -> {
            panel.moveSectionUp(skilling);
            return null;
        });
        assertEquals(List.of(skilling, firstSection), sectionOrder());

        onEdt(() -> {
            panel.moveSectionDown(skilling);
            return null;
        });
        assertEquals(List.of(firstSection, skilling), sectionOrder());
    }

    @Test
    void theFirstSectionHasNoUpArrowAndTheLastHasNoDownArrow() throws Exception {
        onEdt(() -> book.addSection("Skilling"));

        List<SectionView> views = panel.sectionViews();
        assertFalse(hasIcon(views.get(0), ActionIcon.MOVE_UP), "first section offers no move up");
        assertTrue(hasIcon(views.get(0), ActionIcon.MOVE_DOWN));
        assertTrue(hasIcon(views.get(1), ActionIcon.MOVE_UP));
        assertFalse(hasIcon(views.get(1), ActionIcon.MOVE_DOWN), "last section offers no move down");
    }

    @Test
    void aSingleSectionOffersNoReorderArrows() {
        SectionView only = panel.sectionViews().get(0);
        assertFalse(hasIcon(only, ActionIcon.MOVE_UP));
        assertFalse(hasIcon(only, ActionIcon.MOVE_DOWN));
    }

    @Test
    void droppingASectionReordersIt() throws Exception {
        SectionId skilling = onEdt(() -> book.addSection("Skilling").id());

        onEdt(() -> {
            panel.onSectionDropped(skilling, 0);
            return null;
        });

        assertEquals(List.of(skilling, firstSection), sectionOrder());
    }

    @Test
    void expandingInPlaceKeepsTheSameGridSoADragCanContinue() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.toggleSection(firstSection);
            return null;
        });
        SectionView view = panel.sectionViews().get(0);
        SetupGrid gridWhileCollapsed = view.grid();
        assertTrue(view.isCollapsed());

        onEdt(() -> {
            view.expand();
            return null;
        });

        assertFalse(view.isCollapsed());
        assertSame(gridWhileCollapsed, view.grid(), "the grid survives, so a drag in progress stays valid");
        assertNotNull(view.grid().getParent());
    }

    @Test
    void theTrashIconTurnsBulkModeOnAndOff() throws Exception {
        assertFalse(panel.isBulkMode());

        onEdt(() -> {
            panel.toggleBulkMode();
            return null;
        });
        assertTrue(panel.isBulkMode());

        onEdt(() -> {
            panel.toggleBulkMode();
            return null;
        });
        assertFalse(panel.isBulkMode());
    }

    @Test
    void bulkModeHidesTheEditingControls() throws Exception {
        onEdt(() -> book.addSection("Skilling"));
        assertTrue(hasIcon(panel.sectionViews().get(0), ActionIcon.ADD));

        onEdt(() -> {
            panel.bulkDelete().set(true);
            return null;
        });

        SectionView view = panel.sectionViews().get(0);
        assertFalse(hasIcon(view, ActionIcon.ADD));
        assertFalse(hasIcon(view, ActionIcon.RENAME));
        assertFalse(hasIcon(view, ActionIcon.MOVE_DOWN));
    }

    @Test
    void deletingEverySectionLeavesAUsablePanel() throws Exception {
        addSetups("Vorkath");

        onEdt(() -> {
            panel.bulkDelete().set(true);
            panel.bulkDelete().select(BulkTarget.of(firstSection), false);
            panel.bulkDelete().deleteSelected();
            return null;
        });

        assertEquals(1, panel.sectionViews().size());
        assertTrue(tileNames().isEmpty());
        assertTrue(panel.isOnboardingShowing(), "an emptied book gets the getting-started card back");
    }

    @Test
    void draggingASectionGrabsItWhereTheCursorIsNotAtTheTopOfTheList() throws Exception {
        onEdt(() -> book.addSection("Skilling"));

        SectionView second = panel.sectionViews().get(1);
        int sectionTop = 140;
        int grabY = 12;
        onEdt(() -> {
            second.getParent().setBounds(0, 0, 200, 400);
            panel.sectionViews().get(0).setBounds(0, 0, 200, sectionTop);
            second.setBounds(0, sectionTop, 200, 120);
            return null;
        });

        MouseEvent press = new MouseEvent(second, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                InputEvent.BUTTON1_DOWN_MASK, 5, grabY, 1, false, MouseEvent.BUTTON1);
        onEdt(() -> {
            panel.sectionDrags().press(second, press);
            return null;
        });

        assertEquals(new Point(5, grabY), panel.sectionDrags().grabOffset(),
                "the grab offset is measured inside the section, not from the top of the list");
    }

    @Test
    void openingASetupShowsItsContentEditor() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.editContents(setup);
            return null;
        });

        SetupContentPanel content = panel.contentPanel().orElseThrow();
        assertEquals(EquipmentSlot.values().length + ItemGrid.SIZE, content.slots().size(),
                "gear setups edit every equipment slot and the whole inventory");
    }

    @Test
    void aGeneralSetupEditsTwoGrids() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Skilling").withContent(BankContent.empty())));
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.editContents(setup);
            return null;
        });

        assertEquals(ItemGrid.SIZE * 2, panel.contentPanel().orElseThrow().slots().size());
    }

    @Test
    void newSetupsTakeTheChosenType() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });
        SetupEditorForm form = panel.editorForm().orElseThrow();

        onEdt(() -> {
            form.setEnteredName("Skilling");
            form.chooseType(SetupType.BANK);
            form.save();
            return null;
        });

        assertEquals(SetupType.BANK, book.sections().get(0).setups().get(0).type());
    }

    @Test
    void applyingASetupSendsItsLayoutToTheBank() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty()
                        .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151))
                        .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(995, 1000))))));
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.activate(setup);
            return null;
        });

        assertEquals(1, appliedLayouts.size());
        BankLayout layout = appliedLayouts.get(0);
        assertEquals(1, layout.side(BankSide.LEFT).size());
        assertEquals(1, layout.side(BankSide.RIGHT).size());
        assertTrue(activeSetup.isActive(setup.id()));
    }

    @Test
    void applyingTheSameSetupAgainTurnsItOff() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.activate(setup);
            panel.activate(setup);
            return null;
        });

        assertTrue(activeSetup.current().isEmpty());
        assertEquals(1, cleared.size(), "the bank layout is taken down again");
    }

    @Test
    void applyingAnotherSetupReplacesTheActiveOne() throws Exception {
        addSetups("Vorkath", "Zulrah");
        GearSetup vorkath = book.sections().get(0).setups().get(0);
        GearSetup zulrah = book.sections().get(0).setups().get(1);

        onEdt(() -> {
            panel.activate(vorkath);
            panel.activate(zulrah);
            return null;
        });

        assertTrue(activeSetup.isActive(zulrah.id()));
        assertEquals(2, appliedLayouts.size());
    }

    @Test
    void everySubPageCanGetBackToTheList() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.editContents(setup);
            return null;
        });
        assertFalse(panel.isListShowing());

        onEdt(() -> {
            panel.contentPanel().orElseThrow();
            panel.editSlot(SlotRef.of(EquipmentSlot.WEAPON));
            return null;
        });
        SlotEditorForm slotForm = panel.slotEditorForm().orElseThrow();

        onEdt(() -> {
            slotForm.cancel();
            return null;
        });
        assertTrue(panel.contentPanel().isPresent(), "cancelling a slot returns to the setup");

        onEdt(() -> {
            panel.showListCard();
            return null;
        });
        assertTrue(panel.isListShowing());
    }

    @Test
    void savingASlotStoresTheItemAndReturnsToTheSetup() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(EquipmentSlot.WEAPON));
            return null;
        });

        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        onEdt(() -> {
            form.chooseItem(ItemId.of(4151));
            form.setEnteredQuantity("3");
            form.save();
            return null;
        });

        GearContent content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(Optional.of(SetupItem.of(4151, 3)), content.equipped(EquipmentSlot.WEAPON));
        assertTrue(panel.contentPanel().isPresent(), "saving returns to the setup");
    }

    @Test
    void aSlotCannotBeSavedWithoutAnItemOrWithABadAmount() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 0));
            return null;
        });

        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        assertFalse(form.isSaveEnabled(), "no item chosen yet");

        onEdt(() -> {
            form.chooseItem(ItemId.of(995));
            form.setEnteredQuantity("nonsense");
            return null;
        });
        assertFalse(form.isSaveEnabled());

        onEdt(() -> {
            form.setEnteredQuantity("500");
            return null;
        });
        assertTrue(form.isSaveEnabled());
    }

    @Test
    void emptyingASlotRemovesTheItem() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(2, SetupItem.of(4151))))));
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 2));
            return null;
        });
        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        onEdt(() -> {
            form.clear();
            return null;
        });

        GearContent content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertTrue(content.inventory().slot(2).isEmpty());
    }

    @Test
    void gearSetupsNoLongerCarryASpellbookSection() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        assertFalse(labels().contains("Spellbook"));
    }

    @Test
    void theTypeChooserOffersOneLargeButtonPerLayout() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });

        List<SetupTypeButton> buttons = typeButtons(panel.editorForm().orElseThrow());
        assertEquals(List.of(SetupType.GEAR, SetupType.BANK, SetupType.CUSTOM),
                buttons.stream().map(SetupTypeButton::type).collect(Collectors.toList()));
        assertTrue(buttons.stream().allMatch(button -> button.getPreferredSize().height >= 48),
                "the layout buttons are large, not text links");
    }

    @Test
    void theChosenLayoutButtonIsMarkedAndTheOtherIsNot() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });
        SetupEditorForm form = panel.editorForm().orElseThrow();
        List<SetupTypeButton> buttons = typeButtons(form);

        assertTrue(buttons.get(0).isChosen(), "gear is the starting choice");
        assertFalse(buttons.get(1).isChosen());

        onEdt(() -> {
            form.chooseType(SetupType.BANK);
            return null;
        });

        assertFalse(buttons.get(0).isChosen());
        assertTrue(buttons.get(1).isChosen());
        assertEquals(SetupType.BANK, form.chosenType());
    }

    @Test
    void eachLayoutButtonShowsItsArtwork() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });

        for (SetupTypeButton button : typeButtons(panel.editorForm().orElseThrow())) {
            assertTrue(hasArtwork(button), button.type() + " has no picture");
        }
    }

    @Test
    void theLayoutNamesReadAsLayouts() {
        assertEquals("Gear Layout", SetupType.GEAR.displayName());
        assertEquals("Bank Layout", SetupType.BANK.displayName());
    }

    private static List<SetupTypeButton> typeButtons(Container container) {
        List<SetupTypeButton> found = new ArrayList<>();
        collectTypeButtons(container, found);
        return found;
    }

    private static void collectTypeButtons(Container container, List<SetupTypeButton> into) {
        for (Component child : container.getComponents()) {
            if (child instanceof SetupTypeButton) {
                SetupTypeButton button = (SetupTypeButton) child;
                into.add(button);
            }
            if (child instanceof Container) {
                Container nested = (Container) child;
                collectTypeButtons(nested, into);
            }
        }
    }

    private static boolean hasArtwork(Container container) {
        for (Component child : container.getComponents()) {
            if (child instanceof JLabel && ((JLabel) child).getIcon() != null) {
                return true;
            }
            if (child instanceof Container && hasArtwork(((Container) child))) {
                return true;
            }
        }
        return false;
    }

    @Test
    void leavingTheAmountBlankFollowsTheBank() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 0));
            return null;
        });

        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        onEdt(() -> {
            form.chooseItem(ItemId.of(4151));
            form.setEnteredQuantity("");
            return null;
        });

        assertTrue(form.isSaveEnabled(), "a blank amount is allowed");

        onEdt(() -> {
            form.save();
            return null;
        });

        GearContent content = (GearContent) book.setup(setup.id()).orElseThrow().content();
        SetupItem stored = content.inventory().slot(0).orElseThrow();
        assertFalse(stored.hasQuantity());
    }

    @Test
    void anExistingAmountIsPrefilledAndABlankOneIsNot() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty().withInventory(ItemGrid.EMPTY
                        .withSlot(0, SetupItem.of(995, 500))
                        .withSlot(1, SetupItem.of(4151))))));
        GearSetup setup = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 0));
            return null;
        });
        assertEquals(500, panel.slotEditorForm().orElseThrow().enteredQuantity().getAsInt());

        onEdt(() -> {
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 1));
            return null;
        });
        assertTrue(panel.slotEditorForm().orElseThrow().enteredQuantity().isEmpty());
    }

    @Test
    void aNonsenseAmountStillBlocksSaving() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 0));
            return null;
        });

        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        onEdt(() -> {
            form.chooseItem(ItemId.of(4151));
            form.setEnteredQuantity("nonsense");
            return null;
        });
        assertFalse(form.isSaveEnabled());
    }

    @Test
    void clickingASetupShowsItInTheBank() throws Exception {
        addSetups("Vorkath");
        SetupTile tile = panel.sectionViews().get(0).grid().tiles().get(0);

        onEdt(() -> {
            clickTile(tile);
            return null;
        });

        assertTrue(activeSetup.isActive(tile.setup().id()));
        assertEquals(1, appliedLayouts.size());
    }

    @Test
    void clickingTheActiveSetupAgainTurnsItOff() throws Exception {
        addSetups("Vorkath");
        SetupTile tile = panel.sectionViews().get(0).grid().tiles().get(0);

        onEdt(() -> {
            clickTile(tile);
            return null;
        });
        SetupTile afterFirstClick = panel.sectionViews().get(0).grid().tiles().get(0);

        onEdt(() -> {
            clickTile(afterFirstClick);
            return null;
        });

        assertTrue(activeSetup.current().isEmpty());
        assertEquals(1, cleared.size(), "the bank layout is taken down");
    }

    @Test
    void clickingAnotherSetupLeavesOnlyThatOneOn() throws Exception {
        addSetups("Vorkath", "Zulrah");
        List<SetupTile> tiles = panel.sectionViews().get(0).grid().tiles();
        SetupId vorkath = tiles.get(0).setup().id();
        SetupId zulrah = tiles.get(1).setup().id();

        onEdt(() -> {
            clickTile(tiles.get(0));
            return null;
        });
        assertTrue(activeSetup.isActive(vorkath));

        List<SetupTile> refreshed = panel.sectionViews().get(0).grid().tiles();
        onEdt(() -> {
            clickTile(refreshed.get(1));
            return null;
        });

        assertTrue(activeSetup.isActive(zulrah));
        assertFalse(activeSetup.isActive(vorkath), "only one layout is on at a time");
    }

    @Test
    void onlyTheActiveTileIsHighlighted() throws Exception {
        addSetups("Vorkath", "Zulrah");
        onEdt(() -> {
            clickTile(panel.sectionViews().get(0).grid().tiles().get(1));
            return null;
        });

        List<SetupTile> tiles = panel.sectionViews().get(0).grid().tiles();
        assertFalse(tiles.get(0).isSelected());
        assertTrue(tiles.get(1).isSelected());
    }

    @Test
    void aRightClickDoesNotToggleTheLayout() throws Exception {
        addSetups("Vorkath");
        SetupTile tile = panel.sectionViews().get(0).grid().tiles().get(0);

        onEdt(() -> {
            clickTile(tile, MouseEvent.BUTTON3, InputEvent.BUTTON3_DOWN_MASK);
            return null;
        });

        assertTrue(activeSetup.current().isEmpty());
        assertTrue(appliedLayouts.isEmpty());
    }

    private static void clickTile(SetupTile tile) {
        clickTile(tile, MouseEvent.BUTTON1, InputEvent.BUTTON1_DOWN_MASK);
    }

    @SuppressWarnings("MagicConstant")
    private static void clickTile(SetupTile tile, int button, int modifiers) {
        tile.setSize(tile.getPreferredSize());
        MouseEvent event = new MouseEvent(tile, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                modifiers, 5, 5, 1, false, button);
        for (MouseListener listener : tile.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }

    private List<SectionId> sectionOrder() {
        return panel.sectionViews().stream().map(SectionView::sectionId).collect(Collectors.toList());
    }

    private static boolean hasIcon(Container container, ActionIcon icon) {
        for (Component child : container.getComponents()) {
            if (child instanceof AbstractButton && ((AbstractButton) child).getIcon() == icon) {
                return true;
            }
            if (child instanceof Container && hasIcon(((Container) child), icon)) {
                return true;
            }
        }
        return false;
    }

    private void addSetups(String... names) throws Exception {
        for (String name : names) {
            onEdt(() -> book.addSetup(firstSection, name));
        }
    }

    private List<String> tileNames() {
        return panel.sectionViews().stream()
                .flatMap(view -> view.grid().tiles().stream())
                .map(tile -> tile.setup().name())
                .collect(Collectors.toList());
    }

    private List<String> namesIn(int sectionIndex) {
        return panel.sectionViews().get(sectionIndex).grid().tiles().stream()
                .map(tile -> tile.setup().name())
                .collect(Collectors.toList());
    }

    private List<String> labels() {
        List<String> found = new ArrayList<>();
        collectLabels(panel, found);
        return found;
    }

    private List<String> buttonTexts() {
        List<String> found = new ArrayList<>();
        collectButtonTexts(panel, found);
        return found;
    }

    private static void collectButtonTexts(Container container, List<String> into) {
        for (Component child : container.getComponents()) {
            if (child instanceof AbstractButton && ((AbstractButton) child).getText() != null && !((AbstractButton) child).getText().isEmpty()) {
                AbstractButton button = (AbstractButton) child;
                into.add(button.getText());
            }
            if (child instanceof Container) {
                Container nested = (Container) child;
                collectButtonTexts(nested, into);
            }
        }
    }

    private static <T> Optional<T> find(Container container, Class<T> type) {
        for (Component child : container.getComponents()) {
            if (type.isInstance(child)) {
                return Optional.of(type.cast(child));
            }
            if (child instanceof Container) {
                Container nested = (Container) child;
                Optional<T> found = find(nested, type);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    /** Lets queued EDT work, such as sprite loads, finish. */
    private static void flushEdt() throws Exception {
        for (int i = 0; i < 3; i++) {
            onEdt(() -> null);
        }
    }

    private static void collectLabels(Container container, List<String> into) {
        for (Component child : container.getComponents()) {
            if (child instanceof JLabel && ((JLabel) child).getText() != null) {
                JLabel label = (JLabel) child;
                into.add(label.getText());
            }
            if (child instanceof WrappedText) {
                into.add(((WrappedText) child).getText());
            }
            if (child instanceof Container) {
                Container nested = (Container) child;
                collectLabels(nested, into);
            }
        }
    }

    private static <T> T onEdt(Callable<T> action) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                result.set(action.call());
            } catch (Exception e) {
                failure.set(e);
            }
        });
        if (failure.get() != null) {
            throw failure.get();
        }
        return result.get();
    }
    @Test
    void aShareCodeRoundTripsThroughTheClipboard() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.sectionViews().get(0).grid().tiles().get(0).contextMenu().invoke(SetupTile.COPY_SHARE_CODE);
            return null;
        });
        assertTrue(clipboard.paste().orElseThrow().startsWith(ShareCodec.PREFIX));
        assertTrue(prompts.messages().contains("Copied a share code for Vorkath"));

        onEdt(() -> {
            panel.insertSectionBelow(firstSection);
            panel.transfer().pasteShareCode();
            return null;
        });
        SharePreviewPage page = panel.sharePreviewPage().orElseThrow();
        assertEquals("Vorkath", page.setup().name());
        SectionId second = book.sections().get(1).id();
        onEdt(() -> {
            page.choose(second);
            return null;
        });
        onEdt(() -> {
            find(page, PrimaryButton.class).orElseThrow().doClick();
            return null;
        });
        assertEquals(List.of("Vorkath"), namesIn(1));
        assertNotEquals(setup.id(), book.sections().get(1).setups().get(0).id(), "the pasted copy gets its own id");
        assertTrue(panel.sharePreviewPage().isEmpty());
    }

    @Test
    void pastingWithoutAShareCodeSaysSo() throws Exception {
        clipboard.copy("not a code");
        onEdt(() -> {
            panel.transfer().pasteShareCode();
            return null;
        });
        assertTrue(panel.sharePreviewPage().isEmpty());
        assertTrue(prompts.messages().contains("The clipboard holds no share code"));
        assertTrue(onEdt(() -> panel.overflowMenu().entryTexts()).contains(GearSetupPanel.PASTE_SHARE_CODE));
    }

    @Test
    void aSectionCanBeSortedFromItsMenu() throws Exception {
        addSetups("Zulrah", "Bandos", "Vorkath");
        ContextMenu menu = onEdt(() -> panel.sectionViews().get(0).contextMenu());
        assertTrue(menu.entryTexts().contains(SetupOrder.NAME.displayName()));
        onEdt(() -> {
            menu.invoke(SetupOrder.NAME.displayName());
            return null;
        });
        assertEquals(List.of("Bandos", "Vorkath", "Zulrah"), namesIn(0));
        assertTrue(labels().contains("Sorted by name"));
    }

    @Test
    void theTileStyleCyclesAndChangesTheGrid() throws Exception {
        addSetups("Vorkath");
        assertEquals(TileStyle.GRID, panel.tileStyle());
        assertEquals(TileStyle.GRID.columns(), panel.sectionViews().get(0).grid().columns());
        onEdt(() -> {
            panel.cycleTileStyle();
            return null;
        });
        assertEquals(TileStyle.COMPACT, panel.tileStyle());
        assertEquals(TileStyle.COMPACT.columns(), panel.sectionViews().get(0).grid().columns());
        onEdt(() -> {
            panel.cycleTileStyle();
            return null;
        });
        assertEquals(TileStyle.LIST, panel.tileStyle());
        assertEquals(1, panel.sectionViews().get(0).grid().columns());
        assertEquals(TileStyle.LIST, panel.sectionViews().get(0).grid().tiles().get(0).style());
        assertTrue(labels().contains("Vorkath"), "the list style spells the name out");
        onEdt(() -> {
            panel.cycleTileStyle();
            return null;
        });
        assertEquals(TileStyle.GRID, panel.tileStyle());
    }

    @Test
    void theSlotEditorRemembersWhetherToWithdrawNotes() throws Exception {
        addSetups("Vorkath");
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 0));
            return null;
        });
        SlotEditorForm form = panel.slotEditorForm().orElseThrow();
        assertFalse(form.isNoted());
        onEdt(() -> {
            form.chooseItem(ItemId.of(385));
            form.setEnteredQuantity("500");
            form.setNoted(true);
            form.save();
            return null;
        });
        SetupItem stored = ((GearContent) book.setup(setup.id()).orElseThrow().content()).inventory().slot(0).orElseThrow();
        assertTrue(stored.noted());
        onEdt(() -> {
            panel.editSlot(SlotRef.of(GridKind.INVENTORY, 0));
            return null;
        });
        assertTrue(panel.slotEditorForm().orElseThrow().isNoted());
    }

    @Test
    void savingANewSetupLeavesTheEditorWhereverItWasOpenedFrom() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.createSetup();
            panel.editorForm().orElseThrow().setEnteredName("From list");
            panel.editorForm().orElseThrow().save();
            return null;
        });
        assertFalse(panel.isEditorShowing());
        assertTrue(panel.isListShowing());

        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            panel.createSetup();
            return null;
        });
        assertTrue(panel.isEditorShowing());
        assertFalse(panel.isListShowing());
        onEdt(() -> {
            panel.editorForm().orElseThrow().setEnteredName("From contents");
            panel.editorForm().orElseThrow().save();
            return null;
        });
        assertFalse(panel.isEditorShowing());
        assertTrue(panel.contentPanel().isPresent(), "back to the page the editor was opened from");
        assertEquals(List.of("Vorkath", "From list", "From contents"), tileNames());

        onEdt(() -> {
            panel.createSetup();
            panel.editorForm().orElseThrow().cancel();
            return null;
        });
        assertFalse(panel.isEditorShowing());
        assertTrue(panel.contentPanel().isPresent());
    }

    @Test
    void aSubSectionIsIndentedAndHidesWithItsParent() throws Exception {
        onEdt(() -> {
            panel.sectionViews().get(0).contextMenu().invoke(SectionView.NEW_SUBSECTION);
            return null;
        });
        assertEquals(2, book.sectionCount());
        GearSection child = book.sections().get(1);
        assertTrue(child.isChildOf(firstSection));
        assertTrue(panel.sectionViews().get(1).isRenaming(), "a new sub-section opens for renaming");
        assertTrue(panel.sectionViews().get(1).isNested());
        assertFalse(panel.sectionViews().get(0).isNested());
        onEdt(() -> {
            panel.sectionViews().get(1).cancelRename();
            panel.renameSection(child.id(), "ToA");
            book.addSetup(child.id(), "Expert");
            return null;
        });

        onEdt(() -> {
            panel.toggleSection(firstSection);
            return null;
        });
        assertEquals(1, panel.sectionViews().size(), "a collapsed parent hides its children");
        onEdt(() -> {
            panel.toggleSection(firstSection);
            return null;
        });
        assertEquals(2, panel.sectionViews().size());

        onEdt(() -> {
            panel.search("exp");
            return null;
        });
        assertEquals(1, panel.sectionViews().size());
        assertFalse(panel.sectionViews().get(0).isNested(), "search results read flat");
        assertEquals(List.of("Expert"), namesIn(0));
    }

    @Test
    void sectionsNestAndUnnestFromTheMenu() throws Exception {
        SectionId raids = onEdt(() -> book.addSection("Raids").id());
        SectionId toa = onEdt(() -> book.addSection("ToA").id());
        ContextMenu menu = onEdt(() -> panel.sectionViews().get(2).contextMenu());
        assertTrue(menu.entryTexts().contains(SectionView.NEST_PREFIX + "Raids"));
        assertFalse(menu.entryTexts().contains(SectionView.UNNEST));
        onEdt(() -> {
            menu.invoke(SectionView.NEST_PREFIX + "Raids");
            return null;
        });
        assertTrue(book.section(toa).orElseThrow().isChildOf(raids));
        assertTrue(labels().contains("Moved ToA under Raids"));

        ContextMenu parentMenu = onEdt(() -> panel.sectionViews().get(1).contextMenu());
        assertFalse(parentMenu.entryTexts().stream().anyMatch(text -> text.startsWith(SectionView.NEST_PREFIX)), "a parent cannot be nested");
        assertTrue(parentMenu.entryTexts().contains(SectionView.NEW_SUBSECTION));

        ContextMenu childMenu = onEdt(() -> panel.sectionViews().get(2).contextMenu());
        assertTrue(childMenu.entryTexts().contains(SectionView.UNNEST));
        assertFalse(childMenu.entryTexts().contains(SectionView.NEW_SUBSECTION), "one level only");
        onEdt(() -> {
            childMenu.invoke(SectionView.UNNEST);
            return null;
        });
        assertFalse(book.section(toa).orElseThrow().isChild());
        assertEquals(List.of(firstSection, raids, toa), sectionOrder());
    }

    @Test
    void draggingAParentCarriesItsChildrenAndArrowsStayInsideTheFamily() throws Exception {
        SectionId raids = onEdt(() -> book.addSection("Raids").id());
        SectionId toa = onEdt(() -> book.addSubSection(raids, "ToA").id());
        SectionId cox = onEdt(() -> book.addSubSection(raids, "CoX").id());

        onEdt(() -> {
            panel.onSectionDropped(raids, 0);
            return null;
        });
        assertEquals(List.of(raids, toa, cox, firstSection), sectionOrder());

        List<SectionView> views = panel.sectionViews();
        assertFalse(hasIcon(views.get(1), ActionIcon.MOVE_UP), "the first child has no up arrow");
        assertTrue(hasIcon(views.get(1), ActionIcon.MOVE_DOWN));
        assertFalse(hasIcon(views.get(2), ActionIcon.MOVE_DOWN), "the last child has no down arrow");
        onEdt(() -> {
            panel.onSectionDropped(cox, 1);
            return null;
        });
        assertEquals(List.of(raids, cox, toa, firstSection), sectionOrder());
    }

    @Test
    void theLedgerFooterSumsTheOpenSetup() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151))
                .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 4)).withSlot(1, SetupItem.of(999_999))))));
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        LedgerView ledger = panel.contentPanel().orElseThrow().ledger();
        assertFalse(ledger.isOpen(), "closed until asked");
        assertTrue(ledger.lines().get(0).startsWith("Value 2.5M"));
        assertTrue(ledger.lines().get(0).contains("Weight 2.4 kg"));
        assertTrue(ledger.lines().stream().anyMatch(line -> line.startsWith("Att st 0  sl +82")));
        assertTrue(ledger.lines().stream().anyMatch(line -> line.contains("Speed 4")));
        assertTrue(ledger.lines().contains("1 item has no stat data yet"));

        onEdt(() -> {
            ledger.setOpen(true);
            panel.clearSlot(SlotRef.of(GridKind.INVENTORY, 1));
            return null;
        });
        LedgerView rebuilt = panel.contentPanel().orElseThrow().ledger();
        assertTrue(rebuilt.isOpen(), "the open state survives an edit");
        assertFalse(rebuilt.lines().stream().anyMatch(line -> line.contains("no stat data")));
        assertTrue(labels().contains("Value") && labels().contains("2.5M"), "the open ledger shows its tiles");
        assertTrue(labels().contains("Attack") && labels().contains("+82"), "and the bonus table");
    }

    @Test
    void variantsAreAddedRenamedEditedApartSelectedAndDeletedFromTheContentsPage() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151)).withEquipped(EquipmentSlot.HEAD, SetupItem.of(1163)))));
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            return null;
        });
        VariantsPanel first = panel.contentPanel().orElseThrow().variants();
        assertEquals(List.of(SetupVariant.DEFAULT_NAME), first.names());
        assertTrue(first.isOpen() && first.canAdd());
        assertEquals(List.of(NewVariant.COPY.label(), NewVariant.EMPTY.label(), NewVariant.FROM_GAME.label()),
                onEdt(() -> panel.contentPanel().orElseThrow().variants().newVariantMenu().entryTexts()), "gear can start from the game");
        assertEquals(List.of(VariantCard.RENAME, VariantCard.DUPLICATE), onEdt(() -> first.cards().get(0).contextMenu().entryTexts()),
                "the only variant cannot be moved, compared or deleted");
        assertEquals("2 items · 2.5M", first.cards().get(0).totals());

        onEdt(() -> {
            panel.content().addVariant(NewVariant.COPY, "Mage");
            return null;
        });
        GearSetup withMage = book.setup(setup.id()).orElseThrow();
        VariantsPanel variants = panel.contentPanel().orElseThrow().variants();
        assertEquals(List.of(SetupVariant.DEFAULT_NAME, "Mage"), variants.names());
        assertEquals("Mage", variants.chosen(), "the new variant is opened for editing");
        assertEquals(SetupVariant.DEFAULT_NAME, variants.shown(), "while the bank keeps showing the first");
        assertEquals(0, withMage.selectedIndex());
        assertEquals(withMage.variants().get(0).content(), withMage.variants().get(1).content(), "and starts as a copy");
        assertEquals(List.of(VariantCard.RENAME, VariantCard.DUPLICATE, VariantCard.COMPARE, VariantCard.MOVE_DOWN, VariantCard.DELETE),
                onEdt(() -> panel.contentPanel().orElseThrow().variants().cards().get(0).contextMenu().entryTexts()), "the shown variant has no Show");
        assertEquals(List.of(VariantCard.SHOW, VariantCard.RENAME, VariantCard.DUPLICATE, VariantCard.MOVE_UP, VariantCard.DELETE),
                onEdt(() -> panel.contentPanel().orElseThrow().variants().cards().get(1).contextMenu().entryTexts()), "the edited one is not compared with itself");

        onEdt(() -> {
            panel.content().addVariant(NewVariant.EMPTY, "Bare");
            return null;
        });
        assertTrue(book.setup(setup.id()).orElseThrow().variants().get(2).content().isEmpty(), "an empty start holds nothing");
        assertEquals("0 items", panel.contentPanel().orElseThrow().variants().cards().get(2).totals());
        onEdt(() -> {
            panel.content().deleteVariant(2);
            panel.content().renameVariant(1, "Magic");
            panel.editSlot(SlotRef.of(EquipmentSlot.WEAPON));
            panel.slotEditorForm().orElseThrow().chooseItem(ItemId.of(11791));
            panel.slotEditorForm().orElseThrow().save();
            return null;
        });
        GearSetup edited = book.setup(setup.id()).orElseThrow();
        assertEquals("Magic", edited.variants().get(1).name());
        assertEquals(Optional.of(SetupItem.of(11791)), ((GearContent) edited.variants().get(1).content()).equipped(EquipmentSlot.WEAPON), "the edited variant took the item");
        assertEquals(Optional.of(SetupItem.of(4151)), ((GearContent) edited.content()).equipped(EquipmentSlot.WEAPON), "the shown one is untouched");
        assertEquals("Magic", panel.contentPanel().orElseThrow().variants().chosen(), "editing a slot keeps the variant on the page");

        onEdt(() -> {
            panel.content().renameVariant(1, SetupVariant.DEFAULT_NAME);
            return null;
        });
        assertEquals("Magic", book.setup(setup.id()).orElseThrow().variants().get(1).name(), "a taken name is refused");
        assertTrue(labels().stream().anyMatch(text -> text.contains("already a variant named")));

        onEdt(() -> {
            panel.contentPanel().orElseThrow().variants().cards().get(1).contextMenu().invoke(VariantCard.SHOW);
            return null;
        });
        assertEquals(1, book.setup(setup.id()).orElseThrow().selectedIndex(), "a card's Show makes the bank show its variant");
        assertEquals("Magic", panel.contentPanel().orElseThrow().variants().shown());
        assertEquals("Magic", panel.contentPanel().orElseThrow().variants().chosen());
        onEdt(() -> {
            panel.editVariant(0);
            return null;
        });
        assertEquals(SetupVariant.DEFAULT_NAME, panel.contentPanel().orElseThrow().variants().chosen(), "a click opens the variant on the page");
        assertEquals("Magic", panel.contentPanel().orElseThrow().variants().shown(), "without changing what the bank shows");
        assertEquals(1, book.setup(setup.id()).orElseThrow().selectedIndex());

        onEdt(() -> {
            panel.contentPanel().orElseThrow().variants().cards().get(1).contextMenu().invoke(VariantCard.DUPLICATE);
            return null;
        });
        assertEquals(List.of(SetupVariant.DEFAULT_NAME, "Magic", "Magic (2)"), panel.contentPanel().orElseThrow().variants().names());
        onEdt(() -> {
            panel.contentPanel().orElseThrow().variants().cards().get(2).contextMenu().invoke(VariantCard.MOVE_UP);
            return null;
        });
        assertEquals(List.of(SetupVariant.DEFAULT_NAME, "Magic (2)", "Magic"), panel.contentPanel().orElseThrow().variants().names());
        assertEquals("Magic (2)", panel.contentPanel().orElseThrow().variants().chosen(), "the page follows the moved variant");
        assertEquals("Magic", panel.contentPanel().orElseThrow().variants().shown(), "and so does the bank's choice");

        onEdt(() -> {
            panel.contentPanel().orElseThrow().variants().cards().get(0).contextMenu().invoke(VariantCard.COMPARE);
            return null;
        });
        DiffPage diff = panel.diffPage().orElseThrow();
        assertEquals(SetupVariant.DEFAULT_NAME, diff.chosen().orElseThrow().name());
        assertTrue(diff.rowTexts().stream().anyMatch(text -> text.contains("Weapon")), "the variants differ in the weapon");
        onEdt(() -> {
            panel.editContents(book.setup(setup.id()).orElseThrow());
            panel.content().deleteVariant(2);
            panel.content().deleteVariant(1);
            return null;
        });
        GearSetup afterRemoval = book.setup(setup.id()).orElseThrow();
        assertEquals(1, afterRemoval.variants().size());
        assertEquals(SetupVariant.DEFAULT_NAME, afterRemoval.variant().name());

        onEdt(() -> {
            panel.contentPanel().orElseThrow().variants().setOpen(false);
            panel.editSlot(SlotRef.of(EquipmentSlot.HEAD));
            panel.slotEditorForm().orElseThrow().chooseItem(ItemId.of(4151));
            panel.slotEditorForm().orElseThrow().save();
            return null;
        });
        assertFalse(panel.contentPanel().orElseThrow().variants().isOpen(), "a folded list stays folded across edits");
    }

    @Test
    void theTileMenuListsVariantsAndSwitchesTheChosenOne() throws Exception {
        GearContent melee = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(melee)
                .withAddedVariant(new SetupVariant("Mage", melee.withEquipped(EquipmentSlot.WEAPON, SetupItem.of(11791))))
                .withSelectedVariant(0)));
        GearSetup setup = book.sections().get(0).setups().get(0);
        ContextMenu.Submenu variants = onEdt(() -> panel.sectionViews().get(0).grid().tiles().get(0).contextMenu().submenu(SetupTile.VARIANTS).orElseThrow());
        assertEquals(List.of(SetupVariant.DEFAULT_NAME, "Mage", SetupTile.NEW_VARIANT), variants.entryTexts());
        assertEquals(List.of(SetupVariant.DEFAULT_NAME), variants.chosenTexts());
        onEdt(() -> {
            panel.activate(setup);
            return null;
        });
        assertEquals(SetupItem.of(4151), appliedLayouts.get(appliedLayouts.size() - 1).placements().get(0).item());

        onEdt(() -> {
            variants.invoke("Mage");
            return null;
        });
        GearSetup switched = book.setup(setup.id()).orElseThrow();
        assertEquals(1, switched.selectedIndex());
        assertEquals(Optional.of(SetupItem.of(11791)), ((GearContent) switched.content()).equipped(EquipmentSlot.WEAPON));
        assertTrue(labels().contains("Vorkath now shows Mage"));
        assertEquals(List.of("Mage"), onEdt(() -> panel.sectionViews().get(0).grid().tiles().get(0).contextMenu().submenu(SetupTile.VARIANTS).orElseThrow().chosenTexts()));
        assertEquals(SetupItem.of(11791), appliedLayouts.get(appliedLayouts.size() - 1).placements().get(0).item(), "the open bank switches to the chosen variant");

        int drawn = appliedLayouts.size();
        onEdt(() -> {
            panel.editContents(switched);
            panel.editSlot(SlotRef.of(EquipmentSlot.HEAD));
            panel.slotEditorForm().orElseThrow().chooseItem(ItemId.of(1163));
            panel.slotEditorForm().orElseThrow().save();
            return null;
        });
        assertEquals(drawn + 1, appliedLayouts.size(), "an edit to the active setup redraws the bank");
        assertEquals(2, appliedLayouts.get(appliedLayouts.size() - 1).placements().size());
    }

    @Test
    void theComparePageDiffsAgainstAnotherSetupOrTheWornGear() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Trident").withContent(GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151))
                .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 4))))));
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Sang").withContent(GearContent.empty()
                .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 6))))));
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Bank").withContent(BankContent.empty())));
        GearSetup trident = book.sections().get(0).setups().get(0);

        onEdt(() -> {
            panel.compare(trident);
            return null;
        });
        DiffPage page = panel.diffPage().orElseThrow();
        assertEquals("Sang", page.chosen().orElseThrow().setup().orElseThrow().name(), "the first comparable setup is picked");
        assertEquals(List.of("Weapon removed", "Inventory slot 1 +2"), page.rowTexts());
        assertTrue(labels().contains("Trident vs Sang"));
        assertTrue(labels().stream().anyMatch(text -> text.contains("2.5M")), "the ledger delta is listed");

        onEdt(() -> {
            chip(page, DiffPage.WORN_NOW).doClick();
            return null;
        });
        assertTrue(page.chosen().orElseThrow().isLive());
        assertTrue(labels().contains("Trident vs " + DiffPage.WORN_NOW));
        assertTrue(page.rowTexts().contains("Ring added"));
    }

    @Test
    void theSharePageSavesAndCopies() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151)))));
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.share(setup);
            return null;
        });
        SharePage page = panel.sharePage().orElseThrow();
        assertTrue(page.withLedger());

        onEdt(() -> {
            find(page, PrimaryButton.class).orElseThrow().doClick();
            return null;
        });
        flushEdt();
        assertEquals(List.of("gear-composer-vorkath"), savedImages);
        assertTrue(labels().contains("Saved Vorkath to the test folder"));

        onEdt(() -> {
            chipOrButton(page, SharePage.COPY_IMAGE).doClick();
            return null;
        });
        flushEdt();
        assertEquals(1, copiedImages.size(), "the picture went to the clipboard");

        onEdt(() -> {
            chipOrButton(page, SharePage.COPY_TEXT).doClick();
            return null;
        });
        flushEdt();
        assertTrue(clipboard.paste().orElseThrow().contains("Value 2.5M"));
    }

    @Test
    void aFilledSlotNamesItsItemInTheTooltip() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151, 1)))));
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        flushEdt();
        SlotView weapon = panel.contentPanel().orElseThrow().slots().stream()
                .filter(slot -> slot.ref().equals(SlotRef.of(EquipmentSlot.WEAPON))).findFirst().orElseThrow();
        String tooltip = onEdt(weapon::getToolTipText);
        assertTrue(tooltip.contains("<b>Abyssal whip</b><br>"), tooltip);
        assertTrue(tooltip.contains("Weapon: 1x"), tooltip);
    }

    private GearSetup customSetup(int rows) throws Exception {
        onEdt(() -> {
            panel.createSetup();
            SetupEditorForm form = panel.editorForm().orElseThrow();
            form.setEnteredName("Trip");
            form.chooseType(SetupType.CUSTOM);
            form.chooseRows(rows);
            form.save();
            return null;
        });
        return book.sections().get(0).setups().stream().filter(setup -> setup.name().equals("Trip")).findFirst().orElseThrow();
    }

    private SetupContentPanel customPage(GearSetup setup) throws Exception {
        onEdt(() -> {
            panel.editContents(setup);
            return null;
        });
        return panel.contentPanel().orElseThrow();
    }

    private CustomContent customContent(GearSetup setup) {
        return (CustomContent) book.setup(setup.id()).orElseThrow().content();
    }

    @Test
    void theEditorOffersRowsOnlyForACustomLayout() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            return null;
        });
        SetupEditorForm form = panel.editorForm().orElseThrow();
        assertFalse(form.isRowsChooserShowing(), "rows are hidden for a gear layout");
        onEdt(() -> {
            form.chooseType(SetupType.CUSTOM);
            return null;
        });
        assertTrue(form.isRowsChooserShowing());
        assertEquals(CustomContent.DEFAULT_ROWS, form.chosenRows());

        GearSetup trip = customSetup(3);
        assertEquals(SetupType.CUSTOM, trip.type());
        assertEquals(3, customContent(trip).rows());
        assertTrue(customContent(trip).isEmpty());
    }

    @Test
    void cellsGetTheirKindFromThePlaceholderAndShowUpOnTheMap() throws Exception {
        GearSetup trip = customSetup(2);
        SetupContentPanel page = customPage(trip);
        assertEquals(2, page.cellViews().size(), "one row of two cells is shown at a time");
        assertEquals(0, page.shownRow());
        assertTrue(buttonTexts().containsAll(List.of("Row 1", "Row 2")), "rows have tabs");
        assertTrue(page.slots().isEmpty(), "empty cells have no slots");
        assertTrue(buttonTexts().contains(CellKind.EQUIPMENT.displayName()));

        onEdt(() -> {
            panel.setCellKind(CellRef.of(0, 0), CellKind.EQUIPMENT);
            panel.setCellKind(CellRef.of(0, 1), CellKind.INVENTORY);
            return null;
        });
        CustomContent content = customContent(trip);
        assertEquals(CellKind.EQUIPMENT, content.cell(CellRef.of(0, 0)).kind());
        assertEquals(CellKind.INVENTORY, content.cell(CellRef.of(0, 1)).kind());
        SetupContentPanel rebuilt = panel.contentPanel().orElseThrow();
        assertEquals(EquipmentSlot.values().length + ItemGrid.SIZE, rebuilt.slots().size());
        assertEquals(Optional.of(CellRef.of(0, 1)), rebuilt.currentCell(), "the map follows the cell just changed");

        onEdt(() -> {
            rebuilt.showCell(CellRef.of(1, 0));
            return null;
        });
        assertEquals(Optional.of(CellRef.of(1, 0)), rebuilt.currentCell());
        assertEquals(1, rebuilt.shownRow(), "the map jumps to the cell's row");
        assertTrue(labels().contains("Row 2 left"));
        assertFalse(labels().contains("Row 1 left"), "the other row is out of the way");
        onEdt(() -> {
            panel.setCellKind(CellRef.of(1, 1), CellKind.INVENTORY);
            return null;
        });
        assertEquals(1, panel.contentPanel().orElseThrow().shownRow(), "an edit keeps the row");
        assertTrue(labels().contains("Row 2 left"));
    }

    @Test
    void cellSlotsEditLikeAnyOtherAndTheTooltipNamesTheCell() throws Exception {
        GearSetup trip = customSetup(1);
        customPage(trip);
        onEdt(() -> {
            panel.setCellKind(CellRef.of(0, 0), CellKind.EQUIPMENT);
            panel.editSlot(SlotRef.in(CellRef.of(0, 0), SlotRef.of(EquipmentSlot.WEAPON)));
            panel.slotEditorForm().orElseThrow().chooseItem(ItemId.of(4151));
            panel.slotEditorForm().orElseThrow().save();
            return null;
        });
        assertEquals(Optional.of(SetupItem.of(4151)), customContent(trip).cell(CellRef.of(0, 0)).item(SlotRef.of(EquipmentSlot.WEAPON)));
        flushEdt();
        SlotView weapon = panel.contentPanel().orElseThrow().slots().stream()
                .filter(slot -> slot.ref().equals(SlotRef.in(CellRef.of(0, 0), SlotRef.of(EquipmentSlot.WEAPON)))).findFirst().orElseThrow();
        assertTrue(onEdt(weapon::getToolTipText).contains("Row 1 left, Weapon"));
    }

    @Test
    void cellsFillFromTheGameAndFromOtherSetups() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty()
                .withEquipped(EquipmentSlot.HEAD, SetupItem.of(1163)))));
        GearSetup trip = customSetup(1);
        customPage(trip);
        onEdt(() -> {
            panel.setCellKind(CellRef.of(0, 0), CellKind.EQUIPMENT);
            panel.setCellKind(CellRef.of(0, 1), CellKind.INVENTORY);
            panel.fillCellFromGame(CellRef.of(0, 1));
            return null;
        });
        assertEquals(WORN_AND_CARRIED.inventory(), customContent(trip).cell(CellRef.of(0, 1)).inventory());
        assertTrue(labels().contains("Filled Row 1 right from the game"));

        onEdt(() -> {
            panel.fillCellFromSetup(CellRef.of(0, 0));
            return null;
        });
        CellSourcePage sources = panel.cellSourcePage().orElseThrow();
        assertEquals(List.of("Vorkath · Equipment"), sources.sources().stream().map(CellSources.Source::label).collect(Collectors.toList()));
        onEdt(() -> {
            chipOrButton(sources, "Vorkath · Equipment").doClick();
            return null;
        });
        LayoutCell filled = customContent(trip).cell(CellRef.of(0, 0));
        assertEquals(Optional.of(SetupItem.of(1163)), filled.item(SlotRef.of(EquipmentSlot.HEAD)));
        assertEquals("Vorkath", filled.label(), "the cell takes the source's name");
        assertTrue(panel.contentPanel().isPresent(), "back on the contents page");
    }

    @Test
    void cellsRenameClearAndEmptyFromTheirMenu() throws Exception {
        GearSetup trip = customSetup(1);
        customPage(trip);
        onEdt(() -> {
            panel.setCellKind(CellRef.of(0, 0), CellKind.INVENTORY);
            panel.dropItem(WHIP, SlotRef.in(CellRef.of(0, 0), SlotRef.of(GridKind.INVENTORY, 0)));
            return null;
        });
        CellView cell = panel.contentPanel().orElseThrow().cellViews().get(0);
        assertTrue(onEdt(() -> cell.contextMenu().entryTexts()).containsAll(List.of(CellView.FILL_FROM_SETUP, CellView.FILL_FROM_GAME,
                CellView.RENAME, CellView.CLEAR_ITEMS, CellView.EMPTY_CELL)));

        onEdt(() -> {
            panel.content().renameCell(CellRef.of(0, 0), "Supplies");
            return null;
        });
        assertEquals("Supplies", customContent(trip).cell(CellRef.of(0, 0)).label());
        assertTrue(labels().contains("Supplies"));

        onEdt(() -> {
            panel.clearCell(CellRef.of(0, 0));
            return null;
        });
        LayoutCell cleared = customContent(trip).cell(CellRef.of(0, 0));
        assertTrue(cleared.isEmpty());
        assertEquals(CellKind.INVENTORY, cleared.kind());
        assertEquals("Supplies", cleared.label());

        onEdt(() -> {
            panel.content().emptyCell(CellRef.of(0, 0));
            return null;
        });
        assertTrue(customContent(trip).cell(CellRef.of(0, 0)).isBlank());
        assertTrue(panel.contentPanel().orElseThrow().slots().isEmpty());
    }

    @Test
    void rowsChangeThroughTheEditorAndShrinkingIsExplicit() throws Exception {
        GearSetup trip = customSetup(1);
        customPage(trip);
        onEdt(() -> {
            panel.setCellKind(CellRef.of(0, 0), CellKind.INVENTORY);
            panel.edit(book.setup(trip.id()).orElseThrow());
            panel.editorForm().orElseThrow().chooseRows(3);
            panel.editorForm().orElseThrow().save();
            return null;
        });
        assertEquals(3, customContent(trip).rows());
        assertEquals(CellKind.INVENTORY, customContent(trip).cell(CellRef.of(0, 0)).kind(), "existing cells survive growing");

        onEdt(() -> {
            panel.setupCommands().resizeRows(trip.id(), 1);
            return null;
        });
        assertEquals(1, customContent(trip).rows());
    }

    @Test
    void aCustomLayoutComparesOnlyWithSameShapedOnesAndSharesAsText() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Other").withContent(CustomContent.empty(2))));
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Taller").withContent(CustomContent.empty(3))));
        GearSetup trip = customSetup(2);
        onEdt(() -> {
            panel.compare(trip);
            return null;
        });
        DiffPage page = panel.diffPage().orElseThrow();
        assertEquals("Other", page.chosen().orElseThrow().setup().orElseThrow().name());
        assertNull(chipOrNull(page, "Taller"), "a different row count is not offered");
        assertNull(chipOrNull(page, DiffPage.WORN_NOW), "no single set to compare with what is worn");

        onEdt(() -> {
            panel.share(trip);
            chipOrButton(panel.sharePage().orElseThrow(), SharePage.COPY_TEXT).doClick();
            return null;
        });
        flushEdt();
        assertTrue(clipboard.paste().orElseThrow().startsWith("**Trip** · Custom Layout"));
    }

    @Test
    void aParentWithSubSectionsDoesNotReadAsEmptyButItsEmptyChildDoes() throws Exception {
        SectionId raids = onEdt(() -> book.addSection("Raids").id());
        onEdt(() -> book.addSubSection(raids, "ToA"));
        List<SectionView> views = panel.sectionViews();
        assertFalse(labelsIn(views.get(1)).contains("Empty"), "the parent holds its setups in its children");
        assertTrue(labelsIn(views.get(2)).contains("Empty"), "the child is empty");
        assertTrue(labelsIn(views.get(0)).contains("Empty"), "a childless empty section still says so");
    }

    private static List<String> labelsIn(Container container) {
        List<String> found = new ArrayList<>();
        collectLabels(container, found);
        return found;
    }

    @Test
    void aPageGuideStepsThroughItsTopicsAndOnlyCountsWhenFinished() throws Exception {
        addSetups("Vorkath");
        GuideRunner runner = panel.guides();
        onEdt(() -> {
            panel.startGuide(GuideId.LIST);
            return null;
        });
        assertTrue(runner.isRunning());
        assertEquals(HelpTopic.LIST_PAGE, runner.currentStep().orElseThrow().topic());
        assertEquals("The setup list", runner.callout().orElseThrow().titleText());
        assertEquals("1 / " + Guides.of(GuideId.LIST).length(), runner.callout().orElseThrow().counterText());
        assertTrue(panel.guideAnchor(HelpTopic.NEW_SETUP).isPresent(), "the header button is tagged");
        assertTrue(panel.guideAnchor(HelpTopic.SETUP_TILE).isPresent(), "tiles are tagged");

        onEdt(() -> {
            runner.next();
            runner.next();
            runner.back();
            return null;
        });
        assertEquals(1, runner.stepIndex());
        onEdt(() -> {
            runner.end();
            return null;
        });
        assertFalse(runner.isRunning());
        assertFalse(guideProgress.isCompleted(GuideId.LIST), "leaving early earns no tick");

        onEdt(() -> {
            panel.startGuide(GuideId.LIST);
            for (int i = 0; i < Guides.of(GuideId.LIST).length(); i++) {
                runner.next();
            }
            return null;
        });
        assertFalse(runner.isRunning());
        assertTrue(guideProgress.isCompleted(GuideId.LIST));
        assertFalse(panel.guideButton(GuideId.LIST).isUnseen(), "the dot goes once the guide is complete");
        assertTrue(panel.guideButton(GuideId.EDITOR).isUnseen());
    }

    @Test
    void aTutorialWalksItsSampleAcrossThePagesAndRemovesItAtTheEnd() throws Exception {
        prompts.answering(false);
        GuideRunner runner = panel.guides();
        onEdt(() -> {
            panel.startGuide(GuideId.TUTORIAL_GEAR);
            return null;
        });
        SetupId sample = runner.sample().orElseThrow();
        assertTrue(SampleSetups.isSample(book.setup(sample).orElseThrow()));
        assertFalse(panel.isOnboardingShowing(), "the sample fills the book while the tutorial runs");

        Guide tutorial = Guides.of(GuideId.TUTORIAL_GEAR);
        int editorStep = indexOfPage(tutorial, GuidePage.EDITOR);
        int contentsStep = indexOfPage(tutorial, GuidePage.CONTENTS);
        int slotStep = indexOfPage(tutorial, GuidePage.SLOT);
        int bankStep = indexOfPage(tutorial, GuidePage.BANK_PICTURE);

        stepTo(runner, editorStep);
        assertTrue(panel.editorForm().isPresent(), "the editor opens on the sample");
        assertTrue(panel.guideAnchor(HelpTopic.EDITOR_NAME).isPresent());
        stepTo(runner, contentsStep);
        assertTrue(panel.contentPanel().isPresent());
        assertTrue(panel.guideAnchor(HelpTopic.VARIANTS).isPresent());
        stepTo(runner, slotStep);
        assertTrue(panel.slotEditorForm().isPresent());
        assertTrue(panel.guideAnchor(HelpTopic.SLOT_AMOUNT).isPresent());
        stepTo(runner, bankStep);
        assertTrue(panel.bankPicturePage().isPresent());
        flushEdt();
        assertTrue(panel.bankPicturePage().orElseThrow().hasPicture(), "the bank picture is drawn from the planner");
        assertTrue(appliedLayouts.isEmpty(), "no bank is open, so nothing is shown live");

        stepTo(runner, tutorial.length() - 1);
        onEdt(() -> {
            runner.next();
            return null;
        });
        assertFalse(runner.isRunning());
        assertTrue(book.setup(sample).isEmpty(), "the sample is gone");
        assertTrue(guideProgress.isCompleted(GuideId.TUTORIAL_GEAR));
        assertTrue(panel.isListShowing());
        assertFalse(panel.isOnboardingShowing(), "a completed tutorial retires the front door");
    }

    @Test
    void leavingATutorialEarlyStillRemovesTheSample() throws Exception {
        GuideRunner runner = panel.guides();
        onEdt(() -> {
            panel.startGuide(GuideId.TUTORIAL_CUSTOM);
            runner.next();
            runner.next();
            return null;
        });
        SetupId sample = runner.sample().orElseThrow();
        assertEquals(SetupType.CUSTOM, book.setup(sample).orElseThrow().type());
        onEdt(() -> {
            panel.endGuide();
            return null;
        });
        assertTrue(book.setup(sample).isEmpty());
        assertFalse(guideProgress.isCompleted(GuideId.TUTORIAL_CUSTOM));
        assertTrue(panel.isOnboardingShowing());
    }

    @Test
    void theFrontDoorAndTheGuidesPageOfferEveryGuide() throws Exception {
        assertTrue(buttonTexts().containsAll(List.of("Gear Layout", "Bank Layout", "Custom Layout", GuideId.LIST.title())));
        assertTrue(onEdt(() -> panel.overflowMenu().entryTexts()).contains(GearSetupPanel.GUIDES));
        onEdt(() -> {
            panel.showGuides();
            return null;
        });
        GuidesPage page = panel.guidesPage().orElseThrow();
        assertEquals(3, page.guides().size(), "only the tutorials are listed");
        assertTrue(page.guides().stream().allMatch(guide -> guide.id().isTutorial()));
        assertTrue(buttonTexts().contains("Bank Layout"));
        onEdt(() -> {
            chipOrButton(page, "Bank Layout").doClick();
            return null;
        });
        assertEquals(GuideId.TUTORIAL_BANK, panel.guides().current().orElseThrow().id());
        assertTrue(panel.guides().sample().isPresent(), "a tutorial brings its own sample");
        onEdt(() -> {
            panel.endGuide();
            return null;
        });
        assertTrue(panel.isListShowing());
        assertEquals(0, book.setupCount(), "the sample is gone again");
    }

    private static int indexOfPage(Guide guide, GuidePage page) {
        for (int i = 0; i < guide.length(); i++) {
            if (guide.steps().get(i).page() == page) {
                return i;
            }
        }
        throw new IllegalArgumentException("No step on " + page);
    }

    private static void stepTo(GuideRunner runner, int index) throws Exception {
        onEdt(() -> {
            while (runner.stepIndex() < index) {
                runner.next();
            }
            return null;
        });
    }

    @Test
    void theSpotlightCoversOnlyTheVisiblePartAndLeavesWhenTheSidebarHides() throws Exception {
        addSetups("Vorkath", "Zulrah", "Bandos", "Slayer", "Barrows", "Cox", "Toa", "Tob", "Nex", "Zilyana", "Graardor", "Kril");
        JRootPane root = onEdt(() -> {
            JRootPane pane = new JRootPane();
            JScrollPane scroller = new JScrollPane(panel);
            pane.getContentPane().setLayout(new BorderLayout());
            pane.getContentPane().add(scroller, BorderLayout.CENTER);
            pane.setSize(242, 300);
            pane.doLayout();
            pane.getContentPane().doLayout();
            scroller.doLayout();
            scroller.getViewport().doLayout();
            panel.setSize(panel.getPreferredSize());
            return pane;
        });
        int layered = root.getLayeredPane().getComponentCount();
        onEdt(() -> {
            panel.startGuide(GuideId.LIST);
            return null;
        });
        assertEquals(layered + 1, root.getLayeredPane().getComponentCount(), "the overlay joins the layered pane");
        Spotlight spotlight = panel.guides().spotlight().orElseThrow();
        Rectangle covered = onEdt(spotlight::getBounds);
        assertTrue(covered.height <= 300, "the overlay is no taller than what is on screen, got " + covered.height);
        assertTrue(spotlight.hole().isEmpty(), "the whole-page step frames nothing");
        Rectangle callout = onEdt(spotlight::calloutBounds);
        assertTrue(callout.y >= 0 && callout.y + callout.height <= covered.height, "the callout sits inside the view");

        onEdt(() -> {
            panel.guides().next();
            return null;
        });
        assertTrue(spotlight.hole().isPresent(), "the header button is framed");
        assertTrue(spotlight.hole().get().height < covered.height * 0.6);

        onEdt(() -> {
            panel.setVisible(false);
            return null;
        });
        flushEdt();
        assertFalse(panel.guides().isRunning(), "hiding the sidebar ends the guide");
        assertEquals(layered, root.getLayeredPane().getComponentCount(), "and takes the overlay with it");
        onEdt(() -> {
            panel.setVisible(true);
            return null;
        });
    }

    @Test
    void everyPageGuideOpensItsOwnPageWhenStartedFromTheList() throws Exception {
        prompts.answering(false);
        for (GuideId id : GuideId.values()) {
            if (id.isTutorial()) {
                continue;
            }
            onEdt(() -> {
                panel.startStandaloneGuide(id);
                return null;
            });
            GuideRunner runner = panel.guides();
            assertTrue(runner.isRunning(), id.name());
            assertFalse(panel.guidesPage().isPresent(), id + " left the guides list");
            for (int i = 0; i < Guides.standalone(id).length(); i++) {
                GuideStep step = runner.currentStep().orElseThrow();
                if (step.anchored()) {
                    assertTrue(panel.guideAnchor(step.topic()).isPresent(), id + " step " + i + " has nothing to frame for " + step.topic());
                }
                onEdt(() -> {
                    runner.next();
                    return null;
                });
            }
            assertFalse(runner.isRunning(), id.name());
            assertEquals(0, book.setupCount(), id + " left its sample behind");
        }
    }

    @Test
    void theContentsNavBarKeepsItsToolsBesideTheGuideButton() throws Exception {
        addSetups("Vorkath");
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        assertTrue(panel.guideAnchor(HelpTopic.CONTENTS_TOOLS).isPresent(), "the page tools are still on the nav bar");
        assertTrue(hasIcon(panel.contentPanel().orElseThrow(), ActionIcon.SEARCH));
        assertTrue(hasIcon(panel.contentPanel().orElseThrow(), ActionIcon.HELP));
    }

    @Test
    void quickPrayersArePickedFromTheBookAndWarnWhenTheGameDiffers() throws Exception {
        onEdt(() -> {
            panel.createSetup();
            SetupEditorForm form = panel.editorForm().orElseThrow();
            form.setEnteredName("Zulrah");
            form.prayerPicker().toggle(Prayer.AUGURY);
            form.prayerPicker().toggle(Prayer.RIGOUR);
            form.prayerPicker().toggle(Prayer.PIETY);
            form.prayerPicker().toggle(Prayer.PIETY);
            form.save();
            return null;
        });
        GearSetup zulrah = book.sections().get(0).setups().get(0);
        assertEquals(Set.of(Prayer.AUGURY, Prayer.RIGOUR), zulrah.meta().requirements().quickPrayers());
        onEdt(() -> {
            panel.editContents(zulrah);
            return null;
        });
        assertTrue(labels().stream().anyMatch(text -> text.contains("Quick prayers: Rigour, Augury")), "the contents page lists them");

        onEdt(() -> {
            panel.showListCard();
            panel.activate(zulrah);
            quickPrayers.set(Set.of(Prayer.PIETY));
            return null;
        });
        flushEdt();
        assertTrue(panel.isBannerShowing());
        assertTrue(labels().stream().anyMatch(text -> text.contains("Quick prayers differ: set Rigour, Augury")));

        onEdt(() -> {
            quickPrayers.set(Set.of(Prayer.RIGOUR, Prayer.AUGURY));
            return null;
        });
        flushEdt();
        assertFalse(panel.isBannerShowing());

        onEdt(() -> {
            panel.edit(book.setup(zulrah.id()).orElseThrow());
            return null;
        });
        assertTrue(panel.editorForm().orElseThrow().prayerPicker().isChosen(Prayer.RIGOUR), "the picker reopens with the saved choice");
    }

    @Test
    void theSidebarIsOnlyAsTallAsThePageOnShow() throws Exception {
        addSetups("Vorkath");
        int listHeight = onEdt(() -> panel.getPreferredSize().height);
        onEdt(() -> {
            panel.editContents(book.sections().get(0).setups().get(0));
            return null;
        });
        int contentsHeight = onEdt(() -> panel.getPreferredSize().height);
        assertTrue(contentsHeight > listHeight, "the contents page is the taller one");
        onEdt(() -> {
            panel.showListCard();
            return null;
        });
        assertEquals(listHeight, onEdt(() -> panel.getPreferredSize().height), "back on the list the hidden contents page adds nothing");
    }

    @Test
    void aStatusChangeNeverScrollsTheSidebar() throws Exception {
        addSetups("Vorkath", "Zulrah", "Bandos", "Slayer", "Barrows", "Cox", "Toa", "Tob", "Nex", "Zilyana", "Graardor", "Kril");
        JScrollPane scroller = onEdt(() -> {
            JScrollPane pane = new JScrollPane(panel);
            pane.setSize(242, 200);
            pane.doLayout();
            pane.getViewport().doLayout();
            panel.setSize(panel.getPreferredSize());
            return pane;
        });
        onEdt(() -> {
            scroller.getViewport().setViewPosition(new Point(0, 40));
            panel.activate(book.sections().get(0).setups().get(0));
            return null;
        });
        flushEdt();
        assertEquals(40, onEdt(() -> scroller.getViewport().getViewPosition().y), "showing a setup leaves the scroll where it was");
    }

    @Test
    void dividersAreAddedRenamedAndRemovedFromTheGrid() throws Exception {
        onEdt(() -> book.addSetup(firstSection, GearSetup.named("Vorkath").withContent(GearContent.empty()
                .withInventory(ItemGrid.EMPTY.withSlot(4, SetupItem.of(385, 4))))));
        GearSetup setup = book.sections().get(0).setups().get(0);
        onEdt(() -> {
            panel.editContents(setup);
            return null;
        });
        SlotView slot = panel.contentPanel().orElseThrow().slots().stream()
                .filter(view -> view.ref().equals(SlotRef.of(GridKind.INVENTORY, 4))).findFirst().orElseThrow();
        assertTrue(onEdt(() -> slot.contextMenu().entryTexts()).contains(SlotView.ADD_DIVIDER));

        onEdt(() -> {
            panel.editDivider(SlotRef.of(GridKind.INVENTORY, 4));
            return null;
        });
        DividerEditorForm form = panel.dividerForm().orElseThrow();
        assertEquals(0, form.fromColumn());
        assertEquals(ItemGrid.COLUMNS - 1, form.toColumn(), "a new divider on a free row takes the whole row");
        assertTrue(form.draft().isEmpty(), "no text yet");
        onEdt(() -> {
            form.setLabel("x".repeat(40));
            assertTrue(form.draft().isEmpty(), "an overlong label cannot be saved");
            form.setLabel("Food");
            form.save();
            return null;
        });
        GearContent gear = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(List.of(Divider.acrossRow(1, "Food")), gear.inventory().dividers());
        assertEquals(Optional.of(SetupItem.of(385, 4)), gear.inventory().slot(4), "slots are untouched");
        assertTrue(panel.contentPanel().isPresent(), "saving returns to the contents page");
        DividerStrip strip = ((ItemGridView) panel.guideAnchor(HelpTopic.INVENTORY).orElseThrow().getComponent(0)).dividers().get(0);
        assertEquals(List.of("Food"), strip.labels(), "the divider strip is drawn");
        assertTrue(panel.guideAnchor(HelpTopic.DIVIDER).isPresent());

        onEdt(() -> {
            panel.editDivider(SlotRef.of(GridKind.INVENTORY, 6));
            return null;
        });
        DividerEditorForm edit = panel.dividerForm().orElseThrow();
        assertEquals("Food", edit.label(), "clicking the divider opens it");
        onEdt(() -> {
            edit.toggleColumn(0);
            edit.toggleColumn(1);
            edit.setAlign(TextAlign.RIGHT);
            edit.setLabel("Pots");
            edit.save();
            return null;
        });
        gear = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(List.of(new Divider(1, 2, 3, "Pots", TextAlign.RIGHT)), gear.inventory().dividers(), "shrunk from the left and moved right");

        onEdt(() -> {
            panel.editDivider(SlotRef.of(GridKind.INVENTORY, 4));
            return null;
        });
        DividerEditorForm second = panel.dividerForm().orElseThrow();
        assertEquals(List.of(0, 1), List.of(second.fromColumn(), second.toColumn()), "a new divider takes the free columns beside the others");
        onEdt(() -> {
            second.setLabel("Food");
            second.save();
            return null;
        });
        gear = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(List.of("Food", "Pots"), gear.inventory().dividersAbove(1).stream().map(Divider::label).collect(Collectors.toList()));
        assertEquals(List.of(1), List.copyOf(gear.inventory().rowsWithDividers()), "both share one header");
        DividerStrip shared = ((ItemGridView) panel.guideAnchor(HelpTopic.INVENTORY).orElseThrow().getComponent(0)).dividers().get(0);
        assertEquals(List.of(DividerStrip.EDIT, DividerChange.TEXT_CENTRE.label(), DividerChange.TEXT_RIGHT.label(), DividerChange.NO_UNDERLINE.label(), DividerStrip.REMOVE),
                onEdt(() -> shared.contextMenu(0).entryTexts()));
        assertEquals(List.of(DividerStrip.ADD), onEdt(() -> new DividerStrip(SlotRef.of(GridKind.INVENTORY, 8), List.of(), panel.contentPanel().orElseThrow()).contextMenu(0).entryTexts()));

        onEdt(() -> {
            panel.changeDivider(SlotRef.of(GridKind.INVENTORY, 4), DividerChange.TEXT_CENTRE);
            panel.changeDivider(SlotRef.of(GridKind.INVENTORY, 4), DividerChange.NO_UNDERLINE);
            panel.removeDivider(SlotRef.of(GridKind.INVENTORY, 7));
            return null;
        });
        gear = (GearContent) book.setup(setup.id()).orElseThrow().content();
        assertEquals(List.of(new Divider(1, 0, 1, "Food", TextAlign.CENTRE, false)), gear.inventory().dividers(), "only the divider over that column is removed");

        onEdt(() -> {
            panel.editDivider(SlotRef.of(GridKind.INVENTORY, 4));
            return null;
        });
        DividerEditorForm reopened = panel.dividerForm().orElseThrow();
        assertFalse(reopened.isUnderlined(), "the editor reopens with the line off");
        onEdt(() -> {
            reopened.setUnderlined(true);
            reopened.save();
            return null;
        });
        assertTrue(((GearContent) book.setup(setup.id()).orElseThrow().content()).inventory().dividers().get(0).underlined());
    }

}
