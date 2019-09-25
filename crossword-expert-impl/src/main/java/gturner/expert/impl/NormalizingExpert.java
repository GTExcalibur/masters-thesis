package gturner.expert.impl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 10/11/14
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public final class NormalizingExpert implements IClueExpert {

    private final IClueExpert delegate;

    public NormalizingExpert(IClueExpert delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        final Map<String, Long> scoreMap = delegate.performSearch(clue, constraint);
        if(scoreMap != null && !scoreMap.isEmpty()) {
            Percentile ranking = new Percentile();
            ranking.setData(createDoubleArray(scoreMap.values()));
            final double[] scoreLookup = getScoreLookup(ranking);

            return Maps.transformValues(scoreMap, new Function<Long, Long>() {
                @Override
                public Long apply(java.lang.Long input) {
                    return getScore(scoreLookup, input);
                }
            });
        }
        return scoreMap;
    }

    private static double[] createDoubleArray(Collection<Long> values) {
        double[] doubles = new double[values.size() + 1];
        int i = 0;
        for (Long value : values) {
            doubles[i++] = value.doubleValue();
        }
        doubles[i] = 25.0;
        return doubles;
    }

    private static Long getScore(double[] lookup, Long value) {
        for(int i = 0; i < lookup.length; i++) {
            if(lookup[i] < value.doubleValue()) {
                return (long) lookup.length - i;
            }
        }
        return 0l;
    }

    private static double[] getScoreLookup(Percentile percentile) {
        double[] percentiles = new double[25];
        int i = 0;
        percentiles[i++] = percentile.evaluate(100.0); // 0
        percentiles[i++] = percentile.evaluate(99.9);
        percentiles[i++] = percentile.evaluate(99.75);
        percentiles[i++] = percentile.evaluate(99.5);
        percentiles[i++] = percentile.evaluate(99.25);
        percentiles[i++] = percentile.evaluate(99.0);
        percentiles[i++] = percentile.evaluate(98.0);
        percentiles[i++] = percentile.evaluate(97.0);
        percentiles[i++] = percentile.evaluate(96.0);
        percentiles[i++] = percentile.evaluate(95.0);
        percentiles[i++] = percentile.evaluate(94.0);
        percentiles[i++] = percentile.evaluate(93.0);
        percentiles[i++] = percentile.evaluate(92.0);
        percentiles[i++] = percentile.evaluate(91.0);
        percentiles[i++] = percentile.evaluate(90.0);
        percentiles[i++] = percentile.evaluate(85.0);
        percentiles[i++] = percentile.evaluate(80.0);
        percentiles[i++] = percentile.evaluate(70.0);
        percentiles[i++] = percentile.evaluate(60.0);
        percentiles[i++] = 0.0d;

        return percentiles;
    }

}
