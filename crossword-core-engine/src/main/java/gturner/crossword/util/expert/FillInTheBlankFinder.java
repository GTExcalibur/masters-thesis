package gturner.crossword.util.expert;

import gturner.crossword.impl.CWPuzzleFactory;
import gturner.crossword.spec.CWClue;
import gturner.crossword.spec.CWPuzzle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/3/13
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class FillInTheBlankFinder {

    public static void main(String[] args) throws Exception {
        ExecutorService service = Executors.newSingleThreadExecutor();
        final FileOutputStream output = new FileOutputStream("C:\\temp\\allClues.txt");

        ZipFile zf = new ZipFile(new File("C:\\Users\\George Turner\\IdeaProjects\\MastersThesis\\crossword-core-engine\\src\\test\\resources\\xpf_format\\xwordinfo\\converted.zip"));
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if(zipEntry.getName().contains("2013")) {
                continue;
            }

            CWPuzzle cwPuzzle = null;
            try {
                cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(zf.getInputStream(zipEntry), CWPuzzleFactory.FILE_TYPES.XPUF_EXT);
            } catch (IOException e) {
                System.out.println("Failure with entry: " + zipEntry.getName());
                e.printStackTrace();
                continue;
            }
            for (final CWClue cwClue : cwPuzzle.getAllClues()) {
//                if(cwClue.getClueText().contains("_")) {
                    service.submit(new Callable<Void>() {
                        public Void call() throws Exception {
                            final String charsetName = "UTF-8";
                            output.write(getAnswer(cwClue).getBytes(charsetName));
                            output.write(" ::: ".getBytes(charsetName));
                            output.write(Integer.toString(cwClue.getLength()).getBytes(charsetName));
                            output.write(" ::: ".getBytes(charsetName));
                            output.write(cwClue.getClueText().getBytes(charsetName));
                            output.write("\r\n".getBytes(charsetName));
                            return null;
                        }
                    });
//                }
            }
        }

        service.submit(new Callable<Void>() {
            public Void call() throws Exception {
                output.close();
                return null;
            }
        });
        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    private static String getAnswer(CWClue clue) throws Exception {
        Method getClueAnswer = clue.getClass().getDeclaredMethod("getClueAnswer");
        getClueAnswer.setAccessible(true);
        return (String)getClueAnswer.invoke(clue);
    }
}
