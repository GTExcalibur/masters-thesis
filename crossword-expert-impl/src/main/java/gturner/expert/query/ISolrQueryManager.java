package gturner.expert.query;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISolrQueryManager {
    public void performQuery(List<String> shards, String query, QueryResultVisitor visitor);
}
