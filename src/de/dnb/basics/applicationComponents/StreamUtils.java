package de.dnb.basics.applicationComponents;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.zip.ZipInputStream;

import de.dnb.basics.applicationComponents.strings.StringOutputStream;
import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * Diverse kleine Hilfsmethoden für die Streamverarbeitung.
 *
 * @author Michael Inden
 *
 * Copyright 2011 by Michael Inden
 */
public final class StreamUtils {

  private StreamUtils() {

  }

  /** BUFFER SIZE FOR COPY. */
  public static final int BUFFER_SIZE = 8192;

  /**
   * closes an inputstream and ignores IOException.
   *
   * @param is    the input stream to be closed
   */
  public static void safeClose(final Closeable is) {
    try {
      if (is != null) {
        is.close();
      }
    } catch (final IOException e) {
      // ignore only io close exceptions
    }
  }

  /**
   * Copies all contents from the passed input stream into the given
   * output stream using byte wise copy.
   *
   * @param is    the source
   * @param os    the destination
   *
   * @throws IOException --- may occur when performing i/o
   */
  public static void copyBytewise(final InputStream is, final OutputStream os) throws IOException {
    int data = -1;
    while ((data = is.read()) != -1) {
      os.write(data);
    }
    os.flush();
  }

  /**
   *
   * Copies all contents from the passed input stream into the
   * given output stream using buffered streams.
   *
   * @param is			the source
   * @param os			the destination
   * @throws IOException	may occur when performing i/o
   */
  public static void copyBuffered(final InputStream is, final OutputStream os) throws IOException {
    final InputStream bufferedIn = decorateWithBuffer(is);
    final OutputStream bufferedOut = decorateWithBuffer(os);
    copyBytewise(bufferedIn, bufferedOut);
  }

