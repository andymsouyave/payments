package dk.souyave.payments.repo;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import dk.souyave.payments.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Repository
public class TransactionRepo {

  private Map<Integer, Transaction> transactions = new ConcurrentHashMap<>();

  public Mono<Transaction> save(Transaction transaction) {
    transactions.put(transaction.getId(), transaction);
    return Mono.just(transaction);
  }

  /**
   * JPA-like query to find all transactions for provided account id, and limits
   * the result set by the provided size of the most recent transactions
   * 
   * @param size of result set
   * @return stream of most recent transactions limited by size
   */
  public Flux<Transaction> findAllByAccountIdOrderByDate(int accountId, int size) {
    return Flux.fromStream(transactions.values().stream()
        .filter(transaction -> accountId == transaction.getAccount().getId())
        .sorted(Comparator.comparing(Transaction::getDate).reversed())
        .limit(size)).subscribeOn(Schedulers.parallel());
  }
}
