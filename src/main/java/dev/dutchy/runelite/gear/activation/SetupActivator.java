package dev.dutchy.runelite.gear.activation;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.GearSetupBookListener;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.bank.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * The one place a setup or its variant is switched in the bank, whether from a tile, a hotkey, or a menu.
 * While a setup is active the bank follows it: any change to what the bank would show redraws, deleting it clears.
 * Every call changes the book, so callers on the client thread hand over to the EDT first.
 */
public final class SetupActivator implements GearSetupBookListener {

    private final GearSetupBook book;
    private final ActiveSetup active;
    private final BankLayoutPlanner planner;
    private final BankLayoutApplier bank;
    private final BankTagState tags;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private BankLayout shown;

    @Inject
    public SetupActivator(GearSetupBook book, ActiveSetup active, BankLayoutPlanner planner, BankLayoutApplier bank, BankTagState tags) {
        this.book = Objects.requireNonNull(book, "book");
        this.active = Objects.requireNonNull(active, "active");
        this.planner = Objects.requireNonNull(planner, "planner");
        this.bank = Objects.requireNonNull(bank, "bank");
        this.tags = Objects.requireNonNull(tags, "tags");
        book.addChangeListener(this);
    }

    @Override
    public void onBookChanged(GearSetupBook changed) {
        Optional<SetupId> id = active.current();
        if (id.isEmpty()) {
            return;
        }
        Optional<GearSetup> setup = book.setup(id.get());
        if (setup.isEmpty()) {
            clear();
        } else if (!layoutFor(setup.get()).equals(shown)) {
            apply(setup.get());
        }
    }

    /** Told after every switch a player asked for: a setup on or off, or another variant. Edits do not count. */
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeListener(Runnable listener) {
        listeners.remove(Objects.requireNonNull(listener, "listener"));
    }

    /** Turns the setup on, or off when it already is. Returns what is active afterwards. */
    public Optional<GearSetup> toggle(SetupId id) {
        Objects.requireNonNull(id, "id");
        Optional<GearSetup> setup = book.setup(id);
        if (setup.isEmpty()) {
            return current();
        }
        if (active.toggle(id).isPresent()) {
            apply(setup.get());
        } else {
            takeDown();
        }
        switched();
        return current();
    }

    /** Makes the variant the active setup's chosen one; the bank follows through the book. */
    public Optional<GearSetup> selectVariant(int index) {
        Optional<GearSetup> setup = current().filter(found -> found.variantAt(index).isPresent());
        setup.ifPresent(found -> {
            book.change(found.id(), current -> current.withSelectedVariant(index));
            switched();
        });
        return current();
    }

    /** Steps to the active setup's next variant, wrapping around. */
    public Optional<GearSetup> nextVariant() {
        return stepVariant(1);
    }

    public Optional<GearSetup> previousVariant() {
        return stepVariant(-1);
    }

    private Optional<GearSetup> stepVariant(int direction) {
        Optional<GearSetup> setup = current().filter(GearSetup::hasVariants);
        if (setup.isEmpty()) {
            return current();
        }
        return selectVariant(Math.floorMod(setup.get().selectedIndex() + direction, setup.get().variants().size()));
    }

    /** Shows the setup even if it is already active, for example after its items changed. */
    public void show(SetupId id) {
        Objects.requireNonNull(id, "id");
        book.setup(id).ifPresent(setup -> {
            if (!active.isActive(id)) {
                active.toggle(id);
            }
            apply(setup);
            switched();
        });
    }

    public void clear() {
        if (active.current().isPresent()) {
            active.clear();
            takeDown();
            switched();
        }
    }

    private void switched() {
        listeners.forEach(Runnable::run);
    }

    private void apply(GearSetup setup) {
        if (tags.isTagOpen()) {
            tags.closeTag();
        }
        shown = layoutFor(setup);
        bank.apply(shown);
    }

    private BankLayout layoutFor(GearSetup setup) {
        return planner.plan(setup.content()).withVariants(VariantTabs.of(setup));
    }

    private void takeDown() {
        shown = null;
        bank.clear();
    }

    public Optional<GearSetup> current() {
        return active.current().flatMap(book::setup);
    }

    /** Steps to the next setup in book order, wrapping around; starts at the first when none is active. */
    public Optional<GearSetup> next() {
        return step(1);
    }

    public Optional<GearSetup> previous() {
        return step(-1);
    }

    private Optional<GearSetup> step(int direction) {
        List<GearSetup> all = book.sections().stream().flatMap(section -> section.setups().stream()).collect(Collectors.toList());
        if (all.isEmpty()) {
            return Optional.empty();
        }
        int index = active.current().map(id -> indexOf(all, id)).orElse(-1);
        int target = index < 0
                ? (direction > 0 ? 0 : all.size() - 1)
                : Math.floorMod(index + direction, all.size());
        show(all.get(target).id());
        return current();
    }

    private static int indexOf(List<GearSetup> all, SetupId id) {
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /** Position in book order, for messages such as "3 of 7". */
    public Optional<int[]> position() {
        List<GearSetup> all = book.sections().stream().flatMap(section -> section.setups().stream()).collect(Collectors.toList());
        return active.current().map(id -> indexOf(all, id)).filter(index -> index >= 0)
                .map(index -> new int[] {index + 1, all.size()});
    }
}
