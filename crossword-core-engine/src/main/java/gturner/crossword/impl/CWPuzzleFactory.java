package gturner.crossword.impl;

import gturner.crossword.spec.CWClue;
import gturner.crossword.spec.CWPuzzle;
import gturner.crossword.util.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 7:38 AM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class CWPuzzleFactory {
    public static final char BLACK_CELL_CHAR = '.';

    public static enum FILE_TYPES {
        PUZ_EXT(".puz", "Across Lite Format (*.puz)"),
        XPUF_EXT(".xml", "Universal Crossword Puzzle Format XPF (*.xml)");

        private final String extension;
        private final String description;

        public static FILE_TYPES getFileType(String fileName) {
            if(fileName.endsWith(PUZ_EXT.getExtension())) return PUZ_EXT;
            if(fileName.endsWith(XPUF_EXT.getExtension())) return XPUF_EXT;
            return null;
        }

        FILE_TYPES(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        public String getExtension() {
            return extension;
        }

        public String getDescription() {
            return description;
        }
    }

    public static CWPuzzle createFacadePuzzle(Dimension size) {
        ArrayList<Point> blackCells = new ArrayList<Point>();
        for(int i = 0; i < size.width; i++) {
            for(int j = 0; j < size.height; j++) {
                if(i==j || (i+j+1) == size.width) blackCells.add(new Point(i,j));
            }
        }

        return new CWPuzzleImpl(size, blackCells, new ClueLookupMap(size.width, size.height));
    }

    public static CWPuzzle createPuzzleFromInputStream(InputStream is, FILE_TYPES fileType) {
        switch (fileType) {
            case PUZ_EXT:
                return constructPuzzleFromPuzzleFile(is);
            case XPUF_EXT:
                return constructPuzzleFromXMLFile(is);
        }
        throw new ConfigurationException("Unknown file type", null);
    }

    static CWPuzzle constructPuzzleFromXMLFile(InputStream is) {
        try {
            Map<String, Object> metadataMap = parsePuzzleXMLIntoMap(is);
            return constructPuzzleFromMetadataMap(metadataMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static CWPuzzle constructPuzzleFromPuzzleFile(InputStream is) {
        try {
            Map<String, Object> metadataMap = parsePuzzleFileIntoMap(is);
            return constructPuzzleFromMetadataMap(metadataMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static CWPuzzle constructPuzzleFromMetadataMap(Map<String, Object> metadataMap) throws IOException {
        int boardWidth = (Integer)metadataMap.get("boardWidth");
        int boardHeight = (Integer)metadataMap.get("boardHeight");

        List<Point> blackCells = (List<Point>)metadataMap.get("blackCells");
        List<ClueSpec> clueStrings = new ArrayList<ClueSpec>((List<ClueSpec>)metadataMap.get("clueStrings"));

        List<Point> cluePoints = getCluePoints(new Dimension(boardWidth, boardHeight), blackCells);
        char[][] answers = (char[][])metadataMap.get("answers");

        int clueNumber = 1;

        ClueLookupMap lookupMap = new ClueLookupMap(boardWidth, boardHeight);

        for (Point cluePoint : cluePoints) {
            lookupMap.addClueNotation(cluePoint, clueNumber);

            ClueSpec cluePeek = clueStrings.isEmpty() ? null : clueStrings.get(0);

            // the spec says that across will be listed before downward
            if (cluePoint.x == 0 || blackCells.contains(new Point(cluePoint.x - 1, cluePoint.y)) &&
                    !(cluePoint.x + 1 >= boardWidth || blackCells.contains(new Point(cluePoint.x + 1, cluePoint.y))) &&
                    cluePeek != null && cluePeek.sameClue(clueNumber, true)
                    ) {

                char[] answerSpace = new char[boardWidth];
                int maxOffset;
                for (maxOffset = cluePoint.x; maxOffset < boardWidth; maxOffset++) {
                    char chr = answers[maxOffset][cluePoint.y];
                    if (chr == BLACK_CELL_CHAR) break;
                    answerSpace[maxOffset] = chr;
                }
                if(maxOffset - cluePoint.x > 1) {
                    String answer = new String(answerSpace, cluePoint.x, maxOffset - cluePoint.x);

//                    System.out.println("Across " + clueNumber + ": " + answer);

                    lookupMap.addClue(cluePoint, new CWClueImpl(CWClue.DIRECTION.ACROSS,
                            clueNumber, cluePeek.getText(), answer));

                    clueStrings.remove(0);
                    cluePeek = clueStrings.isEmpty() ? null : clueStrings.get(0);
                }
            }
            if (cluePoint.y == 0 || blackCells.contains(new Point(cluePoint.x, cluePoint.y - 1)) &&
                    !(cluePoint.y + 1 >= boardHeight || blackCells.contains(new Point(cluePoint.x, cluePoint.y + 1))) &&
                    cluePeek != null && cluePeek.sameClue(clueNumber, false)
                    ) {

                char[] answerSpace = new char[boardHeight];
                int maxOffset;
                for (maxOffset = cluePoint.y; maxOffset < boardHeight; maxOffset++) {
                    char chr = answers[cluePoint.x][maxOffset];
                    if (chr == BLACK_CELL_CHAR) break;
                    answerSpace[maxOffset] = chr;
                }
                if (maxOffset - cluePoint.y > 1) {
                    String answer = new String(answerSpace, cluePoint.y, maxOffset - cluePoint.y);

//                    System.out.println("Down " + clueNumber + ": " + answer);
                    lookupMap.addClue(cluePoint, new CWClueImpl(CWClue.DIRECTION.DOWN,
                            clueNumber, cluePeek.getText(), answer));

                    clueStrings.remove(0);
                    cluePeek = clueStrings.isEmpty() ? null : clueStrings.get(0);
                }
            }

            clueNumber++;
        }
        if(!clueStrings.isEmpty()) {
            throw new IOException("File has extra clues");
        }

        return new CWPuzzleImpl(metadataMap, lookupMap);
    }

    private static Map<String, Object> parsePuzzleXMLIntoMap(InputStream is) throws IOException {
        try {
            Map<String, Object> metadataMap = new HashMap<String, Object>();

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            XPath xpath = XPathFactory.newInstance().newXPath();

            metadataMap.put("title", xpath.evaluate("//Puzzle[1]/Title[1]/text()", doc, XPathConstants.STRING));
            metadataMap.put("author", xpath.evaluate("//Puzzle[1]/Author[1]/text()", doc, XPathConstants.STRING));
            metadataMap.put("publisher", xpath.evaluate("//Puzzle[1]/Publisher[1]/text()", doc, XPathConstants.STRING));
            metadataMap.put("date", xpath.evaluate("//Puzzle[1]/Date[1]/text()", doc, XPathConstants.STRING));

            final int boardWidth = ((Double)xpath.evaluate("number(//Puzzle[1]/Size[1]/Cols[1]/text())", doc, XPathConstants.NUMBER)).intValue();
            metadataMap.put("boardWidth", boardWidth);
            final int boardHeight = ((Double)xpath.evaluate("number(//Puzzle[1]/Size[1]/Rows[1]/text())", doc, XPathConstants.NUMBER)).intValue();
            metadataMap.put("boardHeight", boardHeight);
            final int numberOfClues = ((Double)xpath.evaluate("count(//Puzzle[1]/Clues[1]/Clue)", doc, XPathConstants.NUMBER)).intValue();
            metadataMap.put("numberOfClues", numberOfClues);

            final NodeList rows = (NodeList)xpath.evaluate("//Puzzle[1]/Grid[1]/Row", doc, XPathConstants.NODESET);
            parsePuzzleLayoutFromAsciiText(new Callable<String>(){
                int itemNumber = 0;

                @Override
                public String call() throws Exception {
                    return rows.item(itemNumber++).getTextContent();
                }
            }, metadataMap, boardWidth, boardHeight);

            // this will provide the same ordering as the puz file format
            ArrayList<ClueSpec> clueStrings = new ArrayList<ClueSpec>(numberOfClues);
            for(int i = 0; i < numberOfClues; i++) {
                String across = (String)xpath.evaluate("//Puzzle[1]/Clues[1]/Clue[@Num='" + i + "' and @Dir='Across']/text()", doc, XPathConstants.STRING);
                if(across.length() > 0) clueStrings.add(new ClueSpec(i, across, true));

                String down = (String)xpath.evaluate("//Puzzle[1]/Clues[1]/Clue[@Num='" + i + "' and @Dir='Down']/text()", doc, XPathConstants.STRING);
                if(down.length() > 0) clueStrings.add(new ClueSpec(i, down, false));
            }
            metadataMap.put("clueStrings", clueStrings);

            return Collections.unmodifiableMap(metadataMap);
        } catch (SAXException e) {
            throw new IOException("Unable to parse XML", e);
        } catch (ParserConfigurationException e) {
            throw new IOException("Unable to parse XML", e);
        } catch (XPathExpressionException e) {
            throw new IOException("Unable to parse XML", e);
        }
    }

    private static Map<String, Object> parsePuzzleFileIntoMap(final InputStream is) throws IOException {
        final byte[] buffer = new byte[1024];

        Map<String, Object> metadataMap = new HashMap<String, Object>();

        metadataMap.put("checksum", readTwoBytesAsShort(buffer, is));
        metadataMap.put("fileMagic", readStringFromByteStream(buffer, is, 12));

        metadataMap.put("cibChecksum", readTwoBytesAsShort(buffer, is));

        metadataMap.put("mLowChecksum1", readTwoBytesAsShort(buffer, is));
        metadataMap.put("mLowChecksum2", readTwoBytesAsShort(buffer, is));

        metadataMap.put("mHighChecksum1", readTwoBytesAsShort(buffer, is));
        metadataMap.put("mHighChecksum1", readTwoBytesAsShort(buffer, is));

        metadataMap.put("versionStr", readStringFromByteStream(buffer, is, 4));

        metadataMap.put("reserved1C", readTwoBytesAsShort(buffer, is));
        metadataMap.put("scrambledChecksum", readTwoBytesAsShort(buffer, is));
        metadataMap.put("reserved20", readStringFromByteStream(buffer, is, 12));

        final int boardWidth = readSingleByteAsInteger(buffer, is);
        metadataMap.put("boardWidth", boardWidth);
        final int boardHeight = readSingleByteAsInteger(buffer, is);
        metadataMap.put("boardHeight", boardHeight);
        final short numberOfClues = readTwoBytesAsShort(buffer, is);
        metadataMap.put("numberOfClues", numberOfClues);

        metadataMap.put("unknownBitmask", readTwoBytesAsShort(buffer, is));
        metadataMap.put("scrambledTag", readTwoBytesAsShort(buffer, is));

        parsePuzzleLayoutFromAsciiText(new Callable<String>(){
            @Override
            public String call() throws Exception {
                return readStringFromByteStream(buffer, is, boardWidth);
            }
        }, metadataMap, boardWidth, boardHeight);

        metadataMap.put("playerState", readStringFromByteStream(buffer, is, boardWidth * boardHeight));

        metadataMap.put("title", readStringFromByteStream(is));
        metadataMap.put("author", readStringFromByteStream(is));
        metadataMap.put("copyright", readStringFromByteStream(is));

        ArrayList<ClueSpec> clueStrings = new ArrayList<ClueSpec>(numberOfClues);
        for(int i = 0; i < numberOfClues; i++) {
            clueStrings.add(new ClueSpec(readStringFromByteStream(is)));
        }
        metadataMap.put("clueStrings", clueStrings);

        metadataMap.put("notes", readStringFromByteStream(is));

        is.close();
        return Collections.unmodifiableMap(metadataMap);
    }

    private static void parsePuzzleLayoutFromAsciiText(Callable<String> stringProvider, Map<String, Object> metadataMap, int boardWidth, int boardHeight) throws IOException {
        try {
            char[][] answers = new char[boardWidth][boardHeight];
            ArrayList<Point> blackCells = new ArrayList<Point>();
            for(int i = 0; i < boardHeight; i++) {
                String level = stringProvider.call();
                for(int j = 0; j < boardWidth; j++) {
                    answers[j][i] = level.charAt(j);
                }

                int j = level.indexOf(BLACK_CELL_CHAR);
                while(j >= 0) {
                    blackCells.add(new Point(j, i));
                    j = level.indexOf(BLACK_CELL_CHAR, j + 1);
                }
            }
            metadataMap.put("blackCells", blackCells);
            metadataMap.put("answers", answers);
        } catch (Exception e) {
            if(e instanceof IOException) throw (IOException)e;
            throw new RuntimeException(e);
        }
    }

    private static short readTwoBytesAsShort(byte[] buffer, InputStream is) throws IOException {
        is.read(buffer, 0, 2);

        return (short)((buffer[1]&0xff) << 8 | (buffer[0]&0xff));
    }

    private static int readSingleByteAsInteger(byte[] buffer, InputStream is) throws IOException {
        is.read(buffer, 0, 1);
        return (int) (buffer[0]&0xff);
    }

    private static String readStringFromByteStream(InputStream is) throws IOException {
        ArrayList<Byte> byteList = new ArrayList<Byte>();
        int byteAsInt = is.read();
        while(byteAsInt > 0) {
            byteList.add((byte)byteAsInt);
            byteAsInt = is.read();
        }

        byte[] fullLength = new byte[byteList.size()];
        for(int i = 0; i < byteList.size(); i++) {
            fullLength[i] = byteList.get(i);
        }
        return new String(fullLength, 0, fullLength.length, "ISO-8859-1");
    }

    private static String readStringFromByteStream(byte[] buffer, InputStream is, int length) throws IOException {
        if(buffer.length < length) {
            buffer = new byte[length];
        }

        is.read(buffer, 0, length);

        String rawString = new String(buffer, 0, length, "ISO-8859-1");
        int strLen = rawString.length();
        // remove null terminated character from the string
        if(rawString.charAt(strLen - 1) == ((char)0)) {
            rawString = rawString.substring(0, strLen - 1);
        }
        return rawString;
    }

    private static List<Point> getCluePoints(Dimension puzzleSize, List<Point> blackCells) {
        ArrayList<Point> cluePoints = new ArrayList<Point>();

        for(int y = 0; y < puzzleSize.height; y++) {
            for(int x = 0; x < puzzleSize.width; x++) {
                Point here = new Point(x, y);
                if(blackCells.contains(here)) continue;

                boolean shouldAdd = false;

                Point above = new Point(x, y-1);
                Point left = new Point(x-1, y);
                Point below = new Point(x, y+1);
                Point right = new Point(x+1, y);

                shouldAdd = shouldAdd || (
                        (y == 0 || blackCells.contains(above) ) &&
                        (y + 1 < puzzleSize.height && !blackCells.contains(below) )
                );

                shouldAdd = shouldAdd || (
                        (x == 0 || blackCells.contains(left) ) &&
                        (x + 1 < puzzleSize.width && !blackCells.contains(right) )
                );

                if(shouldAdd) cluePoints.add(here);

            }
        }
        return cluePoints;
    }

    private static class ClueSpec {
        private final int num;
        private final String text;
        private final Boolean across;

        private ClueSpec(int num, String text, boolean across) {
            this.num = num;
            this.text = text;
            this.across = across;
        }

        private ClueSpec(String text) {
            this.num = Integer.MAX_VALUE;
            this.text = text;
            this.across = null;
        }

        public boolean sameClue(int n, boolean across) {
            if(this.num == Integer.MAX_VALUE) return true;
            if(this.num == n) return this.across == null || (!(this.across ^ across));
            return false;
        }

        public String getText() {
            return text;
        }
    }
}
