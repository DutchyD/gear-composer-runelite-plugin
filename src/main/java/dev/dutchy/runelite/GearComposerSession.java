package dev.dutchy.runelite;

import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.account.RuneLiteAccount;
import dev.dutchy.runelite.gear.activation.ActivationToast;
import dev.dutchy.runelite.gear.activation.SetupActivator;
import dev.dutchy.runelite.gear.activation.SetupHotkeys;
import dev.dutchy.runelite.gear.bank.*;
import dev.dutchy.runelite.gear.config.GearComposerConfig;
import dev.dutchy.runelite.gear.persistence.AutoSave;
import dev.dutchy.runelite.gear.persistence.BookPersistence;
import dev.dutchy.runelite.gear.requirements.RuneLiteQuickPrayers;
import dev.dutchy.runelite.gear.requirements.RuneLiteSpellbook;
import dev.dutchy.runelite.gear.ui.GearSetupPanel;
import dev.dutchy.runelite.libs.ui.search.ItemIndexWarmUp;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/** One run of the plugin: hooks everything built on the book into the client, and unhooks it again. */
final class GearComposerSession {

    private static final String ICON_RESOURCE = "gear_composer_icon.png";

    private final EventBus eventBus;
    private final OverlayManager overlayManager;
    private final KeyManager keyManager;
    private final ClientThread clientThread;
    private final ClientToolbar clientToolbar;
    private final ItemIndexWarmUp itemIndexWarmUp;
    private final BookPersistence persistence;
    private final GearSetupBook book;
    private final GearComposerConfig config;
    private final BankDisplay display;
    private final RuneLiteBankScreen screen;
    private final RuneLiteAccount account;
    private final RuneLiteSpellbook spellbook;
    private final RuneLiteQuickPrayers quickPrayers;
    private final ActiveSetup activeSetup;
    private final SetupHotkeys hotkeys;
    private final ActivationToast toast;
    private final BankTabWatch tabWatch;
    private final BankTabSizes tabSizes;
    private final GearSetupPanel panel;
    private final List<Overlay> overlays;
    private final List<Object> subscribers;
    private final Runnable toastLink = this::announceActivation;
    private final SetupActivator activator;
    private AutoSave autoSave;
    private NavigationButton navigationButton;

    @Inject
    GearComposerSession(EventBus eventBus, OverlayManager overlayManager, KeyManager keyManager, ClientThread clientThread,
                        ClientToolbar clientToolbar, ItemIndexWarmUp itemIndexWarmUp, BookPersistence persistence, GearSetupBook book,
                        GearComposerConfig config, BankDisplay display, RuneLiteBankScreen screen, RuneLiteAccount account,
                        RuneLiteSpellbook spellbook, RuneLiteQuickPrayers quickPrayers, ActiveSetup activeSetup,
                        SetupHotkeys hotkeys, ActivationToast toast, BankTitle title, BankSlotOverlay slotOverlay,
                        NoteToggleOverlay noteOverlay, BankLabelOverlay labelOverlay, BankTabWatch tabWatch, BankTabSizes tabSizes,
                        BankSearchWatch searchWatch, WithdrawMenuSwapper withdrawSwapper, VariantSwitchMenu variantMenu,
                        SetupActivator activator, GearSetupPanel panel) {
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus");
        this.overlayManager = Objects.requireNonNull(overlayManager, "overlayManager");
        this.keyManager = Objects.requireNonNull(keyManager, "keyManager");
        this.clientThread = Objects.requireNonNull(clientThread, "clientThread");
        this.clientToolbar = Objects.requireNonNull(clientToolbar, "clientToolbar");
        this.itemIndexWarmUp = Objects.requireNonNull(itemIndexWarmUp, "itemIndexWarmUp");
        this.persistence = Objects.requireNonNull(persistence, "persistence");
        this.book = Objects.requireNonNull(book, "book");
        this.config = Objects.requireNonNull(config, "config");
        this.display = Objects.requireNonNull(display, "display");
        this.screen = Objects.requireNonNull(screen, "screen");
        this.account = Objects.requireNonNull(account, "account");
        this.spellbook = Objects.requireNonNull(spellbook, "spellbook");
        this.quickPrayers = Objects.requireNonNull(quickPrayers, "quickPrayers");
        this.activeSetup = Objects.requireNonNull(activeSetup, "activeSetup");
        this.hotkeys = Objects.requireNonNull(hotkeys, "hotkeys");
        this.toast = Objects.requireNonNull(toast, "toast");
        this.tabWatch = Objects.requireNonNull(tabWatch, "tabWatch");
        this.tabSizes = Objects.requireNonNull(tabSizes, "tabSizes");
        this.panel = Objects.requireNonNull(panel, "panel");
        this.overlays = List.of(toast, slotOverlay, noteOverlay, labelOverlay);
        this.activator = Objects.requireNonNull(activator, "activator");
        this.subscribers = List.of(screen, account, spellbook, quickPrayers, title, tabWatch, tabSizes, searchWatch, withdrawSwapper, variantMenu, this);
    }

    void start() {
        itemIndexWarmUp.start();
        autoSave = persistence.autoSave(book);
        subscribers.forEach(eventBus::register);
        overlays.forEach(overlayManager::add);
        keyManager.registerKeyListener(hotkeys);
        activator.addListener(toastLink);
        display.drawOn(screen);
        display.onVariantChosen(index -> EdtDispatch.onEdt(() -> activator.selectVariant(index)));
        clientThread.invoke(account::refresh);
        clientThread.invoke(quickPrayers::refresh);
        clientThread.invoke(spellbook::refresh);
        clientThread.invoke(tabWatch::refresh);
        clientThread.invoke(tabSizes::refresh);
        navigationButton = NavigationButton.builder()
                .tooltip("Gear Composer")
                .icon(ImageUtil.loadImageResource(GearComposerPlugin.class, ICON_RESOURCE))
                .priority(5)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navigationButton);
    }

    void stop() {
        panel.endGuide();
        itemIndexWarmUp.stop();
        if (autoSave != null) {
            autoSave.stop();
            autoSave = null;
        }
        keyManager.unregisterKeyListener(hotkeys);
        activator.removeListener(toastLink);
        display.onVariantChosen(index -> {
        });
        overlays.forEach(overlayManager::remove);
        activeSetup.clear();
        display.clear();
        subscribers.forEach(eventBus::unregister);
        display.drawOn(BankScreen.none());
        if (navigationButton != null) {
            clientToolbar.removeNavigation(navigationButton);
            navigationButton = null;
        }
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event) {
        if (event.getGroupId() == InterfaceID.BANKMAIN && event.isUnload() && config.clearLayoutOnBankClose()
                && activeSetup.current().isPresent()) {
            activeSetup.clear();
            display.discard();
        }
    }

    /** Pops the in-game notice whenever the active setup changes, if the player wants it. */
    private void announceActivation() {
        if (config.activationToast()) {
            toast.announce();
        }
    }
}