  /**
   *
   * Copies all contents from the passed input stream into the
   * given output stream using byte buffer copy.
   *
   * @param is
   * @param os
   * @throws IOException
   */
  public static void copyOwnBuffering(final InputStream is, final OutputStream os)
    throws IOException {
    final byte[] buffer = new byte[BUFFER_SIZE];
    int length = -1;
    while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
      os.write(buffer, 0, length);
    }
    os.flush();
  }

  /**
  * Copies all contents from the passed reader into the given writer using
  * char wise copy.
  *
  * @param reader
  * @param writer
  * @throws IOException
  */
  public static void copyCharWise(final Reader reader, final Writer writer) throws IOException {
    int data = -1;
    while ((data = reader.read()) != -1) {
      writer.write(data);
    }
    writer.flush();
  }

  /**
   * Copies all contents from the passed reader into the given writer using
   * buffered Writer and Reader.
   *
   * @param reader
   * @param writer
   * @throws IOException
   */
  public static void copyBuffered(final Reader reader, final Writer writer) throws IOException {
    final Reader bufferedIn = decorateWithBuffer(reader);
    final Writer bufferedOut = decorateWithBuffer(writer);

    copyCharWise(bufferedIn, bufferedOut);
  }

  /**
   * Copies all contents from the passed reader into the given
   * writer using char buffer copy.
   *
   * @param reader
   * @param writer
   * @throws IOException
   */
  public static void copyOwnBuffering(final Reader reader, final Writer writer) throws IOException {
    final char[] buffer = new char[BUFFER_SIZE];
    int length = -1;
    while ((length = reader.read(buffer, 0, BUFFER_SIZE)) != -1) {
      writer.write(buffer, 0, length);
    }
    writer.flush();
  }

  /**
   * Decorates a passed input stream "is" with a BufferedInputStream, if the
   * passed "is" is already a BufferedInputStream no additional
   * BufferedInputStream is created and originally passed "is" is returned.
   *
   * @param inStream
   *            the InputStream to be decorated with a buffer
   * @return the decorated input stream
   */
  public static InputStream decorateWithBuffer(final InputStream inStream) {
    RangeCheckUtils.assertReferenceParamNotNull("inStream", inStream);
    if (!(inStream instanceof BufferedInputStream)) {
      return new BufferedInputStream(inStream, BUFFER_SIZE);
    }
    return inStream;
  }

  /**
   * decorates a passed output stream "os" with a BufferedOutputStream, if the
   * passed "os" is already a BufferedOutputStream no additional
   * BufferedOutputStream is created and the originally passed "os" is
   * returned.
   *
   * @param outStream
   *            the OutputStream to be decorated with a buffer
   * @return the decorated output stream
   */
  public static OutputStream decorateWithBuffer(final OutputStream outStream) {
    RangeCheckUtils.assertReferenceParamNotNull("outStream", outStream);
    //@formatter:off
		return (outStream instanceof BufferedOutputStream)
			?
			outStream
			: new BufferedOutputStream(outStream, BUFFER_SIZE);
	}
	//@formatter:on
  /**
   * decorates a passed Reader "reader" with a BufferedReader, if the passed
   * "reader" is already a BufferedReader no additional BufferedReader is
   * created and originally passed "reader" is returned.
   *
   * @param reader
   *            the Reader to be decorated with a buffer
   * @return the decoreted reader
   */
  public static Reader decorateWithBuffer(final Reader reader) {
    RangeCheckUtils.assertReferenceParamNotNull("reader", reader);
    if (!(reader instanceof BufferedReader)) {
      return new BufferedReader(reader, BUFFER_SIZE);
    }
    return reader;
  }

  /**
   * decorates a passed Writer "writer" with a BufferedWriter, if the passed
   * "writer" is already a BufferedWriter no additional BufferedWriter is
   * created and the originally passed "writer" is returned.
   *
   * @param writer	the Writer to be decorated with a buffer
   * @return 			the decorated writer
   */
  public static Writer decorateWithBuffer(final Writer writer) {
    RangeCheckUtils.assertReferenceParamNotNull("writer", writer);
    //@formatter:off
		return (writer instanceof BufferedWriter)
				? writer
				: new BufferedWriter(writer, BUFFER_SIZE);
	}	//@formatter:on

  /**
   *
   * @param iStream  nicht null
   * @return         als String
   */
  public static String readIntoString(final InputStream iStream) {
    final StringOutputStream oStream = new StringOutputStream();
    try {
      copyBuffered(iStream, oStream);
    } catch (final IOException e) {
      // nix
    }
    return oStream.getContentAsString();
  }

  public static String readIntoString(final Reader reader) {
    final StringWriter writer = new StringWriter();
    try {
      copyBuffered(reader, writer);
    } catch (final IOException e) {
      // nix
    }
    return writer.toString();
  }

  /**
   * Gibt bei einem im Web vorhandenen zip-Container den Eingabestrom der
   * ersten Datei im Container.
   *
   * @param webAdress		URI, nicht null nicht leer
   * @return				Eingabestrom, auch wenn kein zip!
   * @throws IOException	wenn keine Verbindung hergestellt werden kann.
   */
  public static ZipInputStream getZipInputStreamFromWeb(final String webAdress) throws IOException {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("webAdress", webAdress);
    final InputStream in = getInputStreamFromWeb(webAdress);
    final ZipInputStream zipInputStream = getInputStreamOfFirstFileInZipContainer(in);
    return zipInputStream;
  }

  /**
   * @param in 	InputStream aus einem ZipContainer, nicht null
   * @return		neuen InputStream, der das erste File ausliest
   * @throws IOException	wenn kein File vorhanden ...
   */
  public static ZipInputStream getInputStreamOfFirstFileInZipContainer(final InputStream in)
    throws IOException {
    final ZipInputStream zipInputStream = new ZipInputStream(in);
    zipInputStream.getNextEntry();
    return zipInputStream;
  }

  /**
   * @param urlStr	nicht null, nicht leer
   * @return			Eingabestrom
   * @throws IOException	url nicht gültig oder kann nicht gelesen werden
   */
  public static InputStream getInputStreamFromWeb(final String urlStr) throws IOException {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("webAdress", urlStr);
    final URL url = new URL(urlStr);
    final InputStream in = url.openStream();
    return in;
  }

  /**
   * Gibt bei einem im zip-Container den Eingabestrom der
   * ersten Datei im Container.
   *
   * @param path			Dateipfad, nicht null nicht leer
   * @return				Eingabestrom, auch wenn kein zip!
   * @throws IOException	wenn keine Verbindung hergestellt werden kann.
   */
  public static ZipInputStream getZipInputStreamFromFile(final String path) throws IOException {
    RangeCheckUtils.assertStringParamNotNullOrEmpty("path", path);
    final InputStream in = new FileInputStream(path);
    final ZipInputStream zipInputStream = getInputStreamOfFirstFileInZipContainer(in);
    return zipInputStream;
  }

  public static void main(final String[] args) throws IOException {
    final ZipInputStream inputStream = getZipInputStreamFromFile("D:/zips/bib-data.zip");
    System.out.println(inputStream);
    //				"http://classificationweb.net"
    //				+ "/LCMPT/LCMPTvocab_140225.mrc.zip");
    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line = reader.readLine();
    while (line != null) {
      System.out.println(line);
      line = reader.readLine();
    }
  }
}
