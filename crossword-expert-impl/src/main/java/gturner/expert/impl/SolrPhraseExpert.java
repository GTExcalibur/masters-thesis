package gturner.expert.impl;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;
import gturner.expert.impl.helper.TextTokenizer;
import gturner.expert.query.ISolrQueryManager;
import gturner.expert.query.QueryResultVisitor;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import org.apache.commons.codec.language.Soundex;

import java.text.Collator;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 10/5/14
 * Time: 12:34 PM
 * To change this template use File | Settings | File Templates.
 */
public final class SolrPhraseExpert implements IClueExpert {

    private final ISolrQueryManager solr;
    private final List<String> shards;
    private final List<String> stopWords;

    public SolrPhraseExpert(ISolrQueryManager solr, List<String> shards, List<String> stopWords) {
        this.solr = solr;
        this.shards = shards;
        this.stopWords = stopWords;
    }

    @Override
    public Map<String, Long> performSearch(final CWClue clue, final String constraint) {
        final String clueText = clue.getClueText();
        String clueQuery = clueText.replaceAll("[^A-Za-z0-9']", " ").replaceAll("\\s+", " ").trim();

        StringBuilder solrQuery = new StringBuilder();
        for (String queryElement : Arrays.asList(
                "{!edismax}(%s)"
//                "{!dismax}(%s)"
        )) {
            solrQuery.append(String.format(queryElement, clueQuery)).append(" ");
        }

        final Map<String, Long> hits = new LinkedHashMap<String, Long>();

        final Boolean[] pNoDataFound = new Boolean[1];

        final ScoringTokenVisitor scorer = new ScoringTokenVisitor(constraint, clueText);

        solr.performQuery(shards, solrQuery.toString(), new QueryResultVisitor() {
            @Override
            public void visitResult(String title, String content, Set<String> keywords, double relativeScore) {
                pNoDataFound[0] = false;

                scorer.setImportantText(true);
                TextTokenizer.parseTokens(clue.getLength(), title, scorer);
                scorer.setImportantText(false);
                TextTokenizer.parseTokens(clue.getLength(), content, scorer);
                scorer.setImportantText(true);
                for (String keyword : keywords) {
                    TextTokenizer.parseTokens(clue.getLength(), keyword, scorer);
                }
            }

            @Override
            public void noDataFound() {
                pNoDataFound[0] = true;
            }
        });

        if(pNoDataFound[0]) {
            return Collections.emptyMap();
        }
        scorer.pushToMap(hits);
//        System.out.println(clue.toString());
//        List<Map.Entry<String, Long>> localElements = new ArrayList<Map.Entry<String, Long>>(hits.entrySet());
//        Collections.sort(localElements, new Comparator<Map.Entry<String, Long>>() {
//            @Override
//            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
//                int compare = o2.getValue().compareTo(o1.getValue());
//                if(compare == 0) {
//                    compare = Collator.getInstance().compare(o1.getKey(), o2.getKey());
//                }
//                return compare;
//            }
//        });
//        System.out.println(Arrays.toString(localElements.toArray()));

        return hits;
    }


    private final class ScoringTokenVisitor extends ConstrainingTokenVisitor {
        private final List<String> leftWords;
        private final List<String> rightWords;

        private final Map<String, Long> bestScore = new HashMap<String, Long>();
        private final Map<String, Long> importanceScore = new HashMap<String, Long>();
        private final Map<String, Long> hitsScore = new HashMap<String, Long>();

        private boolean isImportantText = false;

        private final StringSimilarityServiceImpl similarityService = new StringSimilarityServiceImpl(new JaroWinklerStrategy());

        private ScoringTokenVisitor(String constraintPattern, String clueText) {
            super(constraintPattern);

            String[] sections = clueText.split("_+");
            if(sections.length > 2) {
                throw new RuntimeException("Bad state!");
            } else if(sections.length == 1) {
                if(clueText.endsWith("_")) {
                    leftWords = Arrays.asList(sections[0].replaceAll("[^A-Za-z0-9']", " ").replaceAll("\\s+", " ").trim().split("[-+ ]"));
                    rightWords = Collections.emptyList();
                } else {
                    leftWords = Collections.emptyList();
                    rightWords = Arrays.asList(sections[0].replaceAll("[^A-Za-z0-9']", " ").replaceAll("\\s+", " ").trim().split("[-+ ]"));
                }
            } else {
                leftWords = Arrays.asList(sections[0].replaceAll("[^A-Za-z0-9']", " ").replaceAll("\\s+", " ").trim().split("[-+ ]"));
                rightWords = Arrays.asList(sections[1].replaceAll("[^A-Za-z0-9']", " ").replaceAll("\\s+", " ").trim().split("[-+ ]"));
            }
        }

        public void setImportantText(boolean importantText) {
            isImportantText = importantText;
        }

