package dev.dutchy.runelite.gear.history;

import com.google.gson.Gson;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.content.BankContent;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.VariantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileHistoryStoreTest {

    @TempDir
    Path directory;

    @Test
    void revisionsSurviveARestart() {
        SetupId setup = SetupId.random();
        FileHistoryStore store = new FileHistoryStore(directory.resolve("history"), new Gson());
        SetupRevision older = new SetupRevision(Instant.parse("2026-09-07T10:00:00Z"), "Sync from game", GearContent.empty());
        SetupRevision newer = new SetupRevision(Instant.parse("2026-09-08T10:00:00Z"), "Edited",
                BankContent.empty().withLeft(ItemGrid.EMPTY.withSlot(1, SetupItem.of(385, 3))));
        store.record(setup, older);
        store.record(setup, newer);

        FileHistoryStore reopened = new FileHistoryStore(directory.resolve("history"), new Gson());
        assertEquals(List.of(newer, older), reopened.revisions(setup));

        reopened.forget(setup);
        assertTrue(reopened.revisions(setup).isEmpty());
    }

    @Test
    void anUnreadableFileCountsAsNoHistory() throws Exception {
        SetupId setup = SetupId.random();
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(setup.value() + ".json"), "not json");
        assertTrue(new FileHistoryStore(directory, new Gson()).revisions(setup).isEmpty());
    }
@Test
    void aRevisionStillKnowsItsVariantAfterARestart() {
        SetupId setup = SetupId.random();
        VariantId variant = VariantId.random();
        FileHistoryStore store = new FileHistoryStore(directory.resolve("history"), new Gson());
        store.record(setup, new SetupRevision(Instant.parse("2026-09-08T10:00:00Z"), "Edited Mage", variant, GearContent.empty()));

        FileHistoryStore reopened = new FileHistoryStore(directory.resolve("history"), new Gson());

        assertEquals(Optional.of(variant), reopened.revisions(setup).get(0).variant());
    }
}
