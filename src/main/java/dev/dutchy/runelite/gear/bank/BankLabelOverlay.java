package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.TextAlign;
import dev.dutchy.runelite.gear.layout.BankSide;
import dev.dutchy.runelite.gear.layout.LayoutLabel;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/** Writes cell names on their blank rows and dividers in the header strip of the row they head, each across the columns it spans. */
public final class BankLabelOverlay extends Overlay {

    private static final Color SHADOW = Color.BLACK;
    private static final String ELLIPSIS = "…";
    private static final int COLUMN_WIDTH = BankGeometry.ITEM_WIDTH + BankGeometry.ITEM_X_PADDING;
    private static final Color RULE = new Color(255, 144, 64, 110);

    private final Client client;
    private final DrawnBankSource bank;

    @Inject
    public BankLabelOverlay(Client client, DrawnBankSource bank) {
        this.client = Objects.requireNonNull(client, "client");
        this.bank = Objects.requireNonNull(bank, "bank");
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        DrawnBank shown = bank.drawnBank();
        if (!shown.active()) {
            return null;
        }
        List<LayoutLabel> labels = shown.labels();
        Widget container = client.getWidget(InterfaceID.Bankmain.ITEMS);
        if (labels.isEmpty() || container == null || container.isHidden()) {
            return null;
        }
        Rectangle bounds = container.getBounds();
        Shape clip = graphics.getClip();
        graphics.setClip(bounds);
        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics metrics = graphics.getFontMetrics();
        BankRows rows = shown.rows();
        for (LayoutLabel label : labels) {
            int firstColumn = (label.side() == BankSide.LEFT ? 0 : BankGrid.COLUMNS_PER_SIDE) + label.fromColumn();
            int left = bounds.x + BankGeometry.x(firstColumn);
            int right = left + label.width() * COLUMN_WIDTH - BankGeometry.ITEM_X_PADDING;
            int top = bounds.y + rows.top(label.row()) - container.getScrollY();
            int y = label.header()
                    ? top + BankGeometry.HEADER_HEIGHT - 3
                    : top + (BankGeometry.ITEM_HEIGHT + metrics.getAscent()) / 2;
            String text = fit(label.text(), metrics, right - left);
            int x = alignedX(label.align(), left, right, metrics.stringWidth(text));
            graphics.setColor(SHADOW);
            graphics.drawString(text, x + 1, y + 1);
            graphics.setColor(ColorScheme.BRAND_ORANGE);
            graphics.drawString(text, x, y);
            if (label.header() && label.underlined()) {
                graphics.setColor(RULE);
                graphics.drawLine(left, y + 2, right, y + 2);
            }
        }
        graphics.setClip(clip);
        return null;
    }

    /** Where text of the given width starts so it sits left, centred or right between the edges. */
    static int alignedX(TextAlign align, int left, int right, int textWidth) {
        switch (align) {
            case CENTRE:
                return left + (right - left - textWidth) / 2;
            case RIGHT:
                return right - textWidth;
            default:
                return left;
        }
    }

    private static String fit(String text, FontMetrics metrics, int width) {
        if (metrics.stringWidth(text) <= width) {
            return text;
        }
        String shortened = text;
        while (shortened.length() > 1 && metrics.stringWidth(shortened + ELLIPSIS) > width) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + ELLIPSIS;
    }
}
