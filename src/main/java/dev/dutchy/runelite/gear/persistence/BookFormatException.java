package dev.dutchy.runelite.gear.persistence;

/** The stored book could not be read: wrong version, malformed JSON, or invalid values. */
public final class BookFormatException extends RuntimeException {

    public BookFormatException(String message) {
        super(message);
    }

    public BookFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
