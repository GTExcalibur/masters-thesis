package gturner.expert.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 10/19/14
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */
public final class AllAnswersExpert implements IClueExpert {

    private final IClueExpert clueExpert;
    private final Multimap<Long, String> cluesByLength;

    public AllAnswersExpert(IClueExpert clueExpert, InputStream inputStream) throws IOException {
        this.clueExpert = clueExpert;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        cluesByLength = HashMultimap.create();

        String line;
        while((line = bufferedReader.readLine()) != null) {
            String[] split = line.split(",");
            cluesByLength.put((long) split[0].length(), split[0]);
        }
        bufferedReader.close();
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        Map<String, Long> results = clueExpert.performSearch(clue, constraint);
        HashMap<String, Long> copy = new HashMap<String, Long>(results);
        Collection<String> allAnswers = cluesByLength.get((long) constraint.length());
        for (String allAnswer : allAnswers) {
            if(!copy.containsKey(allAnswer)) {
                copy.put(allAnswer, 0l);
            }
        }
        return copy;
    }
}
