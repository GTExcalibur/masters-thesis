package gturner.expert.impl.scenario;

import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 8:40 PM
 * To change this template use File | Settings | File Templates.
 */
public final class OmniscientExpert implements IClueExpert {

    private final long delay;

    public OmniscientExpert(long delay) {
        this.delay = delay;
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        try {
            Thread.sleep(delay);
            return Collections.singletonMap(getAnswer(clue), 1l);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getAnswer(CWClue clue) throws Exception {
        Method getClueAnswer = clue.getClass().getDeclaredMethod("getClueAnswer");
        getClueAnswer.setAccessible(true);
        return (String)getClueAnswer.invoke(clue);
    }
}
