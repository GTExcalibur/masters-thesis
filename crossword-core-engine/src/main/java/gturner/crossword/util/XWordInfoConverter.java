package gturner.crossword.util;

import gturner.crossword.impl.CWPuzzleFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/3/13
 * Time: 12:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class XWordInfoConverter {

    private static final Pattern title_pattern;
    private static final Pattern author_pattern;
    private static final Pattern editor_pattern;
    private static final Pattern date_pattern;
    private static final Pattern size_pattern;
    private static final Pattern table_pattern;
    private static final Pattern rowAnswerMatcher;
    private static final Pattern across_pattern;
    private static final Pattern down_pattern;
    private static final Pattern answer_pattern;

    static {
        title_pattern = Pattern.compile("<span id=\"CPHContent_TitleLabel\">([^<]*)</span>");
        author_pattern = Pattern.compile("href=\"Thumbs[?]author=[^\"]*\">([^<]*)</a>");
        editor_pattern = Pattern.compile("Editor: (<span id=\"CPHContent_EditorLabel\">)*([^<]*)(</span>)*");
        date_pattern = Pattern.compile("http://www.xwordinfo.com/Crossword[?]date=([^\"']*)");
        size_pattern = Pattern.compile("<div class=\"xwsize\">(<a[^>]+>){0,1}Rows: (\\d+), Columns: (\\d+)");
        table_pattern = Pattern.compile("<table id=\"CPHContent_PuzTable\".*</table>", Pattern.MULTILINE | Pattern.DOTALL);
        rowAnswerMatcher = Pattern.compile(" class=[\"']([a-z0-9]+)[\"'][^>]*>([^<]*)</");
        across_pattern = Pattern.compile("id=\"CPHContent_AcrossClues\".*");
        down_pattern = Pattern.compile("id=\"CPHContent_DownClues\".*");
        answer_pattern = Pattern.compile("(\\d+)[.] (.*) : <a href=\"[^\"]*\">(<span[^>]+>){0,1}([^<]*)</");
    }

    private static final String CHARSET_NAME = "UTF-8";
    private static final List<String> COLORS = Arrays.asList("RED", "ORANGE", "YELLOW", "GREEN", "BLUE", "INDIGO", "VIOLET");
    private static final List<String> NUMBERS = Arrays.asList("ZERO", "ONE", "TWO");

    public static void main(String[] args) throws Exception {
        final File converted = new File("C:\\Temp\\converted");
        File htmlFiles = new File("C:\\Temp\\puz");
        File[] allFiles = htmlFiles.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".html");
            }
        });
