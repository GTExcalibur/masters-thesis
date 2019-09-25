package gturner.crossword.substance;

import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.painter.SubstancePainterUtils;
import org.pushingpixels.substance.api.painter.decoration.MatteDecorationPainter;
import org.pushingpixels.substance.internal.colorscheme.ShadeColorScheme;
import org.pushingpixels.substance.internal.colorscheme.ToneColorScheme;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 4:11 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class TonedMatteDecorationPainter extends MatteDecorationPainter {


    /*
     * (non-Javadoc)
     *
     *
     *
     *
     *
     * @seeorg.pushingpixels.substance.painter.decoration.SubstanceDecorationPainter
     * # paintDecorationArea(java.awt.Graphics2D, java.awt.Component,
     * org.pushingpixels.substance.painter.decoration.DecorationAreaType, int,
     * int, org.pushingpixels.substance.api.SubstanceSkin)
     */
    @Override
    public void paintDecorationArea(Graphics2D graphics, Component comp,
            DecorationAreaType decorationAreaType, int width, int height,
            SubstanceSkin skin) {
        if ((decorationAreaType == DecorationAreaType.PRIMARY_TITLE_PANE)
                || (decorationAreaType == DecorationAreaType.SECONDARY_TITLE_PANE)) {
            this.paintTitleBackground(graphics, comp, width, height, skin
                    .getBackgroundColorScheme(decorationAreaType));
        } else {
            this.paintExtraBackground(graphics, comp, width, height, skin
                    .getBackgroundColorScheme(decorationAreaType));
        }
    }

    /**
     * Paints the title background.
     *
     * @param graphics
     *            Graphics context.
     * @param comp
     *            Component.
     * @param width
     *            Width.
     * @param height
     *            Height.
     * @param scheme
     *            Color scheme for painting the title background.
     */
    private void paintTitleBackground(Graphics2D graphics, Component comp,
            int width, int height, SubstanceColorScheme scheme) {
        if (!TranslucentRoundedCornersPaneBorder.isActivated(((JComponent)comp).getRootPane())) {
            scheme = new ToneColorScheme(scheme, TonedInternalFrameTitlePane.TONE_AMOUNT);
            scheme = new ShadeColorScheme(scheme, TonedInternalFrameTitlePane.SHADE_AMOUNT);
        }
        Graphics2D temp = (Graphics2D) graphics.create();
        this.fill(temp, comp, scheme, 0, 0, 0, width, height);
        temp.dispose();
    }

    /**
     * Paints the background of non-title decoration areas.
     *
     * @param graphics
     *            Graphics context.
     * @param parent
     *            Component ancestor for computing the correct offset of the
     *            background painting.
     * @param comp
     *            Component.
     * @param width
     *            Width.
     * @param height
     *            Height.
     * @param scheme
     *            Color scheme for painting the title background.
     */
    private void paintExtraBackground(Graphics2D graphics, Component comp,
            int width, int height, SubstanceColorScheme scheme) {

        Point offset = SubstancePainterUtils.getOffsetInRootPaneCoords(comp);
        Graphics2D temp = (Graphics2D) graphics.create();
        this.fill(temp, comp, scheme, offset.y, 0, 0, width, height);
        temp.dispose();
    }

}

