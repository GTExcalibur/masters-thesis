package gturner.crossword.substance;

import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.internal.colorscheme.ShadeColorScheme;
import org.pushingpixels.substance.internal.colorscheme.ToneColorScheme;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.border.SubstancePaneBorder;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 4:10 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class TonedRoundedCornersPaneBorder extends SubstancePaneBorder {

    /**
     * Default border thickness.
     */
    private static final int BT = 4;
    private static final int BT2 = 12;

    boolean isRoundedCorners(Component c) {
        return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        if (isRoundedCorners(c)) {
            paintRoundedBorder(c, g, x, y, w, h);
        } else {
            paintSquareBorder(c, g, x, y, w, h);
        }
    }

    public void paintSquareBorder(Component c, Graphics g, int x, int y, int w, int h) {
        SubstanceColorScheme scheme = getColorScheme(c);
        if (scheme == null) return;
        SubstanceColorScheme borderScheme = getBorderColorScheme(c);

		Graphics2D graphics = (Graphics2D) g;

		// bottom and right in ultra dark
		graphics.setColor(borderScheme.getUltraDarkColor());
		graphics.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
		graphics.drawLine(x + w - 1, y, x + w - 1, y + h - 1);
		// top and left
		graphics.setColor(borderScheme.getDarkColor());
		graphics.drawLine(x, y, x + w - 2, y);
		graphics.drawLine(x, y, x, y + h - 2);
		// inner bottom and right
		graphics.setColor(scheme.getMidColor());
		graphics.drawLine(x + 1, y + h - 2, x + w - 2, y + h - 2);
		graphics.drawLine(x + w - 2, y + 1, x + w - 2, y + h - 2);
		// inner top and left
		graphics.setColor(scheme.getMidColor());
		graphics.drawLine(x + 1, y + 1, x + w - 3, y + 1);
		graphics.drawLine(x + 1, y + 1, x + 1, y + h - 3);
		// inner 2 and 3
		graphics.setColor(scheme.getLightColor());
		graphics.drawRect(x + 2, y + 2, w - 5, h - 5);
		graphics.drawRect(x + 3, y + 3, w - 7, h - 7);
	}

    public void paintRoundedBorder(Component c, Graphics g, int x, int y, int w, int h) {
        SubstanceColorScheme scheme = getColorScheme(c);
        if (scheme == null) return;
        SubstanceColorScheme borderScheme = getBorderColorScheme(c);

        Graphics2D graphics = (Graphics2D) g;

        int xl = x + BT + 2;
        int xr = x + w - BT - 3;
        int yt = y + BT + 2;
        int yb = y + h - BT - 3;

        Object rh = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // bottom and right in ultra dark
        graphics.setColor(borderScheme.getUltraDarkColor());
        graphics.drawLine(xl, y + h - 1, xr, y + h - 1); // bottom
        graphics.drawLine(x + w - 1, yt, x + w - 1, yb); // right
        // se
        graphics.fillOval(x+w-BT2,y+h-BT2,BT2,BT2);


        // top and left
        graphics.setColor(borderScheme.getDarkColor());
        graphics.drawLine(xl, y, xr, y);
        graphics.drawLine(x, yt, x, yb);
        // nw, ne, sw
        graphics.fillOval(0        ,0        ,BT2,BT2);
        graphics.fillOval(0        ,y+h-BT2,BT2,BT2);
        graphics.fillOval(x+w-BT2,0        ,BT2,BT2);



        // inner bottom and right
        graphics.setColor(scheme.getMidColor());
        graphics.drawLine(xl, y + h - 2, xr, y + h - 2);
        graphics.drawLine(x + w - 2, yt, x + w - 2, yb);
        graphics.drawLine(xl, y + 1, xr, y + 1);
        graphics.drawLine(x + 1, yt, x + 1, yb);

        graphics.fillOval(1        ,1        ,BT2,BT2);
        graphics.fillOval(1        ,y+h-BT2-1,BT2,BT2);
        graphics.fillOval(x+w-BT2-1,1        ,BT2,BT2);
        graphics.fillOval(x+w-BT2-1,y+h-BT2-1,BT2,BT2);



        graphics.setColor(scheme.getLightColor());
        graphics.drawLine(xl,        y + 2,     xr,        y + 2);
        graphics.drawLine(x + 2,     yt,        x + 2,     yb);
        graphics.drawLine(xl,        y + h - 3, xr,        y + h - 3);
        graphics.drawLine(x + w - 3, yt,        x + w - 3, yb);
        graphics.drawLine(xl,        y + 3,     xr,        y + 3);
        graphics.drawLine(x + 3,     yt,        x + 3,     yb);
        graphics.drawLine(xl,        y + h - 4, xr,        y + h - 4);
        graphics.drawLine(x + w - 4, yt,        x + w - 4, yb);

        graphics.fillOval(2        ,2        ,BT2,BT2);
        graphics.fillOval(2        ,y+h-BT2-2,BT2,BT2);
        graphics.fillOval(x+w-BT2-2,2        ,BT2,BT2);
        graphics.fillOval(x+w-BT2-2,y+h-BT2-2,BT2,BT2);


        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rh);
    }

    private SubstanceColorScheme getColorScheme(Component c) {
        JRootPane rp = c instanceof JRootPane
                ? (JRootPane) c
                : SwingUtilities.getRootPane(c);

        SubstanceColorScheme scheme = null;
        SubstanceSkin skin = SubstanceCoreUtilities.getSkin(c);
        if (skin != null) {

            scheme = skin
                    .getBackgroundColorScheme(DecorationAreaType.PRIMARY_TITLE_PANE);

            if (!TranslucentRoundedCornersPaneBorder.isActivated(rp)) {
                scheme = new ToneColorScheme(scheme, TonedInternalFrameTitlePane.TONE_AMOUNT);
                scheme = new ShadeColorScheme(scheme, TonedInternalFrameTitlePane.SHADE_AMOUNT);
            }
        }
        return scheme;
    }

    private SubstanceColorScheme getBorderColorScheme(Component c) {
        JRootPane rp = c instanceof JRootPane
                ? (JRootPane) c
                : SwingUtilities.getRootPane(c);

        SubstanceColorScheme scheme = null;
        SubstanceSkin skin = SubstanceCoreUtilities.getSkin(c);
        if (skin != null) {
            Component titlePaneComp = SubstanceLookAndFeel
                    .getTitlePaneComponent(SwingUtilities.windowForComponent(c));
            scheme = skin.getColorScheme(titlePaneComp,
                    ColorSchemeAssociationKind.BORDER, ComponentState.ENABLED);

            if (!TranslucentRoundedCornersPaneBorder.isActivated(rp)) {
                scheme = new ToneColorScheme(scheme, TonedInternalFrameTitlePane.TONE_AMOUNT);
                scheme = new ShadeColorScheme(scheme, TonedInternalFrameTitlePane.SHADE_AMOUNT);
            }
        }
        return scheme;
    }

}

