package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.history.SetupHistory;
import dev.dutchy.runelite.gear.history.SetupRevision;
import dev.dutchy.runelite.gear.player.PlayerItems;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The commands the contents page sends to the setup it has open: variants, cells, slots and syncing
 * from the game. Asks before anything is lost and refuses a name that is already taken, so the page
 * itself only has to pass the click on.
 */
final class ContentHost {

    static final String MAX_VARIANTS = "A setup holds at most " + GearSetup.MAX_VARIANTS + " variants";
    static final String NEW_VARIANT_NAME = "Name for the new variant";
    static final String VARIANT_NAME = "Name for this variant";
    static final String NO_GAME_FOR_VARIANT = "Log in to start a variant from the game.";
    static final String NO_GAME_FOR_CELL = "Log in to fill a cell from the game.";
    static final String NO_GAME_FOR_SYNC = "Log in to sync from the game.";
    static final String NOTHING_COPIED = "Nothing copied yet";

    /** What the page does when a command needs another page. */
    interface Pages {

        /** Back to the contents page, for a command that has finished with the one on show. */
        void showContents();
    }

    private final OpenSetupEditor editor;
    private final SetupHistory history;
    private final PlayerItems playerItems;
    private final BooleanSupplier confirmBeforeSync;

    private Prompts prompts = SilentPrompts.INSTANCE;
    private Pages pages = () -> {
    };

    public ContentHost(OpenSetupEditor editor, SetupHistory history, PlayerItems playerItems, BooleanSupplier confirmBeforeSync) {
        this.editor = Objects.requireNonNull(editor, "editor");
        this.history = Objects.requireNonNull(history, "history");
        this.playerItems = Objects.requireNonNull(playerItems, "playerItems");
        this.confirmBeforeSync = Objects.requireNonNull(confirmBeforeSync, "confirmBeforeSync");
    }

    void shownBy(Prompts newPrompts, Pages newPages) {
        this.prompts = Objects.requireNonNull(newPrompts, "newPrompts");
        this.pages = Objects.requireNonNull(newPages, "newPages");
    }

    // --- variants ---

    /** Asks for a name, suggesting one nothing else is using. */
    public void addVariant(NewVariant start) {
        if (editor.setup().isEmpty()) {
            return;
        }
        if (!editor.canAddVariant()) {
            prompts.say(MAX_VARIANTS);
            return;
        }
        String suggestion = editor.freeVariantName(editor.editingVariant().map(SetupVariant::name)
                .orElse(SetupVariant.DEFAULT_NAME)).orElse("");
        prompts.askForName(NEW_VARIANT_NAME, suggestion).ifPresent(name -> addVariant(start, name));
    }

    public void addVariant(NewVariant start, String name) {
        Optional<GearSetup> setup = editor.setup();
        Optional<String> problem = editor.newVariantNameProblem(name);
        if (setup.isEmpty() || problem.isPresent()) {
            problem.ifPresent(prompts::say);
            return;
        }
        SetupContent edited = editor.content().orElse(setup.get().content());
        switch (start) {
            case COPY:
                editor.addVariant(name, edited);
                break;
            case EMPTY:
                editor.addVariant(name, NewVariant.emptyLike(edited));
                break;
            default:
                whileOpen(loadout -> editor.addVariant(name, new GearContent(loadout.equipment(), loadout.inventory())),
                        NO_GAME_FOR_VARIANT);
                break;
        }
    }

    public void renameVariant(int index) {
        editor.setup().flatMap(setup -> setup.variantAt(index))
                .flatMap(variant -> prompts.askForName(VARIANT_NAME, variant.name()))
                .ifPresent(name -> renameVariant(index, name));
    }

    public void renameVariant(int index, String name) {
        editor.renamedVariantNameProblem(index, name)
                .ifPresentOrElse(prompts::say, () -> editor.renameVariant(index, name));
    }

    public void duplicateVariant(int index) {
        if (editor.canAddVariant()) {
            editor.duplicateVariant(index);
        } else {
            prompts.say(MAX_VARIANTS);
        }
    }

    /** The last variant cannot go, because then the setup would hold nothing. */
    public void deleteVariant(int index) {
        editor.setup().filter(GearSetup::hasVariants).flatMap(setup -> setup.variantAt(index)).ifPresent(variant -> {
            if (prompts.confirm("Delete the variant \"" + variant.name() + "\" and its items?", "Delete variant")) {
                editor.removeVariant(index);
            }
        });
    }

