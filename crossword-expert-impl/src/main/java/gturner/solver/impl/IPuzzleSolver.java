package gturner.solver.impl;

import gturner.crossword.spec.CWPuzzle;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 10/11/14
 * Time: 7:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IPuzzleSolver {
    public static interface PuzzleListener {
        public void currentPuzzle(CWPuzzle puzzle);
    }

    public static final class NoOpPuzzleListener implements PuzzleListener {
        @Override
        public void currentPuzzle(CWPuzzle puzzle) {
            // no-op
        }
    }

    public CWPuzzle solvePuzzle(final CWPuzzle startPuzzle, final PuzzleListener puzzleListener);
}
