package org.bank.account.utilities.mappers;

import org.bank.account.dto.requests.CreateAccountRequest;
import org.bank.account.dto.responses.GetAccountResponse;
import org.bank.account.entity.Account;
import org.bank.account.entity.AccountStatus;
import org.bank.account.utilities.IbanGenerator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;

@Component
public class AccountMapper {
    public Account toAccount(CreateAccountRequest request) {
        if (request == null) {
            return null;
        }
        Account account = new Account();
        account.setOwnerName(request.getOwnerName());
        account.setBalance(0.0);
        String iban = IbanGenerator.generateValidIban("00062", "00001", String.valueOf(new Random().nextLong() & Long.MAX_VALUE));
        account.setAccountNumber(iban);
        account.setCurrency(request.getCurrency());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());
        account.setAccountType(request.getAccountType());
        account.setDeleted(false);
        return account;
    }

    public GetAccountResponse toGetAccountResponse(Account account) {
        if (account == null) {
            return null;
        }
        return new GetAccountResponse(
                account.getId(),
                account.getOwnerName(),
                account.getBalance(),
                account.getAccountNumber(),
                account.getCurrency().toString(),
                account.getStatus().toString(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                account.getAccountType().toString()
        );
    }
}
