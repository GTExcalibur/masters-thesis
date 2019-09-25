package gturner.expert.impl.scenario;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;
import gturner.expert.util.ConstraintPattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/25/14
 * Time: 9:12 AM
 * 1/250 error, with a 3 second delay
 */
public final class HumanExpert implements IClueExpert {

    private final int errorRate;
    private final long delay;
    private final Map<String, Long> clueWeights;
    private final Map<CWClue, Object> visitedClues = new WeakHashMap<CWClue, Object>();

    private int failure;

    public HumanExpert(int errorRate, long delay, long defaultScore, InputStream inputStream) throws IOException {
        this.errorRate = errorRate;
        this.delay = delay;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        ImmutableMap.Builder<String, Long> builder = new ImmutableMap.Builder<String, Long>();

        String line;
        while((line = bufferedReader.readLine()) != null) {
            String[] split = line.split(",");
            builder.put(split[0], defaultScore);
        }
        bufferedReader.close();

        clueWeights = builder.build();

        failure = nextRandom();
    }

    private int nextRandom() {
        int tenPercent = (int) (errorRate * 0.1);
        return new Random(System.currentTimeMillis()).nextInt(tenPercent) + (errorRate - tenPercent);
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        try {
            Thread.sleep(delay);
            String answer = getAnswer(clue);

            boolean makeError;
            synchronized (this) {
                if(visitedClues.put(clue, this) == null) {
                    makeError = (failure-- == 0);
                    if(makeError) {
                        failure = nextRandom();
                    }
                } else {
                    makeError = false;
                }
            }

            if(makeError) {
                Map<String, Long> generalValues = getGeneralValues(constraint);
                generalValues.remove(answer);
                if(generalValues.isEmpty()) {
//                    System.out.println("Human: Failure with no answer");
                    return Collections.emptyMap();
                } else {
//                    System.out.println("Human: Failure with wrong answer");
                    return Collections.singletonMap(generalValues.keySet().iterator().next(), 1000l);
                }
            } else {
                if(ConstraintPattern.createConstraintPatternForConstraint(constraint).matcher(answer).matches()) {
                    return Collections.singletonMap(answer, 1l);
                } else {
//                    System.out.println("Human: Bad constraint, guessing");
                    return Collections.emptyMap();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getAnswer(CWClue clue) throws Exception {
        Method getClueAnswer = clue.getClass().getDeclaredMethod("getClueAnswer");
        getClueAnswer.setAccessible(true);
        return (String)getClueAnswer.invoke(clue);
    }

    private Map<String, Long> getGeneralValues(final String constraint) {
        final Pattern pattern = ConstraintPattern.createConstraintPatternForConstraint(constraint);

        Map<String, Long> result = new HashMap<String, Long>(Maps.filterEntries(clueWeights, new Predicate<Map.Entry<String, Long>>() {
            @Override
            public boolean apply(java.util.Map.Entry<String, Long> input) {
                return input.getKey().length() == constraint.length() && pattern.matcher(input.getKey()).matches();
            }
        }));
        return result;
    }
}
