package org.bank.account.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountFilterCriteria {
    private String id;
    private String ownerName;
    private double balance;
    private String accountNumber;
    private String currency;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String accountType;
    private Boolean isDeleted;
}
