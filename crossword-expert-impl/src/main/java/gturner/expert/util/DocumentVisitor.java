package gturner.expert.util;

import gturner.expert.query.QueryResultVisitor;
import gturner.expert.query.SolrQueryManager;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/31/13
 * Time: 8:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentVisitor {

    public static void main(String[] args) throws Exception {
        Map<String, String> entries = new LinkedHashMap<String, String>();
        entries.put("wikipedia", "localhost:8983/solr/wikipedia");
        entries.put("wiktionary", "localhost:8983/solr/wiktionary");
        entries.put("wikiquote", "localhost:8983/solr/wikiquote");
        entries.put("wikibooks", "localhost:8983/solr/wikibooks");
        entries.put("wikinews", "localhost:8983/solr/wikinews");
        entries.put("imdb", "localhost:8983/solr/imdb");


        SolrQueryManager solrQueryManager = new SolrQueryManager("http://localhost:8983/solr", entries);
        solrQueryManager.setRows(10000);
        solrQueryManager.setMaxRows(Integer.MAX_VALUE);

        final Map<Pattern, Set<String>> database = new LinkedHashMap<Pattern, Set<String>>();
//        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%2s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%3s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%4s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%5s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%6s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%7s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        /*database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%8s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%9s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%10s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%11s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%12s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%13s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%14s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%15s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%16s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%17s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%18s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%19s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%20s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%21s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%22s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));
        database.put(ConstraintPattern.createConstraintPatternForConstraint(String.format("%23s", "").replace(' ', '\u0000')), Collections.synchronizedSortedSet(new TreeSet<String>()));*/

        final Semaphore permits = new Semaphore(100);
        final ExecutorService service = Executors.newFixedThreadPool(4);

//        for (String shards : entries.keySet()) {
//            System.out.println("Starting shard: " + shards);

            String shards = "wiktionary";
            solrQueryManager.performQuery(Collections.singletonList(shards), "*:*", new QueryResultVisitor() {
                @Override
                public void visitResult(String title, String content, Set<String> keywords, double relativeScore) {
                    for (Map.Entry<Pattern, Set<String>> setEntry : database.entrySet()) {
                        final Set<String> answerSet = setEntry.getValue();
                        final Matcher matcher = setEntry.getKey().matcher(content);

                        service.submit(new Callable<Void>() {
                            public Void call() throws Exception {
                                try {
                                    permits.acquire();
                                    while (matcher.find()) {
                                        String match = matcher.group();
                                        String answer = match.replaceAll("[^a-zA-Z]", "").toUpperCase();
                                        if (!answer.isEmpty()) {
                                            answerSet.add(answer);
                                        }
                                    }
                                } finally {
                                    permits.release();
                                }
                                return null;
                            }
                        });
                    }
                }

                @Override public void noDataFound() {}
            });
//        }

        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        int fileCount = 3;
        int count = 0;
        for (Set<String> strings : database.values()) {
            File outputFile = new File("C:\\Temp\\answers\\" + fileCount++ + ".txt");
            if(outputFile.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile)));
                String token = reader.readLine();
                while(token != null) {
                    strings.add(token);
                    token = reader.readLine();
                }
                reader.close();
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
            for (String string : strings) {
                writer.write(string);
                writer.newLine();
                if(count++ % 500 == 0) {
                    writer.flush();
                }
            }

            writer.flush();
            writer.close();
        }
    }
}
