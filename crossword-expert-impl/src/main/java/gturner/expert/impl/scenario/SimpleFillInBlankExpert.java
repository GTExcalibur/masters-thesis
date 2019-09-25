package gturner.expert.impl.scenario;

import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;
import gturner.expert.query.ISolrQueryManager;
import gturner.expert.query.QueryResultVisitor;
import gturner.expert.util.ConstraintPattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/25/14
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */
public final class SimpleFillInBlankExpert implements IClueExpert {
    private final ISolrQueryManager solr;

    private final Pattern lhs = Pattern.compile("([^ \\)]+) _+");
    private final Pattern lhs2 = Pattern.compile("([^ \\)]+) ([^ \\)]+) _+");
    private final Pattern rhs = Pattern.compile("_+ ([^ \\(]+)");
    private final Pattern rhs2 = Pattern.compile("_+ ([^ \\(]+) ([^ \\(]+)");
    private final List<String> shardsToQuery;
    private final boolean urbanDictionary;

    private String compoundPhraseFile;

    public SimpleFillInBlankExpert(ISolrQueryManager solr, List<String> shardsToQuery, boolean urbanDictionary) {
        this.solr = solr;
        this.shardsToQuery = shardsToQuery;
        this.urbanDictionary = urbanDictionary;
    }

    private String getCompoundPhraseFile() {
        if(compoundPhraseFile == null) {
            try {
                FileInputStream urbanDictionaryFile = new FileInputStream(new File("C:\\Temp\\compound-phrase.txt"));
                compoundPhraseFile = new Scanner(urbanDictionaryFile).useDelimiter("\\A").next();
                urbanDictionaryFile.close();
            } catch (IOException e) {
                e.printStackTrace();
                compoundPhraseFile = "";
            }
        }
        return compoundPhraseFile;
    }

