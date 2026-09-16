package dev.dutchy.runelite;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.dutchy.runelite.gear.Dispatch;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.account.AccountDirectory;
import dev.dutchy.runelite.gear.account.ConfigAccountDirectory;
import dev.dutchy.runelite.gear.account.CurrentAccount;
import dev.dutchy.runelite.gear.account.RuneLiteAccount;
import dev.dutchy.runelite.gear.bank.*;
import dev.dutchy.runelite.gear.config.*;
import dev.dutchy.runelite.gear.content.EquipmentSlots;
import dev.dutchy.runelite.gear.items.*;
import dev.dutchy.runelite.gear.persistence.*;
import dev.dutchy.runelite.gear.player.PlayerItems;
import dev.dutchy.runelite.gear.player.RuneLiteEquipmentSlots;
import dev.dutchy.runelite.gear.player.RuneLitePlayerItems;
import dev.dutchy.runelite.gear.requirements.CurrentQuickPrayers;
import dev.dutchy.runelite.gear.requirements.CurrentSpellbook;
import dev.dutchy.runelite.gear.requirements.RuneLiteQuickPrayers;
import dev.dutchy.runelite.gear.requirements.RuneLiteSpellbook;
import dev.dutchy.runelite.gear.ui.*;
import dev.dutchy.runelite.libs.ui.ItemUiModule;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.banktags.BankTagsPlugin;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Clock;

@Slf4j
@PluginDependency(BankTagsPlugin.class)
@PluginDescriptor(
        name = "Gear Composer",
        description = "Lays out your bank in the gear setup you request",
        tags = {"bank layouts", "inventory layouts", "gear", "layouts", "bank", "inventory"}
)
public class GearComposerPlugin extends Plugin {

    static final Path DATA_DIR = RuneLite.RUNELITE_DIR.toPath().resolve("gear-composer");

    @Inject
    private BookPersistence persistence;

    @Inject
    private ConfigManager configManager;

    private GearComposerSession session;

    @Provides
    GearComposerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GearComposerConfig.class);
    }

    @Provides
    @Singleton
    BackupStore provideBackups() {
        return new FileBackupStore(DATA_DIR.resolve("backups"), BackupRotation.standard(), Clock.systemUTC());
    }

    @Provides
    @Singleton
    BookStore provideBookStore(ConfigBookStore store, BackupStore backups) {
        return new BackedUpBookStore(store, backups);
    }

    @Override
    public void configure(Binder binder) {
        binder.install(new ItemUiModule());
        binder.bind(BankContents.class).to(RuneLiteBankContents.class);
        binder.bind(BankItemNames.class).to(RuneLiteBankItemNames.class);
        binder.bind(BankTagState.class).to(RuneLiteBankTags.class);
        binder.bind(BankLayoutApplier.class).to(BankDisplay.class);
        binder.bind(DrawnBankSource.class).to(BankDisplay.class);
        binder.bind(SlotChooser.class).to(BankDisplay.class);
        binder.bind(SetupTypeArtwork.class).to(RuneLiteSetupTypeArtwork.class);
        binder.bind(PlayerItems.class).to(RuneLitePlayerItems.class);
        binder.bind(ItemVariants.class).to(RuneLiteItemVariants.class);
        binder.bind(CurrentAccount.class).to(RuneLiteAccount.class);
        binder.bind(EquipmentSlots.class).to(RuneLiteEquipmentSlots.class);
        binder.bind(CurrentSpellbook.class).to(RuneLiteSpellbook.class);
        binder.bind(CurrentQuickPrayers.class).to(RuneLiteQuickPrayers.class);
        binder.bind(ItemNotes.class).to(RuneLiteItemNotes.class);
        binder.bind(ItemCharges.class).to(RuneLiteItemCharges.class);
        binder.bind(ViewSettings.class).to(ConfigViewSettings.class);
        binder.bind(PrayerArtwork.class).to(RuneLitePrayerArtwork.class);
        binder.bind(EquipmentSlotArtwork.class).to(RuneLiteEquipmentSlotArtwork.class);
        binder.bind(AccountDirectory.class).to(ConfigAccountDirectory.class);
        binder.bind(Dispatch.class).to(SwingDispatch.class);
    }

    @Override
    protected void startUp() {
        GearSetupBook book = new GearSetupBook(new SwingDispatch());
        persistence.restore(book);
        session = getInjector().createChildInjector(new SessionModule(book, DATA_DIR)).getInstance(GearComposerSession.class);
        session.start();
        log.debug("Gear Composer started");
    }


    @Override
    protected void shutDown() {
        if (session != null) {
            session.stop();
            session = null;
        }
        log.debug("Gear Composer stopped");
    }
}
