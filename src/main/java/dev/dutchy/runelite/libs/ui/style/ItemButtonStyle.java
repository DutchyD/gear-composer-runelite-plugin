package dev.dutchy.runelite.libs.ui.style;

import lombok.Value;
import lombok.experimental.Accessors;
import net.runelite.client.ui.ColorScheme;

import java.awt.*;
import java.util.Objects;

@Value
@Accessors(fluent = true)
public class ItemButtonStyle {
    Dimension size;
    Insets padding;
    Color background;
    Color hoverBackground;
    Color pressedBackground;
    Color border;
    Color selectedBorder;
    int borderThickness;
    int cornerRadius;
    Color placeholder;

    public ItemButtonStyle(Dimension size, Insets padding, Color background, Color hoverBackground, Color pressedBackground, Color border, Color selectedBorder, int borderThickness, int cornerRadius, Color placeholder) {
        Objects.requireNonNull(size, "size");
        Objects.requireNonNull(padding, "padding");
        Objects.requireNonNull(background, "background");
        Objects.requireNonNull(hoverBackground, "hoverBackground");
        Objects.requireNonNull(pressedBackground, "pressedBackground");
        Objects.requireNonNull(selectedBorder, "selectedBorder");
        Objects.requireNonNull(placeholder, "placeholder");
        if (size.width <= 0 || size.height <= 0) {
            throw new IllegalArgumentException("Size must be positive, got " + size);
        }
        if (borderThickness < 0) {
            throw new IllegalArgumentException("Border thickness must be non-negative, got " + borderThickness);
        }
        if (cornerRadius < 0) {
            throw new IllegalArgumentException("Corner radius must be non-negative, got " + cornerRadius);
        }
        size = new Dimension(size);
        padding = (Insets) padding.clone();
        this.size = size;
        this.padding = padding;
        this.background = background;
        this.hoverBackground = hoverBackground;
        this.pressedBackground = pressedBackground;
        this.border = border;
        this.selectedBorder = selectedBorder;
        this.borderThickness = borderThickness;
        this.cornerRadius = cornerRadius;
        this.placeholder = placeholder;
    }

    public static ItemButtonStyle runeLite() {
        return builder().build();
    }

    public static ItemButtonStyle compact() {
        return builder().size(26, 26).padding(1).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    /** Area left for the sprite once padding and border are removed. */
    public Dimension contentSize() {
        int horizontal = padding.left + padding.right + 2 * borderThickness;
        int vertical = padding.top + padding.bottom + 2 * borderThickness;
        return new Dimension(Math.max(1, size.width - horizontal), Math.max(1, size.height - vertical));
    }

    public Dimension size() {
        return new Dimension(size);
    }

    public Insets padding() {
        return (Insets) padding.clone();
    }

    public static final class Builder {

        private Dimension size = new Dimension(40, 36);
        private Insets padding = new Insets(2, 2, 2, 2);
        private Color background = ColorScheme.DARKER_GRAY_COLOR;
        private Color hoverBackground = ColorScheme.DARKER_GRAY_HOVER_COLOR;
        private Color pressedBackground = ColorScheme.MEDIUM_GRAY_COLOR;
        private Color border = ColorScheme.BORDER_COLOR;
        private Color selectedBorder = ColorScheme.BRAND_ORANGE;
        private int borderThickness = 1;
        private int cornerRadius = 4;
        private Color placeholder = ColorScheme.LIGHT_GRAY_COLOR;

        private Builder() {
        }

        private Builder(ItemButtonStyle style) {
            size = style.size();
            padding = style.padding();
            background = style.background;
            hoverBackground = style.hoverBackground;
            pressedBackground = style.pressedBackground;
            border = style.border;
            selectedBorder = style.selectedBorder;
            borderThickness = style.borderThickness;
            cornerRadius = style.cornerRadius;
            placeholder = style.placeholder;
        }

        public Builder size(int width, int height) {
            this.size = new Dimension(width, height);
            return this;
        }

        public Builder padding(int all) {
            this.padding = new Insets(all, all, all, all);
            return this;
        }

        public Builder background(Color background) {
            this.background = background;
            return this;
        }

        public Builder selectedBorder(Color selectedBorder) {
            this.selectedBorder = selectedBorder;
            return this;
        }

        public Builder borderThickness(int borderThickness) {
            this.borderThickness = borderThickness;
            return this;
        }

        public Builder placeholder(Color placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public ItemButtonStyle build() {
            return new ItemButtonStyle(size, padding, background, hoverBackground, pressedBackground,
                    border, selectedBorder, borderThickness, cornerRadius, placeholder);
        }
    }
}
