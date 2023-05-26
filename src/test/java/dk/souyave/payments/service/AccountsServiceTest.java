package dk.souyave.payments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import dk.souyave.payments.domain.Account;
import dk.souyave.payments.domain.AccountStatus;
import dk.souyave.payments.exception.InsufficientFundsException;
import dk.souyave.payments.exception.InvalidAccountException;
import dk.souyave.payments.exception.UnmatchedCurrenciesException;
import dk.souyave.payments.repo.AccountsRepo;
import dk.souyave.payments.repo.TransactionRepo;
import reactor.core.publisher.Mono;

@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @MockBean
  private AccountsRepo accountsRepo;

  @MockBean
  private TransactionRepo transactionRepo;

  @Test
  public void givenExistingAccounts_whenRequestTransfer_thenAccountBalancesAdjusted() {
    Account fromAccount = new Account(), toAccount = new Account();
    fromAccount.setCurrency("DKK");
    fromAccount.setBalance(BigDecimal.TEN);
    toAccount.setCurrency("DKK");

    when(accountsRepo.getAccount(1)).thenReturn(Mono.just(fromAccount));
    when(accountsRepo.getAccount(2)).thenReturn(Mono.just(toAccount));

    accountsService.transfer(1, 2, new BigDecimal(1.23)).block();

    assertEquals(new BigDecimal(8.77).setScale(2, RoundingMode.HALF_UP),
        fromAccount.getBalance().setScale(2, RoundingMode.HALF_UP));
    assertEquals(new BigDecimal(1.23).setScale(2, RoundingMode.HALF_UP),
        toAccount.getBalance().setScale(2, RoundingMode.HALF_UP));
  }

  @Test
  public void givenExistingAccountsWithDifferentCurrencies_whenRequestTransfer_thenUnmatchedCurrenciesError() {
    Account fromAccount = new Account(), toAccount = new Account();
    fromAccount.setCurrency("DKK");
    fromAccount.setBalance(BigDecimal.TEN);
    toAccount.setCurrency("USD");

    when(accountsRepo.getAccount(1)).thenReturn(Mono.just(fromAccount));
    when(accountsRepo.getAccount(2)).thenReturn(Mono.just(toAccount));

    assertThrows(UnmatchedCurrenciesException.class, () -> {
      accountsService.transfer(1, 2, new BigDecimal(1.23)).block();
    });
  }

  @Test
  public void givenNonExistingAccount_whenRequestTransfer_thenInvalidAccountError() {

    when(accountsRepo.getAccount(1)).thenReturn(Mono.just(new Account()));
    when(accountsRepo.getAccount(2)).thenReturn(Mono.error(new InvalidAccountException()));

    assertThrows(InvalidAccountException.class, () -> {
      accountsService.transfer(1, 2, BigDecimal.TEN).block();
    });
  }

  @Test
  public void givenDeletedAccount_whenRequestTransfer_thenInvalidAccountError() {
    Account fromAccount = new Account(), toAccount = new Account();
    toAccount.setStatus(AccountStatus.DELETED);

    when(accountsRepo.getAccount(1)).thenReturn(Mono.just(fromAccount));
    when(accountsRepo.getAccount(2)).thenReturn(Mono.just(toAccount));

    assertThrows(InvalidAccountException.class, () -> {
      accountsService.transfer(1, 2, BigDecimal.TEN).block();
    });
  }

  @Test
  public void givenAccountWithNoBalance_whenRequestTransfer_thenInvalidAccountError() {
    Account fromAccount = new Account(), toAccount = new Account();
    fromAccount.setCurrency("DKK");
    toAccount.setCurrency("DKK");

    when(accountsRepo.getAccount(1)).thenReturn(Mono.just(fromAccount));
    when(accountsRepo.getAccount(2)).thenReturn(Mono.just(toAccount));

    assertThrows(InsufficientFundsException.class, () -> {
      accountsService.transfer(1, 2, BigDecimal.TEN).block();
    });
  }
}
