package moznion.net.mysql.namelocker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
  public static void initial() throws SQLException {
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
    }

    // create table for testing
    try (Connection connection =
        DriverManager.getConnection("jdbc:mysql://localhost/" + dbName, "root", "")) {
      try (Statement stmt = connection.createStatement()) {
        stmt.executeUpdate("CREATE TABLE test_table(id int)");
      }
    }
  }

  @AfterClass
  public static void terminate() throws SQLException {
    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost", "root", "")) {
      // drop testing database
      try (Statement stmt = connection.createStatement()) {
        stmt.executeUpdate("DROP DATABASE " + dbName);
      }
    }
  }

  @Test
  public void shouldGetLockSuccessfully() throws SQLException {
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
    }
  }

  @Test
  public void shouldBlockLockingSuccessfully() throws SQLException {
    Connection conn1 =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "");
    Connection conn2 =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "");

    try (NameLocker locker1 = new NameLocker(conn1, "Lock Star")) {
      try (NameLocker locker2 = new NameLocker(conn2, "Lock Star")) {
        // do something
      }
    } catch (AlreadyLockedException ale) {
      assertTrue("Block successfully", true);
      return;
    } catch (Exception e) {
      fail("Catch unexpected exception");
    }

    conn1.close();
    conn2.close();

    fail("Not blocked");
  }

  @Test
  public void shouldReleaseResourceSuccessfully() throws SQLException {
    Connection conn1 =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "");
    Connection conn2 =
        DriverManager.getConnection(
            new StringBuilder().append("jdbc:mysql://localhost/").append(dbName).toString(),
            "root", "");

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

    conn1.close();
    conn2.close();

    assertTrue("Release resource successfully", true);
  }
}
