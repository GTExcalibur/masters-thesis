package gturner.crossword.substance;

import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.lafwidget.animation.effects.GhostPaintingUtils;
import org.pushingpixels.lafwidget.utils.RenderingUtils;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.internal.colorscheme.ShadeColorScheme;
import org.pushingpixels.substance.internal.colorscheme.ToneColorScheme;
import org.pushingpixels.substance.internal.painter.BackgroundPaintingUtils;
import org.pushingpixels.substance.internal.ui.SubstanceRootPaneUI;
import org.pushingpixels.substance.internal.utils.SubstanceColorUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceTextUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceTitlePane;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 4:13 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class TonedTitlePane extends SubstanceTitlePane {

    /**
     * Creates a new title pane.
     *
     * @param root Root pane.
     * @param ui   Root pane UI.
     */
    public TonedTitlePane(JRootPane root, SubstanceRootPaneUI ui) {
        super(root, ui);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        // long start = System.nanoTime();
        // As state isn't bound, we need a convenience place to check
        // if it has changed. Changing the state typically changes the
        if (this.getFrame() != null) {
            this.setState(this.getFrame().getExtendedState());
        }
        final JRootPane rootPane = this.getRootPane();
        Window window = this.getWindow();
        boolean leftToRight = (window == null) ? rootPane
                .getComponentOrientation().isLeftToRight() : window
                .getComponentOrientation().isLeftToRight();
        int width = this.getWidth();
        int height = this.getHeight();

        SubstanceSkin skin = SubstanceCoreUtilities.getSkin(rootPane);
        if (skin == null) {
            SubstanceCoreUtilities
                    .traceSubstanceApiUsage(this,
                            "Substance delegate used when Substance is not the current LAF");
        }
        SubstanceColorScheme scheme = skin
                .getEnabledColorScheme(DecorationAreaType.PRIMARY_TITLE_PANE);
        boolean activated = TranslucentRoundedCornersPaneBorder.isActivated(rootPane);
        if (!activated) {
            scheme = new ToneColorScheme(scheme, TonedInternalFrameTitlePane.TONE_AMOUNT);
            scheme = new ShadeColorScheme(scheme, TonedInternalFrameTitlePane.SHADE_AMOUNT);
        }

        int xOffset = 0;
        String theTitle = this.getTitle();

        if (theTitle != null) {
            Rectangle titleTextRect = this.getTitleTextRectangle();
            FontMetrics fm = rootPane.getFontMetrics(g.getFont());
            int titleWidth = titleTextRect.width - 20;
            String clippedTitle = SubstanceCoreUtilities.clipString(fm,
                    titleWidth, theTitle);
            // show tooltip with full title only if necessary
            if (theTitle.equals(clippedTitle)) {
                this.setToolTipText(null);
            } else {
                this.setToolTipText(theTitle);
            }
            theTitle = clippedTitle;
            if (leftToRight)
                xOffset = titleTextRect.x;
            else
                xOffset = titleTextRect.x + titleTextRect.width
                        - fm.stringWidth(theTitle);
        }

        Graphics2D graphics = (Graphics2D) g.create();
        Font font = SubstanceLookAndFeel.getFontPolicy().getFontSet(
                "Substance", null).getWindowTitleFont();
        graphics.setFont(font);

        BackgroundPaintingUtils
                .update(graphics, TonedTitlePane.this, false);
        // DecorationPainterUtils.paintDecorationBackground(graphics,
        // SubstanceTitlePane.this, false);

        // draw the title (if needed)
        if (theTitle != null) {
            FontMetrics fm = rootPane.getFontMetrics(graphics.getFont());
            int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();

            SubstanceTextUtilities.paintTextWithDropShadow(this, graphics,
                    SubstanceColorUtilities.getForegroundColor(scheme),
                    theTitle, width, height, xOffset, yOffset);
        }

        GhostPaintingUtils.paintGhostImages(this, graphics);

        // long end = System.nanoTime();
        // System.out.println(end - start);
        graphics.dispose();
    }

    /**
     * Paints text with drop shadow.
     *
     * @param c
     *            Component.
     * @param g
     *            Graphics context.
     * @param foregroundColor
     *            Foreground color.
     * @param text
     *            Text to paint.
     * @param width
     *            Text rectangle width.
     * @param height
     *            Text rectangle height.
     * @param xOffset
     *            Text rectangle X offset.
     * @param yOffset
     *            Text rectangle Y offset.
     */
    public static void paintTextWithFrosting(JComponent c, Graphics g,
            Color foregroundColor, String text, int width, int height,
            int xOffset, int yOffset) {
        Graphics2D graphics = (Graphics2D) g.create();
        RenderingUtils.installDesktopHints(graphics, c);

        // blur the text shadow
        BufferedImage blurred = SubstanceCoreUtilities.getBlankImage(width,
                height);
        Graphics2D gBlurred = (Graphics2D) blurred.getGraphics();
        gBlurred.setFont(graphics.getFont());
        gBlurred.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        // Color neg =
        // SubstanceColorUtilities.getNegativeColor(foregroundColor);
        float luminFactor = SubstanceColorUtilities
                .getColorStrength(foregroundColor);
        gBlurred.setColor(SubstanceColorUtilities
                .getNegativeColor(foregroundColor));
        ConvolveOp convolve = new ConvolveOp(new Kernel(12, 9, new float[] {
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
                0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f, 0.03f, 0.01f,
        }),
                ConvolveOp.EDGE_NO_OP, null);
        gBlurred.drawString(text, xOffset, yOffset - 1);
        blurred = convolve.filter(blurred, null);

        graphics.setComposite(LafWidgetUtilities.getAlphaComposite(c,
                luminFactor, g));
        graphics.drawImage(blurred, 0, 0, null);
        graphics.setComposite(LafWidgetUtilities.getAlphaComposite(c, g));

        FontMetrics fm = graphics.getFontMetrics();
        SubstanceTextUtilities.paintText(graphics, c, new Rectangle(xOffset,
                yOffset - fm.getAscent(), width - xOffset, fm.getHeight()),
                text, -1, graphics.getFont(), foregroundColor, graphics
                        .getClipBounds());

        graphics.dispose();
    }

}

