package gturner.crossword.substance;

import com.sun.awt.AWTUtilities;
import org.pushingpixels.substance.internal.ui.SubstanceRootPaneUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 4:12 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class TonedRoundedRootPaneUI extends SubstanceRootPaneUI {

    /**
     * Creates a UI for a <code>JRootPane</code>.
     *
     * @param comp the JRootPane the RootPaneUI will be created for
     * @return the RootPaneUI implementation for the passed in JRootPane
     */
    public static ComponentUI createUI(JComponent comp) {
        SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new TonedRoundedRootPaneUI();
    }

    /**
     * Invokes supers implementation of <code>installUI</code> to install the
     * necessary state onto the passed in <code>JRootPane</code> to render the
     * metal look and feel implementation of <code>RootPaneUI</code>. If the
     * <code>windowDecorationStyle</code> property of the <code>JRootPane</code>
     * is other than <code>JRootPane.NONE</code>, this will add a custom
     * <code>Component</code> to render the widgets to <code>JRootPane</code>,
     * as well as installing a custom <code>Border</code> and
     * <code>LayoutManager</code> on the <code>JRootPane</code>.
     *
     * @param c the JRootPane to install state onto
     */
    @Override
    public void installUI(final JComponent c) {
        c.addHierarchyListener(RESIZE_LOADER);
        super.installUI(c);
    }

    public static ComponentListener WINDOW_ROUNDER = new ComponentAdapter() {

        @Override
        public void componentResized(ComponentEvent e) {
            if (e.getComponent() instanceof Window) {
                Window w = (Window) e.getComponent();
                if ((w instanceof RootPaneContainer)
                    && (((RootPaneContainer)w).getRootPane().getWindowDecorationStyle() == JRootPane.NONE))
                {
                    // special case, mostly for the splash
                    return;
                }
                AWTUtilities.setWindowShape(w, new RoundRectangle2D.Double(0, 0, w.getWidth(), w.getHeight(), 12, 12));
            }
        }
    };

    // When a JRootPane is created, it hasn't been added to a window yet.
    // when we are added, set the window to transparent if we are a JDialog or JFrame
    static HierarchyListener RESIZE_LOADER = new HierarchyListener() {
        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            // if we are not talking about a rootpane being added to a window, quit and never try again
            if (!((e.getChangedParent() instanceof Dialog) || (e.getChangedParent() instanceof Frame))) {
                e.getChanged().removeHierarchyListener(this);
                return;
            }
            // if we are something other than a change event, quit
            if ((e.getID() != HierarchyEvent.HIERARCHY_CHANGED)
                || !(e.getChanged() instanceof JRootPane))
            {
                return;
            }
            Window w = (Window) e.getChangedParent();
            if (w != null) {
                if (!Arrays.asList(w.getComponentListeners()).contains(WINDOW_ROUNDER)) {
                    w.addComponentListener(WINDOW_ROUNDER);
                }
                e.getChanged().removeHierarchyListener(this);
            }
        }
    };

    @Override
    protected JComponent createTitlePane(JRootPane root) {
        return new TonedTitlePane(root, this);
    }
}

