package gturner.solver.impl;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import gturner.crossword.spec.CWClue;
import gturner.expert.util.ClusteringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
class SearchWrapper implements Comparable<SearchWrapper> {

    final CWClue clue;
    final int filledLetters;
    final Map<String, Long> backingAnswers;

    final boolean singleAnswer;
    final Long bestScore;
    Iterable<String> bestAnswers;

    public SearchWrapper(CWClue clue, int filledLetters, Map<String, Long> backingAnswers) {
        this.clue = clue;
        this.filledLetters = filledLetters;
        this.backingAnswers = backingAnswers;

        final List<Map.Entry<String,Long>> highClusterValues = backingAnswers == null ? null : new ArrayList<Map.Entry<String,Long>>(backingAnswers.entrySet());
        if(highClusterValues != null && highClusterValues.size() == 1) {
            singleAnswer = true;
            bestScore = highClusterValues.get(0).getValue();
            bestAnswers = Collections.singletonList(highClusterValues.get(0).getKey());
        } else if(highClusterValues != null && !highClusterValues.isEmpty()) {
            singleAnswer = false;
            bestScore = highClusterValues.get(0).getValue();
            bestAnswers = Lists.transform(highClusterValues, new Function<Map.Entry<String, Long>, String>() {
                @Override
                public String apply(java.util.Map.Entry<String, Long> input) {
                    return input.getKey();
                }
            });
        } else {
            singleAnswer = false;
            bestScore = 0l;
            bestAnswers = null;
        }
    }

    public CWClue getClue() {
        return clue;
    }

    public Iterable<String> getBestAnswers() {
        return bestAnswers;
    }

    public Long getBestScore() {
        return bestScore;
    }

    @Override
    public int compareTo(SearchWrapper o) {
        if(singleAnswer ^ o.singleAnswer) {
            return singleAnswer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        } else {
            if(filledLetters == o.filledLetters) {
                return bestScore.compareTo(o.bestScore);
            } else {
                return filledLetters - o.filledLetters;
            }
        }
    }

    @Override
    public String toString() {
        return "SearchWrapper{" +
                "clue=" + clue +
                ", singleAnswer=" + singleAnswer +
                ", bestAnswer='" + (bestAnswers == null ? "null" : Iterables.toString(bestAnswers)) + '\'' +
                ", bestScore=" + bestScore +
                '}';
    }
}
