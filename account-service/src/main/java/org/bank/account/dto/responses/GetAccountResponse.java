package org.bank.account.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountResponse {
    private String id;
    private String ownerName;
    private double balance;
    private String accountNumber;
    private String currency;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private String accountType;
}
