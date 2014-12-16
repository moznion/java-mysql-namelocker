package moznion.net.mysql.namelocker.exception;

/**
 * Exception which occurs when `SELECT GET_LOCK()` returns the unknown status code.
 * 
 * @author moznion
 *
 */
public class UnknownGetLockStatusException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UnknownGetLockStatusException(int responseCode) {
    super(new StringBuilder().append("`SELECT GET_LOCK()` returns the unknown status code (code: ")
        .append(responseCode).append(")").toString());
  }
}
