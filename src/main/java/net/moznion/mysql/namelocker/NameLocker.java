package net.moznion.mysql.namelocker;

import net.moznion.mysql.namelocker.exception.AlreadyLockedException;
import net.moznion.mysql.namelocker.exception.CannotObtainLockException;
import net.moznion.mysql.namelocker.exception.UnknownGetLockStatusException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL named locker.
 * 
 * @author moznion
 * 
 */
public class NameLocker implements AutoCloseable {
  private Connection connection;
  private String lockName;

  /**
   * Get a named lock.
   * 
   * <p>
   * This method does not wait for a timeout when getting a lock.
   * </p>
   * 
   * @param connection MySQL connection
   * @param lockName Name of lock
   * @throws SQLException SQL is something wrong
   * @throws CannotObtainLockException Cannot obtain a lock by something error of MySQL
   * @throws AlreadyLockedException A lock has already taken by other client
   */
  public NameLocker(Connection connection, String lockName) throws SQLException,
      CannotObtainLockException, AlreadyLockedException {
    this(connection, lockName, 0);
  }

  /**
   * Get a named lock.
   * 
   * @param connection MySQL connection
   * @param lockName Name of lock
   * @param timeout Seconds for timeout
   * @throws SQLException SQL is something wrong
   * @throws CannotObtainLockException Cannot obtain a lock by something error of MySQL
   * @throws AlreadyLockedException A lock has already taken by other client
   */
  public NameLocker(Connection connection, String lockName, int timeout) throws SQLException,
      CannotObtainLockException, AlreadyLockedException {
    if (connection == null) {
      throw new IllegalArgumentException("`connection` must not be null");
    }

    if (lockName == null) {
      throw new IllegalArgumentException("`lockName` must not be null");
    }

    if (lockName.length() == 0) {
      throw new IllegalArgumentException("`lockName` must not be empty");
    }

    this.connection = connection;
    this.lockName = lockName;

    try (PreparedStatement sth = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
      sth.setString(1, lockName);
      sth.setInt(2, timeout);

      ResultSet resultSet = sth.executeQuery();
      resultSet.next();
      Integer status = resultSet.getInt(1);

      if (resultSet.wasNull()) {
        throw new CannotObtainLockException();
      }

      if (status == 0) {
        throw new AlreadyLockedException(this.lockName);
      }

      if (status != 1) {
        // Probably here is unreachable
        throw new UnknownGetLockStatusException(status);
      }
    }
  }

  @Override
  public void close() throws Exception {
    if (connection.isClosed()) {
      // Connection has been already closed, so lock has also been released.
      // Therefore here is nothing to do.
      return;
    }

    // Release lock
    try (PreparedStatement sth = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
      sth.setString(1, lockName);
      sth.executeQuery();
    }
  }
}
