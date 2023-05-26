package dk.souyave.payments.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import dk.souyave.payments.domain.Account;
import dk.souyave.payments.domain.AccountStatus;
import dk.souyave.payments.domain.Transaction;
import dk.souyave.payments.domain.TransactionType;
import dk.souyave.payments.exception.InvalidAccountException;
import dk.souyave.payments.service.AccountsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AccountsControllerTest {

  @Autowired
  private WebTestClient webClient;

  @MockBean
  private AccountsService accountsService;

  @AfterEach
  public void cleanUp() {
    Account.ID_GENERATOR.set(1);
  }

  @Test
  public void givenNonExistingAccount_whenCreationRequested_thenAccountCreatedSuccessfully() {

    Account account = new Account();
    account.setCurrency("DKK");
    Account.ID_GENERATOR.set(1);

    var body = "{\n" +
        "\"currency\":\"DKK\"\n" +
        "}";

    this.webClient.post().uri("/accounts/create")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .expectStatus().isCreated();

    verify(accountsService, times(1)).createAccount(account);
  }

  @Test
  public void givenExistingAccountId_whenDetailsRequested_thenSingleAccountReturned() {
    Account account = new Account();
    account.setCurrency("DKK");

    Mono<Account> accountMono = Mono.just(account);

    when(accountsService.getAccount(1)).thenReturn(accountMono);

    this.webClient.get().uri("/accounts/1/balance")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo(1)
        .jsonPath("$.currency").isEqualTo("DKK")
        .jsonPath("$.balance").isEqualTo(0.00)
        .jsonPath("$.status").isEqualTo("ACTIVE");
  }

  @Test
  public void givenNonExistentAccountId_whenDetailsRequested_thenResourceNotFoundReturned() {
    when(accountsService.getAccount(anyInt()))
    .thenReturn(Mono.error(new InvalidAccountException()));
    
    this.webClient.get().uri("/accounts/1/balance")
        .exchange()
        .expectStatus().is5xxServerError();
  }

  @Test
  public void givenInactiveAccountId_whenDetailsRequested_thenSingleAccountReturned() {

    Account account = new Account();
    account.setCurrency("DKK");
    account.setStatus(AccountStatus.INACTIVE);

    Mono<Account> accountMono = Mono.just(account);

    when(accountsService.getAccount(1)).thenReturn(accountMono);

    this.webClient.get().uri("/accounts/1/balance")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.status").isEqualTo("INACTIVE");
  }

  @Test
  public void givenDeletedAccountId_whenDetailsRequested_thenSingleAccountReturned() {
    Account account = new Account();
    account.setCurrency("DKK");
    account.setStatus(AccountStatus.DELETED);

    when(accountsService.getAccount(1)).thenReturn(Mono.just(account));

    this.webClient.get().uri("/accounts/1/balance")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.status").isEqualTo("DELETED");
  }

  @Test
  public void givenExistingAccountIds_whenTransfer_thenTransactionReturned() {
    Account account = new Account();
    account.setCurrency("DKK");
    Transaction transaction = Transaction.builder()
        .amount(new BigDecimal(1.5))
        .account(account)
        .type(TransactionType.DEBIT)
        .build();

    when(accountsService.transfer(anyInt(), anyInt(), any(BigDecimal.class))).thenReturn(Mono.just(transaction));

    this.webClient.patch().uri("/accounts/1/transfer/2/1.5")
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.amount").isEqualTo(1.5)
        .jsonPath("$.type").isEqualTo(TransactionType.DEBIT.toString());

    verify(accountsService, times(1))
        .transfer(1, 2, new BigDecimal(1.5));
  }

  @Test
  public void givenExistingTransactions_whenMiniList_thenListOfTransactionsReturned() {

    Flux<Transaction> transactions = Flux.fromIterable(
        IntStream.range(0, 20)
            .mapToObj(i -> mock(Transaction.class))
            .toList());

    when(accountsService.miniList(anyInt())).thenReturn(transactions);

    this.webClient.get().uri("/accounts/1/statements/mini")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(20);
  }
}
