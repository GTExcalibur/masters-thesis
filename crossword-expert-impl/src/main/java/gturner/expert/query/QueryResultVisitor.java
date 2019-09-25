package gturner.expert.query;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/23/13
 * Time: 8:53 PM
 * To change this template use File | Settings | File Templates.
 */
public interface QueryResultVisitor {

    public void visitResult(String title, String content, Set<String> keywords, double relativeScore);

    public void noDataFound();
}
