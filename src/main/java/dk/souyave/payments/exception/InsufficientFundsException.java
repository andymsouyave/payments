package dk.souyave.payments.exception;

public class InsufficientFundsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InsufficientFundsException() {
    super("The selected account has insufficient funds to do complete transaction");
  };

  public InsufficientFundsException(String message) {
    super(message);
  }

}
