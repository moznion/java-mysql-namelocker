package net.moznion.mysql.namelocker.exception;

/**
 * Exception which occurs when `SELECT GET_LOCK()` returns the unknown status code.
 * 
 * @author moznion
 *
 */
public class UnknownGetLockStatusException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Generate exception.
   * 
   * @param statusCode status code
   */
  public UnknownGetLockStatusException(int statusCode) {
    super(new StringBuilder().append("`SELECT GET_LOCK()` returns the unknown status code (code: ")
        .append(statusCode).append(")").toString());
  }
}
