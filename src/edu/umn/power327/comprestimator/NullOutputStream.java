package edu.umn.power327.comprestimator;

import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
    private int count = 0;

    @Override
    public void write(int b) {
        count++;
    }

    @Override
    public void write(byte[] b) {
        count += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        count += len;
    }

    @Override
    public void flush() { }

    @Override
    public void close() { }

    public int size() {
        return count;
    }

    public void resetByteCount() {
        count = 0;
    }
}
