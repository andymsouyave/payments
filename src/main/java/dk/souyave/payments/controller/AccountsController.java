package dk.souyave.payments.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dk.souyave.payments.domain.Account;
import dk.souyave.payments.domain.Transaction;
import dk.souyave.payments.service.AccountsService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides account details including balances for currencies
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountsController {

  private final AccountsService accountsService;

  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Account> createAccount(@RequestBody Account account) {
    return accountsService.createAccount(account);
  }

  @GetMapping("/{accountId}/balance")
  public Mono<Account> getBalance(@PathVariable Integer accountId) {
    return accountsService.getAccount(accountId);
  }

  @PatchMapping("/{accountId}/transfer/{toAccountId}/{amount}")
  public Mono<Transaction> transfer(@PathVariable Integer accountId, @PathVariable Integer toAccountId,
      @PathVariable BigDecimal amount) {
    return accountsService.transfer(accountId, toAccountId, amount);
  }

  @GetMapping(path = "/{accountId}/statements/mini")
  public Flux<Transaction> getTransactions(@PathVariable Integer accountId) {
    return accountsService.miniList(accountId);
  }

  @DeleteMapping("/delete")
  public void accountsService(int accountId) {
    accountsService.deleteAccount(accountId);
  }
}
