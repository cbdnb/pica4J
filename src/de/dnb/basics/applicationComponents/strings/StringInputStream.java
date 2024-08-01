package de.dnb.basics.applicationComponents.strings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementierung eines Eingabestremas, der auf einem String arbeitet.
 * 
 * @author Michael Inden
 * 
 * Copyright 2011 by Michael Inden 
 */
public final class StringInputStream extends InputStream {
    private InputStream stream;
    private boolean closed = false;

    public StringInputStream(final String inputData) {
        this.stream = new ByteArrayInputStream(inputData.getBytes());
    }

    public StringInputStream(final byte[] inputData) {
        this.stream = new ByteArrayInputStream(inputData);
    }

    // ...
    @Override
    public synchronized int read() throws IOException {
        if (this.closed)
            throw new IOException("read forbidden -- stream already closed!");

        return this.stream.read();
    }

    @Override
    public synchronized int available() throws IOException {
        return this.stream.available();
    }

    @Override
    public synchronized void close() throws IOException {
        this.closed = true;
        this.stream = new ByteArrayInputStream(new byte[0]);
    }
}