        @Override
        protected void visitTokenImpl(List<String> previous, String token, boolean composite, List<String> next) {
            long leftScore = 0;
            final int leftWordsSize = leftWords.size();
            final int previousSize = previous.size();
            long leftWordCount = 0;

            for(int i = 0; i < leftWordsSize && i < previousSize; i++) {
                int multiplier = Math.max(10 - i*i, 1);
                int difference = 0;
                try {
                    String target = previous.get(previousSize - i - 1);
                    if(!stopWords.contains(target)) {
                        String feature = leftWords.get(leftWordsSize - i - 1);

                        if(target.equalsIgnoreCase(feature)) {
                            difference = 4;
                        }

//                        difference = (int) (4 * similarityService.score(feature, target));
    //                    difference = (int) (4 * WS4J.runWUP(leftWords.get(leftWordsSize - i - 1), previous.get(previousSize - i - 1)));
    //                    difference = Soundex.US_ENGLISH.difference(leftWords.get(leftWordsSize - i - 1), previous.get(previousSize - i - 1));
    //                    difference = difference / 2; // we don't want weak matches to score
                    }
                } catch (Exception e) {
                    // ignore
                }
                leftScore += multiplier * difference;
                leftWordCount++;

                if(difference == 0) {
                    break;
                }
            }

            long rightScore = 0;
            final int rightWordsSize = rightWords.size();
            final int nextSize = next.size();
            long rightWordCount = 0;

            for(int i = 0; i < rightWordsSize && i < nextSize; i++) {
                int multiplier = Math.max(10 - i*i, 1);
                int difference = 0;
                try {
                    String target = next.get(i);
                    if(!stopWords.contains(target)) {
                        String feature = rightWords.get(i);

                        if(target.equalsIgnoreCase(feature)) {
                            difference = 4;
                        }

//                        difference = (int) (4 * similarityService.score(feature, target));
    //                    difference = (int) (4 * WS4J.runWUP(rightWords.get(i), next.get(i)));
    //                    difference = Soundex.US_ENGLISH.difference(rightWords.get(i), next.get(i));
    //                    difference = difference / 2; // we don't want weak matches to score
                    }
                } catch (Exception e) {
                    // ignore
                }
                rightScore += multiplier * difference;
                rightWordCount++;

                if(difference == 0) {
                    break;
                }
            }

            long newScore = 0;
            if(leftWordCount > 0) {
                newScore += 10 * leftScore / leftWordCount;
            }
            if(rightWordCount > 0) {
                newScore += 10 * rightScore / rightWordCount;
            }
            if(composite) {
                newScore = (long) (newScore * 0.75);
            }

            Long oldBestScore = bestScore.get(token);
            if(oldBestScore == null || oldBestScore < newScore) {
                bestScore.put(token, newScore);
            }

            Long hits = hitsScore.get(token);
            if(hits == null) {
                hits = 0l;
            }
            hitsScore.put(token, hits+1);
            if(isImportantText && newScore > 0) {
                hits = importanceScore.get(token);
                if(hits == null) {
                    hits = 0l;
                }
                importanceScore.put(token, hits+1);
            }

//            localScore.put(token, 1l);
        }

        public void pushToMap(Map<String, Long> scoreMap) {
            if (!bestScore.isEmpty()) {
                final Long maxBestScore = Math.max(1, Collections.max(bestScore.values()));
                final Long importanceBestScore = importanceScore.isEmpty() ? 1 : Collections.max(importanceScore.values());
                final Long maxHitsScore = Math.max(1, Collections.max(hitsScore.values()));

                final List<String> negativeWords = Lists.transform(Lists.newArrayList(
                        Iterables.concat(leftWords, rightWords)), new Function<String, String>() {
                    @Override
                    public String apply(java.lang.String input) {
                        return TextTokenizer.crosswordizeText(input);
                    }
                });

                scoreMap.putAll(Maps.transformEntries(bestScore, new Maps.EntryTransformer<String, Long, Long>() {
                    @Override
                    public Long transformEntry(java.lang.String key, java.lang.Long value) {
                        Long localImportance = importanceScore.get(key);
                        if(localImportance == null) localImportance = 0l;
                        long potentialScore = 1 + (21 * value / maxBestScore) + (2 * localImportance / importanceBestScore) + (1 * hitsScore.get(key) / maxHitsScore);
                        if(stopWords.contains(key)) {
                            potentialScore = potentialScore - 5;
                            potentialScore = Math.max(1, potentialScore);
                        }
                        if(negativeWords.contains(key)) {
                            potentialScore = potentialScore - 5;
                            potentialScore = Math.max(1, potentialScore);
                        }
                        return potentialScore;
                    }
                }));
            }
//            scoreMap.putAll(localScore);
        }
    }

    private static abstract class ConstrainingTokenVisitor implements TextTokenizer.TokenVisitor {
        private final String constraintPattern;

        private ConstrainingTokenVisitor(String constraintPattern) {
            this.constraintPattern = constraintPattern;
        }

        @Override
        public final void visitToken(List<String> previous, String token, boolean composite, List<String> next) {
            for(int i = 0; i < constraintPattern.length(); i++) {
                char constraint = constraintPattern.charAt(i);
                if(constraint != '\u0000' && constraint != token.charAt(i)) {
                    return;
                }
            }
            visitTokenImpl(previous, token, composite, next);
        }

        protected abstract void visitTokenImpl(List<String> previous, String token, boolean composite, List<String> next);
    }
}
