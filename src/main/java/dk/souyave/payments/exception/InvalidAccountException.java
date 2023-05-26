package dk.souyave.payments.exception;

public class InvalidAccountException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidAccountException() {
    super("The selected account is invalid");
  };

  public InvalidAccountException(String message) {
    super(message);
  }

}
