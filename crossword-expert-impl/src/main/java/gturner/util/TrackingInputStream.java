package gturner.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 3/16/13
 * Time: 7:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrackingInputStream extends InputStream {
    private final InputStream delegate;
    private long bytesRead;
    private long mark;

    public TrackingInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        int read = delegate.read();
        bytesRead++;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = delegate.read(b);
        bytesRead += read;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = delegate.read(b, off, len);
        bytesRead += read;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long skip = delegate.skip(n);
        bytesRead += skip;
        return skip;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void mark(int readlimit) {
        mark = bytesRead;
        delegate.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        bytesRead = mark;
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
