package dev.dutchy.runelite.gear.persistence;

import java.util.List;

/** Timestamped copies of the encoded book, newest first. */
public interface BackupStore {

    void save(String encoded);

    List<Backup> list();

    String read(Backup backup);
}
