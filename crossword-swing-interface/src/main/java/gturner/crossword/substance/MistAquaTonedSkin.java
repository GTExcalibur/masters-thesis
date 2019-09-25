/*
 * Copyright (c) 2008 Intelligent Software Solutions
 * Unpublished-all rights reserved under the copyright laws of the
 * United States.
 *
 * This software was developed under sponsorship from the
 * Air Force Research Laboratory under FA8750-06-D-005.
 *
 * Contractor: Intelligent Software Solutions,
 * 5450 Tech Center Drive, Suite 400, Colorado Springs, 80919.
 * http://www.issinc.com
 * Expiration Date: June 2015
 *
 * Intelligent Software Solutions has title to the rights in this computer
 * software. The Government's rights to use, modify, reproduce, release,
 * perform, display, or disclose these technical data are restricted by
 * paragraph (b)(2) of the Rights in Technical Data-Noncommercial Items
 * clause contained in Contract No. FA8750-06-D-005. No restrictions to the
 * Government apply after the expiration date shown above. Any
 * reproduction of technical data or portions thereof marked with this
 * legend must also reproduce the markings.
 *
 * Intelligent Software Solutions does not grant permission inconsistent with
 * the aforementioned unlimited government rights to use, disclose, copy,
 * or make derivative works of this software to parties outside the
 * Government.
 */

package gturner.crossword.substance;

import org.pushingpixels.substance.api.skin.MistAquaSkin;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 4:08 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class MistAquaTonedSkin extends MistAquaSkin {

    /**
     * Display name for <code>this</code> skin.
     */
    public static final String NAME = "Mist Aqua Glass";

    public MistAquaTonedSkin() {
        super();

        UIManager.put("RootPane.border", new TonedRoundedCornersPaneBorder());
        UIManager.put("InternalFrame.border", new TonedRoundedCornersPaneBorder());
        UIManager.put("RootPaneUI", TonedRoundedRootPaneUI.class.getName());
        UIManager.put("InternalFrameUI", TonedInternalFrameUI.class.getName());

        this.decorationPainter = new TonedMatteDecorationPainter();
    }

    @Override
    public String getDisplayName() {
        return NAME;
    }
}
