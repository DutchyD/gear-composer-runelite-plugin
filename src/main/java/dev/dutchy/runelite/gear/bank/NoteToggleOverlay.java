package dev.dutchy.runelite.gear.bank;

import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.Objects;

/** Rings the bank's Note button while the shown layout wants notes and the bank would hand out items. */
public final class NoteToggleOverlay extends Overlay {

    private static final Color RING = new Color(120, 190, 255);

    private final Client client;
    private final DrawnBankSource bank;

    @Inject
    public NoteToggleOverlay(Client client, DrawnBankSource bank) {
        this.client = Objects.requireNonNull(client, "client");
        this.bank = Objects.requireNonNull(bank, "bank");
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        DrawnBank shown = bank.drawnBank();
        if (!shown.active() || !shown.wantsNotes() || client.getVarbitValue(VarbitID.BANK_WITHDRAWNOTES) == 1
                || GroupStorage.isOpen(client)) {
            return null;
        }
        Widget toggle = client.getWidget(InterfaceID.Bankmain.NOTE);
        if (toggle == null || toggle.isHidden()) {
            return null;
        }
        OverlayUtil.renderPolygon(graphics, toggle.getBounds(), RING, new BasicStroke(2));
        return null;
    }
}
