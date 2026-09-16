package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.image.ImageTransform;
import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.style.ItemButtonStyle;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** All methods must be called on the EDT. */
public final class ItemButton extends JButton {

    private static final Logger log = LoggerFactory.getLogger(ItemButton.class);
    private static final float DISABLED_OPACITY = 0.4f;
    private static final String PLACEHOLDER_GLYPH = "?";

    private final ItemLoader loader;
    private final List<ItemClickListener> clickListeners = new CopyOnWriteArrayList<>();

    private ItemButtonStyle style;
    private final ImageTransform transform;
    private ItemImageOptions imageOptions;
    private String customTooltip;

    private ItemReference reference;
    private ResolvedItem item;
    private BufferedImage rawSprite;
    private BufferedImage displaySprite;
    private ItemButtonState state = ItemButtonState.EMPTY;
    private int generation;

    ItemButton(ItemLoader loader, ItemButtonStyle style, ImageTransform transform, ItemImageOptions imageOptions) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.style = Objects.requireNonNull(style, "style");
        this.transform = Objects.requireNonNull(transform, "transform");
        this.imageOptions = Objects.requireNonNull(imageOptions, "imageOptions");
        configureLookAndFeel();
        installMouseHandling();
        applySize();
        updateTooltip();
    }

    /** Discards any load still in flight for a previous reference. */
    public void setItem(ItemReference newReference) {
        EdtDispatch.requireEdt();
        Objects.requireNonNull(newReference, "newReference");
        reference = newReference;
        beginLoad();
    }

    public void clearItem() {
        EdtDispatch.requireEdt();
        generation++;
        reference = null;
        resetContent(ItemButtonState.EMPTY);
    }

    public void reload() {
        EdtDispatch.requireEdt();
        if (reference != null) {
            beginLoad();
        }
    }

    public Optional<ItemReference> reference() {
        return Optional.ofNullable(reference);
    }

    public Optional<ResolvedItem> item() {
        return Optional.ofNullable(item);
    }

    public ItemButtonState state() {
        return state;
    }

    public ItemButtonStyle style() {
        return style;
    }

    public void setStyle(ItemButtonStyle newStyle) {
        EdtDispatch.requireEdt();
        style = Objects.requireNonNull(newStyle, "newStyle");
        applySize();
        refreshDisplaySprite();
        revalidate();
        repaint();
    }

    public void setImageOptions(ItemImageOptions newOptions) {
        EdtDispatch.requireEdt();
        imageOptions = Objects.requireNonNull(newOptions, "newOptions");
        reload();
    }

    public void setCustomTooltip(String tooltip) {
        EdtDispatch.requireEdt();
        customTooltip = tooltip;
        updateTooltip();
    }

    public void addItemClickListener(ItemClickListener listener) {
        clickListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private void beginLoad() {
        int myGeneration = ++generation;
        resetContent(ItemButtonState.RESOLVING);
        loader.load(reference, imageOptions).whenComplete((result, error) -> EdtDispatch.onEdt(() -> {
            if (myGeneration != generation) {
                return;
            }
            if (error != null) {
                onFailed(error);
            } else {
                result.ifPresentOrElse(loaded -> onResolved(myGeneration, loaded), () -> transitionTo(ItemButtonState.UNRESOLVED));
            }
        }));
    }

    private void onFailed(Throwable error) {
        log.warn("Failed to load {}", reference.describe(), error);
        transitionTo(ItemButtonState.FAILED);
    }

    private void onResolved(int myGeneration, LoadedItem loaded) {
        item = loaded.item();
        transitionTo(ItemButtonState.LOADING);
        loaded.image().whenLoaded(sprite -> onSpriteLoaded(myGeneration, sprite));
    }

    private void onSpriteLoaded(int myGeneration, BufferedImage sprite) {
        if (myGeneration != generation) {
            return;
        }
        rawSprite = sprite;
        refreshDisplaySprite();
        transitionTo(ItemButtonState.READY);
    }

    private void resetContent(ItemButtonState newState) {
        item = null;
        rawSprite = null;
        displaySprite = null;
        transitionTo(newState);
    }

    private void transitionTo(ItemButtonState newState) {
        state = newState;
        updateTooltip();
        repaint();
    }

    private void refreshDisplaySprite() {
        if (rawSprite == null) {
            displaySprite = null;
            return;
        }
        Dimension content = style.contentSize();
        ImageTransform fit = ImageTransforms.scaleDownToFit(content.width, content.height);
        displaySprite = transform.andThen(fit).apply(rawSprite);
    }

    private void updateTooltip() {
        String tooltip = customTooltip != null ? customTooltip : automaticTooltip();
        setToolTipText(tooltip);
        getAccessibleContext().setAccessibleName(tooltip);
    }

    private String automaticTooltip() {
        switch (state) {
            case RESOLVING:
                return "Loading " + reference.describe();
            case LOADING:
            case READY:
                return item.name();
            case UNRESOLVED:
                return "Unknown item: " + reference.describe();
            case FAILED:
                return "Failed to load " + reference.describe();
            case EMPTY:
            default:
                return null;
        }
    }

    private void installMouseHandling() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispatchClick(e);
            }
        });
    }

    private void dispatchClick(MouseEvent e) {
        if (!isEnabled() || item == null || !contains(e.getPoint())) {
            return;
        }
        ItemClickEvent event = new ItemClickEvent(this, item, MouseButton.from(e), e.getClickCount(), e);
        for (ItemClickListener listener : clickListeners) {
            listener.onItemClicked(event);
        }
    }

    private void configureLookAndFeel() {
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setMargin(new Insets(0, 0, 0, 0));
    }

    private void applySize() {
        Dimension size = style.size();
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            paintBackground(g2);
            paintBorder(g2);
            if (displaySprite != null) {
                paintSprite(g2);
            } else if (state == ItemButtonState.UNRESOLVED || state == ItemButtonState.FAILED) {
                paintPlaceholder(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void paintBackground(Graphics2D g2) {
        g2.setColor(currentBackground());
        int radius = style.cornerRadius();
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
    }

    private Color currentBackground() {
        if (getModel().isArmed() && getModel().isPressed()) {
            return style.pressedBackground();
        }
        if (getModel().isRollover()) {
            return style.hoverBackground();
        }
        return style.background();
    }

    private void paintBorder(Graphics2D g2) {
        int thickness = style.borderThickness();
        Color color = isSelected() ? style.selectedBorder() : style.border();
        if (thickness == 0 || color == null) {
            return;
        }
        g2.setColor(color);
        int radius = style.cornerRadius();
        for (int i = 0; i < thickness; i++) {
            g2.drawRoundRect(i, i, getWidth() - 1 - 2 * i, getHeight() - 1 - 2 * i, radius, radius);
        }
    }

    private void paintSprite(Graphics2D g2) {
        Insets padding = style.padding();
        int border = style.borderThickness();
        int areaX = border + padding.left;
        int areaY = border + padding.top;
        int areaW = getWidth() - areaX - border - padding.right;
        int areaH = getHeight() - areaY - border - padding.bottom;
        int x = areaX + (areaW - displaySprite.getWidth()) / 2;
        int y = areaY + (areaH - displaySprite.getHeight()) / 2;
        if (!isEnabled()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, DISABLED_OPACITY));
        }
        g2.drawImage(displaySprite, x, y, null);
    }

    private void paintPlaceholder(Graphics2D g2) {
        g2.setColor(style.placeholder());
        g2.setFont(getFont());
        FontMetrics metrics = g2.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(PLACEHOLDER_GLYPH)) / 2;
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(PLACEHOLDER_GLYPH, x, y);
    }

    @Override
    public String toString() {
        return "ItemButton[" + state + ", " + (reference == null ? "empty" : reference.describe()) + "]";
    }
}
