package org.bank.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String id;
    private String transactionId;
    private Long senderId;
    private Long receiverId;
    private double amount;
    private Currency currency;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
