package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.SetupNames;
import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.history.SetupHistory;
import dev.dutchy.runelite.gear.history.SetupRevision;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Edits the items and variants of the one setup open in the sidebar, recording every item change in its history.
 * The page edits one variant while the bank may show another; only an explicit selection moves the bank.
 * A change that leaves the setup identical is dropped; a change to a setup that no longer exists reports it gone.
 */
final class OpenSetupEditor {

    /** Told after each recorded change, with how the contents page should look next, or when the open setup is gone. */
    interface Listener {
        void changed(String cause, UnaryOperator<ContentViewState> focus);

        void gone();
    }

    private final GearSetupBook book;
    private final SetupHistory history;
    private final Listener listener;

    private SetupId open;
    private VariantId editing;
    private SlotClipboard clipboard = SlotClipboard.empty();

    OpenSetupEditor(GearSetupBook book, SetupHistory history, Listener listener) {
        this.book = Objects.requireNonNull(book, "book");
        this.history = Objects.requireNonNull(history, "history");
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    /** Opens the setup on its bank-shown variant; reopening the same setup keeps the variant on the page. */
    void open(SetupId id) {
        Objects.requireNonNull(id, "id");
        if (!id.equals(open)) {
            editing = book.setup(id).map(GearSetup::selected).orElse(null);
        }
        open = id;
    }

    void close() {
        open = null;
    }

    Optional<SetupId> id() {
        return Optional.ofNullable(open);
    }

    boolean isOpen(SetupId id) {
        return id.equals(open);
    }

    Optional<GearSetup> setup() {
        return open == null ? Optional.empty() : book.setup(open);
    }

    /** The variant the page edits, falling back to the shown one once the pointer goes stale. */
    Optional<SetupVariant> editingVariant() {
        return setup().map(found -> found.variant(editing).orElseGet(found::variant));
    }

    /** Where the edited variant sits, for views that lay the variants out in order. */
    int editingIndex() {
        return setup().flatMap(found -> editingVariant().map(variant -> found.positionOf(variant.id())))
                .filter(at -> at >= 0).orElse(0);
    }

    Optional<SetupContent> content() {
        return editingVariant().map(SetupVariant::content);
    }

    Optional<LayoutCell> cellAt(CellRef ref) {
        return content().filter(content -> content instanceof CustomContent && ref.row() < ((CustomContent) content).rows())
                .map(content -> ((CustomContent) content).cell(ref));
    }

    /** The divider over the slot's column, if one spans it. */
    Optional<Divider> dividerAt(SlotRef slot) {
        return content().flatMap(content -> SetupContentEditor.dividerAt(content, slot));
    }

    List<Divider> dividersAbove(SlotRef slot) {
        return content().map(content -> SetupContentEditor.dividersAbove(content, slot)).orElse(List.of());
    }

    void setItem(SlotRef ref, SetupItem item) {
        change(content -> SetupContentEditor.withItem(content, ref, item), "Set " + ref.describe());
    }

    void clearSlot(SlotRef ref) {
        change(content -> SetupContentEditor.cleared(content, ref), "Emptied " + ref.describe());
    }

    void clearSlots(List<SlotRef> refs) {
        change(content -> {
            SetupContent result = content;
            for (SlotRef ref : refs) {
                result = SetupContentEditor.cleared(result, ref);
            }
            return result;
        }, "Emptied " + refs.size() + " slot(s)");
    }

    void moveItem(SlotRef from, SlotRef to, boolean copy) {
        change(content -> SetupContentEditor.moved(content, from, to, copy), (copy ? "Copied to " : "Moved to ") + to.describe());
    }

    void fillRemaining(SlotRef from) {
        change(content -> SetupContentEditor.filledFrom(content, from), "Filled the rest after " + from.describe());
    }

    void fillRow(SlotRef from) {
        change(content -> SetupContentEditor.filledRow(content, from), "Filled the row of " + from.describe());
    }

    /** Puts the divider in the slot's grid in place of the one over the slot's column; neighbours it overlaps are trimmed. */
    void setDivider(SlotRef slot, Divider divider) {
        change(content -> SetupContentEditor.withDivider(content, slot, divider), "Divider: " + divider.label());
    }

    void removeDivider(SlotRef slot) {
        change(content -> SetupContentEditor.withoutDividerAt(content, slot), "Removed the divider");
    }

    void changeDivider(SlotRef slot, DividerChange change) {
        change(content -> SetupContentEditor.dividerAt(content, slot)
                .map(divider -> SetupContentEditor.withDivider(content, slot, change.apply(divider))).orElse(content), change.describe());
    }

    /** Copies the slots' items for a later paste; the number of items taken. */
    int copy(List<SlotRef> refs) {
        clipboard = content().map(content -> SetupContentEditor.copied(content, refs)).orElse(SlotClipboard.empty());
        return clipboard.size();
    }

    boolean canPaste() {
        return !clipboard.isEmpty();
    }

    void paste(SlotRef anchor) {
        change(content -> SetupContentEditor.pasted(content, clipboard, anchor), "Pasted at " + anchor.describe());
    }

    /** Why a new variant cannot take the name, or empty when it can. */
    Optional<String> newVariantNameProblem(String name) {
        return variantNameProblem(name, GearSetup::variants);
    }

    /** Why the variant cannot be renamed to the name, or empty when it can; keeping its own name is fine. */
    Optional<String> renamedVariantNameProblem(int index, String name) {
        return variantNameProblem(name, found -> found.variants().stream()
                .filter(variant -> variant != found.variantAt(index).orElse(null)).collect(Collectors.toList()));
    }

    private Optional<String> variantNameProblem(String name, Function<GearSetup, List<SetupVariant>> others) {
        if (!SetupVariant.isValidName(name)) {
            return Optional.of("A variant name is 1 to " + SetupVariant.MAX_NAME_LENGTH + " characters");
        }
        return setup().flatMap(found -> others.apply(found).stream().filter(variant -> variant.isNamed(name)).findFirst())
                .map(taken -> "There is already a variant named " + taken.name());
    }

    /** A name after the given one that does not clash with the ones the setup has. */
    Optional<String> freeVariantName(String after) {
        return setup().map(found -> SetupNames.copyOf(after,
                found.variants().stream().map(SetupVariant::name).collect(Collectors.toList()), SetupVariant.MAX_NAME_LENGTH));
    }

    boolean canAddVariant() {
        return setup().map(found -> found.variants().size() < GearSetup.MAX_VARIANTS).orElse(false);
    }

    /** Makes the variant the setup's chosen one, so the bank shows it; the page stays on what it edits. */
    void selectVariant(int index) {
        changeSetup(setup -> setup.variantIdAt(index).map(setup::withSelectedVariant).orElse(setup),
                setup -> "Showing " + setup.variant().name());
    }

    /** Puts the variant on the page without changing which one the bank shows. */
    void editVariant(int index) {
        Optional<GearSetup> found = setup();
        if (found.isEmpty()) {
            listener.gone();
            return;
        }
        Optional<SetupVariant> variant = found.get().variantAt(index);
        if (variant.isEmpty() || editingVariant().map(current -> current.hasId(variant.get().id())).orElse(false)) {
            return;
        }
        editing = variant.get().id();
        listener.changed("Editing " + variant.get().name(), UnaryOperator.identity());
    }

    /** Adds a variant holding the content after the last and opens it on the page. */
    void addVariant(String name, SetupContent content) {
        SetupVariant added = new SetupVariant(name, content);
        changeSetup(setup -> setup.variants().size() < GearSetup.MAX_VARIANTS ? setup.withAddedVariant(added) : setup,
                setup -> "Added variant " + added.name(), added.id());
    }

    /** Inserts a copy right after the variant, named after it, and opens the copy on the page. */
    void duplicateVariant(int index) {
        Optional<SetupVariant> source = setup().flatMap(found -> found.variantAt(index));
        Optional<String> free = source.flatMap(variant -> freeVariantName(variant.name()));
        if (source.isEmpty() || free.isEmpty()) {
            return;
        }
        SetupVariant copy = source.get().withId(VariantId.random()).withName(free.get());
        changeSetup(setup -> setup.variants().size() < GearSetup.MAX_VARIANTS
                        ? setup.withVariantInserted(index + 1, copy) : setup,
                setup -> "Added variant " + copy.name(), copy.id());
    }

    void renameVariant(int index, String name) {
        changeSetup(setup -> setup.variantAt(index).map(variant -> setup.withVariant(index, variant.withName(name))).orElse(setup),
                setup -> "Renamed the variant to " + name.strip());
    }

    /** Drops the variant; the last variant of a setup stays. Deleting the edited one moves the page next to it. */
    void removeVariant(int index) {
        Optional<GearSetup> found = setup();
        if (found.isEmpty()) {
            listener.gone();
            return;
        }
        boolean editingIt = found.get().variantAt(index)
                .flatMap(doomed -> editingVariant().map(current -> current.hasId(doomed.id()))).orElse(false);
        VariantId next = editingIt ? nearestTo(found.get(), index) : null;
        changeSetup(setup -> setup.hasVariants() && setup.variantAt(index).isPresent() ? setup.withoutVariant(index) : setup,
                setup -> "Deleted the variant", next);
    }

    /** Where the page goes when the variant it edits is deleted: the one before it, else the one after. */
    private static VariantId nearestTo(GearSetup setup, int index) {
        return setup.variantAt(index - 1).or(() -> setup.variantAt(index + 1)).map(SetupVariant::id).orElse(null);
    }

    /** Moves the variant. The page and the bank both point at ids, so neither follows the position. */
    void moveVariant(int index, int to) {
        changeSetup(setup -> setup.variantAt(index).isPresent() && setup.variantAt(to).isPresent() ? setup.withVariantMoved(index, to) : setup,
                setup -> "Moved the variant");
    }

    void setCellKind(CellRef ref, CellKind kind) {
        changeCell(ref, cell -> cell.withKind(kind), "Made " + ref.describe() + " an " + kind.displayName().toLowerCase() + " cell");
    }

    /** Replaces the cell with another setup's part, named after that setup. */
    void fillCell(CellRef ref, LayoutCell source, String sourceName) {
        changeCell(ref, current -> source.withName(sourceName), "Filled " + ref.describe() + " from " + sourceName);
    }

    void fillCellFromGame(CellRef ref, Loadout loadout) {
        changeCell(ref, cell -> {
            switch (cell.kind()) {
                case EQUIPMENT:
                    return cell.withEquipment(loadout.equipment());
                case INVENTORY:
                    return cell.withInventory(loadout.inventory());
                default:
                    return cell;
            }
        }, "Filled " + ref.describe() + " from the game");
    }

    void renameCell(CellRef ref, String name) {
        changeCell(ref, cell -> cell.withName(name), "Named " + ref.describe() + " " + name.strip());
    }

    /** Keeps the cell's kind and name, drops its items. */
    void clearCell(CellRef ref) {
        changeCell(ref, LayoutCell::cleared, "Emptied the items of " + ref.describe());
    }

    /** Turns the cell back into an empty one. */
    void emptyCell(CellRef ref) {
        changeCell(ref, cell -> cell.withKind(CellKind.EMPTY), "Emptied " + ref.describe());
    }

    void sync(SyncScope scope, Loadout loadout, String cause) {
        change(content -> LoadoutSync.apply(content, scope, loadout), cause);
    }

    void restore(SetupRevision revision) {
        Optional<GearSetup> found = setup();
        if (found.isEmpty()) {
            listener.gone();
            return;
        }
        history.restore(open, revision);
        revision.variant().ifPresent(variant -> editing = variant);
        listener.changed("Restored " + found.get().name(), UnaryOperator.identity());
    }

    private void changeCell(CellRef ref, UnaryOperator<LayoutCell> changeCell, String cause) {
        change(content -> {
            if (!(content instanceof CustomContent) || ref.row() >= ((CustomContent) content).rows()) {
                return content;
            }
            CustomContent custom = (CustomContent) content;
            return custom.withCell(ref, changeCell.apply(custom.cell(ref)));
        }, cause, state -> state.at(ref));
    }

    private void change(UnaryOperator<SetupContent> changeContent, String cause) {
        change(changeContent, cause, UnaryOperator.identity());
    }

    private void change(UnaryOperator<SetupContent> changeContent, String cause, UnaryOperator<ContentViewState> focus) {
        Optional<GearSetup> found = setup();
        if (found.isEmpty()) {
            listener.gone();
            return;
        }
        Optional<SetupVariant> target = editingVariant();
        if (target.isEmpty()) {
            listener.gone();
            return;
        }
        SetupContent before = target.get().content();
        SetupContent after = changeContent.apply(before);
        if (after.equals(before)) {
            return;
        }
        history.applyContent(open, target.get().id(), after, cause);
        listener.changed(cause, focus);
    }

    /** A change to the setup beyond its items, described after the fact so the cause can name the result. */
    private void changeSetup(UnaryOperator<GearSetup> changeSetup, Function<GearSetup, String> cause) {
        changeSetup(changeSetup, cause, null);
    }

    /** {@code nextEditing} moves the page onto a variant the change created; null leaves it where it is. */
    private void changeSetup(UnaryOperator<GearSetup> changeSetup, Function<GearSetup, String> cause, VariantId nextEditing) {
        Optional<GearSetup> found = setup();
        if (found.isEmpty()) {
            listener.gone();
            return;
        }
        GearSetup after = book.change(open, changeSetup);
        if (after.equals(found.get())) {
            return;
        }
        if (nextEditing != null) {
            editing = nextEditing;
        }
        listener.changed(cause.apply(after), UnaryOperator.identity());
    }
}
