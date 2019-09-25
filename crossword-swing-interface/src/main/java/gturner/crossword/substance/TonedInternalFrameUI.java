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

import org.pushingpixels.substance.internal.ui.SubstanceInternalFrameUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 4:18 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class TonedInternalFrameUI extends SubstanceInternalFrameUI {

    /**
     * Creates a UI for a <code>JInternalFrame</code>.
     *
     * @param comp
     *            the JInteralFrame the InternalFrameUI will be created for
     * @return the InternalFrameUI implementation for the passed in JRootPane
     */
    public static ComponentUI createUI(JComponent comp) {
        SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new TonedInternalFrameUI((JInternalFrame) comp);
    }

    public TonedInternalFrameUI(JInternalFrame b) {
        super(b);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.swing.plaf.basic.BasicInternalFrameUI#createNorthPane(javax.swing
     * .JInternalFrame)
     */
    @Override
    protected JComponent createNorthPane(JInternalFrame w) {
        this.titlePane = new TonedInternalFrameTitlePane(w);

        // f.putClientProperty(INTERNAL_FRAME_PINNED, Boolean.TRUE);

        return this.titlePane;
    }


}
