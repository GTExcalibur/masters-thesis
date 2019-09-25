package gturner.solver.impl;

import gturner.crossword.spec.CWClue;
import gturner.crossword.spec.CWPuzzle;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */
class WeightedPuzzle implements Comparable<WeightedPuzzle> {

//    private final WeightedPuzzle parent;
    private final CWPuzzle puzzleState;
    private final Long puzzleScore;
    private final int cluesSolved;
    private final int assignedDiscrepancyCount;
    private int discrepancyCount;
    private final boolean isRoot;

    public static WeightedPuzzle initialize(CWPuzzle puzzleState) {
        return new WeightedPuzzle(null, puzzleState, 0l, 0, 0, true);
    }

    public WeightedPuzzle performUpdate(CWClue clue, String state, boolean isBestGuess, Long score) {
        if(isRoot) {
            CWPuzzle copy = puzzleState.copy();
            copy.setClueState(clue, state);
            return new WeightedPuzzle(this, copy, puzzleScore + score, cluesSolved+1, discrepancyCount++, !isBestGuess);
        } else if(isBestGuess) {
            CWPuzzle copy = puzzleState.copy();
            copy.setClueState(clue, state);
            return new WeightedPuzzle(this, copy, puzzleScore + score, cluesSolved+1, discrepancyCount, false);
        } else {
            return null;
        }
    }

    private WeightedPuzzle(WeightedPuzzle parent, CWPuzzle puzzleState, Long puzzleScore, int cluesSolved, int discrepancyCount, boolean isRoot) {
//        this.parent = parent;
        this.puzzleState = puzzleState;
        this.puzzleScore = puzzleScore;
        this.cluesSolved = cluesSolved;
        this.assignedDiscrepancyCount = discrepancyCount;
        this.discrepancyCount = discrepancyCount;
        this.isRoot = isRoot;
    }

    public CWPuzzle getPuzzleState() {
        return puzzleState;
    }

    public int getDiscrepancyCount() {
        return discrepancyCount;
    }

    public int getCluesSolved() {
        return cluesSolved;
    }

    @Override
    public int compareTo(WeightedPuzzle o) {
        if(discrepancyCount == o.discrepancyCount) {
            if(cluesSolved == o.cluesSolved) {
                int compare = o.puzzleScore.compareTo(puzzleScore);
                if(compare == 0) {
                    // we're always less, so there are no collisions
                    return o == this ? 0 : -1;
                } else {
                    return compare;
                }
            } else {
                return o.cluesSolved - cluesSolved;
            }
        } else {
            return discrepancyCount - o.discrepancyCount;
        }
    }

    public boolean betterFit(WeightedPuzzle o) {
        if(o == null) {
            return true;
        }

        if(discrepancyCount == o.discrepancyCount) {
            if(cluesSolved == o.cluesSolved) {
                // go with the one we already have
                return false;
            } else {
                return cluesSolved > o.cluesSolved;
            }
        } else {
            return discrepancyCount < o.discrepancyCount;
        }
    }
}
