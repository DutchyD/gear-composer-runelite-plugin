package dev.dutchy.runelite;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SetupFilter;
import dev.dutchy.runelite.gear.UndoHistory;
import dev.dutchy.runelite.gear.activation.ActivationToast;
import dev.dutchy.runelite.gear.activation.SetupActivator;
import dev.dutchy.runelite.gear.activation.SetupHotkeys;
import dev.dutchy.runelite.gear.bank.*;
import dev.dutchy.runelite.gear.content.DropRule;
import dev.dutchy.runelite.gear.history.FileHistoryStore;
import dev.dutchy.runelite.gear.history.HistoryStore;
import dev.dutchy.runelite.gear.history.SetupHistory;
import dev.dutchy.runelite.gear.ledger.ItemFactsSource;
import dev.dutchy.runelite.gear.requirements.RequirementWatch;
import dev.dutchy.runelite.gear.share.Clipboard;
import dev.dutchy.runelite.gear.share.ImageSink;
import dev.dutchy.runelite.gear.share.ShareService;
import dev.dutchy.runelite.gear.share.SystemClipboard;
import dev.dutchy.runelite.gear.transfer.BookTransfer;
import dev.dutchy.runelite.gear.transfer.FileDialogs;
import dev.dutchy.runelite.gear.transfer.SwingFileDialogs;
import dev.dutchy.runelite.gear.ui.*;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;

/** What one run of the plugin shares: the book read from storage and everything built on it, each made once. */
final class SessionModule extends AbstractModule {

    private final GearSetupBook book;
    private final Path dataDir;

    SessionModule(GearSetupBook book, Path dataDir) {
        this.book = Objects.requireNonNull(book, "book");
        this.dataDir = Objects.requireNonNull(dataDir, "dataDir");
    }

    @Override
    protected void configure() {
        bind(GearSetupBook.class).toInstance(book);
        bind(ActiveSetup.class).in(Singleton.class);
        bind(SetupActivator.class).in(Singleton.class);
        bind(RequirementWatch.class).in(Singleton.class);
        bind(UndoHistory.class).in(Singleton.class);
        bind(SetupHistory.class).in(Singleton.class);
        bind(DropRule.class).in(Singleton.class);
        bind(BookTransfer.class).in(Singleton.class);
        bind(SetupFilter.class).in(Singleton.class);
        bind(BankLayoutPlanner.class).in(Singleton.class);
        bind(SetupHotkeys.class).in(Singleton.class);
        bind(ActivationToast.class).in(Singleton.class);
        bind(BankTitle.class).in(Singleton.class);
        bind(BankSlotOverlay.class).in(Singleton.class);
        bind(NoteToggleOverlay.class).in(Singleton.class);
        bind(BankLabelOverlay.class).in(Singleton.class);
        bind(BankTabWatch.class).in(Singleton.class);
        bind(BankTabSizes.class).in(Singleton.class);
        bind(BankSearchWatch.class).in(Singleton.class);
        bind(WithdrawMenuSwapper.class).in(Singleton.class);
        bind(VariantSwitchMenu.class).in(Singleton.class);
        bind(PageParts.class).in(Singleton.class);
        bind(BulkDelete.class).in(Singleton.class);
        bind(Transfer.class).in(Singleton.class);
        bind(GuideSamples.class).in(Singleton.class);
        bind(Accounts.class).in(Singleton.class);
        bind(SectionCommands.class).in(Singleton.class);
        bind(SetupCommands.class).in(Singleton.class);
        bind(GearSetupPanel.class).in(Singleton.class);
        bind(GearComposerSession.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Provides
    @Singleton
    HistoryStore historyStore(Gson gson) {
        return new FileHistoryStore(dataDir.resolve("history"), gson);
    }

    @Provides
    @Singleton
    FileDialogs fileDialogs() {
        return new SwingFileDialogs(null, dataDir.toFile());
    }

    @Provides
    @Singleton
    Clipboard clipboard() {
        return new SystemClipboard();
    }

    @Provides
    @Singleton
    ShareService shareService(ItemIconFactory icons, ItemFactsSource facts, ImageSink images, Clipboard clipboard) {
        return new ShareService(icons.loader(), facts, images, clipboard);
    }
}
