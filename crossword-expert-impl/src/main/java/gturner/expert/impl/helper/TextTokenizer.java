package gturner.expert.impl.helper;

import com.google.common.collect.ForwardingList;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 10/11/14
 * Time: 9:47 AM
 * To change this template use File | Settings | File Templates.
 */
public final class TextTokenizer {

    private static final Pattern textSplitter = Pattern.compile("[-_+ ]");
    private static final Pattern wordSplitter = Pattern.compile("[^A-Z]");
    private static final Pattern asciiMatcher = Pattern.compile("[^\\p{ASCII}]");

    private static final Pattern diacriticalReplace = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static interface TokenVisitor {
        public void visitToken(List<String> previous, String token, boolean composite, List<String> next);
    }

    public static void parseTokens(int clueLength, String text, final TokenVisitor visitor) {
        String[] tempTokens = textSplitter.split(text);

        final List<String> previousTokens = new LinkedList<String>() {
            @Override
            public boolean add(String s) {
                boolean add = super.add(s);
                if(size() > 10) {
                    remove(0);
                }
                return add;
            }
        };
        final List<String> nextTokens = new LinkedList<String>(Arrays.asList(tempTokens));

        for(int i = 0; i < tempTokens.length; i++) {
            nextTokens.remove(0);

            // actual work

            String currentToken = crosswordizeText(tempTokens[i]);
            int j = i+1;
            boolean composite = false;
            while(currentToken.length() < clueLength && j < tempTokens.length) {
                currentToken = currentToken + crosswordizeText(tempTokens[j++]);
                composite = true;
            }
            if(currentToken.length() == clueLength) {
                visitor.visitToken(
                        Collections.unmodifiableList(previousTokens),
                        currentToken,
                        composite,
                        Collections.unmodifiableList(nextTokens.subList(0, Math.min(10, nextTokens.size())))
                );
            }

            // end actual work

            previousTokens.add(tempTokens[i]);
        }
    }

    public static String crosswordizeText(String currentToken) {
        if(asciiMatcher.matcher(currentToken).find()) {
            currentToken = Normalizer.normalize(currentToken, Normalizer.Form.NFD);
            currentToken = diacriticalReplace.matcher(currentToken).replaceAll("");
        }
        currentToken = currentToken.toUpperCase();
        currentToken = wordSplitter.matcher(currentToken).replaceAll("");
        return currentToken;
    }
}
