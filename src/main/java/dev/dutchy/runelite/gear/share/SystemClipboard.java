package dev.dutchy.runelite.gear.share;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class SystemClipboard implements Clipboard {

    private static final Logger log = LoggerFactory.getLogger(SystemClipboard.class);

    @Override
    public void copy(String text) {
        Objects.requireNonNull(text, "text");
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (IllegalStateException e) {
            log.warn("The clipboard could not be written", e);
        }
    }

    @Override
    public Optional<String> paste() {
        try {
            Object data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            return data instanceof String ? Optional.of((String) data) : Optional.empty();
        } catch (UnsupportedFlavorException | IOException | IllegalStateException e) {
            return Optional.empty();
        }
    }
}
