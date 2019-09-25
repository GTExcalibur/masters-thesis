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

package gturner.crossword.impl;

import gturner.crossword.spec.CWClue;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 1:21 PM
 * <br> A data structure to hold clues for CWPuzzleImpl.
 *
 * @author George.Turner
 */
class ClueLookupMap {
    private final java.util.List<CWClue> allClues = new ArrayList<CWClue>();

    private final Map<Point, CWClue> acrossStore = new HashMap<Point, CWClue>();
    private final Map<Point, CWClue> verticalStore = new HashMap<Point, CWClue>();

    private final Map<Point, Integer> clueNotations = new HashMap<Point, Integer>();

    private final CWClue[][] acrossLookup;
    private final CWClue[][] downLookup;

    ClueLookupMap(int boardWidth, int boardHeight) {
        acrossLookup = new CWClue[boardWidth][boardHeight];
        downLookup = new CWClue[boardWidth][boardHeight];
    }

    void addClueNotation(Point pt, int value) {
        clueNotations.put(pt, value);
    }

    public Integer getClueNotation(int x, int y) {
        return clueNotations.get(new Point(x, y));
    }

    void addClue(Point pt, CWClue clue) {
        switch (clue.getDirection()) {
            case ACROSS:
                acrossStore.put(pt, clue);
                for(int i = pt.x; i < pt.x+clue.getLength(); i++) {
                    acrossLookup[i][pt.y] = clue;
                }
                break;
            case DOWN:
                verticalStore.put(pt, clue);
                for(int i = pt.y; i < pt.y+clue.getLength(); i++) {
                    downLookup[pt.x][i] = clue;
                }
                break;
        }
        allClues.add(clue);
    }

    public java.util.List<CWClue> getIntersectingClues(CWClue clue) {
        Point location = getClueLocation(clue.getLocation(), clue.getDirection());
        ArrayList<CWClue> intersections = new ArrayList<CWClue>();
        switch (clue.getDirection()) {
            case ACROSS:
                for(int i = location.x; i < location.x+clue.getLength(); i++) {
                    intersections.add(downLookup[i][location.y]);
                }
                break;
            case DOWN:
                for(int i = location.y; i < location.y+clue.getLength(); i++) {
                    intersections.add(acrossLookup[location.x][i]);
                }
                break;
        }
        return intersections;
    }

    Point getClueLocation(int clue, CWClue.DIRECTION direction) {
        switch (direction) {
            case ACROSS:
                for (Map.Entry<Point, CWClue> entry : acrossStore.entrySet()) {
                    if(entry.getValue().getLocation() == clue) return entry.getKey();
                }
                break;
            case DOWN:
                for (Map.Entry<Point, CWClue> entry : verticalStore.entrySet()) {
                    if(entry.getValue().getLocation() == clue) return entry.getKey();
                }
                break;
        }
        throw new IllegalArgumentException("Unknown clue/direction combination");
    }

    public java.util.List<CWClue> getAllClues() {
        return allClues;
    }
}
