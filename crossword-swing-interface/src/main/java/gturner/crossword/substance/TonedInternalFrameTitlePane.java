package gturner.crossword.substance;

import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.internal.colorscheme.ShadeColorScheme;
import org.pushingpixels.substance.internal.colorscheme.ShiftColorScheme;
import org.pushingpixels.substance.internal.colorscheme.ToneColorScheme;
import org.pushingpixels.substance.internal.painter.BackgroundPaintingUtils;
import org.pushingpixels.substance.internal.utils.SubstanceColorUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceInternalFrameTitlePane;
import org.pushingpixels.substance.internal.utils.SubstanceTextUtilities;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 4:17 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class TonedInternalFrameTitlePane extends SubstanceInternalFrameTitlePane {

    public static final float TONE_AMOUNT = 0.2f;
    public static final float SHADE_AMOUNT = 0.2f;

    /**
     * Simple constructor.
     *
     * @param f Associated internal frame.
     */
    public TonedInternalFrameTitlePane(JInternalFrame f) {
        super(f);
    }

    @Override
    public JRootPane getRootPane() {
        return frame.getRootPane();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        // if (this.isPalette) {
        // this.paintPalette(g);
        // return;
        // }
        Graphics2D graphics = (Graphics2D) g.create();
        // Desktop icon is translucent.
        final float coef = (this.getParent() instanceof JInternalFrame.JDesktopIcon) ? 0.6f
                : 1.0f;
        graphics.setComposite(LafWidgetUtilities.getAlphaComposite(this.frame,
                coef, g));

        boolean leftToRight = this.frame.getComponentOrientation()
                .isLeftToRight();

        int width = this.getWidth();
        int height = this.getHeight() + 2;

        SubstanceColorScheme scheme = SubstanceCoreUtilities
                .getSkin(this.frame).getEnabledColorScheme(
                        DecorationAreaType.SECONDARY_TITLE_PANE);
        boolean activated = TranslucentRoundedCornersPaneBorder.isActivated(frame.getRootPane());
        if (!activated) {
            scheme = new ToneColorScheme(scheme, TonedInternalFrameTitlePane.TONE_AMOUNT);
            scheme = new ShadeColorScheme(scheme, TonedInternalFrameTitlePane.SHADE_AMOUNT);
        }

        JInternalFrame hostFrame = (JInternalFrame) SwingUtilities
                .getAncestorOfClass(JInternalFrame.class, this);
        JComponent hostForColorization = hostFrame;
        if (hostFrame == null) {
            // try desktop icon
            JInternalFrame.JDesktopIcon desktopIcon = (JInternalFrame.JDesktopIcon) SwingUtilities
                    .getAncestorOfClass(JInternalFrame.JDesktopIcon.class, this);
            if (desktopIcon != null)
                hostFrame = desktopIcon.getInternalFrame();
            hostForColorization = desktopIcon;
        }
        // if ((hostFrame != null) && SubstanceCoreUtilities.hasColorization(
        // this)) {
        Color backgr = hostFrame.getBackground();
        if (!(backgr instanceof UIResource)) {
            double colorization = SubstanceCoreUtilities
                    .getColorizationFactor(hostForColorization);
            scheme = ShiftColorScheme.getShiftedScheme(scheme, backgr,
                    colorization, null, 0.0);
        }
        // }
        String theTitle = this.frame.getTitle();

        // offset of border
        int xOffset;
        int leftEnd;
        int rightEnd;

        if (leftToRight) {
            xOffset = 5;
            Icon icon = this.frame.getFrameIcon();
            if (icon != null) {
                xOffset += icon.getIconWidth() + 5;
            }

            leftEnd = (this.menuBar == null) ? 0
                    : (this.menuBar.getWidth() + 5);
            xOffset += leftEnd;
            if (icon != null)
                leftEnd += (icon.getIconWidth() + 5);

            rightEnd = width - 5;

            // find the leftmost button for the right end
            AbstractButton leftmostButton = null;
            if (this.frame.isIconifiable()) {
                leftmostButton = this.iconButton;
            } else {
                if (this.frame.isMaximizable()) {
                    leftmostButton = this.maxButton;
                } else {
                    if (this.frame.isClosable()) {
                        leftmostButton = this.closeButton;
                    }
                }
            }

            if (leftmostButton != null) {
                Rectangle rect = leftmostButton.getBounds();
                rightEnd = rect.getBounds().x - 5;
            }
            if (theTitle != null) {
                FontMetrics fm = this.frame.getFontMetrics(graphics.getFont());
                int titleWidth = rightEnd - leftEnd;
                String clippedTitle = SubstanceCoreUtilities.clipString(fm,
                        titleWidth, theTitle);
                // show tooltip with full title only if necessary
                if (theTitle.equals(clippedTitle))
                    this.setToolTipText(null);
                else
                    this.setToolTipText(theTitle);
                theTitle = clippedTitle;
            }
        } else {
            xOffset = width - 5;

            Icon icon = this.frame.getFrameIcon();
            if (icon != null) {
                xOffset -= (icon.getIconWidth() + 5);
            }

            rightEnd = (this.menuBar == null) ? xOffset : xOffset
                    - this.menuBar.getWidth() - 5;

            // find the rightmost button for the left end
            AbstractButton rightmostButton = null;
            if (this.frame.isIconifiable()) {
                rightmostButton = this.iconButton;
            } else {
                if (this.frame.isMaximizable()) {
                    rightmostButton = this.maxButton;
                } else {
                    if (this.frame.isClosable()) {
                        rightmostButton = this.closeButton;
                    }
                }
            }

            leftEnd = 5;
            if (rightmostButton != null) {
                Rectangle rect = rightmostButton.getBounds();
                leftEnd = rect.getBounds().x + 5;
            }
            if (theTitle != null) {
                FontMetrics fm = this.frame.getFontMetrics(graphics.getFont());
                int titleWidth = rightEnd - leftEnd;
                String clippedTitle = SubstanceCoreUtilities.clipString(fm,
                        titleWidth, theTitle);
                // show tooltip with full title only if necessary
                if (theTitle.equals(clippedTitle)) {
                    this.setToolTipText(null);
                } else {
                    this.setToolTipText(theTitle);
                }
                theTitle = clippedTitle;
                xOffset = rightEnd - fm.stringWidth(theTitle);
            }
        }

        BackgroundPaintingUtils.update(graphics,
                TonedInternalFrameTitlePane.this, false);
        // DecorationPainterUtils.paintDecorationBackground(graphics,
        // SubstanceInternalFrameTitlePane.this, false);

        // draw the title (if needed)
        if (theTitle != null) {
            JRootPane rootPane = this.getRootPane();
            FontMetrics fm = rootPane.getFontMetrics(graphics.getFont());
            int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();

            SubstanceTextUtilities.paintTextWithDropShadow(this, graphics,
                    SubstanceColorUtilities.getForegroundColor(scheme),
                    theTitle, width, height, xOffset, yOffset);
        }

        Icon icon = this.frame.getFrameIcon();
        if (icon != null) {
            if (leftToRight) {
                int iconY = ((height / 2) - (icon.getIconHeight() / 2));
                icon.paintIcon(this.frame, graphics, 5, iconY);
            } else {
                int iconY = ((height / 2) - (icon.getIconHeight() / 2));
                icon.paintIcon(this.frame, graphics, width - 5
                        - icon.getIconWidth(), iconY);
            }
        }

        graphics.dispose();
    }

}
