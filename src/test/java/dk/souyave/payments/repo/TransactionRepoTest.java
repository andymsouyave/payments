package dk.souyave.payments.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dk.souyave.payments.domain.Account;
import dk.souyave.payments.domain.Transaction;
import dk.souyave.payments.domain.TransactionType;

@SpringBootTest
public class TransactionRepoTest {

  @Autowired
  private TransactionRepo transactionRepo;

  @BeforeEach
  public void init() {
    Account.ID_GENERATOR.set(1);
  }

  @Test
  public void givenExistingTransactionsForAccount_whenRequestStatement_thenAllTransactionsForAccountSupplied() {

    Account fromAccount = new Account(), toAccount = new Account();
    fromAccount.setCurrency("DKK");

    transactionRepo.save(Transaction.builder().account(fromAccount).amount(BigDecimal.TEN)
        .type(TransactionType.CREDIT).build());
    transactionRepo.save(Transaction.builder().account(fromAccount).amount(BigDecimal.ONE)
        .type(TransactionType.DEBIT).build());
    transactionRepo.save(Transaction.builder().account(toAccount).amount(BigDecimal.ONE)
        .type(TransactionType.DEBIT).build());

    List<Transaction> transactions = transactionRepo.findAllByAccountIdOrderByDate(1, 2).collectList().block();
    assertEquals(2, transactions.size());
    assertTrue(transactions.get(0).getAmount().equals(BigDecimal.ONE));
    assertTrue(transactions.get(1).getAmount().equals(BigDecimal.TEN));
  }
}
