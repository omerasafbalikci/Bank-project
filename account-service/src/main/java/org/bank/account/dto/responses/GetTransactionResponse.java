package org.bank.account.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTransactionResponse implements Serializable {
    private String id;
    private String transactionId;
    private String senderId;
    private String receiverId;
    private double amount;
    private String currency;
    private String transactionType;
    private String transactionStatus;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