//        List<File> allFiles = Arrays.asList(new File(htmlFiles, "11-21-1993.html"));
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (final File htmlFile : allFiles) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    final File convertedFile = new File(converted, htmlFile.getName() + "-puz.xml");
                    try {
                        if(!convertedFile.exists()){
                            return;
//                            convertHTMLToPuzzleXML(new FileInputStream(htmlFile), new FileOutputStream(convertedFile));
                        }
                        CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream(convertedFile), CWPuzzleFactory.FILE_TYPES.XPUF_EXT);
                    } catch (Exception e) {
                        System.out.println("File conversion failure: "+ convertedFile.getName());
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    private static void convertHTMLToPuzzleXML(InputStream is, OutputStream os) throws IOException {
        String content = new Scanner(new InputStreamReader(is, CHARSET_NAME)).useDelimiter("\\A").next();

        Matcher matcher = title_pattern.matcher(content);
        String title = matcher.find() ? matcher.group(1) : "";

        matcher = author_pattern.matcher(content);
        String author = matcher.find() ? matcher.group(1) : "";

        matcher = editor_pattern.matcher(content);
        String editor = matcher.find() ? matcher.group(2) : "";

        matcher = date_pattern.matcher(content);
        String date = matcher.find() ? matcher.group(1) : "";

        matcher = size_pattern.matcher(content);
        String rows = matcher.find() ? matcher.group(2) : "";
        String cols = "".equals(rows) ? "" : matcher.group(3);
        if(rows.isEmpty() || cols.isEmpty()) {
            throw new RuntimeException("could not find row/cols");
        }

        matcher = table_pattern.matcher(content);
        String tableContent = matcher.find() ? matcher.group() : "";

        Map<String, Map<String, String>> clueLocations = new LinkedHashMap<String, Map<String, String>>();

        int i = 1;
        int j = 1;

        List<String> rowAnswers = new ArrayList<String>();
        String[] rowData = tableContent.split("</tr>");
        for (String rowInstance : rowData) {
            StringBuilder sb = new StringBuilder();
            matcher = rowAnswerMatcher.matcher(rowInstance);
            while(matcher.find()) {
                String type = matcher.group(1);
                String cellContent = matcher.group(2);
                if("letter".equals(type)) {
                    sb.append(cellContent);
                    j++;
                } else if("vblack".equals(type)) {
                    sb.append(".");
                    j++;
                } else if("subst".equals(type) || "subst2".equals(type)) {
                    if(cellContent.length() > 0) {
                        if(COLORS.contains(cellContent) || NUMBERS.contains(cellContent) || cellContent.contains("/")) {
                            sb.append(cellContent.charAt(0));
                        } else {
                            sb.append(cellContent);
                        }
                    }
                    j++;
                } else if("num".equals(type)) {
                    LinkedHashMap<String, String> attrs = new LinkedHashMap<String, String>();
                    attrs.put("Row", Integer.toString(i));
                    attrs.put("Col", Integer.toString(j));
                    attrs.put("Num", cellContent);
                    clueLocations.put(cellContent, attrs);
                }
            }
            if(sb.length() > 0) {
                rowAnswers.add(sb.toString());
            }
            i++;j=1;
        }

        writeHeader(os);
        // yes ... i'm being lazy ... get over it
        os.write("<Puzzles Version=\"1.0\">\n".getBytes(CHARSET_NAME));
        os.write("  <Puzzle>\n".getBytes(CHARSET_NAME));



        writeTag(os, 4, "Title", Collections.<String, String>emptyMap(), title);
        writeTag(os, 4, "Author", Collections.<String, String>emptyMap(), author);
        writeTag(os, 4, "Editor", Collections.<String, String>emptyMap(), editor);
        writeTag(os, 4, "Publisher", Collections.<String, String>emptyMap(), "The New York Times");
        writeTag(os, 4, "Date", Collections.<String, String>emptyMap(), date);



        os.write("    <Size>\n".getBytes(CHARSET_NAME));
        os.write("      <Rows>".getBytes(CHARSET_NAME));
        os.write(rows.getBytes(CHARSET_NAME));
        os.write("</Rows>\n".getBytes(CHARSET_NAME));
        os.write("      <Cols>".getBytes(CHARSET_NAME));
        os.write(cols.getBytes(CHARSET_NAME));
        os.write("</Cols>\n".getBytes(CHARSET_NAME));
        os.write("    </Size>\n".getBytes(CHARSET_NAME));



        os.write("    <Grid>\n".getBytes(CHARSET_NAME));
        for (String s : rowAnswers) {
            writeTag(os, 6, "Row", Collections.<String, String>emptyMap(), s);
        }
        os.write("    </Grid>\n".getBytes(CHARSET_NAME));



        os.write("    <Clues>\n".getBytes(CHARSET_NAME));

        matcher = across_pattern.matcher(content);
        String acrossClues = matcher.find() ? matcher.group() : "";
        String[] acrossCluesSplit = acrossClues.split("<br\\s*/{0,1}>");
        for (String acrossClue : acrossCluesSplit) {
            matcher = answer_pattern.matcher(acrossClue);
            if(matcher.find()) {
                String number = matcher.group(1);
                String clue = matcher.group(2);
                String answer = matcher.group(4);

                LinkedHashMap<String, String> attrs = new LinkedHashMap<String, String>(clueLocations.get(number));
                attrs.put("Dir", "Across");
                attrs.put("Ans", answer);
                writeTag(os, 6, "Clue", attrs, clue);
            }
        }

        matcher = down_pattern.matcher(content);
        String downClues = matcher.find() ? matcher.group() : "";
        String[] downClueSplit = downClues.split("<br\\s*/{0,1}>");
        for (String downClue : downClueSplit) {
            matcher = answer_pattern.matcher(downClue);
            if(matcher.find()) {
                String number = matcher.group(1);
                String clue = matcher.group(2);
                String answer = matcher.group(4);

                LinkedHashMap<String, String> attrs = new LinkedHashMap<String, String>(clueLocations.get(number));
                attrs.put("Dir", "Down");
                attrs.put("Ans", answer);
                writeTag(os, 6, "Clue", attrs, clue);
            }
        }

        os.write("    </Clues>\n".getBytes(CHARSET_NAME));

        os.write("  </Puzzle>\n".getBytes(CHARSET_NAME));
        os.write("</Puzzles>".getBytes(CHARSET_NAME));
        os.close();
    }

    private static void writeHeader(OutputStream os) throws IOException {
        os.write(("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!--XPF document for the Sunday, November 21, 1993 New York Times crossword.-->\n" +
                "<!--Conforms to Universal Crossword Puzzle Format XPF. See www.xwordinfo.com/XPF for details and licensing information.-->\n").getBytes(CHARSET_NAME));
    }

    private static void writeTag(OutputStream os, int spaces, String tag, Map<String, String> attrs, String value) throws IOException {
        while(spaces-- > 0) os.write(" ".getBytes(CHARSET_NAME));
        os.write("<".getBytes(CHARSET_NAME));
        os.write(tag.getBytes(CHARSET_NAME));
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            os.write(" ".getBytes(CHARSET_NAME));
            os.write(attr.getKey().getBytes(CHARSET_NAME));
            os.write("=\"".getBytes(CHARSET_NAME));
            os.write(attr.getValue().getBytes(CHARSET_NAME));
            os.write("\"".getBytes(CHARSET_NAME));
        }
        os.write(">".getBytes(CHARSET_NAME));
        os.write(value.getBytes(CHARSET_NAME));
        os.write("</".getBytes(CHARSET_NAME));
        os.write(tag.getBytes(CHARSET_NAME));
        os.write(">\n".getBytes(CHARSET_NAME));
    }
}
