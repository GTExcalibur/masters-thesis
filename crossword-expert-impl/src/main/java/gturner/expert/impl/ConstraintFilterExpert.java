package gturner.expert.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;
import gturner.expert.util.ConstraintPattern;

import java.io.Closeable;
import java.io.IOException;
import java.text.Collator;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/25/14
 * Time: 10:08 AM
 * A safety to prevent from modifying the puzzle
 */
public final class ConstraintFilterExpert implements IClueExpert, Closeable {

    private final IClueExpert delegate;

    public ConstraintFilterExpert(IClueExpert delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        final Pattern patternForConstraint = ConstraintPattern.createConstraintPatternForConstraint(constraint);

        Map<String, Long> scoreMap = new HashMap<String, Long>(Maps.filterEntries(delegate.performSearch(clue, constraint), new Predicate<Map.Entry<String, Long>>() {
            @Override
            public boolean apply(Map.Entry<String, Long> input) {
                return input.getKey() != null && patternForConstraint.matcher(input.getKey()).matches();
            }
        }));

        System.out.println(clue.toString());
        List<Map.Entry<String, Long>> localElements = new ArrayList<Map.Entry<String, Long>>(scoreMap.entrySet());
        Collections.sort(localElements, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                int compare = o2.getValue().compareTo(o1.getValue());
                if (compare == 0) {
                    compare = Collator.getInstance().compare(o1.getKey(), o2.getKey());
                }
                return compare;
            }
        });
        System.out.println(Arrays.toString(localElements.toArray()));

        return scoreMap;
    }

    @Override
    public void close() throws IOException {
        if(delegate instanceof Closeable) {
            ((Closeable)delegate).close();
        }
    }
}
