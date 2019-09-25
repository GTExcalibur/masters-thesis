package gturner.crossword.substance;

import com.sun.awt.AWTUtilities;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.border.SubstancePaneBorder;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 4:15 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class TranslucentRoundedCornersPaneBorder extends SubstancePaneBorder {

    /**
     * Default border thickness.
     */
    private static final int BT = 4;
    private static final int BT2 = 8;

    boolean isRoundedCorners(Component c) {
        //TODO: check for:
        // - translucency not enabled
        // - frame is maximized
        try {
            return !AWTUtilities.isWindowOpaque(SwingUtilities.getWindowAncestor(c));
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            return false;
        }
    }

    public static boolean isActivated(JRootPane rp) {
        Component c = rp.getParent();
        if (c instanceof JInternalFrame) {
            return ((JInternalFrame)c).isSelected();
        } else if (c instanceof Window) {
            return ((Window)c).isActive();
        } else {
            return false;
        }
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        if (!isRoundedCorners(c)) {
            super.paintBorder(c, g, x, y, w, h);    //To change body of overridden methods use File | Settings | File Templates.
            return;
        }

        JRootPane rp = c instanceof JRootPane
                ? (JRootPane) c
                : SwingUtilities.getRootPane(c);

        SubstanceSkin skin = SubstanceCoreUtilities.getSkin(c);
        if (skin == null)
            return;

        SubstanceColorScheme scheme = skin
                .getBackgroundColorScheme(DecorationAreaType.PRIMARY_TITLE_PANE);
        Component titlePaneComp = SubstanceLookAndFeel
                .getTitlePaneComponent(SwingUtilities.windowForComponent(c));
//        SubstanceColorScheme borderScheme = skin.getColorScheme(titlePaneComp,
//                ColorSchemeAssociationKind.BORDER, isActivated(c) ? ComponentState.ENABLED : ComponentState.DISABLED_DEFAULT);

        Graphics2D graphics = (Graphics2D) g;

        int xl = x + BT + 1;
        int xr = x + w - BT - 2;
        int yt = y + BT + 1;
        int yb = y + h - BT - 2;


        if (!isActivated(rp)) {
            scheme = new AlphaColorScheme(scheme, 0.5);
        }
        Object rh = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // bottom and right in ultra dark
        graphics.setColor(scheme.getUltraDarkColor());
        graphics.drawLine(xl, y + h - 1, xr, y + h - 1); // bottom
        graphics.drawLine(x + w - 1, yt, x + w - 1, yb); // right
        // se
        graphics.drawArc(x+w-BT2-1,y+h-BT2-1,BT2,BT2, 270, 90);


        // top and left
        graphics.setColor(scheme.getDarkColor());
        graphics.drawLine(xl, y, xr, y);
        graphics.drawLine(x, yt, x, yb);
        // nw, ne, sw
        graphics.drawArc(0        ,0        ,BT2,BT2, 90, 90);
        graphics.drawArc(0        ,y+h-BT2-1,BT2,BT2, 180, 90);
        graphics.drawArc(x+w-BT2-1,0        ,BT2,BT2, 0, 90);



        // inner bottom and right
        graphics.setColor(scheme.getMidColor());
        graphics.drawLine(xl, y + h - 2, xr, y + h - 2);
        graphics.drawLine(x + w - 2, yt, x + w - 2, yb);
        graphics.drawLine(xl, y + 1, xr, y + 1);
        graphics.drawLine(x + 1, yt, x + 1, yb);

        graphics.drawArc(1        ,1        ,BT2-1,BT2-1, 90, 90);
        graphics.drawArc(1        ,y+h-BT2  ,BT2-2,BT2-2, 180, 90);
        graphics.drawArc(x+w-BT2  ,1        ,BT2-2,BT2-1, 0, 90);
        graphics.drawArc(x+w-BT2  ,y+h-BT2  ,BT2-2,BT2-2, 270, 90);



        graphics.setColor(scheme.getLightColor());
        graphics.drawLine(xl,        y + 2,     xr,        y + 2);
        graphics.drawLine(x + 2,     yt,        x + 2,     yb);
        graphics.drawLine(xl,        y + h - 3, xr,        y + h - 3);
        graphics.drawLine(x + w - 3, yt,        x + w - 3, yb);
        graphics.drawLine(xl,        y + 3,     xr,        y + 3);
        graphics.drawLine(x + 3,     yt,        x + 3,     yb);
        graphics.drawLine(xl,        y + h - 4, xr,        y + h - 4);
        graphics.drawLine(x + w - 4, yt,        x + w - 4, yb);

        graphics.drawArc(2        ,2        ,BT2-3,BT2-3, 90, 90);
        graphics.drawArc(3        ,3        ,BT2-5,BT2-5, 90, 90);
        graphics.fillArc(2        ,y+h-BT2  ,BT2-2,BT2-2, 180, 90);
        graphics.drawArc(x+w-BT2+1,2        ,BT2-4,BT2-3, 0, 90);
        graphics.drawArc(x+w-BT2+2,3        ,BT2-6,BT2-5, 0, 90);
        graphics.fillArc(x+w-BT2  ,y+h-BT2  ,BT2-2,BT2-2, 270, 90);


        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rh);
    }

}

