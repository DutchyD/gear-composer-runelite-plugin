package dev.dutchy.runelite.libs.ui.selector;

import dev.dutchy.runelite.libs.ui.button.ItemButtonFactory;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.search.ItemSearch;
import dev.dutchy.runelite.libs.ui.style.ItemButtonStyle;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.IconTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** All methods must be called on the EDT. */
public final class ItemSelector extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(ItemSelector.class);
    private static final int DEFAULT_FIELD_HEIGHT = 30;

    private final ItemSearch search;
    private final ItemButtonFactory buttons;
    private final ItemButtonStyle rowSpriteStyle;
    private final int limit;
    private final ItemRowDragListener rowDrag;
    private final boolean keepResultsOnSelect;
    private final List<ItemSelectionListener> listeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> clearListeners = new CopyOnWriteArrayList<>();

    private final IconTextField input = new IconTextField();
    private final JPanel fieldRow = new JPanel(new BorderLayout(4, 0));
    private final JLabel status = new JLabel();
    private final JPanel results = new JPanel();
    private final Timer debounce;
    private final List<ItemSearchResultRow> rows = new ArrayList<>();

    private JComponent leading;

    private int highlighted = -1;
    private int generation;
    private boolean suppressSearch;
    private ResolvedItem selected;

    ItemSelector(ItemSearch search, ItemButtonFactory buttons, ItemButtonStyle rowSpriteStyle, int limit, int debounceMillis,
                 ItemRowDragListener rowDrag, boolean keepResultsOnSelect) {
        this.rowDrag = rowDrag;
        this.keepResultsOnSelect = keepResultsOnSelect;
        this.search = Objects.requireNonNull(search, "search");
        this.buttons = Objects.requireNonNull(buttons, "buttons");
        this.rowSpriteStyle = Objects.requireNonNull(rowSpriteStyle, "rowSpriteStyle");
        this.limit = limit;
        this.debounce = new Timer(debounceMillis, e -> runSearch());
        this.debounce.setRepeats(false);
        buildLayout();
        installInputHandling();
        showStatus("Type an item name or id");
    }

    public Optional<ResolvedItem> selectedItem() {
        return Optional.ofNullable(selected);
    }

    public void addSelectionListener(ItemSelectionListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Runs whenever the query and selection are wiped, whether by the clear button, Escape, or {@link #clear()}. */
    public void addClearListener(Runnable listener) {
        clearListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Places a component to the left of the search field; the field grows to match its height. */
    public void setLeadingComponent(JComponent component) {
        EdtDispatch.requireEdt();
        if (leading != null) {
            fieldRow.remove(leading);
        }
        leading = component;
        int height = component == null ? DEFAULT_FIELD_HEIGHT : component.getPreferredSize().height;
        if (component != null) {
            fieldRow.add(component, BorderLayout.WEST);
        }
        input.setPreferredSize(new Dimension(100, height));
        fieldRow.revalidate();
        fieldRow.repaint();
    }

    /** The whole selector, search field included, paints on this colour. */
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if (input != null) {
            input.setBackground(color);
            input.setHoverBackgroundColor(color);
        }
    }

    public void setQuery(String query) {
        EdtDispatch.requireEdt();
        setInputText(Objects.requireNonNull(query, "query"));
        debounce.stop();
        runSearch();
    }

    public String query() {
        return input.getText();
    }

    public void clear() {
        EdtDispatch.requireEdt();
        debounce.stop();
        generation++;
        selected = null;
        setInputText("");
        clearResults();
        showStatus("Type an item name or id");
        clearListeners.forEach(Runnable::run);
    }

    public void highlightNext() {
        EdtDispatch.requireEdt();
        moveHighlight(1);
    }

    public void highlightPrevious() {
        EdtDispatch.requireEdt();
        moveHighlight(-1);
    }

    public void selectHighlighted() {
        EdtDispatch.requireEdt();
        if (rows.isEmpty()) {
            return;
        }
        int index = Math.max(highlighted, 0);
        select(rows.get(index).item());
    }

    public void select(ResolvedItem item) {
        EdtDispatch.requireEdt();
        Objects.requireNonNull(item, "item");
        debounce.stop();
        generation++;
        selected = item;
        if (keepResultsOnSelect) {
            highlightRowOf(item);
        } else {
            setInputText(item.name());
            clearResults();
        }
        showStatus("Selected " + item.name() + " " + item.id());
        for (ItemSelectionListener listener : listeners) {
            listener.onItemSelected(item);
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        return input.requestFocusInWindow();
    }

    public int resultCount() {
        return rows.size();
    }

    List<ItemSearchResultRow> rows() {
        return List.copyOf(rows);
    }

    private void runSearch() {
        String query = input.getText().strip();
        int myGeneration = ++generation;
        if (query.isEmpty()) {
            clearResults();
            showStatus("Type an item name or id");
            return;
        }
        input.setIcon(IconTextField.Icon.LOADING);
        showStatus("Searching…");
        search.search(query, limit).whenComplete((hits, error) ->
                EdtDispatch.onEdt(() -> onSearchComplete(myGeneration, hits, error)));
    }

    private void onSearchComplete(int myGeneration, List<ResolvedItem> hits, Throwable error) {
        if (myGeneration != generation) {
            return;
        }
        input.setIcon(IconTextField.Icon.SEARCH);
        if (error != null) {
            log.warn("Item search failed", error);
            clearResults();
            showStatus("Search failed");
            return;
        }
        showResults(hits);
    }

    private void showResults(List<ResolvedItem> hits) {
        clearResults();
        for (ResolvedItem hit : hits) {
            ItemSearchResultRow row = new ItemSearchResultRow(hit, buttons, rowSpriteStyle, this::select, rowDrag);
            rows.add(row);
            results.add(row);
        }
        if (rows.isEmpty()) {
            showStatus("No items match");
        } else {
            showStatus(rows.size() + (rows.size() == limit ? "+ results" : " results"));
            setHighlight(0);
        }
        results.revalidate();
        results.repaint();
    }

    private void clearResults() {
        rows.clear();
        results.removeAll();
        highlighted = -1;
        results.revalidate();
        results.repaint();
    }

    private void highlightRowOf(ResolvedItem item) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).item().equals(item)) {
                setHighlight(i);
                return;
            }
        }
    }

    private void moveHighlight(int delta) {
        if (rows.isEmpty()) {
            return;
        }
        int next = highlighted < 0 ? (delta > 0 ? 0 : rows.size() - 1) : Math.floorMod(highlighted + delta, rows.size());
        setHighlight(next);
    }

    private void setHighlight(int index) {
        if (highlighted >= 0 && highlighted < rows.size()) {
            rows.get(highlighted).setHighlighted(false);
        }
        highlighted = index;
        if (highlighted >= 0 && highlighted < rows.size()) {
            rows.get(highlighted).setHighlighted(true);
        }
    }

    private void setInputText(String text) {
        suppressSearch = true;
        try {
            input.setText(text);
        } finally {
            suppressSearch = false;
        }
    }

    private void showStatus(String text) {
        status.setText(text);
    }

    private void buildLayout() {
        setLayout(new BorderLayout(0, 4));

        input.setIcon(IconTextField.Icon.SEARCH);
        input.setPreferredSize(new Dimension(100, DEFAULT_FIELD_HEIGHT));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        fieldRow.setOpaque(false);
        fieldRow.add(input, BorderLayout.CENTER);

        status.setFont(FontManager.getRunescapeSmallFont());
        status.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        status.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(0, 2));
        header.setOpaque(false);
        header.add(fieldRow, BorderLayout.NORTH);
        header.add(status, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(results, BorderLayout.CENTER);
    }

    private void installInputHandling() {
        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onTextChanged();
            }
        });
        input.addClearListener(this::clear);
        input.addActionListener(e -> selectHighlighted());
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        highlightNext();
                        e.consume();
                        break;
                    case KeyEvent.VK_UP:
                        highlightPrevious();
                        e.consume();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        clear();
                        e.consume();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void onTextChanged() {
        if (suppressSearch) {
            return;
        }
        selected = null;
        debounce.restart();
    }
}
