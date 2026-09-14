package dev.dutchy.runelite.gear.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Prompts that never open a dialog: they answer what the test told them to and keep what was said. */
final class RecordingPrompts implements Prompts {

    private final List<String> messages = new ArrayList<>();
    private final List<String> questions = new ArrayList<>();
    private boolean answer = true;
    private String typed;
    private boolean backOut;
    private Consumer<String> said = message -> {
    };
    private Consumer<String> announced = message -> {
    };

    /** Passes what is said on as well, so a test can watch the real status line. */
    RecordingPrompts reportingTo(Consumer<String> toSay, Consumer<String> toAnnounce) {
        said = toSay;
        announced = toAnnounce;
        return this;
    }

    /** What a confirm answers from now on. */
    void answering(boolean yes) {
        answer = yes;
    }

    /** What a name prompt returns from now on; {@code null} backs out. */
    void typing(String name) {
        typed = name;
        backOut = name == null;
    }

    List<String> messages() {
        return List.copyOf(messages);
    }

    List<String> questions() {
        return List.copyOf(questions);
    }

    String lastMessage() {
        return messages.isEmpty() ? "" : messages.get(messages.size() - 1);
    }

    @Override
    public boolean confirm(String question, String title) {
        questions.add(question);
        return answer;
    }

    @Override
    public Optional<String> askForName(String question, String initial) {
        questions.add(question);
        return backOut ? Optional.empty() : Optional.of(typed == null ? initial : typed);
    }

    @Override
    public void say(String message) {
        messages.add(message);
        said.accept(message);
    }

    @Override
    public void announce(String message) {
        messages.add(message);
        announced.accept(message);
    }
}
