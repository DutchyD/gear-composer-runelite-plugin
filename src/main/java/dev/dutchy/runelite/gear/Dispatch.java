package dev.dutchy.runelite.gear;

/**
 * The thread the book is written on. Production writes on Swing's event dispatch thread; tests run
 * on whichever thread they are already on.
 */
@FunctionalInterface
public interface Dispatch {

    /** Whether the calling thread is the write thread. */
    boolean isCurrent();

    /** Treats every thread as the write thread, for tests. */
    static Dispatch inline() {
        return () -> true;
    }
}
