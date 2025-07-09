package org.bank.account.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bank.account.entity.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    @NotNull(message = "Sender must not be null")
    String fromAccountId;
    @NotNull(message = "Receiver must not be null")
    String toAccountId;
    @NotNull(message = "Amount must not be null")
    double amount;
    @NotNull(message = "Currency must not be null")
    Currency currency;
    String description;
}
