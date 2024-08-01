package de.dnb.basics.applicationComponents;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility-Klasse zum einfacheren Zugriff auf Datenbankfunktionalitäten
 * <br>
 * <b>Auflage 2.</b>
 *
 * @author Michael Inden
 *
 * Copyright 2012 by Michael Inden
 */
public final class DbUtils {

  public static void printSqlExceptionAndWarningInfos(final SQLException ex) {
    System.out.println(ex.getClass().getSimpleName() + " occured\n");
    printSqlExceptionAndWarningInfosImpl(ex);

    SQLException followingException = ex.getNextException();
    while (followingException != null) {
      printSqlExceptionAndWarningInfosImpl(followingException);

      followingException = followingException.getNextException();
    }
  }

  private static void printSqlExceptionAndWarningInfosImpl(final SQLException ex) {
    System.out.println("Message:   " + ex.getLocalizedMessage());
    System.out.println("SQLState:   " + ex.getSQLState());
    System.out.println("ErrorCode:   " + ex.getErrorCode());
  }

  /**
   * Null-sicher.
   * @param con	Connection, auch null erlaubt
   */
  public static void safeCloseConnection(final Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (final SQLException e) {
        printSqlExceptionAndWarningInfos(e);
      }
    }
  }

  /**
   * Null-sicher.
   * @param stmt	Statement, auch null erlaubt
   */
  public static void safeCloseStatement(final Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (final SQLException e) {
        printSqlExceptionAndWarningInfos(e);
      }
    }
  }

  /**
   * Null-sicher.
   * @param rs	ResultSet, auch null erlaubt
   */
  public static void safeCloseResultSet(final ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (final SQLException e) {
        printSqlExceptionAndWarningInfos(e);
      }
    }
  }
}
