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
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/25/14
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
public final class RandomChooserExpert implements IClueExpert {

    private final Map<String, Long> clueWeights;

    public RandomChooserExpert(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        ImmutableMap.Builder<String, Long> builder = new ImmutableMap.Builder<String, Long>();

        String line;
        while((line = bufferedReader.readLine()) != null) {
            String[] split = line.split(",");
            long value = Long.parseLong(split[1]);
            builder.put(split[0], value > 10 ? 10 : value);
        }
        bufferedReader.close();

        clueWeights = builder.build();
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        Map<String, Long> scoreMap = getGeneralValues(constraint);
        if(scoreMap.isEmpty()) {
            return scoreMap;
        }

        ArrayList<String> answers = new ArrayList<String>(scoreMap.keySet());
        int randomNumber = new Random(System.currentTimeMillis()).nextInt(answers.size());
        return Collections.singletonMap(answers.get(randomNumber), 1l);
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
