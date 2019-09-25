package gturner.expert;

import gturner.crossword.spec.CWClue;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 9:43 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IClueExpert {
    public Map<String, Long> performSearch(CWClue clue, String constraint);
}

