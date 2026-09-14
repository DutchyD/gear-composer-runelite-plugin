package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.*;
import dev.dutchy.runelite.gear.activation.SetupActivator;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.history.SetupHistory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/** What can be done to a whole setup: shown in the bank, pinned, coloured, bound to a key, copied, deleted. */
public final class SetupCommands {

    /** How many setups can sit in the pinned row at once. */
    public static final int MAX_PINNED = 8;

    private final GearSetupBook book;
    private final SetupHistory history;
    private final SetupActivator activator;

    private Prompts prompts = SilentPrompts.INSTANCE;
    private Runnable shownChanged = () -> {
    };
    private Supplier<Owner> owner = Owner::shared;

    @Inject
    public SetupCommands(GearSetupBook book, SetupHistory history, SetupActivator activator) {
        this.book = Objects.requireNonNull(book, "book");
        this.history = Objects.requireNonNull(history, "history");
        this.activator = Objects.requireNonNull(activator, "activator");
    }

    /** Wired by the list, which marks which setup the bank is showing and owns the dialogs. */
    void shownBy(Prompts newPrompts, Runnable onShownChanged, Supplier<Owner> ownerForNewSetups) {
        this.prompts = Objects.requireNonNull(newPrompts, "newPrompts");
        this.shownChanged = Objects.requireNonNull(onShownChanged, "onShownChanged");
        this.owner = Objects.requireNonNull(ownerForNewSetups, "ownerForNewSetups");
    }

    /** Shows the setup in the bank, or stops showing it when it is the one already up. */
    public void activate(GearSetup setup) {
        boolean nowShown = activator.toggle(setup.id()).filter(current -> current.id().equals(setup.id())).isPresent();
        prompts.say(nowShown ? "Showing " + setup.name() + " in the bank" : "Stopped showing " + setup.name());
        shownChanged.run();
    }

    public void delete(GearSetup setup) {
        if (!prompts.confirm("Delete \"" + setup.name() + "\"?", "Delete setup")) {
            return;
        }
        book.removeSetup(setup.id());
        history.forget(setup.id());
        prompts.announce("Deleted " + setup.name());
    }

    public void duplicate(GearSetup setup) {
        prompts.announce("Duplicated as " + book.duplicateSetup(setup.id()).name());
    }

    /** The pinned row holds only so many, so a further pin is refused rather than dropping one. */
    public void togglePin(GearSetup setup) {
        if (!setup.isPinned() && book.pinnedSetups().size() >= MAX_PINNED) {
            prompts.say("Up to " + MAX_PINNED + " setups can be pinned");
            return;
        }
        book.changeMeta(setup.id(), meta -> meta.withPinned(!setup.isPinned()));
        prompts.announce(setup.isPinned() ? "Unpinned " + setup.name() : "Pinned " + setup.name());
    }

    public void setLabel(GearSetup setup, ColourLabel label) {
        book.changeMeta(setup.id(), meta -> meta.withLabel(label));
        prompts.announce(label.isNone() ? "Removed the colour from " + setup.name() : "Coloured " + setup.name());
    }

    /** Binds the key to this setup alone; {@link Hotkey#NONE} clears it. */
    public void setHotkey(GearSetup setup, Hotkey hotkey) {
        if (hotkey.isSet()) {
            book.sections().stream().flatMap(section -> section.setups().stream())
                    .filter(other -> !other.id().equals(setup.id()) && other.meta().hotkey().equals(hotkey))
                    .forEach(other -> book.changeMeta(other.id(), meta -> meta.withHotkey(Hotkey.NONE)));
        }
        book.changeMeta(setup.id(), meta -> meta.withHotkey(hotkey));
        prompts.announce(hotkey.isSet() ? setup.name() + " is now on " + hotkey.describe() : "Removed the hotkey from " + setup.name());
    }

    /** Switches which variant the bank shows, from a menu rather than from the editor. */
    public void showVariant(GearSetup setup, int index) {
        setup.variantAt(index).ifPresent(variant -> {
            book.change(setup.id(), current -> current.withSelectedVariant(index));
            prompts.announce(setup.name() + " now shows " + variant.name());
        });
    }

    public GearSetup add(SectionId sectionId, GearSetup draft) {
        GearSetup added = book.addSetup(sectionId, draft.withOwner(owner.get()));
        prompts.announce("Created " + draft.name());
        return added;
    }

    /** A setup made from what the player is wearing right now. */
    public void addFromGame(SectionId sectionId, GearSetup draft) {
        book.addSetup(sectionId, draft.withOwner(owner.get()));
        prompts.announce("Created " + draft.name() + " from the game");
    }

    /**
     * Name, icon and meta always follow the draft; a custom layout's row count does too, once any
     * loss of items is confirmed.
     */
    public void applyEdit(SetupId id, GearSetup draft) {
        book.change(id, current -> current.withName(draft.name()).withIconOf(draft).withMeta(draft.meta()));
        prompts.announce("Saved " + draft.name());
        Optional<CustomContent> custom = book.setup(id).map(GearSetup::content)
                .filter(content -> content instanceof CustomContent).map(content -> (CustomContent) content);
        if (custom.isEmpty() || !(draft.content() instanceof CustomContent)) {
            return;
        }
        int rows = ((CustomContent) draft.content()).rows();
        if (rows == custom.get().rows()) {
            return;
        }
        if (custom.get().hasItemsBelow(rows) && !prompts.confirm("Drop the rows below row " + rows + " and everything in them?", "Fewer rows")) {
            return;
        }
        resizeRows(id, rows);
    }

    public void resizeRows(SetupId id, int rows) {
        book.change(id, current -> current.content() instanceof CustomContent
                ? current.withContent(((CustomContent) current.content()).withRows(rows)) : current);
    }

    public void moveToSection(SetupId setupId, SectionId section, int index) {
        book.moveSetup(setupId, section, index);
    }
}
