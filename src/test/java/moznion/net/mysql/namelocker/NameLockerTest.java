package moznion.net.mysql.namelocker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import moznion.net.mysql.namelocker.exception.AlreadyLockedException;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class NameLockerTest {
  private static String dbName;

  @BeforeClass
  public static void initial() {
    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost", "root", "")) {
      while (true) {
        dbName = "test_name_locker_" + RandomStringUtils.randomAlphanumeric(16);

        // check has DB already existed or not?
        try (PreparedStatement sth =
            connection
                .prepareStatement("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?")) {
          sth.setString(1, dbName);
          ResultSet resultSet = sth.executeQuery();
          resultSet.next();

          if (resultSet.getRow() > 0) {
            // when DB has already existed
            continue;
          }
        }

        break;
      }

      System.out.println("Using DB Name: " + dbName);

      // create database for testing
      try (Statement stmt = connection.createStatement()) {
        stmt.executeUpdate("CREATE DATABASE " + dbName);
      }
    } catch (SQLException sqle) {
      return;
    }

    // create table for testing
    try (Connection connection =
        DriverManager.getConnection("jdbc:mysql://localhost/" + dbName, "root", "")) {
      try (Statement stmt = connection.createStatement()) {
        stmt.executeUpdate("CREATE TABLE test_table(id int)");
      }
    } catch (SQLException sqle) {
      return;
    }
  }

  @AfterClass
  public static void terminate() {
    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost", "root", "")) {
      // drop testing database
      try (Statement stmt = connection.createStatement()) {
        stmt.executeUpdate("DROP DATABASE " + dbName);
      }
    } catch (SQLException e) {
      return;
    }
  }

  @Test
  public void shouldGetLockSuccessfully() {
    try (Connection connection =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "")) {
      try (NameLocker locker = new NameLocker(connection, "Lock Star")) {
        // do something
      } catch (Exception e) {
        fail("Catch unexpected exception");
      }
      assertTrue("Get lock successfully", true);
    } catch (SQLException e1) {
      assumeTrue("MySQL may not be upped", false);
      return;
    }
  }

  @Test
  public void shouldGetLockSuccessfullyWithTimeout() {
    try (Connection connection =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "")) {
      try (NameLocker locker = new NameLocker(connection, "Lock Star", 10)) {
        // do something
      } catch (Exception e) {
        fail("Catch unexpected exception");
      }
      assertTrue("Get lock successfully", true);
    } catch (SQLException e1) {
      assumeTrue("MySQL may not be upped", false);
      return;
    }
  }

  @Test
  public void shouldBlockLockingSuccessfully() {
    try (Connection conn1 =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "")) {
      try (Connection conn2 =
          DriverManager.getConnection(
              new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
              "root", "")) {
        try (NameLocker locker1 = new NameLocker(conn1, "Lock Star")) {
          try (NameLocker locker2 = new NameLocker(conn2, "Lock Star")) {
            // do something
          }
        } catch (AlreadyLockedException ale) {
          assertTrue("Block successfully", true);
          return;
        }
      }
    } catch (SQLException sqle) {
      assumeTrue("MySQL may not be upped", false);
      return;
    } catch (Exception e) {
      fail("Catch unexpected exception");
    }

    fail("Not blocked");
  }

  @Test
  public void shouldReleaseResourceSuccessfully() {
    try (Connection conn1 =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "")) {
      try (Connection conn2 =
          DriverManager.getConnection(
              new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
              "root", "")) {
        try (NameLocker locker1 = new NameLocker(conn1, "Lock Star")) {
          // do something
        } catch (Exception e) {
          fail("Catch unexpected exception");
        }

        try (NameLocker locker2 = new NameLocker(conn2, "Lock Star")) {
          // do something
        } catch (Exception e) {
          fail("Catch unexpected exception");
        }
      }
    } catch (SQLException sqle) {
      assumeTrue("MySQL may not be upped", false);
      return;
    }

    assertTrue("Release resource successfully", true);
  }

  @Test
  public void argumentConstraintIsWorkingRightly() throws SQLException {
    try (NameLocker locker = new NameLocker(null, "Lock Star")) {
      // do something
      fail();
    } catch (IllegalArgumentException iae) {
      assertTrue(true);
    } catch (Exception e) {
      fail();
    }

    try (Connection conn =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "")) {
      try (NameLocker locker = new NameLocker(conn, null)) {
        // do something
        fail();
      } catch (IllegalArgumentException iae) {
        assertTrue(true);
      } catch (Exception e) {
        fail();
      }
    }
  }
}
