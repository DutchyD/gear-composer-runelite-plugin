package dev.dutchy.runelite.gear.share;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.ledger.ItemFactsSource;
import dev.dutchy.runelite.gear.ledger.Ledger;
import dev.dutchy.runelite.libs.ui.button.ItemLoader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Turns a setup into a picture or a text listing. Every future completes on the EDT with a message for the status line. */
public final class ShareService {

    private final ItemLoader loader;
    private final ItemFactsSource facts;
    private final ImageSink images;
    private final Clipboard clipboard;

    public ShareService(ItemLoader loader, ItemFactsSource facts, ImageSink images, Clipboard clipboard) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.facts = Objects.requireNonNull(facts, "facts");
        this.images = Objects.requireNonNull(images, "images");
        this.clipboard = Objects.requireNonNull(clipboard, "clipboard");
    }

    public CompletableFuture<BufferedImage> render(GearSetup setup, boolean withLedger) {
        return LoadedSetup.load(setup, loader).thenApply(loaded -> SetupImageRenderer.render(setup, loaded, ledger(setup, withLedger)));
    }

    public CompletableFuture<String> saveImage(GearSetup setup, boolean withLedger) {
        return render(setup, withLedger).thenApply(image -> {
            try {
                return "Saved " + setup.name() + " to " + images.save(image, fileName(setup));
            } catch (IOException e) {
                return "Could not save the image: " + e.getMessage();
            }
        });
    }

    public CompletableFuture<String> copyImage(GearSetup setup, boolean withLedger) {
        return render(setup, withLedger).thenApply(image -> {
            images.copy(image);
            return "Copied " + setup.name() + " as an image";
        });
    }

    public CompletableFuture<String> copyText(GearSetup setup, boolean withLedger) {
        return LoadedSetup.load(setup, loader).thenApply(loaded -> {
            clipboard.copy(SetupText.markdown(setup, loaded::name, ledger(setup, withLedger)));
            return "Copied " + setup.name() + " as text";
        });
    }

    /** The totals to print, null when they were not asked for. */
    private Ledger ledger(GearSetup setup, boolean withLedger) {
        return withLedger ? Ledger.of(setup.content(), facts) : null;
    }

    private static String fileName(GearSetup setup) {
        return "gear-composer-" + setup.name().replaceAll("[^A-Za-z0-9-]+", "-").toLowerCase();
    }
}
