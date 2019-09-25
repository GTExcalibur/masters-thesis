package gturner.crossword.impl;

import gturner.crossword.spec.CWClue;
import gturner.crossword.spec.CWPuzzle;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 7:39 AM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
class CWPuzzleImpl implements CWPuzzle {
    private final Dimension size;
    private final char[][] board;
    private final ClueLookupMap lookupMap;

    private Map<String, Object> metadataMap = new HashMap<String, Object>();

    CWPuzzleImpl(Map<String, Object> metadataMap, ClueLookupMap lookupMap) {
        this(new Dimension((Integer)metadataMap.get("boardWidth"), (Integer)metadataMap.get("boardHeight")),
                (List<Point>)metadataMap.get("blackCells"), lookupMap);
        this.metadataMap = metadataMap;
    }

    CWPuzzleImpl(Dimension size, List<Point> blackCells, ClueLookupMap lookupMap) {
        this(size, new char[size.width][size.height], lookupMap);

        for (Point blackCell : blackCells) {
            board[blackCell.x][blackCell.y] = CWPuzzleFactory.BLACK_CELL_CHAR;
        }
    }

    private CWPuzzleImpl(Dimension size, char[][] board, ClueLookupMap lookupMap) {
        this.size = size;
        this.board = board;
        this.lookupMap = lookupMap;
    }

    @Override
    public CWPuzzle copy() {
        CWPuzzleImpl copy = new CWPuzzleImpl(size, new char[size.width][size.height], lookupMap);
        for(int i = 0; i < size.width; i++) {
            copy.board[i] = Arrays.copyOf(board[i], size.height);
        }
        copy.metadataMap = metadataMap;
        return copy;
    }

    @Override
    public boolean isSameStructure(CWPuzzle puzzle) {
        return puzzle instanceof CWPuzzleImpl && ((CWPuzzleImpl)puzzle).metadataMap == this.metadataMap;
    }

    @Override
    public String getTitle() {
        String title = (String)metadataMap.get("title");
        if(title != null) return title;
        return "Unknown";
    }

    @Override
    public String getDate() {
        String title = (String)metadataMap.get("date");
        if(title != null) return title;
        return "Unknown";
    }

    @Override
    public Dimension getBoardSize() {
        return size;
    }

    @Override
    public char getCellState(int x, int y) {
        return board[x][y];
//        return ((char[][])metadataMap.get("answers"))[x][y];
    }

    @Override
    public void setCellState(int x, int y, char ch) {
        board[x][y] = ch;
    }

    @Override
    public Boolean isValidValue(int x, int y) {
        char value = board[x][y];
        if(value == '\0' || value == CWPuzzleFactory.BLACK_CELL_CHAR) return null;

        Object answers = metadataMap.get("answers");
        if(answers != null) {
            return value == ((char[][])metadataMap.get("answers"))[x][y];
        }

        return null;
    }

    @Override
    public boolean isClueSolved(CWClue clue) {
        CWClue.DIRECTION direction = clue.getDirection();
        Point location = getClueLocation(clue.getLocation(), direction);
        int length = clue.getLength();

        switch (direction) {
            case ACROSS:
                for(int i = location.x; i < location.x+length; i++) {
                    if(board[i][location.y] == '\u0000') return false;
                }
                break;
            case DOWN:
                for(int i = location.y; i < location.y+length; i++) {
                    if(board[location.x][i] == '\u0000') return false;
                }
                break;
        }
        return true;
    }

    @Override
    public boolean intersectsSolvedClue(CWClue clue) {
        CWClue.DIRECTION direction = clue.getDirection();
        Point location = getClueLocation(clue.getLocation(), direction);
        int length = clue.getLength();

        switch (direction) {
            case ACROSS:
                for(int i = location.x; i < location.x+length; i++) {
                    if(board[i][location.y] != '\u0000') return true;
                }
                break;
            case DOWN:
                for(int i = location.y; i < location.y+length; i++) {
                    if(board[location.x][i] != '\u0000') return true;
                }
                break;
        }
        return false;
    }

    @Override
    public String getClueState(CWClue clue) {
        CWClue.DIRECTION direction = clue.getDirection();
        Point location = getClueLocation(clue.getLocation(), direction);
        int length = clue.getLength();

        StringBuilder sb = new StringBuilder();
        switch (direction) {
            case ACROSS:
                for(int i = location.x; i < location.x+length; i++) {
                    sb.append(board[i][location.y]);
                }
                break;
            case DOWN:
                for(int i = location.y; i < location.y+length; i++) {
                    sb.append(board[location.x][i]);
                }
                break;
        }
        return sb.toString();
    }

