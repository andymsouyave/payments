package dk.souyave.payments.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import dk.souyave.payments.domain.Account;
import dk.souyave.payments.domain.AccountStatus;
import dk.souyave.payments.domain.Transaction;
import dk.souyave.payments.domain.TransactionType;
import dk.souyave.payments.exception.InsufficientFundsException;
import dk.souyave.payments.exception.InvalidAccountException;
import dk.souyave.payments.exception.UnmatchedCurrenciesException;
import dk.souyave.payments.repo.AccountsRepo;
import dk.souyave.payments.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {

  private final AccountsRepo accountsRepo;
  private final TransactionRepo transactionRepo;

  public Mono<Account> getAccount(int accountId) {
    return accountsRepo.getAccount(accountId);
  }

  public Mono<Account> createAccount(Account account) {
    return accountsRepo.createAccount(account);
  }

  public void deleteAccount(int accountId) {
    accountsRepo.deleteAccount(accountId);
  }

  public Flux<Transaction> miniList(int accountId) {
    return transactionRepo.findAllByAccountIdOrderByDate(accountId, 20);
  }

  /**
   * Transfer amount of money between provided accounts
   * 
   * @param fromAccountId
   * @param toAccountId
   * @param amount
   * @return the 'from' account transaction details
   */
  public Mono<Transaction> transfer(int fromAccountId, int toAccountId, BigDecimal amount) {

    Mono<Account> fromAccount = accountsRepo.getAccount(fromAccountId);
    Mono<Account> toAccount = accountsRepo.getAccount(toAccountId);

    return Mono
        .zip(fromAccount, toAccount)
        .flatMap(v -> this.processTransfer(v.getT1(), v.getT2(), amount))
        .doOnSuccess(t -> {
          log.info("Successfully transferred {} {} from account {} to account {}",
              amount, t.getAccount().getCurrency(), fromAccountId, toAccountId);
        });
  }

  public Mono<Transaction> processTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {

    // both accounts should be active for a transaction to occur
    if (!AccountStatus.ACTIVE.equals(fromAccount.getStatus()) || !AccountStatus.ACTIVE.equals(toAccount.getStatus())) {
      log.error("One of the accounts for the transfer is not active. account {} = {}, account {} = {}",
          fromAccount.getId(), fromAccount.getStatus(), toAccount.getId(), toAccount.getStatus());
      throw new InvalidAccountException();

      // check if the currencies match for a straight conversion
    } else if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
      log.error("The account currencies to not match. account {} = {}, account {} = {}",
          fromAccount.getId(), fromAccount.getCurrency(), toAccount.getId(), toAccount.getCurrency());
      throw new UnmatchedCurrenciesException();

      // check if from account has sufficient funds to do the transaction
    } else if (fromAccount.getBalance().compareTo(amount) < 0) {
      log.error("The debiting account has insufficient funds to do the transfer. account {} = {}, amount = {}",
          fromAccount.getId(), fromAccount.getBalance(), amount);
      throw new InsufficientFundsException();
    }

    // adjust the balances accordingly for each account
    fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
    toAccount.setBalance(toAccount.getBalance().add(amount));

    // add the main and inverse transactions to the store
    Transaction fromTransaction = Transaction.builder()
        .account(fromAccount)
        .amount(amount)
        .type(TransactionType.DEBIT)
        .build();

    Transaction toTransaction = Transaction.builder()
        .account(toAccount)
        .amount(amount)
        .type(TransactionType.CREDIT)
        .build();

    transactionRepo.save(toTransaction);
    transactionRepo.save(fromTransaction);

    // return the originating transaction
    return Mono.just(fromTransaction);
  }
}
