package dk.souyave.payments.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Builder
public class Transaction {

  public static AtomicInteger ID_GENERATOR = new AtomicInteger(1);

  // Automatically generate the transaction Id's from a generator
  @NotNull
  @Setter(AccessLevel.NONE)
  @Builder.Default
  private int id = ID_GENERATOR.getAndIncrement();

  @NotNull
  @DecimalMin("0")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal amount;

  // Storing the entire account object here instead of just an Id as am treating
  // it like a relational database
  @NotNull
  @Schema(description = "The account that the transaction is performed against")
  private Account account;

  @NotNull
  @Schema(description = "The transaction type of debit or credit", example = "CREDIT")
  private TransactionType type;

  @Setter(AccessLevel.NONE)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @PastOrPresent
  @Builder.Default
  private LocalDateTime date = LocalDateTime.now();
}
