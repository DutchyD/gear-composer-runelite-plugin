package dev.dutchy.runelite.gear.ui;

import com.google.gson.Gson;
import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.persistence.Backup;
import dev.dutchy.runelite.gear.persistence.BackupStore;
import dev.dutchy.runelite.gear.persistence.BookCodec;
import dev.dutchy.runelite.gear.persistence.BookFiles;
import dev.dutchy.runelite.gear.share.InMemoryClipboard;
import dev.dutchy.runelite.gear.share.ShareCodec;
import dev.dutchy.runelite.gear.transfer.BookTransfer;
import dev.dutchy.runelite.gear.transfer.FileDialogs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferTest {

    @TempDir
    Path directory;

    private final GearSetupBook book = new GearSetupBook();
    private final BookCodec codec = new BookCodec(new Gson());
    private final List<String> stored = new ArrayList<>();
    private final RecordingPrompts prompts = new RecordingPrompts();
    private final InMemoryClipboard clipboard = new InMemoryClipboard();

    private Path toOpen;
    private Path toSave;

    private final List<String> shown = new ArrayList<>();
    private GearSetup offered;
    private List<GearSection> imported = List.of();

    private final BackupStore backups = new BackupStore() {
        @Override
        public void save(String encoded) {
            stored.add(0, encoded);
        }

        @Override
        public List<Backup> list() {
            List<Backup> list = new ArrayList<>();
            for (int i = 0; i < stored.size(); i++) {
                list.add(new Backup(Path.of("backup-" + i + ".json"), Instant.parse("2026-09-08T10:00:00Z")));
            }
            return list;
        }

        @Override
        public String read(Backup backup) {
            return stored.get(Integer.parseInt(backup.file().toString().replaceAll("\\D", "")));
        }
    };

    private final FileDialogs dialogs = new FileDialogs() {
        @Override
        public Optional<Path> chooseFileToOpen() {
            return Optional.ofNullable(toOpen);
        }

        @Override
        public Optional<Path> chooseFileToSave(String suggestedName) {
            shown.add("save:" + suggestedName);
            return Optional.ofNullable(toSave);
        }
    };

    private final Transfer transfer = new Transfer(book, new BookTransfer(new BookFiles(codec), backups, codec),
            dialogs, new ShareCodec(new Gson()), clipboard);

    TransferTest() {
        transfer.shownBy(prompts, new Transfer.Pages() {
            @Override
            public void showImport(String source, List<GearSection> sections) {
                shown.add("import:" + source);
                imported = sections;
            }

            @Override
            public void showBackups(List<Backup> list) {
                shown.add("backups:" + list.size());
            }

            @Override
            public void showShared(GearSetup shared) {
                shown.add("shared:" + shared.name());
                offered = shared;
            }

            @Override
            public void backToList() {
                shown.add("list");
            }
        }, () -> Owner.account("rsprofile.main"));
    }

    private List<String> sectionNames() {
        return book.sections().stream().map(GearSection::name).collect(Collectors.toList());
    }

    @Test
    void anExportIsNamedForWhatItHoldsAndAnImportComesBackAsAnOffer() {
        book.addSetup(book.sections().get(0).id(), "Vorkath");
        Path file = directory.resolve("out.json");
        toSave = file;

        transfer.exportAll();

        assertTrue(Files.exists(file));
        assertEquals("Exported to out.json", prompts.lastMessage());
        assertTrue(shown.contains("save:" + Transfer.EXPORT_FILE_NAME));

        toOpen = file;
        transfer.importFile();

        assertEquals(1, imported.size(), "what was read is offered, not merged");
        assertEquals(List.of("Vorkath"), imported.get(0).setups().stream().map(GearSetup::name).collect(Collectors.toList()));
        assertEquals(1, book.sectionCount(), "and the book is untouched until the offer is taken");
    }

    @Test
    void aSectionIsExportedUnderASafeFileName() {
        SectionId slayer = book.addSection("Slayer / Bossing!").id();
        toSave = directory.resolve("slayer.json");

        transfer.exportSection(slayer);

        assertTrue(shown.contains("save:slayer-bossing-.json"));
    }

    @Test
    void takingAnOfferEitherFoldsItInOrReplacesEverything() {
        book.addSetup(book.sections().get(0).id(), "Vorkath");
        List<GearSection> incoming = List.of(new GearSection(SectionId.random(), "Imported", List.of(GearSetup.named("Zulrah"))));

        transfer.addImported(incoming);

        assertEquals(2, book.sectionCount());
        assertEquals("Added 1 setup(s)", prompts.lastMessage());

        transfer.replaceWithImported("a file", incoming);

        assertEquals(List.of("Imported"), sectionNames());
        assertEquals("Replaced everything from a file", prompts.lastMessage());
    }

    @Test
    void aFileThatCannotBeReadIsReportedRatherThanThrown() {
        toOpen = directory.resolve("broken.json");

        transfer.importFile();

        assertTrue(prompts.lastMessage().startsWith("Could not read broken.json: "), prompts.lastMessage());
        assertTrue(imported.isEmpty());
    }

    @Test
    void backupsAreListedAndRestoringReplacesTheBook() {
        stored.add(codec.encode(List.of(new GearSection(SectionId.random(), "From backup", List.of(GearSetup.named("Old"))))));

        transfer.showBackups();
        assertTrue(shown.contains("backups:1"));

        transfer.restoreBackup(backups.list().get(0));

        assertEquals(List.of("From backup"), sectionNames());
        assertEquals("Restored the backup", prompts.lastMessage());
    }

    @Test
    void aBackupThatCannotBeReadIsReported() {
        stored.add("not json");

        transfer.restoreBackup(backups.list().get(0));

        assertTrue(prompts.lastMessage().startsWith("That backup could not be read"), prompts.lastMessage());
        assertEquals(1, book.sectionCount());
    }

    @Test
    void aShareCodeGoesToTheClipboardAndComesBackAsAnOffer() {
        GearSetup vorkath = book.addSetup(book.sections().get(0).id(), "Vorkath");

        transfer.copyShareCode(vorkath);

        assertTrue(clipboard.paste().orElseThrow().startsWith(ShareCodec.PREFIX));
        assertEquals("Copied a share code for Vorkath", prompts.lastMessage());

        transfer.pasteShareCode();

        assertEquals("Vorkath", offered.name());
    }

    @Test
    void aClipboardWithoutACodeAndOneWithABrokenCodeBothSaySo() {
        clipboard.copy("not a code");
        transfer.pasteShareCode();
        assertEquals("The clipboard holds no share code", prompts.lastMessage());

        clipboard.copy(ShareCodec.PREFIX + "nonsense");
        transfer.pasteShareCode();
        assertFalse(prompts.lastMessage().isEmpty());
        assertEquals(1, book.sectionCount(), "nothing was added either way");
    }

    @Test
    void aSharedSetupBelongsToWhoeverIsLookingAtTheList() {
        SectionId section = book.sections().get(0).id();

        transfer.addShared(GearSetup.named("Vorkath"), section);

        GearSetup added = book.sections().get(0).setups().get(0);
        assertEquals(Owner.account("rsprofile.main"), added.owner());
        assertEquals("Added Vorkath", prompts.lastMessage());
        assertTrue(shown.contains("list"));
    }
}
