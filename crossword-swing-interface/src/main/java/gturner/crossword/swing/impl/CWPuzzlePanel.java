package gturner.crossword.swing.impl;

import gturner.crossword.impl.CWPuzzleFactory;
import gturner.crossword.spec.CWClue;
import gturner.crossword.spec.CWPuzzle;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.ComponentStateFacet;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.ui.SubstanceTableUI;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 11:13 AM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class CWPuzzlePanel extends JPanel {
    private CWPuzzle puzzle;

    private JTable boardArea;
    private CWPuzzleTableModelDecorator puzzleTableModel;

    public CWPuzzlePanel() {
        updatePanelFromPuzzle(CWPuzzleFactory.createFacadePuzzle(new Dimension(15, 15)));
    }

    public void updatePanelFromPuzzle(CWPuzzle puzzle) {
        if(this.puzzle != puzzle) {
            boolean isDifferent = this.puzzle == null || !this.puzzle.isSameStructure(puzzle);
            this.puzzle = puzzle.copy();

            if(isDifferent) {
                layoutComponents(puzzle);
            }
        }
    }

    public CWPuzzle getPuzzle() {
        return puzzle;
    }

    protected void layoutComponents(CWPuzzle puzzle) {
        // start with a fresh state
        removeAll();
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        constructBoardArea(puzzle, layout);
        constructClueArea(puzzle, layout);

        SpringLayout.Constraints thisConstraints = layout.getConstraints(this);
        thisConstraints.setConstraint(SpringLayout.EAST, Spring.sum(Spring.constant(5), constructSpringForComponents(
                Arrays.asList(getComponents()), layout, SpringLayout.EAST
        )));
        thisConstraints.setConstraint(SpringLayout.SOUTH, Spring.sum(Spring.constant(5), constructSpringForComponents(
                Arrays.asList(getComponents()), layout, SpringLayout.SOUTH
        )));
    }

    private static Spring constructSpringForComponents(java.util.List<Component> components, SpringLayout layout, String outerEdge) {
        int size = components.size();
        if(size == 0) return Spring.constant(0);

        Component firstChild = components.get(0);
        Spring constraint = layout.getConstraint(outerEdge, firstChild);
        if(size == 1) {
            return constraint;
        } else {
            return Spring.max(constraint, constructSpringForComponents(components.subList(1, size), layout, outerEdge));
        }
    }

    private void constructBoardArea(CWPuzzle puzzle, SpringLayout layout) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        puzzleTableModel = new CWPuzzleTableModelDecorator();
        boardArea = new JTable(puzzleTableModel) {
            @Override
            public void updateUI() {
                setUI(new DisabledRolloverTableUI());
            }
        };
        boardArea.setDefaultRenderer(Character.class, new CWTableCellRenderer());
        boardArea.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        boardArea.setCellSelectionEnabled(false);
        boardArea.setFocusable(false);
        boardArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        boardArea.setIntercellSpacing(new Dimension(0, 0));
        boardArea.setRowHeight(30);
        boardArea.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        boardArea.setRowSelectionAllowed(true);
        boardArea.setColumnSelectionAllowed(true);
        boardArea.setCellSelectionEnabled(true);
        Dimension staticSize = new Dimension(boardArea.getRowHeight() * puzzle.getBoardSize().width, boardArea.getRowHeight() * puzzle.getBoardSize().height);
        boardArea.setMinimumSize(staticSize);
        boardArea.setMaximumSize(staticSize);
        Enumeration<TableColumn> columns = boardArea.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn tableColumn = columns.nextElement();
            tableColumn.setPreferredWidth(boardArea.getRowHeight());
        }

        wrapper.add(boardArea);
        wrapper.add(Box.createVerticalGlue());

        layout.putConstraint(SpringLayout.WEST, wrapper,
                             5,
                             SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, wrapper,
                             5,
                             SpringLayout.NORTH, this);

        add(wrapper);
    }

    private void constructClueArea(final CWPuzzle puzzle, SpringLayout layout) {
        JPanel acrossPanel = new JPanel();
        acrossPanel.setLayout(new BoxLayout(acrossPanel, BoxLayout.Y_AXIS));
        acrossPanel.add(new JLabel("Across"));

        DefaultListModel acrossClueModel = new DefaultListModel();
        final JList acrossClueList = new JList(acrossClueModel);
        JScrollPane acrossScroll = new JScrollPane(acrossClueList);
        acrossPanel.add(acrossScroll);

        JPanel downPanel = new JPanel();
        downPanel.setLayout(new BoxLayout(downPanel, BoxLayout.Y_AXIS));
        downPanel.add(new JLabel("Down"));

        DefaultListModel downClueModel = new DefaultListModel();
        final JList downClueList = new JList(downClueModel);
        JScrollPane downScroll = new JScrollPane(downClueList);
        downPanel.add(downScroll);

        ListSelectionListener clueListener = new ListSelectionListener() {
            private final List<CWClue> selectedClues = new ArrayList<CWClue>();

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if(e.getSource() == acrossClueList) {
                        CWClue clue = (CWClue)acrossClueList.getModel().getElementAt(acrossClueList.getSelectedIndex());
                        boardArea.clearSelection();

                        Point clueLoc = puzzle.getClueLocation(clue.getLocation(), clue.getDirection());
                        boardArea.changeSelection(clueLoc.y, clueLoc.x, false, false);
                        boardArea.changeSelection(clueLoc.y, clueLoc.x+clue.getLength()-1, false, true);
                    } else {
                        CWClue clue = (CWClue)downClueList.getModel().getElementAt(downClueList.getSelectedIndex());
                        boardArea.clearSelection();

                        Point clueLoc = puzzle.getClueLocation(clue.getLocation(), clue.getDirection());
                        boardArea.changeSelection(clueLoc.y, clueLoc.x, false, false);
                        boardArea.changeSelection(clueLoc.y+clue.getLength()-1, clueLoc.x, false, true);
                    }
                }
            }
        };
        acrossClueList.addListSelectionListener(clueListener);
        downClueList.addListSelectionListener(clueListener);

        List<CWClue> allClues = puzzle.getAllClues();
        for (CWClue clue : allClues) {
            switch (clue.getDirection()) {
                case ACROSS:
                    acrossClueModel.addElement(new CWClueJListDecorator(clue));
                    break;
                case DOWN:
                    downClueModel.addElement(new CWClueJListDecorator(clue));
            }
        }
        Dimension boardPrefSize = boardArea.getPreferredSize();
        Dimension preferredSize = new Dimension(boardPrefSize.width / 2, boardPrefSize.height);
        acrossPanel.setPreferredSize(preferredSize);
        downPanel.setPreferredSize(preferredSize);

        layout.putConstraint(SpringLayout.NORTH, acrossPanel,
                0,
                SpringLayout.NORTH, boardArea.getParent());
        layout.putConstraint(SpringLayout.WEST, acrossPanel,
                5,
                SpringLayout.EAST, boardArea.getParent());

        layout.putConstraint(SpringLayout.NORTH, downPanel,
                0,
                SpringLayout.NORTH, acrossPanel);
        layout.putConstraint(SpringLayout.WEST, downPanel,
                5,
                SpringLayout.EAST, acrossPanel);


        add(acrossPanel);
        add(downPanel);
    }

    private class CWPuzzleTableModelDecorator extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return puzzle.getBoardSize().height;
        }

        @Override
        public int getColumnCount() {
            return puzzle.getBoardSize().width;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Character.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return puzzle.getCellState(columnIndex, rowIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException("Decorator cannot modify puzzle");
        }
    }

    private class CWTableCellRenderer extends SubstanceDefaultTableCellRenderer {
        final Character nullChar = '\0';
        final Character blackCell = CWPuzzleFactory.BLACK_CELL_CHAR;
        Integer cellNotation;

        CWTableCellRenderer() {
            setIcon(new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (cellNotation != null) {
                        g.setColor(Color.BLACK);
                        g.setFont(getFont().deriveFont(9f));
                        g.drawString(Integer.toString(cellNotation), 2, 10);
                    }
                }

                @Override
                public int getIconWidth() {
                    return 0;
                }

                @Override
                public int getIconHeight() {
                    return 0;
                }
            });
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setHorizontalAlignment(JLabel.CENTER);
            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
            setFont(getFont().deriveFont(Font.BOLD, 19f));

            if (blackCell.equals(value)) {
                setText("");
                setBackground(Color.BLACK);
            } else {
                if (nullChar.equals(value)) {
                    setText("");
                }
                Boolean valid = puzzle.isValidValue(column, row);
                if(valid == null) {
                    setForeground(Color.YELLOW);
                } else if (!valid) {
                    setForeground(Color.RED);
                }
            }
            cellNotation = puzzle.getCellNotation(column, row);

            return this;
        }
    }
}

