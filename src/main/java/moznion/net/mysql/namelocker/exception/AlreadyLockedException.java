package moznion.net.mysql.namelocker.exception;

/**
 * Exception which occurs when it has been already locked by specified name by other clients.
 * 
 * @author moznion
 *
 */
public class AlreadyLockedException extends Exception {
  private static final long serialVersionUID = 1L;

  public AlreadyLockedException(String lockName) {
    super(new StringBuilder()
        .append("Specified name of lock has been already locked by other clients (name: ")
        .append(lockName).append(")").toString());
  }
}
