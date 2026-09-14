package dev.dutchy.runelite.gear.share;

import net.runelite.client.util.ImageCapture;

import javax.inject.Inject;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.util.Objects;

/** Saves through RuneLite's screenshot pipeline, so the file lands where every other screenshot does. */
public final class RuneLiteImageSink implements ImageSink {

    static final String SUB_DIRECTORY = "gear-composer";

    private final ImageCapture capture;

    @Inject
    public RuneLiteImageSink(ImageCapture capture) {
        this.capture = Objects.requireNonNull(capture, "capture");
    }

    @Override
    public String save(BufferedImage image, String fileName) {
        // notify the player, but leave the clipboard alone: copying is its own action on the share page.
        capture.saveScreenshot(image, fileName, SUB_DIRECTORY, true, false);
        return "the screenshots folder";
    }

    @Override
    public void copy(BufferedImage image) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageTransferable(image), null);
    }

    private static final class ImageTransferable implements Transferable {
        private final BufferedImage image;

        ImageTransferable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}
