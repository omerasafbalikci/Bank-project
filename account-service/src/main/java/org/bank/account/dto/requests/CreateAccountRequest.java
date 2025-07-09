package org.bank.account.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bank.account.entity.AccountType;
import org.bank.account.entity.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {
    @NotBlank(message = "Owner name must not be blank")
    private String ownerName;
    @NotNull(message = "Currency must not be null")
    private Currency currency;
    @NotNull(message = "Account type must not be null")
    private AccountType accountType;
}
