/**
 *
 */
package de.dnb.basics.applicationComponents;

import java.util.function.Predicate;

import de.dnb.basics.applicationComponents.LogiLexer.TOKEN;
import de.dnb.basics.applicationComponents.strings.StringUtils;
import de.dnb.basics.applicationComponents.tuples.Pair;
import de.dnb.basics.filtering.AcceptEverything;

/**
 * Parser mit folgender Grammatik:
 *
 * <pre>
 * expression := term | term 'or' expression
 * term := factor | factor 'and' term | factor term
 * factor := string | '(' expression ') | 'not' factor
 * </pre>
 *
 *
 * @author baumann
 *
 */
public class LogiParser {

  private final boolean caseInsensitive;

  /**
   * @param lexer
   */
  private LogiParser(final String expression, final boolean caseInsensitive) {
    this.caseInsensitive = caseInsensitive;
    lexer = new LogiLexer(expression);
  }

  private final LogiLexer lexer;

  private Predicate<String> factor() {
    if (!lexer.hasnext())
      throw new IllegalArgumentException(
        "Faktor: Fehlendes Terminal oder (Expression) oder 'not'.\nEingabe:"
          + lexer.getReadTokens());
    Pair<TOKEN, String> next = lexer.next();
    if (next.first == TOKEN.KET) {
      throw new IllegalArgumentException(
        "Faktor: ')' ist falsch.\nEingabe:" + lexer.getReadTokens());
    }
    if (next.first == TOKEN.BRA) {
      final Predicate<String> exPred = expression();
      if (!lexer.hasnext())
        throw new IllegalArgumentException(
          "Faktor: Fehlende ')'.\nEingabe:" + lexer.getReadTokens());
      next = lexer.next();
      if (next.first != TOKEN.KET)
        throw new IllegalArgumentException(
          "Faktor: etwas anderes als ')'.\nEingabe:" + lexer.getReadTokens());
      return exPred;
    } else if (next.first == TOKEN.NOT) {
      final Predicate<String> facPred = factor();
      return facPred.negate();
    } else if (next.first != TOKEN.STRING) {
      throw new IllegalArgumentException(
        "Faktor: Suchstring erwartet.\nEingabe:" + lexer.getReadTokens());
    } else {
      // Aktion:
      return StringUtils.getSimpleTruncPredicate(next.second, caseInsensitive);

    }

  }

  private Predicate<String> term() {
    if (!lexer.hasnext())
      throw new IllegalArgumentException("Term: Fehlt Faktor.\nEingabe:" + lexer.getReadTokens());

    final Predicate<String> facPred = factor();
    if (!lexer.hasnext())
      return facPred;
    final Pair<TOKEN, String> peek = lexer.peek();
    final TOKEN peekFirst = peek.first;

    if (peekFirst == TOKEN.AND) {
      lexer.next(); // einfach verbrauchen
      final Predicate<String> termPred = term();
      // Aktion:
      return facPred.and(termPred);
    }

    if (peekFirst == TOKEN.STRING || peekFirst == TOKEN.BRA || peekFirst == TOKEN.NOT) {
      final Predicate<String> termPred = term();
      // Aktion:
      return facPred.and(termPred);
    }

    return facPred;

  }

  private Predicate<String> expression() {
    if (!lexer.hasnext())
      throw new IllegalArgumentException(
        "Expression: fehlender Term.\nEingabe:" + lexer.getReadTokens());

    final Predicate<String> termPred = term();
    if (!lexer.hasnext())
      return termPred;
    if (lexer.peek().first == TOKEN.KET)
      return termPred;
    final Pair<TOKEN, String> next = lexer.next();
    if (next.first != TOKEN.OR) {
      throw new IllegalArgumentException("Fehlendes 'or'.\nEingabe:" + lexer.getReadTokens());
    }
    final Predicate<String> exprPred = expression();
    // Aktion:
    return termPred.or(exprPred);
  }

  /**
   *
   * @param expression      auch null
   * @param caseInsensitive groß/klein ist egal
   * @return                Test auf logischen Ausdruck, wie er in der IBW implementiert ist
   */
  public static final
    Predicate<String>
    getPredicate(final String expression, final boolean caseInsensitive) {
    if (expression == null)
      return new AcceptEverything<String>().negate();
    if (expression.isEmpty())
      return new AcceptEverything<String>();
    final LogiParser parser = new LogiParser(expression, caseInsensitive);
    final Predicate<String> ret = parser.expression();
    return ret;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final LogiParser parser = new LogiParser(") ab", true);
    final Predicate<String> ret = parser.expression();
    System.out.println(ret.test("abc"));

  }

}
