package gturner.solver.impl;

import gturner.crossword.impl.CWPuzzleFactory;
import gturner.crossword.spec.CWPuzzle;
import gturner.expert.impl.FillInBlankExpert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 10:24 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                "classpath:expert-layout.xml"
        }
)
public class PuzzleSolverTest {

    @Autowired
    @Qualifier("solver.puzzleSolver.activeInstance")
    private PuzzleSolver testInstance;

    @Test
    public void test_thesis_5x5_1puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\5x5\\1.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

    @Test
    public void test_thesis_5x5_2puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\5x5\\2.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

    @Test
    public void test_thesis_5x5_3puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\5x5\\3.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

//    @Test
    public void test_thesis_5x5_4puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\5x5\\4.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

    @Test
    public void test_thesis_5x5_5puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\5x5\\5.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

//    @Test
    public void test_thesis_5x5_6puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\5x5\\6.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

//    @Test
    public void test_thesis_7x7_1puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\7x7\\1.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

//    @Test
    public void test_thesis_7x7_2puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\7x7\\2.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

//    @Test
    public void test_thesis_9x9_1puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\9x9\\1.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }

    @Test
    public void test_thesis_15x15_1puz() throws Exception {
        CWPuzzle cwPuzzle = CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream("F:\\dev\\thesis-test-data\\15x15\\1.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);

        CWPuzzle answer = testInstance.solvePuzzle(cwPuzzle, new IPuzzleSolver.NoOpPuzzleListener());
        Assert.assertTrue("Failure: " + answer.getCorrectPercentage(), 80.0 < answer.getCorrectPercentage());
    }
}
