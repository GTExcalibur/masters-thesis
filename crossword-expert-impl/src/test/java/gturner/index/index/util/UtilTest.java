package gturner.index.index.util;

import junit.framework.Assert;
import org.junit.Test;

import java.text.Normalizer;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/16/13
 * Time: 7:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class UtilTest {

    @Test
    public void test_normalizeAlg() {
        String text = "יאש";

        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("[^\\p{ASCII}]", "");

        Assert.assertEquals("eau", text);
    }
}
