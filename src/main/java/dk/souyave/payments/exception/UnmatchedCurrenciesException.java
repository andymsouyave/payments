package dk.souyave.payments.exception;

public class UnmatchedCurrenciesException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UnmatchedCurrenciesException() {
    super("The selected accounts for the transfer have umatched currencies");
  };

  public UnmatchedCurrenciesException(String message) {
    super(message);
  }

}
