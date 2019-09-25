package gturner.crossword.swing.impl;

import gturner.crossword.impl.CWPuzzleFactory;
import gturner.crossword.spec.CWPuzzle;
import gturner.crossword.substance.MistAquaTonedSkin;
import gturner.crossword.substance.SubstanceSkinLookAndFeel;
import gturner.crossword.util.ConfigurationException;
import gturner.solver.impl.IPuzzleSolver;
import gturner.solver.impl.PuzzleSolver;
import org.apache.tools.ant.util.TeeOutputStream;
import org.divxdede.swing.busy.BusyModel;
import org.divxdede.swing.busy.JBusyComponent;
import org.divxdede.swing.busy.ui.BasicBusyLayerUI;
import org.divxdede.swing.busy.ui.BusyLayerUI;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.internal.utils.SubstanceColorUtilities;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/25/11, Time: 11:18 PM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class MainFrame extends JFrame {
    static Class<? extends MainFrame> frameToConstruct = MainFrame.class;

    private static ImageIcon open_file_image;
    private static ImageIcon training_image;
    private static ImageIcon solve_image;

    private JBusyComponent<CWPuzzlePanel> busyComponent;
    private JFileChooser cachedChooser;
    private JToolBar rootToolBar;
    private JToolBar statusBar;

    private final IPuzzleSolver puzzleSolver;

    public MainFrame() throws HeadlessException, IOException {
        super("Crossword Puzzle Solver");

        System.setOut(new PrintStream(new TeeOutputStream(System.out, new FileOutputStream("C:\\mainFrame.out"))));


        this.puzzleSolver = new ClassPathXmlApplicationContext("expert-layout.xml").getBean("solver.puzzleSolver.activeInstance", IPuzzleSolver.class);
    }

    /*protected AbstractExpertManager registerExpertManager() {
        return null; //TODO
    }*/

    protected void performLayout() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        rootToolBar = new JToolBar();
        rootToolBar.setFloatable(false);
        rootToolBar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, SubstanceColorUtilities.getTopFillColor(SubstanceLookAndFeel.getCurrentSkin().getWatermarkColorScheme())));
        contentPane.add(rootToolBar, BorderLayout.NORTH);

        statusBar = new JToolBar();
        statusBar.setFloatable(false);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, SubstanceColorUtilities.getTopFillColor(SubstanceLookAndFeel.getCurrentSkin().getWatermarkColorScheme())));
        contentPane.add(statusBar, BorderLayout.SOUTH);

        JButton openButton = new JButton(open_file_image);
        openButton.setFocusable(false);
        openButton.setToolTipText("Open a Crossword puzzle file");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performOpenAction();
            }
        });
        rootToolBar.add(Box.createHorizontalStrut(10));
        rootToolBar.add(openButton);

        JButton trainingButton = new JButton(training_image);
        trainingButton.setFocusable(false);
        trainingButton.setToolTipText("Perform expert training");
        trainingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performTrainingAction();
            }
        });
        rootToolBar.add(Box.createHorizontalStrut(10));
        rootToolBar.add(trainingButton);

        JButton solveButton = new JButton(solve_image);
        solveButton.setFocusable(false);
        solveButton.setToolTipText("Start solving agent");
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSolvingAction();
            }
        });
        rootToolBar.add(Box.createHorizontalStrut(10));
        rootToolBar.add(solveButton);

        busyComponent = new JBusyComponent<CWPuzzlePanel>(new CWPuzzlePanel());
        contentPane.add(busyComponent);
    }

    protected void performOpenAction() {
        if (cachedChooser == null) {
            cachedChooser = new JFileChooser();
            cachedChooser.addChoosableFileFilter(new PuzzleFileFilter(CWPuzzleFactory.FILE_TYPES.XPUF_EXT));
            cachedChooser.addChoosableFileFilter(new PuzzleFileFilter(CWPuzzleFactory.FILE_TYPES.PUZ_EXT));
        }
        initializeBusyState(1);
        int returnVal = cachedChooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            SwingWorker<CWPuzzle, Void> worker = new SwingWorker<CWPuzzle, Void>() {
                @Override
                protected CWPuzzle doInBackground() throws Exception {
                    File selectedFile = cachedChooser.getSelectedFile();
                    CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(
                            new FileInputStream(selectedFile), CWPuzzleFactory.FILE_TYPES.getFileType(selectedFile.getName()));
                    publish();
                    return cwPuzzle;
                }

                @Override
                protected void process(List<Void> chunks) {
                    incrementTaskCompletion();
                }

                @Override
                protected void done() {
                    try {
                        statusBar.removeAll();
                        statusBar.add(Box.createHorizontalStrut(5));
                        statusBar.add(new JLabel("Title: " + get().getTitle()));

                        busyComponent.getView().updatePanelFromPuzzle(get());
                    } catch (InterruptedException ignore) {
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    MainFrame.this.pack();
                    finishBusyState();
                }
            };
            worker.execute();
        } else {
            finishBusyState();
        }
    }

    protected void performTrainingAction() {
        /*final List<AbstractCrosswordExpert> experts = AbstractExpertManager.getExperts();
        initializeBusyState(100);

        SwingWorker<Void, Runnable> worker = new SwingWorker<Void, Runnable>() {
            @Override
            protected Void doInBackground() throws Exception {
                AbstractExpertManager.performTraining(experts, 5, new AbstractExpertManager.INotificationHandler(){
                    @Override
                    public void notifyUIStatus(final int percentage) {
                        publish(new Runnable() {
                            @Override
                            public void run() {
                                BusyModel model = busyComponent.getBusyModel();
                                model.setValue(percentage);
                            }
                        });
                    }
                });
                return null;
            }

            @Override
            protected void process(List<Runnable> chunks) {
                chunks.get(chunks.size() - 1).run();
            }

            @Override
            protected void done() {
                finishBusyState();
            }
        };
        worker.execute();*/
    }

    protected void performSolvingAction() {
        busyComponent.setBusyLayerUI(createBasicBusyLayerUI(busyComponent.getView()));
        BusyModel model = busyComponent.getBusyModel();
        model.setDeterminate(false);
        busyComponent.setBusy(true);
        for (Component component : rootToolBar.getComponents()) {
            component.setEnabled(false);
        }

        SwingWorker<Void, CWPuzzle> worker = new SwingWorker<Void, CWPuzzle>() {
            @Override
            protected Void doInBackground() throws Exception {
                puzzleSolver.solvePuzzle(busyComponent.getView().getPuzzle(), new IPuzzleSolver.PuzzleListener() {
                    @Override
                    public void currentPuzzle(CWPuzzle puzzle) {
                        publish(puzzle);
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ignore) {
                        }
                    }
                });

                return null;
            }

            @Override
            protected void process(List<CWPuzzle> chunks) {
                busyComponent.getView().updatePanelFromPuzzle(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                finishBusyState();
            }
        };
        worker.execute();
    }

    protected void initializeBusyState(int taskCount) {
        busyComponent.setBusyLayerUI(createBasicBusyLayerUI(busyComponent.getView()));
        BusyModel model = busyComponent.getBusyModel();
        model.setDeterminate(true);
        model.setMinimum(0);
        model.setMaximum(taskCount);
        model.setValue(0);

        busyComponent.setBusy(true);
        for (Component component : rootToolBar.getComponents()) {
            component.setEnabled(false);
        }
    }

    protected void incrementTaskCompletion() {
        BusyModel model = busyComponent.getBusyModel();
        model.setValue(model.getValue() + 1);
    }

    protected void finishBusyState() {
        // wait one second before closing the busy state
        Timer timer = new Timer(750, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer)e.getSource()).stop();
                busyComponent.setBusy(false);
                for (Component component : rootToolBar.getComponents()) {
                    component.setEnabled(true);
                }
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        try {
            open_file_image = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("open_32.png")));
            training_image = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("training_32.png")));
            solve_image = new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("solve_32.png")));
        } catch (IOException e) {
            throw new ConfigurationException("Unable to load images", e);
        }

        initializeSubstanceLookAndFeel();

        final MainFrame frame = constructInDispatchThread(frameToConstruct);

        callInDispatchThread(new Callable<Void>(){
            public Void call() throws Exception {
                frame.performLayout();

                return null;
            }
        });

        final java.util.List<Runnable> initTasks = new ArrayList<Runnable>();
        /*initTasks.add(new Runnable() {
            @Override
            public void run() {
                frame.registerExpertManager();
            }
        });*/

        callInDispatchThread(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                frame.initializeBusyState(initTasks.size());
                frame.pack();
                frame.setLocation(200, 200);
                frame.setVisible(true);

                return null;
            }
        });

        SwingWorker<Void, Runnable> initWorker = new SwingWorker<Void, Runnable>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (Runnable initTask : initTasks) {
                    initTask.run();
                    publish(initTask);
                }

                return null;
            }

            @Override
            protected void process(java.util.List<Runnable> chunks) {
                for (Runnable task : chunks) {
                    frame.incrementTaskCompletion();
                }
            }

            @Override
            protected void done() {
                frame.finishBusyState();
            }
        };
        initWorker.execute();
    }

    protected static void initializeSubstanceLookAndFeel() {
        callInDispatchThread(new Callable<Void>() {
            public Void call() throws Exception {
                UIManager.setLookAndFeel(new SubstanceSkinLookAndFeel(new MistAquaTonedSkin()));

                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);

                return null;
            }
        });
    }

    protected static <T> T constructInDispatchThread(final Class<T> clz) {
        return callInDispatchThread(new Callable<T>() {
            public T call() throws Exception {
                return clz.newInstance();
            }
        });
    }

    protected static <T> T callInDispatchThread(Callable<T> callable) {
        if(SwingUtilities.isEventDispatchThread()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            FutureTask<T> future = new FutureTask<T>(callable);
            SwingUtilities.invokeLater(future);
            return future.get();
        } catch (InterruptedException ignore) {
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class PuzzleFileFilter extends FileFilter {
        private final CWPuzzleFactory.FILE_TYPES type;

        private PuzzleFileFilter(CWPuzzleFactory.FILE_TYPES type) {
            this.type = type;
        }

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(type.getExtension());
        }

        @Override
        public String getDescription() {
            return type.getDescription();
        }
    }

    /** the shading delay in milliseconds for rendering the <code>busy</code> state change */
    private static final int SHADE_DELAY = 0; //no shading
    /** Frames per Second to use for rendering the shading animation */
    private static final int SHADE_FPS = 0;
    /** the alpha ratio to use for the veil when the model is <code>busy</code> */
    private static final float VEIL_ALPHA = 0.7f;
    public static BusyLayerUI createBasicBusyLayerUI(final JComponent view) {
        return callInDispatchThread(new Callable<BusyLayerUI>() {
            @Override
            public BusyLayerUI call() throws Exception {
                Color veilColor = SubstanceColorUtilities.getBackgroundFillColor(view);
                return new BasicBusyLayerUI(SHADE_DELAY, SHADE_FPS, VEIL_ALPHA, veilColor);
            }
        });
    }
}