class CellSelectionManager implements ListSelectionListener {
    @Override
    public void valueChanged(ListSelectionEvent e) {

    }
}

class DisabledRolloverTableUI extends SubstanceTableUI {
    final Character blackCell = CWPuzzleFactory.BLACK_CELL_CHAR;

    DisabledRolloverTableUI() {
        super();
    }

    @Override
    protected void paintCell(Graphics g, Rectangle cellRect, Rectangle highlightCellRect, int row, int column) {
        Object value = table.getValueAt(row, column);
        if(blackCell.equals(value))
            rolledOverIndices.remove(new TableCellId(row, column));

        super.paintCell(g, cellRect, highlightCellRect, row, column);
    }

    @Override
    protected void paintGrid(Graphics g, int rMin, int rMax, int cMin, int cMax) {
        super.paintGrid(g, rMin, rMax, cMin, cMax);

        g.setColor(Color.BLACK);

        for(int row = rMin; row <= rMax && row < table.getRowCount(); row++) {
            for(int col = cMin; col <= cMax && col < table.getColumnCount(); col++) {
                Object value = table.getValueAt(row, col);
                if(blackCell.equals(value)) {
                    boolean hasNextCol = col + 1 < table.getColumnCount() &&
                            blackCell.equals(table.getValueAt(row, col+1));
                    if(hasNextCol) {
                        Rectangle cellRect = table.getCellRect(row, col, true);
                        g.fillRect(
                                cellRect.x + cellRect.width - 2, cellRect.y,
                                3, cellRect.height - 1
                        );
                    }
                    boolean hasNextRow = row + 1 < table.getRowCount() &&
                            blackCell.equals(table.getValueAt(row+1, col));
                    if(hasNextRow) {
                        Rectangle cellRect = table.getCellRect(row, col, true);
                        g.fillRect(
                                cellRect.x, cellRect.y + cellRect.height - 2,
                                cellRect.width - 1, 3
                        );
                    }
                    if(hasNextCol && hasNextRow && blackCell.equals(table.getValueAt(row+1, col+1))) {
                        Rectangle cellRect = table.getCellRect(row, col, true);
                        g.drawLine(
                                cellRect.x + cellRect.width - 2, cellRect.y + cellRect.height - 1,
                                cellRect.x + cellRect.width - 1, cellRect.y + cellRect.height - 1
                        );
                    }
                }
            }
        }
    }

