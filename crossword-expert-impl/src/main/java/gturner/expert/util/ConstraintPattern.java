package gturner.expert.util;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/23/13
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintPattern {

    public static String stripOddOccurrences(String clue) {
        clue = clue.replace(". . .", "");
        clue = clue.replace("...", "");
        clue = clue.replace("!", "");
        clue = clue.replace("?", "");
        clue = clue.replace(".", "");
        clue = clue.replace("/", "");
        clue = clue.replace("\\\\", "");
        clue = clue.replace(",", "");
        clue = clue.replace("'", "");
        clue = clue.replace(":", "");
        clue = clue.replace("+", "");
        clue = clue.replace("*", "");
        clue = clue.replace("{", "");
        clue = clue.replace("}", "");
        clue = clue.replace("^", "");
        clue = clue.replace("-", " "); // replace with white space rather than removing
        return clue;
    }

    public static Pattern createConstraintPatternForConstraint(String constraint) {
        return Pattern.compile(
                getConstraintRegex(constraint),
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    }

    public static String getConstraintRegex(String constraint) {
        return "(^|[\\[\\]\\(\\)\\.,\\!\\?<>': \r\n])" +
                constraint.replaceAll("[A-Z]", "$0(| |[ ]{0,1}[.']|[.'][ ]{0,1})").
                           replaceAll("\u0000", "[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})") +
                "($|[\\[\\]\\(\\)\\.,\\!\\?<>': \r\n])";
    }
}
