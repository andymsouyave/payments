package dk.souyave.payments.repo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import dk.souyave.payments.domain.Account;
import dk.souyave.payments.domain.AccountStatus;
import dk.souyave.payments.exception.InvalidAccountException;
import reactor.core.publisher.Mono;

/**
 * Repository holding the all the accounts and payment details
 */
@Repository
public class AccountsRepo {

  private Map<Integer, Account> accounts = new ConcurrentHashMap<>();

  /**
   * Get the account details for the provided id
   * 
   * @param accountId to look for
   * @return found account object or invalid account if it doesnt exist
   */
  public Mono<Account> getAccount(int accountId) {
    return Mono.justOrEmpty(this.accounts.get(accountId))
        .switchIfEmpty(Mono.error(new InvalidAccountException()));
  }

  /**
   * Create an account with the provided account details
   * 
   * @param account
   */
  public Mono<Account> createAccount(Account account) {
    this.accounts.put(account.getId(), account);
    return Mono.justOrEmpty(account);
  }

  /**
   * Delete an account with the associated account id
   * 
   * @param accountId
   */
  public void deleteAccount(int accountId) {
    Optional.ofNullable(this.accounts.get(accountId))
        .ifPresentOrElse(
            account -> account.setStatus(AccountStatus.DELETED),
            () -> {
              throw new InvalidAccountException();
            });
  }

  public void clearAll() {
    accounts.clear();
    Account.ID_GENERATOR.set(1);
  }
}
