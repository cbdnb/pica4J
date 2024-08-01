package de.dnb.basics.applicationComponents.strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementierung eines Ausgabestremas, der auf einem String arbeitet.
 * 
 * @author Michael Inden
 * 
 * Copyright 2011 by Michael Inden 
 */
public class StringOutputStream extends OutputStream {
    private ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private boolean closed = false;

    // ByteArrayOutputStream wirft keine Exception
    @Override
    public final synchronized void write(final int b) throws IOException {
        if (this.closed)
            throw new IOException("write forbidden -- stream already closed!");

        this.stream.write(b);
    }

    /**
     * 
     * @return  als String
     */
    public final synchronized String getContentAsString() {
        return this.stream.toString();
    }

    /**
     * 
     * @return  geschriebene Daten als byte-Array
     */
    public final synchronized byte[] getContent() {
        return this.stream.toByteArray();
    }

    @Override
    public final String toString() {
        return this.getClass() + " / Content: '" + getContentAsString() + "'";
    }

    @Override
    public final synchronized void close() throws IOException {
        this.closed = true;
        reset();
    }

    public final synchronized void reset() {
        // trick: this frees larger allocated byte array blocks ...
        this.stream = new ByteArrayOutputStream();
    }
}
