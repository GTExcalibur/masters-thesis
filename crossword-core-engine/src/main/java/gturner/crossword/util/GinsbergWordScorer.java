package gturner.crossword.util;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class GinsbergWordScorer {

    public static void main(String[] args) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("F:\\dev\\thesis\\ginsberg-clues.txt"));

        Pattern compile = Pattern.compile("^([A-Z]+)\\s+\\d.*$");
        Map<String, Long> hitCount = new TreeMap<String, Long>();

        String line;
        while((line = bufferedReader.readLine()) != null) {
            Matcher matcher = compile.matcher(line);
            if(matcher.matches()) {
                String match = matcher.group(1);
                Long count = hitCount.get(match);
                if(count == null) {
                    count = 0l;
                }
                hitCount.put(match, count+1);
            }
        }
        bufferedReader.close();

        PrintWriter printWriter = new PrintWriter(new FileWriter("F:\\dev\\thesis\\george-clues-score.txt"));
        for (Map.Entry<String, Long> entry : hitCount.entrySet()) {
            printWriter.println(entry.getKey()+","+entry.getValue());
        }
        printWriter.close();
    }
}
