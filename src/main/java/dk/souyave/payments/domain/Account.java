package dk.souyave.payments.domain;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The class holding account information
 * 
 * NB: The Lombok builder annotation is not used here as it clashes with the
 * other annotations used in this class
 */
@Data
@NoArgsConstructor
public class Account {

  public static AtomicInteger ID_GENERATOR = new AtomicInteger(1);

  // Automatically generate the account Id's from a generator
  @NotNull
  @Setter(AccessLevel.NONE)
  private final int id = ID_GENERATOR.getAndIncrement();

  @NotNull
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal balance = BigDecimal.ZERO;

  // restrict to only 3 characters long as per currency code specs
  @NotEmpty
  @Pattern(regexp = "^[A-Z]{3}$")
  @Schema(description = "3 letter currency code", example = "DKK")
  private String currency;

  @Schema(description = "Current status of the account.")
  private AccountStatus status = AccountStatus.ACTIVE;
}
