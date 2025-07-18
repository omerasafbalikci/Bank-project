package org.bank.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private String id;
    private String ownerName;
    private double balance;
    private String accountNumber;
    private Currency currency;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private AccountType accountType;
    private boolean isDeleted;
}
