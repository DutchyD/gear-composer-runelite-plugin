package dev.dutchy.runelite.gear.transfer;

import java.nio.file.Path;
import java.util.Optional;

/** Asks the player for a file; empty when they cancel. */
public interface FileDialogs {

    Optional<Path> chooseFileToOpen();

    Optional<Path> chooseFileToSave(String suggestedName);
}
