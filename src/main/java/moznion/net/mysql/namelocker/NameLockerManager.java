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
public class NameLockerManager implements AutoCloseable {
  private Connection connection;
  private String lockName;

  public NameLockerManager(Connection connection, String lockName) {
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

  public Integer getLock() throws SQLException {
    return getLock(0);
  }

  public Integer getLock(int timeout) throws SQLException {
    try (PreparedStatement sth = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
      sth.setString(1, lockName);
      sth.setInt(2, timeout);

      ResultSet resultSet = sth.executeQuery();
      resultSet.next();
      Integer status = resultSet.getInt(1);
      if (resultSet.wasNull()) {
        return null;
      }

      return status;
    }
  }

  @Override
  public void close() throws Exception {
    if (connection.isClosed()) {
      return;
    }

    PreparedStatement sth = connection.prepareStatement("SELECT RELEASE_LOCK(?)");
    sth.setString(1, lockName);
    sth.executeQuery();
    sth.close();
  }
}
