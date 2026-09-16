package dev.dutchy.runelite.gear.transfer;

import dev.dutchy.runelite.gear.persistence.BookFiles;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class SwingFileDialogs implements FileDialogs {

    private final Component parent;
    private final File startIn;

    public SwingFileDialogs(Component parent, File startIn) {
        this.parent = parent;
        this.startIn = Objects.requireNonNull(startIn, "startIn");
    }

    @Override
    public Optional<Path> chooseFileToOpen() {
        JFileChooser chooser = chooser("Import setups");
        return chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION
                ? Optional.of(chooser.getSelectedFile().toPath())
                : Optional.empty();
    }

    @Override
    public Optional<Path> chooseFileToSave(String suggestedName) {
        JFileChooser chooser = chooser("Export setups");
        chooser.setSelectedFile(new File(startIn, suggestedName));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        File chosen = chooser.getSelectedFile();
        if (!chosen.getName().toLowerCase().endsWith("." + BookFiles.EXTENSION)) {
            chosen = new File(chosen.getParentFile(), chosen.getName() + "." + BookFiles.EXTENSION);
        }
        return Optional.of(chosen.toPath());
    }

    private JFileChooser chooser(String title) {
        JFileChooser chooser = new JFileChooser(startIn);
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileNameExtensionFilter("Gear Composer setups (*.json)", BookFiles.EXTENSION));
        return chooser;
    }
}
