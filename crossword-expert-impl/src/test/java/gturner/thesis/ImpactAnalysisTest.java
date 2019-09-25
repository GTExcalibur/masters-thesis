package gturner.thesis;

import gturner.crossword.impl.CWPuzzleFactory;
import gturner.crossword.spec.CWClue;
import gturner.crossword.spec.CWPuzzle;
import gturner.solver.impl.PuzzleSolver;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.awt.*;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/25/14
 * Time: 12:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImpactAnalysisTest {

    public static Test suite() {
        TestSuite testSuite = new TestSuite();

        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("expert-layout.xml");
        String testScenario = System.getProperty("testScenario");
        if(testScenario == null) testScenario = "6"; // default to ours

        for(String group : Arrays.asList("5x5", "7x7", "9x9", "11x11", "13x13", "15x15", "17x17", "19x19", "21x21")) {
            for(int num = 1; num < 16; num++) {
                testSuite.addTest(new AnalysisTestCase(
                        "scenario: " + testScenario + "," + group + ":" + num,
                        "solver.puzzleSolver.scenario" + testScenario,
                        "F:\\dev\\thesis-test-data\\" + group + "\\" + num + ".puz",
                        applicationContext
                ));
            }
        }

        return testSuite;
    }

    private static final ExecutorService workerService = Executors.newCachedThreadPool();

    public static class AnalysisTestCase extends TestCase {


        private final String name;
        private final String beanName;
        private final String testFileName;

        private ApplicationContext applicationContext;
        private PuzzleSolver testInstance;
        private CWPuzzle testPuzzle;

        private AnalysisTestCase(String name, String beanName, String testFileName, ApplicationContext applicationContext) {
            super("testSolve");
            this.name = name;
            this.beanName = beanName;
            this.testFileName = testFileName;
            this.applicationContext = applicationContext;
        }

        @org.junit.Test
        public void testSolve() throws Exception {
            final long start = System.currentTimeMillis();
            final AtomicReference<CWPuzzle> finalState = new AtomicReference<CWPuzzle>();

            System.out.println(name + "-----------------------------------");
            Future<?> future = workerService.submit(new Runnable() {
                @Override
                public void run() {
                    testInstance.solvePuzzle(testPuzzle, new PuzzleSolver.PuzzleListener() {
                        @Override
                        public void currentPuzzle(CWPuzzle puzzle) {
                            finalState.set(puzzle);
                        }
                    });
                }
            });
            boolean timedOut = false;
            try {
                future.get(10, TimeUnit.MINUTES);
            } catch (TimeoutException e) {
                try {
                    future.cancel(true);
                } catch (Exception ignore) {
                }
                timedOut = true;
            }
            CWPuzzle answer = finalState.get();

            final long timeInMillis = System.currentTimeMillis() - start;
            final int timeInMinutes = roundMinutes(timeInMillis);

            final int totalWords = answer.getAllClues().size();
            final int wordsWrong = getWordsWrong(answer);

            final int totalLetters = answer.getTotalLetters();
            final int missingLetters = answer.getMissingLetters();
            final int correctLetters = answer.getCorrectLetters();

            System.out.println("timedOut: " + timedOut);
            System.out.println("timeInMillis: " + timeInMillis);
            System.out.println("timeInMinutes: " + timeInMinutes);

            System.out.println("totalWords: " + totalWords);
            System.out.println("wordsWrong: " + wordsWrong);
            System.out.println("percentage: " + (100.0f * (totalWords - wordsWrong) / totalWords));

            System.out.println("totalLetters: " + totalLetters);
            System.out.println("missingLetters: " + missingLetters);
            System.out.println("correctLetters: " + correctLetters);

            System.out.println("ACPT score: " + getACPTScore(
                    totalWords - wordsWrong, timeInMinutes,
                    totalLetters - missingLetters - correctLetters,
                    testPuzzle.getBoardSize(),
                    answer.isSolvedAndCorrect())
            );

            System.out.println("given answer: " + outputPuzzle(answer));

            Assert.assertFalse(true);
        }

        private int roundMinutes(long systemTime) {
            DateTime dateTime = new DateTime(systemTime, DateTimeZone.UTC);

            int round = 0;
            if(dateTime.getSecondOfMinute() > 0 || dateTime.getMillisOfSecond() > 0) {
                round = 1;
            }

            return dateTime.getMinuteOfDay() + 1;
        }

        private int getWordsWrong(CWPuzzle puzzle) throws Exception {
            int count = 0;

            for (CWClue cwClue : puzzle.getAllClues()) {
                if(!getAnswer(cwClue).equals(puzzle.getClueState(cwClue))) {
                    count++;
                }
            }
            return count;
        }

        private int getACPTScore(int correctWords, int timeInMinutes, int incorrectLetters, Dimension puzzleSize, boolean isCorrect) {
            int score = correctWords * 10;
            int puzzleTime;
            if(puzzleSize.width > 16) {
                puzzleTime = 15;
            } else if(puzzleSize.width > 10) {
                puzzleTime = 10;
            } else {
                puzzleTime = 5;
            }

            int bonus = Math.max(0, (puzzleTime - timeInMinutes) * 25 - (incorrectLetters * 25));

            return isCorrect ? score + bonus + 150 : score + bonus;
        }

        private static String getAnswer(CWClue clue) throws Exception {
            Method getClueAnswer = clue.getClass().getDeclaredMethod("getClueAnswer");
            getClueAnswer.setAccessible(true);
            return (String)getClueAnswer.invoke(clue);
        }

        private String outputPuzzle(CWPuzzle puzzle) {
            StringBuilder sb = new StringBuilder("\n");
            Dimension boardSize = puzzle.getBoardSize();
            for(int j = 0; j < boardSize.height; j++) {
                for(int i = 0; i < boardSize.width; i++) {
                    sb.append(puzzle.getCellState(i, j));
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        protected void setUp() throws Exception {
            super.setUp();
            testInstance = applicationContext.getBean(beanName, PuzzleSolver.class);
            testPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream(testFileName), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
