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

package gturner.crossword.spec;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 7:44 AM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public interface CWPuzzle {

    public String getTitle();

    public String getDate();

    public boolean isSameStructure(CWPuzzle puzzle);

    public CWPuzzle copy();

    public Dimension getBoardSize();

    /**
     * @param x integer representing col
     * @param y integer representing row
     * @return BLACK_CELL_CHAR if cell cannot contain data, null char if the cell has
     * no data, else should return A-Z
     */
    public char getCellState(int x, int y);

    public void setCellState(int x, int y, char ch);

    public Boolean isValidValue(int x, int y);

    public boolean isClueSolved(CWClue clue);

    public boolean intersectsSolvedClue(CWClue clue);

    public String getClueState(CWClue clue);

    public void setClueState(CWClue clue, String state);

    public int filledLetters(CWClue clue);

    public java.util.List<CWClue> getIntersectingClues(CWClue clue);

    /**
     * @param x integer representing col
     * @param y integer representing row
     * @return Integer designating the clue for the cell, null if a notation should not appear in the cell
     */
    public Integer getCellNotation(int x, int y);

    public Point getClueLocation(int clue, CWClue.DIRECTION direction);

    public java.util.List<CWClue> getAllClues();

    public int getMissingLetters();

    /**
     * @return true if all data has been added, in other words no empty spaces
     */
    public boolean isComplete();

    public int getTotalLetters();

    public int getCorrectLetters();

    public float getCorrectPercentage();

    public float getEmptyPercentage();

    public boolean isSolvedAndCorrect();
}