    @Override
    public Map<String, Long> performSearch(CWClue clue, String constraint) {
        StringBuilder solrQuery = new StringBuilder();
        boolean quoted = false;

        final String clueText = ConstraintPattern.stripOddOccurrences(clue.getClueText());
        if(clueText.contains("\"")) {
            Matcher quoteFinder = Pattern.compile("\"([^\"]+)\"").matcher(clueText);
            List<String> quotes = new ArrayList<String>();
            while(quoteFinder.find()) {
                quotes.add(quoteFinder.group(1));
            }

            for (String quote : quotes) {
                if(quote.contains("_")) {
                    String[] elements = quote.split("_+");
                    for (String element : elements) {
                        element = element.trim();
                        if(!element.isEmpty()) {
                            element = element.replace("(", "").replace(")", "");
                            solrQuery.append(String.format("keywords:\"%s\"^10 text:\"%s\"^8 keywords:(%s)^2 text:(%s) ", element, element, element, element));
                        }
                    }
                } else {
                    quote = quote.replace("(", "").replace(")", "");
                    solrQuery.append(String.format("keywords:\"%s\"^10 text:\"%s\"^8 keywords:(%s)^2 text:(%s) ", quote, quote, quote, quote));
                }
            }
        }
        String[] elements = clueText.replace("\"", "").split("_+");
        for (String element : elements) {
            element = element.trim();
            if(!element.isEmpty()) {
                element = element.replace("(", "").replace(")", "");
                solrQuery.append(String.format("keywords:\"%s\"^10 text:\"%s\"^8 keywords:(%s)^2 text:(%s) ", element, element, element, element));
            }
        }

        final Boolean[] pNoDataFound = new Boolean[1];
        final Map<String, Long> hits = new LinkedHashMap<String, Long>();

        final Map<Pattern, Long> patternToScore = new LinkedHashMap<Pattern, Long>();

        patternToScore.put(
                Pattern.compile(replaceBlankWithPattern(constraint, clueText.replace("\"", "")),
                                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
                100l
        );

        patternToScore.put(
                Pattern.compile(replaceBlankWithPattern(constraint, clueText.replace("\"", "").replaceAll("\\([^\\)]*\\)", "")),
                                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
                100l
        );

        if (urbanDictionary) {
            for (Map.Entry<Pattern, Long> entry : patternToScore.entrySet()) {
                for (String specialFiles : Arrays.asList(getCompoundPhraseFile())) {
                    Matcher phraseMatcher = entry.getKey().matcher(specialFiles);
                    while(phraseMatcher.find()) {
                        String match = phraseMatcher.group(1);
                        String answer = match.replaceAll("[^a-zA-Z]", "").toUpperCase();
                        Long count = hits.get(answer);
                        if(count == null) {
                            count = entry.getValue() * entry.getValue();
                        } else {
                            count += entry.getValue() * entry.getValue();
                        }
                        hits.put(answer, count * 100000);
                    }
                }
            }

            if(hits.size() == 1) {
//                System.out.println("Found match in Urban Dictionary.");
                return hits;
            }
        }

        Matcher matcher = lhs.matcher(clueText);
        if(matcher.find()) {
            String group = matcher.group().replace("\"", "");
            if(!group.toLowerCase().startsWith("the") && !group.toLowerCase().startsWith("of")) {
                patternToScore.put(
                        Pattern.compile(replaceBlankWithPattern(constraint, group),
                                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
                        10l
                );
            }

            matcher = lhs2.matcher(clueText);
            if(matcher.find()) {
                group = matcher.group().replace("\"", "");
                patternToScore.put(
                        Pattern.compile(replaceBlankWithPattern(constraint, group),
                                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
                        30l
                );
            }
        }

        matcher = rhs.matcher(clueText);
        if(matcher.find()) {
            patternToScore.put(
                    Pattern.compile(replaceBlankWithPattern(constraint, matcher.group().replace("\"", "")),
                                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
                    10l
            );

            matcher = rhs2.matcher(clueText);
            if(matcher.find()) {
                patternToScore.put(
                        Pattern.compile(replaceBlankWithPattern(constraint, matcher.group().replace("\"", "")),
                                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
                        30l
                );
            }
        }

        if (!shardsToQuery.isEmpty()) {
//            System.out.println(solrQuery.toString());
            solr.performQuery(shardsToQuery, solrQuery.toString(), new QueryResultVisitor() {
                @Override
                public void visitResult(String title, String content, Set<String> keywords, double relativeScore) {
                    pNoDataFound[0] = false;

                    Map<String, Long> searchAreaWithMulti = new LinkedHashMap<String, Long>();
                    searchAreaWithMulti.put(title, 3l);
                    searchAreaWithMulti.put(content, 1l);
                    for (String keyword : keywords) {
                        searchAreaWithMulti.put(keyword, 2l);
                    }

                    for (Map.Entry<Pattern, Long> entry : patternToScore.entrySet()) {
                        for (Map.Entry<String, Long> searchArea : searchAreaWithMulti.entrySet()) {
                            Matcher matcher = entry.getKey().matcher(searchArea.getKey());
                            while(matcher.find()) {
                                String match = matcher.group(1);
                                String answer = match.replaceAll("[^a-zA-Z]", "").toUpperCase();
                                Long count = hits.get(answer);
                                if(count == null) {
                                    count = entry.getValue() * entry.getValue();
                                } else {
                                    count += entry.getValue() * entry.getValue();
                                }
                                hits.put(answer, count);
                            }
                        }
                    }
                }

                @Override
                public void noDataFound() {
                    pNoDataFound[0] = true;
                }
            });
        } else {
            pNoDataFound[0] = true;
        }

        if(pNoDataFound[0]) {
            return Collections.emptyMap();
        }

        return hits;
    }

    private static String replaceBlankWithPattern(String constraint, String group) {
        String pattern = group.replaceAll("([A-Za-z]) ", "$1[.,]? ").
                               replaceAll("[A-Za-z]", "$0[']?").
                               replace("(", "").
                               replace(")", "").
                               replaceAll("\\s*_+\\s*", "***").
                               replace("***", "(" + ConstraintPattern.getConstraintRegex(constraint) + ")");

//        System.out.println(pattern);

        return pattern;
    }
}
