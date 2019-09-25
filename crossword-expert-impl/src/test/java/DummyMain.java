import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/24/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class DummyMain {

    public static void main(String[] args) {


        System.out.println(Pattern.compile("S[']?t[']?[.,]? J[']?o[']?h[']?n[']?s[']?[.,]?((^|[\\[\\]\\(\\)\\.,\\!\\?<>': \n" +
                "])[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})($|[\\[\\]\\(\\)\\.,\\!\\?<>': \n" +
                "]))", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher("\n" +
                "St. John's, Antigua and Barbuda \n" +
                "St. Johns, Saba, a village on the Caribbean island of Saba, a special municipality of the Netherlands\n" +
                "St John's wort, herb\n" +
                "HMCS St. John's (FFH 340), a Halifax-class frigate in the Canadian Navy\n" +
                "Leeds St Johns, a British rugby league football club today known as Leeds Rhinos\n" +
                "St. Johnsville, New York (disambiguation)\n").find());


        System.out.println(Pattern.compile("S[']?t[']?[.,]? J[']?o[']?h[']?n[']?s[']?[.,]?((^|[\\[\\]\\(\\)\\.\\!\\?<>': \n" +
                "])[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})[a-z])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher("\n" +
                "St. John's, Antigua and Barbuda \n" +
                "St. Johns, Saba, a village on the Caribbean island of Saba, a special municipality of the Netherlands\n" +
                "St John's wort, herb\n" +
                "HMCS St. John's (FFH 340), a Halifax-class frigate in the Canadian Navy\n" +
                "Leeds St Johns, a British rugby league football club today known as Leeds Rhinos\n" +
                "St. Johnsville, New York (disambiguation)\n").find());


        System.out.println(Pattern.compile("S[']?t[']?[.,]? J[']?o[']?h[']?n[']?s[']?[.,]?((^|[\\[\\]\\(\\)\\.\\!\\?<>': \n" +
                "])[a-z](| |[ ]{0,1}[.']|[.'][ ]{0,1})[a-z])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher("\n" +
                "St. John's, Antigua and Barbuda \n" +
                "St. Johns, Saba, a village on the Caribbean island of Saba, a special municipality of the Netherlands\n" +
                "St John's wort, herb\n" +
                "HMCS St. John's (FFH 340), a Halifax-class frigate in the Canadian Navy\n" +
                "Leeds St Johns, a British rugby league football club today known as Leeds Rhinos\n" +
                "St. Johnsville, New York (disambiguation)\n").find());


        System.out.println(Pattern.compile("S[']?t[']?[.,]? J[']?o[']?h[']?n[']?s[']?[.,]?((^|[\\[\\]\\(\\)\\.\\!\\?<>': \n" +
                "])[a-z])", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher("\n" +
                "St. John's, Antigua and Barbuda \n" +
                "St. Johns, Saba, a village on the Caribbean island of Saba, a special municipality of the Netherlands\n" +
                "St John's wort, herb\n" +
                "HMCS St. John's (FFH 340), a Halifax-class frigate in the Canadian Navy\n" +
                "Leeds St Johns, a British rugby league football club today known as Leeds Rhinos\n" +
                "St. Johnsville, New York (disambiguation)\n").find());


        System.out.println(Pattern.compile("S[']?t[']?[.,]? J[']?o[']?h[']?n[']?s[']?[.,]?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher("\n" +
                        "St. John's, Antigua and Barbuda \n" +
                        "St. Johns, Saba, a village on the Caribbean island of Saba, a special municipality of the Netherlands\n" +
                        "St John's wort, herb\n" +
                        "HMCS St. John's (FFH 340), a Halifax-class frigate in the Canadian Navy\n" +
                        "Leeds St Johns, a British rugby league football club today known as Leeds Rhinos\n" +
                        "St. Johnsville, New York (disambiguation)\n").find());



        System.out.println(Pattern.compile("S[']?t[']?[.,]? ", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher("\n" +
                "St. John's, Antigua and Barbuda \n" +
                "St. Johns, Saba, a village on the Caribbean island of Saba, a special municipality of the Netherlands\n" +
                "St John's wort, herb\n" +
                "HMCS St. John's (FFH 340), a Halifax-class frigate in the Canadian Navy\n" +
                "Leeds St Johns, a British rugby league football club today known as Leeds Rhinos\n" +
                "St. Johnsville, New York (disambiguation)\n").find());




    }
}
