package moznion.net.mysql.namelocker.exception;

/**
 * Exception which occurs when something error by MySQL has happened.
 * 
 * @author moznion
 *
 */
public class CannotObtainLockException extends Exception {
  private static final long serialVersionUID = 1L;

  public CannotObtainLockException() {
    super("Cannot obtain MySQL name lock. Something error by MySQL has happened "
        + "(such as running out of memory or the thread was killed with mysqladmin kill)");
  }
}
