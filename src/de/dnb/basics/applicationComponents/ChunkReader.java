package de.dnb.basics.applicationComponents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import de.dnb.basics.filtering.RangeCheckUtils;

/**
 * Liest eine Portion von Daten aus einem Strom.
 *
 * @author baumann
 *
 */
public class ChunkReader implements Iterator<String> {

  private Pattern delimiter = Pattern.compile("\\s");

  private final Scanner scanner;

  /**Constructs a new ChunkReader that produces values scanned from the 
   * specified file.
   * @param source
   * @param charsetName
   * @throws FileNotFoundException 
   */
  public ChunkReader(final File source) throws FileNotFoundException {
    scanner = new Scanner(source);
    scanner.useDelimiter(delimiter);
  }

  /**
   * Constructs a new ChunkReader that produces values scanned from the 
   * specified file.
   * @param source
   * @param charsetName
   * @throws FileNotFoundException 
   */
  public ChunkReader(final File source, final String charsetName) throws FileNotFoundException {
    scanner = new Scanner(source, charsetName);
    scanner.useDelimiter(delimiter);
  }

  /**
   * Constructs a new ChunkReader that produces values scanned from the 
   * specified input stream.
   * @param source
   */
  public ChunkReader(final InputStream source) {
    scanner = new Scanner(source);
    scanner.useDelimiter(delimiter);
  }

  /**
   * Constructs a new ChunkReader that produces values scanned from the 
   * specified input stream.
   * @param source
   * @param charsetName
   */
  public ChunkReader(final InputStream source, final String charsetName) {
    scanner = new Scanner(source, charsetName);
    scanner.useDelimiter(delimiter);
  }

  /**
   * Constructs a new ChunkReader that produces values scanned from the 
   * specified source.
   * @param source
   */
  public ChunkReader(final Readable source) {
    scanner = new Scanner(source);
    scanner.useDelimiter(delimiter);
  }

  /**
   * Constructs a new ChunkReader that produces values scanned from the 
   * specified string.
   * @param source
   */
  public ChunkReader(final String source) {
    scanner = new Scanner(source);
    scanner.useDelimiter(delimiter);
  }

  @Override
  public boolean hasNext() {
    return scanner.hasNext();
  }

  /**
   * @param aDelimiter the delimiter to set
   */
  public final void setDelimiter(final String aDelimiter) {
    RangeCheckUtils.assertReferenceParamNotNull("delimiter", aDelimiter);
    delimiter = Pattern.compile("(?=(" + aDelimiter + "))");
    scanner.useDelimiter(delimiter);
  }

  @Override
  /**
   * Finds and returns the next complete token from this chunkReader. 
   * A complete token is consists of input that matches 
   * the delimiter pattern plus the following input. It is followed by
   * input that matches the next delimiter pattern.
   * 
   * This method may block while waiting for input 
   * to scan, even if a previous invocation of hasNext() returned true. 
   * 
   * @return	the next token 
   * @throws	NoSuchElementException - if no more tokens are available 
   * @throws  IllegalStateException - if this scanner is closed
   */
  public String next() {
    return scanner.next();
  }

  /**
   * The remove operation is not supported by this implementation of 
   * Iterator. 
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();

  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final File source = new File("documents/GNDBeispiel.txt");
    try {
      final ChunkReader reader = new ChunkReader(source);
      reader.setDelimiter("SET: | ");

      while (reader.hasNext()) {
        System.out.println(reader.next());
        System.out.println("---------------");
      }
    } catch (final FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
