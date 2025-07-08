package org.bank.account.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private double amount;
    private Instant timestamp;
    private String type;
}
