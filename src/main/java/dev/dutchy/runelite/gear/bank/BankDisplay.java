package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.config.GearComposerConfig;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.libs.ui.item.ItemId;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

/**
 * What the bank currently shows. Holds the layout, the dose choices, the live search and where the
 * variant strip is scrolled to, and turns all of that into a {@link BankFrame} once per bank build.
 * Nothing here touches the game, so every decision it makes is testable; a screen writes the result.
 */
@Singleton
public final class BankDisplay implements BankLayoutApplier, DrawnBankSource, SlotChooser {

    private static final int PLACEHOLDER_OPACITY = 120;
    private static final int SEARCH_MISS_OPACITY = 190;
    private static final int STRIP_TOP = 2;
    private static final int FULLY_OPAQUE = 0;
    private static final String PLACEHOLDER_COLOUR = "<col=ff9040>";

    private final BankGrid grid;
    private final BankContents contents;
    private final BankItemNames itemNames;
    private final BooleanSupplier dimSearch;

    private BankScreen screen = BankScreen.none();
    private IntConsumer chooser = index -> {
    };

    private BankLayout layout = BankLayout.EMPTY;
    private final Map<Integer, ItemId> chosen = new HashMap<>();
    private String searchQuery = "";
    private boolean rewind;
    private int stripOffset;
    private volatile DrawnBank drawnBank = DrawnBank.NONE;

    @Inject
    public BankDisplay(BankGrid grid, BankContents contents, BankItemNames itemNames, GearComposerConfig config) {
        this(grid, contents, itemNames, config::dimUnmatchedOnSearch);
    }

    public BankDisplay(BankGrid grid, BankContents contents, BankItemNames itemNames, BooleanSupplier dimSearch) {
        this.grid = Objects.requireNonNull(grid, "grid");
        this.contents = Objects.requireNonNull(contents, "contents");
        this.itemNames = Objects.requireNonNull(itemNames, "itemNames");
        this.dimSearch = Objects.requireNonNull(dimSearch, "dimSearch");
    }

    /** Wired after construction, because the screen asks the display for frames. */
    public void drawOn(BankScreen newScreen) {
        screen = Objects.requireNonNull(newScreen, "newScreen");
    }

    /** Where a click on a variant tab goes; the session points it at the activator while it runs. */
    public void onVariantChosen(IntConsumer newChooser) {
        chooser = Objects.requireNonNull(newChooser, "newChooser");
    }

    @Override
    public void apply(BankLayout newLayout) {
        layout = Objects.requireNonNull(newLayout, "newLayout");
        chosen.clear();
        stripOffset = layout.variants().offsetShowingChosen(0);
        rewind = true;
        publish(Map.of());
        screen.rebuild();
    }

    @Override
    public void clear() {
        if (layout.isEmpty()) {
            return;
        }
        forget();
        screen.rebuild();
    }

    /** Drops the layout without redrawing, for when the bank is already closing. */
    @Override
    public void discard() {
        forget();
    }

    private void forget() {
        layout = BankLayout.EMPTY;
        chosen.clear();
        stripOffset = 0;
        rewind = false;
        publish(Map.of());
    }

    /** A dose choice lasts as long as the layout, so applying a layout forgets it. */
    @Override
    public void show(int slotIndex, ItemId id) {
        if (layout.isEmpty()) {
            return;
        }
        chosen.put(slotIndex, Objects.requireNonNull(id, "id"));
        screen.rebuild();
    }

    /** Told what the player typed into the bank search, so misses can be dimmed. */
    public void searching(String query) {
        searchQuery = Objects.requireNonNull(query, "query");
    }

    /** Whether a live search fades the items it misses instead of letting the game hide them. */
    public boolean dimsSearchMisses() {
        return dimSearch.getAsBoolean();
    }

    public void chooseVariant(int index) {
        chooser.accept(index);
    }

    /** Scrolls the variant strip and redraws, ignoring a scroll that would go nowhere. */
    public void scrollStrip(int direction) {
        int next = layout.variants().clampOffset(stripOffset + direction);
        if (next == stripOffset || direction == 0) {
            return;
        }
        stripOffset = next;
        screen.rebuild();
    }

    public boolean isActive() {
        return !layout.isEmpty();
    }

    @Override
    public boolean isBankOpen() {
        return screen.isBankOpen();
    }

    @Override
    public DrawnBank drawnBank() {
        return drawnBank;
    }

    /** The appearance of the build the screen is in the middle of, from what the bank shows it. */
    public BankFrame frameFor(BankSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (layout.isEmpty()) {
            publish(Map.of());
            return BankFrame.NONE;
        }

        SlotPool<Integer> pool = new SlotPool<>();
        Map<Integer, Integer> held = new HashMap<>();
        for (BankSnapshot.Shown shown : snapshot.shown()) {
            pool.add(shown.itemId(), shown.child());
            held.put(shown.child(), shown.quantity());
        }

        Map<Integer, SlotFace> faces = new HashMap<>();
        List<BankSlotPlan> borrowed = new ArrayList<>();
        for (BankSlotPlan plan : grid.plan(layout, contents, chosen)) {
            Optional<Integer> owned = pool.claim(plan.shown().value());
            if (owned.isPresent()) {
                int child = owned.get();
                faces.put(child, ownedFace(plan, child, held.getOrDefault(child, 0)));
            } else {
                borrowed.add(plan);
            }
        }

        int next = snapshot.firstSpareChild();
        for (BankSlotPlan plan : borrowed) {
            Optional<Integer> spare = pool.spare();
            int child = spare.isPresent() ? spare.get() : next++;
            faces.put(child, pool.template(plan.shown().value())
                    .map(template -> twinFace(plan, template))
                    .orElseGet(() -> placeholderFace(plan)));
        }

        int rows = grid.rowsNeeded(layout);
        BankFrame frame = new BankFrame(dim(faces), tabsFor(snapshot.width()), arrowsFor(snapshot.width()),
                next, rows > 0 ? layout.rows().height(rows) : 0, rewind);
        rewind = false;
        drawnBank = frame.drawnBank(layout.labels(), layout.rows(), searchQuery);
        return frame;
    }

