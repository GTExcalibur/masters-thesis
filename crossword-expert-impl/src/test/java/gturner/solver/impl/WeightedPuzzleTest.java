package gturner.solver.impl;

import gturner.crossword.spec.CWPuzzle;
import junit.framework.Assert;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class WeightedPuzzleTest {

    @Test
    public void test_comparator() throws Exception {
        CWPuzzle mockTest = EasyMock.createNiceMock(CWPuzzle.class);
        EasyMock.expect(mockTest.copy()).andReturn(mockTest).anyTimes();

        EasyMock.replay(mockTest);

        WeightedPuzzle start = WeightedPuzzle.initialize(mockTest);

        WeightedPuzzle puzzle1 = start.performUpdate(null, "", true, 0l);
        WeightedPuzzle puzzle2 = start.performUpdate(null, "", true, 0l).performUpdate(null, "", true, 0l);
        WeightedPuzzle puzzle3 = start.performUpdate(null, "", true, 0l).performUpdate(null, "", true, 1l);
        WeightedPuzzle puzzle4 = start.performUpdate(null, "", true, 0l).performUpdate(null, "", false, 1l);

        ArrayList<WeightedPuzzle> testInstances = new ArrayList<WeightedPuzzle>();
        testInstances.add(puzzle1);
        testInstances.add(puzzle2);
        testInstances.add(puzzle3);
        testInstances.add(puzzle4);
        Collections.sort(testInstances);

        Assert.assertSame(puzzle3, testInstances.get(0));
        Assert.assertSame(puzzle2, testInstances.get(1));
        Assert.assertSame(puzzle1, testInstances.get(2));
        Assert.assertSame(puzzle4, testInstances.get(3));

        EasyMock.verify(mockTest);
    }

    @Test
    public void test_betterFit() throws Exception {
        CWPuzzle mockTest = EasyMock.createNiceMock(CWPuzzle.class);
        EasyMock.expect(mockTest.copy()).andReturn(mockTest).anyTimes();

        EasyMock.replay(mockTest);

        WeightedPuzzle start = WeightedPuzzle.initialize(mockTest);

        WeightedPuzzle puzzle1 = start.performUpdate(null, "", true, 0l);
        WeightedPuzzle puzzle2 = start.performUpdate(null, "", true, 0l).performUpdate(null, "", true, 0l);
        WeightedPuzzle puzzle3 = start.performUpdate(null, "", true, 0l).performUpdate(null, "", true, 1l);
        WeightedPuzzle puzzle4 = start.performUpdate(null, "", true, 0l).performUpdate(null, "", false, 1l);

        Assert.assertTrue(puzzle3.betterFit(puzzle4));
        Assert.assertTrue(puzzle3.betterFit(puzzle2));
        Assert.assertTrue(puzzle3.betterFit(puzzle1));
        Assert.assertFalse(puzzle1.betterFit(puzzle4));

        EasyMock.verify(mockTest);

    }
}
