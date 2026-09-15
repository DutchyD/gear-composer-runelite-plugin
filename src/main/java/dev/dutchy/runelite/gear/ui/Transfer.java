package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.*;
import dev.dutchy.runelite.gear.persistence.Backup;
import dev.dutchy.runelite.gear.persistence.BookFormatException;
import dev.dutchy.runelite.gear.persistence.BookMerger;
import dev.dutchy.runelite.gear.transfer.BookTransfer;
import dev.dutchy.runelite.gear.transfer.FileDialogs;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

/**
 * Setups coming in and going out: files and backups. Every failure is reported rather
 * than thrown, because there is nowhere above here for it to go.
 */
public final class Transfer {

    static final String EXPORT_FILE_NAME = "gear-composer-setups.json";

    /** The pages this module needs put on screen. */
    interface Pages {

        /** What was read, to be added or to replace everything. */
        void showImport(String source, List<GearSection> imported);

        void showBackups(List<Backup> backups);

        void backToList();
    }

    private final GearSetupBook book;
    private final BookTransfer books;
    private final FileDialogs dialogs;

    private Prompts prompts = SilentPrompts.INSTANCE;
    private Pages pages = new Pages() {
        @Override
        public void showImport(String source, List<GearSection> imported) {
        }

        @Override
        public void showBackups(List<Backup> backups) {
        }

        @Override
        public void backToList() {
        }
    };

    @Inject
    public Transfer(GearSetupBook book, BookTransfer books, FileDialogs dialogs) {
        this.book = Objects.requireNonNull(book, "book");
        this.books = Objects.requireNonNull(books, "books");
        this.dialogs = Objects.requireNonNull(dialogs, "dialogs");
    }

    /** Wired by the sidebar, which owns the dialogs, the pages and whose list is on show. */
    void shownBy(Prompts newPrompts, Pages newPages) {
        this.prompts = Objects.requireNonNull(newPrompts, "newPrompts");
        this.pages = Objects.requireNonNull(newPages, "newPages");
    }

    public void importFile() {
        dialogs.chooseFileToOpen().ifPresent(file -> {
            try {
                offerImport(file.getFileName().toString(), books.importFrom(file));
            } catch (IOException | BookFormatException e) {
                prompts.say("Could not read " + file.getFileName() + ": " + e.getMessage());
            }
        });
    }

    public void exportAll() {
        exportSections(EXPORT_FILE_NAME, book.sections());
    }

    /** A section and everything nested under it, named after the section. */
    public void exportSection(SectionId sectionId) {
        book.section(sectionId).ifPresent(section ->
                exportSections(section.name().replaceAll("[^A-Za-z0-9-]+", "-").toLowerCase() + ".json",
                        SectionTree.familyOf(book.sections(), sectionId)));
    }

    private void exportSections(String suggestedName, List<GearSection> sections) {
        dialogs.chooseFileToSave(suggestedName).ifPresent(file -> {
            try {
                books.exportTo(file, sections);
                prompts.say("Exported to " + file.getFileName());
            } catch (IOException e) {
                prompts.say("Could not write " + file.getFileName() + ": " + e.getMessage());
            }
        });
    }

    /** Shows what was read, so nothing is merged before the player has seen it. */
    public void offerImport(String source, List<GearSection> imported) {
        pages.showImport(source, List.copyOf(imported));
    }

    /** Keeps what is there and folds the import into it. */
    public void addImported(List<GearSection> imported) {
        book.replaceSections(BookMerger.merge(book.sections(), imported));
        pages.backToList();
        prompts.announce("Added " + imported.stream().mapToInt(GearSection::size).sum() + " setup(s)");
    }

    public void replaceWithImported(String source, List<GearSection> imported) {
        book.replaceSections(imported);
        pages.backToList();
        prompts.announce("Replaced everything from " + source);
    }

    public void showBackups() {
        pages.showBackups(books.backups());
    }

    public void restoreBackup(Backup backup) {
        try {
            book.replaceSections(books.readBackup(backup));
            pages.backToList();
            prompts.announce("Restored the backup");
        } catch (BookFormatException | UncheckedIOException e) {
            prompts.say("That backup could not be read: " + e.getMessage());
        }
    }
}
