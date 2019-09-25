package gturner.expert.impl.scenario;

import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/25/14
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
public final class AnswerChooserExpert implements IClueExpert {

    private final IClueExpert delegate;

    public AnswerChooserExpert(IClueExpert delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        try {
            String answer = getAnswer(clue);
            Map<String, Long> scoreMap = delegate.performSearch(clue, constraint);
            if(scoreMap.containsKey(answer)) {
                return Collections.singletonMap(answer, Long.MAX_VALUE);
            } else {
                return Collections.emptyMap();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String getAnswer(CWClue clue) throws Exception {
        Method getClueAnswer = clue.getClass().getDeclaredMethod("getClueAnswer");
        getClueAnswer.setAccessible(true);
        return (String)getClueAnswer.invoke(clue);
    }
}
