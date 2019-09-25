package gturner.expert.impl;

import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;
import gturner.expert.util.ClusteringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/25/14
 * Time: 9:49 AM
 * To change this template use File | Settings | File Templates.
 */
public final class ClusteringExpert implements IClueExpert {

    private final IClueExpert delegate;

    public ClusteringExpert(IClueExpert delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        Map<String, Long> scoreMap = delegate.performSearch(clue, constraint);

        HashMap<String, Long> result = new HashMap<String, Long>();
        final List<Map.Entry<String,Long>> highClusterValues = ClusteringUtils.getHighClusterValues(scoreMap);
        for (Map.Entry<String, Long> highClusterValue : highClusterValues) {
            result.put(highClusterValue.getKey(), highClusterValue.getValue());
        }
        return result;
    }
}
