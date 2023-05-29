package dk.souyave.payments.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dk.souyave.payments.domain.Account;
import dk.souyave.payments.domain.AccountStatus;
import dk.souyave.payments.exception.InvalidAccountException;

@SpringBootTest
public class AccountsRepoTest {

  @Autowired
  private AccountsRepo accountsRepo;

  @BeforeEach
  public void init() {
    accountsRepo.clearAll();
  }

  @Test
  public void givenNonExistentAccount_whenRequestAccountCreation_thenAddedToStorage() {

    Account newAccount = new Account();
    newAccount.setCurrency("DKK");

    accountsRepo.createAccount(newAccount);
    Account account = accountsRepo.getAccount(1).block();

    assertNotNull(account);
    assertEquals("DKK", account.getCurrency());
    assertEquals(AccountStatus.ACTIVE, account.getStatus());
  }

  @Test
  public void givenExisctingAccount_whenRequestAccountDelete_thenStatusChanged() {
    Account newAccount = new Account();
    newAccount.setCurrency("DKK");

    accountsRepo.createAccount(newAccount);
    accountsRepo.deleteAccount(1);
    Account account = accountsRepo.getAccount(1).block();

    assertNotNull(account);
    assertEquals("DKK", account.getCurrency());
    assertEquals(AccountStatus.DELETED, account.getStatus());
  }

  @Test
  public void givenNonExistingAccount_whenRequestAccountDelete_thenNotFound() {
    assertThrows(InvalidAccountException.class, () -> {
      accountsRepo.deleteAccount(1);
    });
  }

}
