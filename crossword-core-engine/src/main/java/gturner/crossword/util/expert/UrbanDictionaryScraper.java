package gturner.crossword.util.expert;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/30/13
 * Time: 8:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class UrbanDictionaryScraper {

    public static void main(String[] args) throws Exception {
        TreeSet<String> tokens = new TreeSet<String>();

        Pattern searchPattern = Pattern.compile("<a href=\"/define[.]php[?]term=[^\"]+\">([^<]+)</a>");
        Pattern fileName = Pattern.compile("([A-Z])(\\d+)\\.html$");

        Map<String, Integer> highestValues = new LinkedHashMap<String, Integer>();

        for (String htmlLetter : Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")) {
            for(int htmlFileNum = 1; htmlFileNum < 1230; htmlFileNum++) {
                File htmlFile = new File("C:\\Temp\\urbandictionary\\scrape\\" + htmlLetter + htmlFileNum + ".html");
                if(!htmlFile.exists()) continue;

                FileInputStream source = new FileInputStream(htmlFile);
                String contents = new Scanner(source).useDelimiter("\\A").next();
                source.close();

                if(contents.contains("<title>Urban Dictionary, March")) {
                    while(htmlFile.exists()) htmlFile.delete();
                    continue;
                }

                Matcher nameMatch = fileName.matcher(htmlFile.getName());
                if(nameMatch.find()) {
                    String letter = nameMatch.group(1);
                    String fileNum = nameMatch.group(2);
                    int fileAsNum = Integer.parseInt(fileNum);

                    if(!highestValues.containsKey(letter) || highestValues.get(letter) < fileAsNum) {
                        highestValues.put(letter, fileAsNum);
                    }
                }

                Matcher matcher = searchPattern.matcher(contents);
                while(matcher.find()) {
                    tokens.add(StringEscapeUtils.unescapeHtml(matcher.group(1)));
                }
            }
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Temp\\urbandictionary.txt")));
        long count = 0;
        for (String token : tokens) {
            writer.write(token);
            writer.newLine();
            if(count++ % 500 == 0) {
                writer.flush();
            }
        }

        writer.flush();

        System.out.println(highestValues);
    }
}
