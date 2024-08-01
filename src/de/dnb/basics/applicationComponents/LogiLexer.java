/**
 *
 */
package de.dnb.basics.applicationComponents;

import java.util.Scanner;
import java.util.regex.Pattern;

import de.dnb.basics.applicationComponents.tuples.Pair;

/**
 * @author baumann
 *
 */
public class LogiLexer {

  private String read = "";

  public static enum TOKEN {
      BRA, KET, AND, OR, NOT, STRING, NULL;

    public static Pair<TOKEN, String> getToken(String s) {

      if (s == null)
        return new Pair<TOKEN, String>(NULL, null);
      s = s.trim();
      if (s.isEmpty())
        return new Pair<TOKEN, String>(NULL, null);

      if (s.equalsIgnoreCase("and") || s.equalsIgnoreCase("und"))
        return new Pair<TOKEN, String>(AND, null);
      if (s.equalsIgnoreCase("or") || s.equalsIgnoreCase("oder"))
        return new Pair<TOKEN, String>(OR, null);
      if (s.equalsIgnoreCase("not") || s.equalsIgnoreCase("nicht"))
        return new Pair<TOKEN, String>(NOT, null);
      if (s.equalsIgnoreCase("("))
        return new Pair<TOKEN, String>(BRA, null);
      if (s.equalsIgnoreCase(")"))
        return new Pair<TOKEN, String>(KET, null);
      if (s.startsWith("\""))
        s = s.substring(1);
      if (s.endsWith("\""))
        s = s.substring(0, s.length() - 1);
      if (s.isEmpty())
        return new Pair<TOKEN, String>(STRING, null);
      return new Pair<TOKEN, String>(STRING, s);

    }
  };

  /**
   * @return the peek
   */
  public Pair<TOKEN, String> peek() {
    return peek;
  }

  @Override
  protected void finalize() throws Throwable {
    StreamUtils.safeClose(scanner);
  }

  /**
   * Klammern oder kein whitespace.
   */
  static final String LOGI_STR = "\\\".*\\\"|\\(|\\)|[^()\\s\"]+";
  static final Pattern LOGI_PAT = Pattern.compile(LOGI_STR);

  private Pair<TOKEN, String> peek = null;

  public boolean hasnext() {
    return peek != null;
  }

  /**
   *
   * @return nächstes Paar (Token, Wert) oder null
   */
  public Pair<TOKEN, String> next() {
    final Pair<TOKEN, String> ret = peek;
    final String next = findNextString();
    peek = next != null ? TOKEN.getToken(next) : null;
    return ret;
  }

  private final Scanner scanner;

  /**
   * @param source
   */
  public LogiLexer(final String source) {
    super();
    scanner = new Scanner(source);
    final String found = findNextString();
    peek = TOKEN.getToken(found);
  }

  /**
   * @return
   */
  public String findNextString() {
    final String findInLine = scanner.findInLine(LOGI_PAT);
    if (findInLine != null)
      read += " " + findInLine;
    return findInLine;
  }

  /**
   * @return the read
   */
  public String getReadTokens() {
    return read;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final String source = "a and not \"asd qwe*\"";
    final LogiLexer lexer = new LogiLexer(source);
    System.out.println("\t" + lexer.peek());
    while (lexer.hasnext())
      System.out.println(lexer.next() + "\t" + lexer.peek());

  }

}