    /**
     * The game's own child for an item the player holds, which is also the bank slot its withdraws
     * act on. Its name and menu are left alone so withdrawing keeps working.
     */
    private SlotFace ownedFace(BankSlotPlan plan, int child, int held) {
        SetupItem item = plan.item();
        boolean counted = item.quantity().isPresent();
        return new SlotFace(SlotFace.Kind.OWNED, plan, "", child, SlotFace.NONE, x(plan), y(plan), FULLY_OPAQUE,
                counted ? item.shownQuantity(held) : SlotFace.NONE, counted);
    }

    private SlotFace twinFace(BankSlotPlan plan, int template) {
        SetupItem item = plan.item();
        return new SlotFace(SlotFace.Kind.TWIN, plan, "", template, template, x(plan), y(plan), FULLY_OPAQUE,
                item.shownQuantity(plan.supply().shownCount()), item.quantity().isPresent());
    }

    private SlotFace placeholderFace(BankSlotPlan plan) {
        String name = PLACEHOLDER_COLOUR + GameText.escape(itemNames.of(plan.shown())) + "</col>";
        return new SlotFace(SlotFace.Kind.PLACEHOLDER, plan, name, SlotFace.NONE, SlotFace.NONE,
                x(plan), y(plan), PLACEHOLDER_OPACITY, Integer.MAX_VALUE, true);
    }

    private int x(BankSlotPlan plan) {
        return BankGeometry.x(plan.slotIndex());
    }

    private int y(BankSlotPlan plan) {
        return layout.rows().itemY(plan.slotIndex() / BankGrid.ITEMS_PER_ROW);
    }

    /** Items a live search does not match are faded, so what was typed still stands out. */
    private Map<Integer, SlotFace> dim(Map<Integer, SlotFace> faces) {
        if (searchQuery.isEmpty() || !dimSearch.getAsBoolean()) {
            return faces;
        }
        String needle = searchQuery.toLowerCase();
        Map<Integer, SlotFace> result = new HashMap<>(faces.size());
        faces.forEach((child, face) -> result.put(child, misses(face, needle) ? face.dimmed(SEARCH_MISS_OPACITY) : face));
        return result;
    }

    private boolean misses(SlotFace face, String needle) {
        return !face.isPlaceholder() && !itemNames.of(face.plan().shown()).toLowerCase().contains(needle);
    }

    private List<TabFace> tabsFor(int areaWidth) {
        VariantTabs tabs = layout.variants();
        if (tabs.isEmpty()) {
            return List.of();
        }
        int width = stripWidth(areaWidth);
        stripOffset = tabs.clampOffset(stripOffset);
        int height = BankGeometry.VARIANT_STRIP_HEIGHT - 2 * STRIP_TOP;
        int tabWidth = tabs.tabWidth(width);
        List<TabFace> faces = new ArrayList<>();
        for (int position = 0; position < tabs.visibleCount() && stripOffset + position < tabs.count(); position++) {
            int index = stripOffset + position;
            faces.add(new TabFace(GameText.escape(tabs.names().get(index)), index, index == tabs.chosen(),
                    BankGeometry.ITEM_START_X + tabs.tabX(position, width), STRIP_TOP, tabWidth, height));
        }
        return faces;
    }

    private List<ArrowFace> arrowsFor(int areaWidth) {
        VariantTabs tabs = layout.variants();
        if (tabs.isEmpty()) {
            return List.of();
        }
        int width = stripWidth(areaWidth);
        int height = BankGeometry.VARIANT_STRIP_HEIGHT - 2 * STRIP_TOP;
        List<ArrowFace> faces = new ArrayList<>();
        if (stripOffset > 0) {
            faces.add(new ArrowFace(false, GameText.escape("<"), BankGeometry.ITEM_START_X, STRIP_TOP,
                    VariantTabs.ARROW_WIDTH, height));
        }
        if (stripOffset < tabs.maxOffset()) {
            faces.add(new ArrowFace(true, GameText.escape(">"),
                    BankGeometry.ITEM_START_X + width - VariantTabs.ARROW_WIDTH, STRIP_TOP,
                    VariantTabs.ARROW_WIDTH, height));
        }
        return faces;
    }

    private static int stripWidth(int areaWidth) {
        return Math.max(1, areaWidth - BankGeometry.ITEM_START_X);
    }

    private void publish(Map<Integer, DrawnSlot> slots) {
        drawnBank = new DrawnBank(isActive(), slots, layout.labels(), layout.rows(), searchQuery);
    }
}
