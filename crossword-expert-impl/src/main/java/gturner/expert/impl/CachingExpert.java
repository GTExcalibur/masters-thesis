package gturner.expert.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gturner.crossword.spec.CWClue;
import gturner.expert.IClueExpert;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 9/26/14
 * Time: 7:56 AM
 * To change this template use File | Settings | File Templates.
 */
public final class CachingExpert implements IClueExpert, Closeable {

    private final IClueExpert delegate;
    private final boolean cacheState;
    private final Cache<String, Map<String, Long>> searchCache;

    public CachingExpert(IClueExpert delegate, boolean cacheState) {
        this.delegate = delegate;
        this.cacheState = cacheState;
        this.searchCache = CacheBuilder.newBuilder().concurrencyLevel(4).softValues().maximumSize(100).build();
    }

    @Override
    public Map<String, Long> performSearch(final CWClue clue, final String constraint) {
        try {
            String key;
            if(cacheState) {
                key = constraint + ":::" + clue.getClueText();
            } else {
                key = clue.getClueText();
            }
            return searchCache.get(key, new Callable<Map<String, Long>>() {
                @Override
                public Map<String, Long> call() throws Exception {
                    return delegate.performSearch(clue, constraint);
                }
            });
        } catch (ExecutionException e) {
            if(e.getCause() instanceof RuntimeException) {
                throw (RuntimeException)e.getCause();
            }
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public void close() throws IOException {
        searchCache.invalidateAll();
        searchCache.cleanUp();
    }
}
