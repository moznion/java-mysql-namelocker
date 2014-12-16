package moznion.net.mysql.namelocker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import moznion.net.mysql.namelocker.exception.AlreadyLockedException;
import moznion.net.mysql.namelocker.exception.CannotObtainLockException;
import moznion.net.mysql.namelocker.exception.UnknownGetLockStatusException;

/**
 * MySQL name based locker.
 * 
 * @author moznion
 * 
 */
public class NameLocker implements AutoCloseable {
  private Connection connection;
  private String lockName;

  public NameLocker(Connection connection, String lockName) throws SQLException,
      CannotObtainLockException, AlreadyLockedException {
    this(connection, lockName, 0);
  }

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
