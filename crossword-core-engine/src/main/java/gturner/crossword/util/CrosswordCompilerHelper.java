package gturner.crossword.util;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/13/14
 * Time: 1:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrosswordCompilerHelper {

    public static void main(String[] args) throws Exception {
        File file = new File("F:\\dev\\thesis\\ot-fill_in_the_blanks-temp.txt");
        File output = new File("C:\\Users\\George Turner\\AppData\\Roaming\\Crossword Compiler 9\\Clue Databases\\FillInTheBlank2.csv");
        PrintWriter outputWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));

        Pattern compile = Pattern.compile("(\\S+) ::: \\d+ ::: ([^\\n]+)");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        while((line = bufferedReader.readLine()) != null) {
            Matcher matcher = compile.matcher(line);
            matcher.find();

            String group = matcher.group(2);
            if(group.contains("\"")) {
                group = "\"" + group.replaceAll("\"", "\"\"") + "\"";
            } else if(group.contains(",")) {
                group = "\"" + group + "\"";
            }

            outputWriter.println(matcher.group(1).toLowerCase()+","+group);
        }
    }
}
