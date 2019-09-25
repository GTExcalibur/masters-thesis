package gturner.crossword.impl;

import gturner.crossword.spec.CWClue;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 7:50 AM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
class CWClueImpl implements CWClue {
    private final DIRECTION direction;
    private final int location;
    private final int length;
    private final String clueText;
    private final String clueAnswer;

    CWClueImpl(DIRECTION direction, int location, String clueText, String clueAnswer) {
        this.direction = direction;
        this.location = location;
        this.length = clueAnswer.length();
        this.clueText = clueText;
        this.clueAnswer = clueAnswer;
    }

    @Override
    public DIRECTION getDirection() {
        return direction;
    }

    @Override
    public int getLocation() {
        return location;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String getClueText() {
        return clueText;
    }

    String getClueAnswer() {
        return clueAnswer;
    }

    @Override
    public String toString() {
        return "CWClueImpl{" +
                "clueLoc='" + location + '\'' +
                ", clueText='" + clueText + '\'' +
                ", direction=" + direction +
                ", clueAnswer='" + clueAnswer + '\'' +
                '}';
    }
}
