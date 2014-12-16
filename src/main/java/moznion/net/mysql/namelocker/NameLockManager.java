package moznion.net.mysql.namelocker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Manager of named lock for MySQL.
 * 
 * @author moznion
 */
public class NameLockManager implements AutoCloseable {
  private Connection connection;
  private String lockName;

  public NameLockManager(Connection connection, String lockName) {
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
  }

  public LockStatus getLock() throws SQLException {
    return getLock(0);
  }

  public LockStatus getLock(int timeout) throws SQLException {
    try (PreparedStatement sth = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
      sth.setString(1, lockName);
      sth.setInt(2, timeout);

      ResultSet resultSet = sth.executeQuery();
      resultSet.next();
      Integer status = resultSet.getInt(1);

      if (resultSet.wasNull()) {
        return LockStatus.ERROR;
      }

      switch (status) {
        case 0:
          return LockStatus.FAILURE;
        case 1:
          return LockStatus.SUCCESS;
      }

      // Probably here is unreachable
      return LockStatus.NA;
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
