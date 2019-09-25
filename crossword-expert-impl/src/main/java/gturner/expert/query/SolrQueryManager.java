package gturner.expert.query;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/23/13
 * Time: 8:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class SolrQueryManager implements ISolrQueryManager {

    private final String rootHost;
    private final Map<String, String> aliasToShard;

    private final Client jerseyClient;

    private int rows = 100; // default to 100
    private int maxRows = 1000; // default to 1000
    private String qf = null;
    private JsonFactory jfactory = new JsonFactory();

    private final ThreadLocal<Integer> docId = new ThreadLocal<Integer>();


    public SolrQueryManager(String rootHost, Map<String, String> aliasToShard) {
        this.rootHost = rootHost;
        this.aliasToShard = aliasToShard;

        jerseyClient = Client.create();
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public void setQf(String qf) {
        this.qf = qf;
    }

    public void setJsonfactory(JsonFactory jfactory) {
        this.jfactory = jfactory;
    }

    @Override
    public void performQuery(List<String> shards, String query, QueryResultVisitor visitor) {
        try {
            if (shards.isEmpty()) throw new IllegalArgumentException("At least one shard must be specified");
            WebResource resource;
            int startRow = 0;
            int requestMaxRows = maxRows;

            if (shards.size() == 1) {
                resource = jerseyClient.resource(rootHost).
                        path(shards.get(0)).
                        path("select").
                        queryParam("q", query).
                        queryParam("rows", Integer.toString(rows)).
                        queryParam("wt", "json").
                        queryParam("fl", "[docid],*,score");
                if(qf != null) {
                    resource = resource.queryParam("qf", qf);
                }
                InputStream inputStream = resource.
                        accept(MediaType.APPLICATION_JSON_TYPE).
                        get(InputStream.class);

                System.out.println("URI: " + resource.getURI());
                requestMaxRows = visitResults(inputStream, 0, visitor);

            } else {
                StringBuilder shardList = new StringBuilder();
                for (String shard : shards) {
                    shardList.append(shard).append(",");
                }

                resource = jerseyClient.resource(rootHost).
                        path("select").
                        queryParam("shards", shardList.deleteCharAt(shardList.length() - 1).toString()).
                        queryParam("q", query).
                        queryParam("rows", Integer.toString(rows)).
                        queryParam("wt", "json").
                        queryParam("fl", "[docid],*,score");
                if(qf != null) {
                    resource = resource.queryParam("qf", qf);
                }
                System.out.println("URI: " + resource.getURI());
                InputStream inputStream = resource.
                        accept(MediaType.APPLICATION_JSON_TYPE).
                        get(InputStream.class);

                requestMaxRows = visitResults(inputStream, 0, visitor);
            }

            startRow = startRow + rows;
            while (startRow < requestMaxRows && startRow < maxRows) {
                if (docId.get() != null) {
                    requestMaxRows = visitResults(
                            resource.queryParam("start", Integer.toString(startRow)).
                                    queryParam("pageDoc", Integer.toString(docId.get())).
                                    accept(MediaType.APPLICATION_JSON_TYPE).
                                    get(InputStream.class),
                            startRow,
                            visitor
                    );
                } else {
                    requestMaxRows = visitResults(
                            resource.queryParam("start", Integer.toString(startRow)).
                                    accept(MediaType.APPLICATION_JSON_TYPE).
                                    get(InputStream.class),
                            startRow,
                            visitor
                    );
                }


                startRow = startRow + rows;
            }
        } catch (UniformInterfaceException e) {
            // swallow the exception and move on, it's statically insignificant
            visitor.noDataFound();
        }
    }

    private int visitResults(InputStream stream, int startRow, QueryResultVisitor visitor) {
        boolean found = false;
        int numFound = startRow + rows;
        double maxScore = 1.0;

        try {
            JsonParser jParser = jfactory.createJsonParser(stream);

            JsonToken current = jParser.nextToken();
            // loop until token equal to "}"
            while (jParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jParser.getCurrentName();
                // from fieldname to field value
                current = jParser.nextToken();
                if ("responseHeader".equals(fieldName)) {
                    while (jParser.nextToken() != JsonToken.END_OBJECT) ;
                    current = jParser.nextToken();
                } else if ("response".equals(fieldName)) {
                    if (current == JsonToken.START_OBJECT) {
                        current = jParser.nextToken();

                        while (current != JsonToken.END_OBJECT) {
                            fieldName = jParser.getCurrentName();
                            if ("numFound".equals(fieldName)) {
                                current = jParser.nextToken();
                                numFound = startRow + jParser.getIntValue();
                            } else if ("maxScore".equals(fieldName)) {
                                current = jParser.nextToken();
                                maxScore = jParser.getDoubleValue();
                            } else if ("docs".equals(fieldName)) {
                                current = jParser.nextToken();
                                if (current == JsonToken.START_ARRAY) {
                                    current = jParser.nextToken();
                                    while (current != JsonToken.END_ARRAY) {
                                        visitObject(visitor, jParser, maxScore);
                                        found = true;
                                        current = jParser.nextToken();
                                    }
                                }
                            }
                            current = jParser.nextToken();
                        }
                    }
                }
            }

            if (!found) {
                visitor.noDataFound();
                return 0;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return numFound;
    }

    private void visitObject(QueryResultVisitor visitor, JsonParser jParser, double maxScore) throws IOException {
        String title = null;
        String text = null;
        HashSet<String> keywords = new HashSet<String>();
        double score = 1.0;

        while (jParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jParser.getCurrentName();

            JsonToken current = jParser.nextToken();
            if ("text".equals(fieldName)) {
                if (current == JsonToken.START_ARRAY) {
                    current = jParser.nextToken();
                    while (current != JsonToken.END_ARRAY) {
                        text = jParser.getText();
                        current = jParser.nextToken();
                    }
                } else {
                    text = jParser.getText();
                }
            } else if ("title".equals(fieldName)) {
                if (current == JsonToken.START_ARRAY) {
                    current = jParser.nextToken();
                    while (current != JsonToken.END_ARRAY) {
                        title = jParser.getText();
                        current = jParser.nextToken();
                    }
                } else {
                    title = jParser.getText();
                }
            } else if ("keywords".equals(fieldName)) {
                if (current == JsonToken.START_ARRAY) {
                    current = jParser.nextToken();
                    while (current != JsonToken.END_ARRAY) {
                        keywords.add(StringEscapeUtils.unescapeHtml(jParser.getText()));
                        current = jParser.nextToken();
                    }
                }
            } else if ("[docid]".equals(fieldName)) {
                docId.set(jParser.getIntValue());
            } else if ("score".equals(fieldName)) {
                score = jParser.getDoubleValue() / maxScore;
            }
        }

        visitor.visitResult(StringEscapeUtils.unescapeHtml(title), StringEscapeUtils.unescapeHtml(text), keywords, score);
    }
}