    @Override
    public void setClueState(CWClue clue, String state) {
        CWClue.DIRECTION direction = clue.getDirection();
        Point location = getClueLocation(clue.getLocation(), direction);
        int length = clue.getLength();

        switch (direction) {
            case ACROSS:
                for (int i = 0; i < length; i++) {
                    board[i+location.x][location.y] = state.charAt(i);
                }
                break;
            case DOWN:
                for (int i = 0; i < length; i++) {
                    board[location.x][i+location.y] = state.charAt(i);
                }
                break;
        }
    }

    @Override
    public int filledLetters(CWClue clue) {
        int count = 0;
        CWClue.DIRECTION direction = clue.getDirection();
        Point location = getClueLocation(clue.getLocation(), direction);
        int length = clue.getLength();

        switch (direction) {
            case ACROSS:
                for(int i = location.x; i < location.x+length; i++) {
                    if(board[i][location.y] != '\u0000') count++;
                }
                break;
            case DOWN:
                for(int i = location.y; i < location.y+length; i++) {
                    if(board[location.x][i] != '\u0000') count++;
                }
                break;
        }
        return count;
    }

    @Override
    public List<CWClue> getIntersectingClues(CWClue clue) {
        return lookupMap.getIntersectingClues(clue);
    }

    @Override
    public Integer getCellNotation(int x, int y) {
        return lookupMap.getClueNotation(x, y);
    }

    @Override
    public Point getClueLocation(int clue, CWClue.DIRECTION direction) {
        return lookupMap.getClueLocation(clue, direction);
    }

    @Override
    public List<CWClue> getAllClues() {
        return lookupMap.getAllClues();
    }

    @Override
    public int getMissingLetters() {
        int missingLetters = 0;
        for(int i = 0; i < size.width; i++) {
            for(int j = 0; j < size.height; j++) {
                if(board[i][j] == '\u0000')
                    missingLetters++;
            }
        }
        return missingLetters;
    }

    @Override
    public boolean isComplete() {
        for(int i = 0; i < size.width; i++) {
            for(int j = 0; j < size.height; j++) {
                if(board[i][j] == '\u0000')
                    return false;
            }
        }
        return true;
    }

    @Override
    public int getTotalLetters() {
        int totalLetters = 0;
        char[][] answers = (char[][]) metadataMap.get("answers");
        if(answers == null) return 0;
        for(int i = 0; i < size.width; i++) {
            for(int j = 0; j < size.height; j++) {
                if(board[i][j] != CWPuzzleFactory.BLACK_CELL_CHAR) {
                    totalLetters++;
                }
            }
        }
        return totalLetters;
    }

    @Override
    public int getCorrectLetters() {
        int correctLetters = 0;
        char[][] answers = (char[][]) metadataMap.get("answers");
        if(answers == null) return 0;
        for(int i = 0; i < size.width; i++) {
            for(int j = 0; j < size.height; j++) {
                if(board[i][j] != CWPuzzleFactory.BLACK_CELL_CHAR) {
                    if(board[i][j] == answers[i][j])
                        correctLetters++;
                }
            }
        }
        return correctLetters;
    }

    @Override
    public float getCorrectPercentage() {
        int totalLetters = 0;
        int correctLetters = 0;
        char[][] answers = (char[][]) metadataMap.get("answers");
        if(answers == null) return 0f;
        for(int i = 0; i < size.width; i++) {
            for(int j = 0; j < size.height; j++) {
                if(board[i][j] != CWPuzzleFactory.BLACK_CELL_CHAR) {
                    totalLetters++;
                    if(board[i][j] == answers[i][j])
                        correctLetters++;
                }
            }
        }
        return 100.0f * correctLetters / totalLetters;
    }

    @Override
    public float getEmptyPercentage() {
        int totalLetters = 0;
        int correctLetters = 0;
        for(int i = 0; i < size.width; i++) {
            for(int j = 0; j < size.height; j++) {
                if(board[i][j] != CWPuzzleFactory.BLACK_CELL_CHAR) {
                    totalLetters++;
                    if(board[i][j] == '\u0000')
                        correctLetters++;
                }
            }
        }
        return 100.0f * correctLetters / totalLetters;
    }

    @Override
    public boolean isSolvedAndCorrect() {
        char[][] answers = (char[][]) metadataMap.get("answers");
        if(answers == null) return false;
        for(int i = 0; i < size.width; i++) {
            for(int j = 0; j < size.height; j++) {
                if(board[i][j] != CWPuzzleFactory.BLACK_CELL_CHAR) {
                    if(board[i][j] != answers[i][j])
                        return false;
                }
            }
        }
        return true;
    }
}
