package dev.dutchy.runelite.gear.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** The sidebar's pages, one on show at a time, each replaced whole and found again by type. */
final class PageStack {

    private final CardLayout cards = new CardLayout();
    private final CardHost host = new CardHost(cards);
    private final Consumer<String> onShown;
    private String current;
    private String beneath;

    PageStack(String home, JPanel homeCard, Consumer<String> onShown) {
        this.onShown = Objects.requireNonNull(onShown, "onShown");
        this.current = Objects.requireNonNull(home, "home");
        this.beneath = home;
        replace(home, Objects.requireNonNull(homeCard, "homeCard"));
    }

    JComponent component() {
        return host;
    }

    boolean isShowing(String name) {
        return current.equals(name);
    }

    /** Puts the page in place of any earlier page of that name without showing it. */
    void replace(String name, JPanel page) {
        for (Component existing : host.getComponents()) {
            if (name.equals(existing.getName())) {
                host.remove(existing);
            }
        }
        page.setName(name);
        host.add(page, name);
        host.revalidate();
        host.repaint();
    }

    void show(String name) {
        current = name;
        cards.show(host, name);
        onShown.accept(name);
    }

    /** Shows the page over the current one, remembering what it covers so it can close back to it. */
    void showOver(String name) {
        if (!current.equals(name)) {
            beneath = current;
        }
        show(name);
    }

    /** The page a page shown with {@link #showOver} was opened from. */
    String beneath() {
        return beneath;
    }

    /** The page of that type currently on show. */
    <T> Optional<T> visible(Class<T> type) {
        for (Component component : host.getComponents()) {
            if (type.isInstance(component) && component.isVisible()) {
                return Optional.of(type.cast(component));
            }
        }
        return Optional.empty();
    }

    /** The page of that type whether or not another page covers it. */
    <T> Optional<T> any(Class<T> type) {
        for (Component component : host.getComponents()) {
            if (type.isInstance(component)) {
                return Optional.of(type.cast(component));
            }
        }
        return Optional.empty();
    }
}