    // --- cells ---

    public void renameCell(CellRef ref) {
        Optional<LayoutCell> cell = editor.cellAt(ref);
        if (cell.isEmpty()) {
            return;
        }
        prompts.askForName("Name for " + ref.describe(), cell.get().label()).ifPresent(name -> renameCell(ref, name));
    }

    public void renameCell(CellRef ref, String name) {
        if (!LayoutCell.isValidName(name)) {
            prompts.say("A cell name is 1 to " + LayoutCell.MAX_NAME_LENGTH + " characters");
            return;
        }
        editor.renameCell(ref, name);
    }

    /** Turning a cell back into an empty one drops its items, so it is asked about first. */
    public void emptyCell(CellRef ref) {
        Optional<LayoutCell> cell = editor.cellAt(ref);
        if (cell.isEmpty()) {
            return;
        }
        if (!cell.get().isEmpty() && !prompts.confirm(
                "Empty " + ref.describe() + " and drop its " + cell.get().items().size() + " item(s)?", "Empty cell")) {
            return;
        }
        editor.emptyCell(ref);
    }

    public void fillCellFromGame(CellRef ref) {
        whileOpen(loadout -> editor.fillCellFromGame(ref, loadout), NO_GAME_FOR_CELL);
    }

    // --- slots ---

    public void copySlots(List<SlotRef> refs) {
        prompts.say("Copied " + editor.copy(refs) + " item(s)");
        pages.showContents();
    }

    public void pasteAt(SlotRef anchor) {
        if (!editor.canPaste()) {
            prompts.say(NOTHING_COPIED);
            return;
        }
        editor.paste(anchor);
    }

    // --- history ---

    /** Every change to the setup on the page, newest first, whichever variant it came from. */
    public List<SetupRevision> revisions(SetupId setup) {
        return history.revisions(setup);
    }

    /** Puts an old revision back and opens the variant it came from. */
    public void restore(SetupRevision revision) {
        editor.restore(revision);
    }

    // --- syncing from the game ---

    /** Overwrites every slot in scope, so it is asked about unless that was turned off. */
    public void requestSync(SyncScope scope) {
        Optional<GearSetup> setup = editor.setup();
        if (setup.isEmpty()) {
            return;
        }
        if (!confirmBeforeSync.getAsBoolean() || prompts.confirm(warningFor(scope, setup.get()), "Sync from game")) {
            applySync(scope);
        }
    }

    public void applySync(SyncScope scope) {
        whileOpen(loadout -> editor.setup().ifPresent(setup -> editor.sync(scope, loadout, doneFor(scope, setup))),
                NO_GAME_FOR_SYNC);
    }

    static String warningFor(SyncScope scope, GearSetup setup) {
        switch (scope) {
            case GEAR:
                return "Replace everything in \"" + setup.name() + "\" with what you are wearing and carrying right now?\n"
                        + "Every equipment and inventory slot will be overwritten, including the ones you leave empty.";
            case LEFT_SIDE:
                return "Replace the left side of \"" + setup.name() + "\" with what you are carrying right now?\n"
                        + "Every slot on that side will be overwritten; the right side is left alone.";
            default:
                return "Replace the right side of \"" + setup.name() + "\" with what you are carrying right now?\n"
                        + "Every slot on that side will be overwritten; the left side is left alone.";
        }
    }

    static String doneFor(SyncScope scope, GearSetup setup) {
        switch (scope) {
            case GEAR:
                return setup.name() + " synced from the game";
            case LEFT_SIDE:
                return "Left side of " + setup.name() + " synced";
            default:
                return "Right side of " + setup.name() + " synced";
        }
    }

    /** Captures the player's items for the setup open now, dropping the capture if another was opened meanwhile. */
    private void whileOpen(Consumer<Loadout> onCaptured, String whenLoggedOut) {
        Optional<SetupId> target = editor.id();
        if (target.isEmpty()) {
            return;
        }
        playerItems.capture(loadout -> {
            if (editor.isOpen(target.get())) {
                onCaptured.accept(loadout);
            }
        }, () -> prompts.say(whenLoggedOut));
    }
}
