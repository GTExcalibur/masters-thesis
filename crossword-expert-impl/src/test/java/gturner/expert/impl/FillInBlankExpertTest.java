package gturner.expert.impl;

import gturner.crossword.spec.CWClue;
import gturner.expert.util.ClusteringUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/23/13
 * Time: 8:21 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                "classpath:expert-layout.xml"
        }
)
public class FillInBlankExpertTest {

    @Autowired
    private ApplicationContext applicationContext;

    private FillInBlankExpert instance;

    @Before
    public void setup() {
        instance = (FillInBlankExpert)applicationContext.getBean("expert.fillInBlank");
    }

    @Test
    public void simpleTest() {
        // FROM ::: 4 ::: "___ Here to Eternity"
        Map<String, Long> result = instance.performSearch(new CWClueStub("\"___ Here to Eternity\"", 4), "\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("FROM", answer);
    }

    @Test
    public void simpleTest2() {
        // DOGG ::: 4 ::: Rapper Snoop ___"
        Map<String, Long> result = instance.performSearch(new CWClueStub("Rapper Snoop ___", 4), "\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("DOGG", answer);
    }

    @Test
    public void simpleTest3() {
        // GHENT ::: 5 ::: "The ___ Altarpiece," a painting by ..."
        Map<String, Long> result = instance.performSearch(new CWClueStub("\"The ___ Altarpiece,\" a painting by ...", 5), "\u0000\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("GHENT", answer);
    }

    @Test
    public void simpleTest4() {
        // LANKA ::: 5 ::: Sri ___, formerly Ceylon
        Map<String, Long> result = instance.performSearch(new CWClueStub("Sri ___, formerly Ceylon", 5), "\u0000\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("LANKA", answer);
    }

    @Test
    public void simpleTest5() {
        // BEVAN ::: 5 ::: Britain's Aneurin ___
        Map<String, Long> result = instance.performSearch(new CWClueStub("Britain's Aneurin ___", 5), "\u0000\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("BEVAN", answer);
    }

    @Test
    public void simpleTest6() {
        // BATOR ::: 5 ::: Ulan ___, Mongolia
        Map<String, Long> result = instance.performSearch(new CWClueStub("Ulan ___, Mongolia", 5), "\u0000\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("BATOR", answer);
    }

    @Test
    public void simpleTest7() {
        // IVE ::: 3 ::: "___ Got Five Dollars," 1931 song
        Map<String, Long> result = instance.performSearch(new CWClueStub("\"___ Got Five Dollars,\" 1931 song", 3), "\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("IVE", answer);
    }

    @Test
    public void simpleTest8() {
        // ABAT ::: 4 ::: Blind as ___
        Map<String, Long> result = instance.performSearch(new CWClueStub("Blind as ___", 4), "\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("ABAT", answer);

        // ABAT ::: 4 ::: Blind as ___
        result = instance.performSearch(new CWClueStub("Blind as ___", 4), "A\u0000A\u0000");
        answer = selectBestAnswer(result);
        Assert.assertEquals("ABAT", answer);
    }

    @Test
    public void simpleTest9() {
        // LEA ::: 3 ::: "Glee" actress ___ Michele
        Map<String, Long> result = instance.performSearch(new CWClueStub("\"Glee\" actress ___ Michele", 3), "\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("LEA", answer);
    }

    @Test
    public void simpleTest10() {
        // WORT ::: 4 ::: St. John's ___ (herbal remedy)
        Map<String, Long> result = instance.performSearch(new CWClueStub("St. John's ___ (herbal remedy)", 4), "\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("WORT", answer);
    }

    @Test
    public void simpleTest11() {
        // INITIO ::: 6 ::: Ab ___ (from the beginning)
        Map<String, Long> result = instance.performSearch(new CWClueStub("Ab ___ (from the beginning)", 6), "\u0000\u0000\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("INITIO", answer);
    }

    @Test
    public void simpleTest12() {
        // EST ::: 3 ::: Id ___ (that is)
        Map<String, Long> result = instance.performSearch(new CWClueStub("Id ___ (that is)", 3), "\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("EST", answer);
    }

    @Test
    public void simpleTest13() {
        // ELLIS ::: 5 ::: ___ Island (immigrants' site)
        Map<String, Long> result = instance.performSearch(new CWClueStub("___ Island (immigrants' site)", 5), "\u0000\u0000\u0000\u0000\u0000");
        String answer = selectBestAnswer(result);
        Assert.assertEquals("ELLIS", answer);
    }



    @Test
    public void scoreFillBlankFinder() throws Exception {
        scoreFillBlankFinder_main(new StringTransformer() {
            @Override
            public String transform(String answer) {
                return String.format("%" + answer.length() + "s", "").replace(' ', '\u0000');
            }
        });
    }

//    @Test
    public void scoreFillBlankFinder_WithKnownLetter() throws Exception {
        scoreFillBlankFinder_main(new StringTransformer() {
            @Override
            public String transform(String answer) {
                String blank = String.format("%" + answer.length() + "s", "").replace(' ', '\u0000');
                return blank.substring(0, 2) + answer.charAt(2) + blank.substring(3);
            }
        });
    }

//    @Test
    public void scoreFillBlankFinder_WithKnownLetter2() throws Exception {
        scoreFillBlankFinder_main(new StringTransformer() {
            @Override
            public String transform(String answer) {
                String blank = String.format("%" + answer.length() + "s", "").replace(' ', '\u0000');
                return answer.charAt(0) + blank.substring(1,2) + answer.charAt(2) + blank.substring(3);
            }
        });
    }

    private interface StringTransformer {
        public String transform(String answer);
    }

    private void scoreFillBlankFinder_main(final StringTransformer transformer) throws Exception {
        final AtomicLong correctAnswers = new AtomicLong(0);
        final AtomicLong nodata = new AtomicLong(0);
        final AtomicLong declined = new AtomicLong(0);
        final AtomicLong unknown = new AtomicLong(0);
        final AtomicLong wrong = new AtomicLong(0);
        final AtomicLong total = new AtomicLong(0);

        ExecutorService clueService = Executors.newFixedThreadPool(8);
        final PrintStream originalOut = System.out;

        final ThreadLocal<ByteArrayOutputStream> byteStreams = new ThreadLocal<ByteArrayOutputStream>() {
            @Override
            protected ByteArrayOutputStream initialValue() {
                return new ByteArrayOutputStream();
            }
        };
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                byteStreams.get().write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                byteStreams.get().write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                byteStreams.get().write(b, off, len);
            }
        }));
        final Callable<Void> flushCaller = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                synchronized (originalOut) {
                    ByteArrayOutputStream outputStream = byteStreams.get();
                    outputStream.writeTo(originalOut);
                    outputStream.reset();
                }
                return null;
            }
        };

        int clues = 0;

        InputStream resourceAsStream = FillInBlankExpertTest.class.getResourceAsStream("/gturner/expert/impl/fillInBlanks.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
        String readLine = bufferedReader.readLine();
        while(readLine != null) {
            final String clueLine = readLine;

            clueService.submit(new Callable<Void>() {
                public Void call() throws Exception {
                    System.out.println(String.format("%160s", "").replace(' ', '-'));
                    System.out.println(clueLine);
                    String[] clueArray = clueLine.split("\\s*:::\\s*");
                    int length = Integer.parseInt(clueArray[1]);
                    Map<String, Long> result;
                    try {
                        result = instance.performSearch(new CWClueStub(clueArray[2], length), transformer.transform(clueArray[0]));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    String bestGuess = selectBestAnswer(result);
                    if (clueArray[0].equals(bestGuess)) {
                        System.out.println(correctAnswers.incrementAndGet() + " ----- " + clueLine);
                    } else if (result.isEmpty()) {
                        System.out.println("No data: " + clueLine);
                        nodata.incrementAndGet();
                    } else if (bestGuess == null && result.containsKey(clueArray[0])) {
                        System.out.println("Declined: " + clueLine);
                        declined.incrementAndGet();
                    } else if (bestGuess == null) {
                        System.out.println("Skipping: " + clueLine);
                        unknown.incrementAndGet();
                    } else if (result.containsKey(clueArray[0])) {
                        System.out.println("Wrong guess: " + clueLine);
                        wrong.incrementAndGet();
                    } else {
                        System.out.println("Failure: " + clueLine + "   best guess: " + bestGuess);
                    }
                    total.incrementAndGet();
//                    System.out.format("Correct: %f%%, Weighted:%f%% %n", 100.0f * correctAnswers.get() / (total.get() - unknown.get()), 100.0f * (correctAnswers.get() + unknown.get()) / total.get());
//                    System.out.format("Correct: %f%%", 100.0f * correctAnswers.get() / total.get());
                    System.out.format("Correct: %f%%", 100.0f * correctAnswers.get() / total.get());

                    flushCaller.call();
                    return null;
                }
            });
            readLine = bufferedReader.readLine();

            if(clues++ >= 10071) {
                break;
            }
        }

        clueService.shutdown();
        clueService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        originalOut.println("Correct Answers:" + correctAnswers);
        originalOut.flush();
//        originalOut.format("Correct: %f%%, Weighted:%f%% %n", 100.0f * correctAnswers.get() / (total.get() - unknown.get()), 100.0f * (correctAnswers.get() + unknown.get()) / total.get());
//        originalOut.flush();

        originalOut.println(correctAnswers.get());
        originalOut.println(nodata.get());
        originalOut.println(declined.get());
        originalOut.println(unknown.get());
        originalOut.println(wrong.get());
        originalOut.println(total.get());
        originalOut.flush();
    }

    private String selectBestAnswer(Map<String, Long> answers) {
        if(answers == null || answers.isEmpty()) return null;

        List<Map.Entry<String,Long>> highVals = ClusteringUtils.getHighClusterValues(answers);

        if(highVals.size() == 1) {
            return highVals.get(0).getKey();
        }

        for (Map.Entry<String, Long> highVal : highVals) {
            System.out.println("Guess: " + highVal.getKey());
        }

        return null;
    }

    private static class CWClueStub implements CWClue {
        private final String clue;
        private final int length;

        private CWClueStub(String clue, int length) {
            this.clue = clue;
            this.length = length;
        }

        @Override
        public DIRECTION getDirection() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public int getLocation() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getClueText() {
            return clue;
        }
    }
}
