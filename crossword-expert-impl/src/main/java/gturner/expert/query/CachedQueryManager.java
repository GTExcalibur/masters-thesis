package gturner.expert.query;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/24/14
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public final class CachedQueryManager implements ISolrQueryManager {

    private final ISolrQueryManager delegate;
    private final Cache<String, QueryResultVisitorReplay> cache;

    public CachedQueryManager(ISolrQueryManager delegate, int cacheSize) {
        this.delegate = delegate;
        this.cache = CacheBuilder.newBuilder().concurrencyLevel(4).softValues().maximumSize(cacheSize).build();
    }

    public void performQuery(final List<String> shards, final String query, QueryResultVisitor visitor) {
        QueryResultVisitorReplay replay;
        try {
            replay = cache.get(query, new Callable<QueryResultVisitorReplay>() {
                @Override
                public QueryResultVisitorReplay call() throws Exception {
                    final AtomicReference<QueryResultVisitorReplay> pointer = new AtomicReference<QueryResultVisitorReplay>();

                    delegate.performQuery(shards, query, new QueryResultVisitor() {
                        @Override
                        public void visitResult(String title, String content, Set<String> keywords, double relativeScore) {
                            if(pointer.get() == null) {
                                pointer.set(new DefaultQueryResultVisitorReplay());
                            }
                            ((DefaultQueryResultVisitorReplay)pointer.get()).add(title, content, keywords, relativeScore);
                        }

                        @Override
                        public void noDataFound() {
                            pointer.set(new NoOpQueryResultVisitorReplay());
                        }
                    });

                    return pointer.get();
                }
            });
        } catch (ExecutionException e) {
            if(e.getCause() instanceof RuntimeException) {
                throw (RuntimeException)e.getCause();
            }
            throw new RuntimeException(e.getCause());
        }

        replay.replay(visitor);
    }

    private static interface QueryResultVisitorReplay {
        public void replay(QueryResultVisitor visitor);
    }

    private static final class NoOpQueryResultVisitorReplay implements QueryResultVisitorReplay {
        @Override
        public void replay(QueryResultVisitor visitor) {
            visitor.noDataFound();
        }
    }

    private static final class DefaultQueryResultVisitorReplay implements QueryResultVisitorReplay {
        private final List<ReplayData> replayData = new ArrayList<ReplayData>();

        @Override
        public void replay(QueryResultVisitor visitor) {
            for (ReplayData data : replayData) {
                visitor.visitResult(data.getTitle(), data.getContent(), data.getKeywords(), data.getRelativeScore());
            }
        }

        public void add(String title, String content, Set<String> keywords, double relativeScore) {
            replayData.add(new ReplayData(title, content, keywords, relativeScore));
        }
    }

    private static final class ReplayData {
        private final String title;
        private final String content;
        private final Set<String> keywords;
        private final double relativeScore;

        public ReplayData(String title, String content, Set<String> keywords, double relativeScore) {
            this.title = title;
            this.content = content;
            this.keywords = keywords;
            this.relativeScore = relativeScore;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public Set<String> getKeywords() {
            return keywords;
        }

        private double getRelativeScore() {
            return relativeScore;
        }
    }
}
