package org.bank.account.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bank.account.entity.AccountType;
import org.bank.account.entity.Currency;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {
    private String ownerName;
    private double balance;
    private Currency currency;
    private Instant createdAt;
    private Instant updatedAt;
    private AccountType accountType;
}