    @Override
    public StateTransitionTracker.ModelStateInfo getModelStateInfo(TableCellId cellId) {
        Object value = getValueAt(cellId);
        if(blackCell.equals(value)) {
            return null;
        }

        return super.getModelStateInfo(cellId);
    }

    private Object getValueAt(TableCellId cellId) {
        int row = (Integer) InvokerHelper.getAttribute(cellId, "row");
        int column = (Integer)InvokerHelper.getAttribute(cellId, "column");

        return table.getValueAt(row, column);
    }

    @Override
    public ComponentState getCellState(TableCellId cellIndex) {
        ComponentState cellState = super.getCellState(cellIndex);

        Object value = getValueAt(cellIndex);
        if(blackCell.equals(value)) {
            return ComponentState.getState(false, false,
                    cellState.isFacetActive(ComponentStateFacet.SELECTION));
        }

        return cellState;
    }
}

class CWClueJListDecorator implements CWClue {
    private final CWClue clue;

    CWClueJListDecorator(CWClue clue) {
        this.clue = clue;
    }

    @Override
    public DIRECTION getDirection() {
        return clue.getDirection();
    }

    @Override
    public int getLength() {
        return clue.getLength();
    }

    @Override
    public int getLocation() {
        return clue.getLocation();
    }

    @Override
    public String getClueText() {
        return clue.getClueText();
    }

    @Override
    public String toString() {
        return clue.getLocation() + ") " + clue.getClueText();
    }
}
