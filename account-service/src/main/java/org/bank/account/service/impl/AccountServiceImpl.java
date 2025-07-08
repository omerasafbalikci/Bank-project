package org.bank.account.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bank.account.database.RocksDbService;
import org.bank.account.dto.requests.CreateAccountRequest;
import org.bank.account.dto.responses.GetAccountResponse;
import org.bank.account.entity.Account;
import org.bank.account.utilities.exceptions.AccountBalanceException;
import org.bank.account.utilities.exceptions.AccountNotFoundException;
import org.bank.account.utilities.mappers.AccountMapper;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Log4j2
public class AccountServiceImpl {
    private final RocksDbService rocksDb;
    private final AccountMapper accountMapper;

    public GetAccountResponse createAccount(CreateAccountRequest createAccountRequest) {
        if (createAccountRequest.getBalance() < 0) {
            throw new AccountBalanceException("Balance cannot be negative");
        }
        Account account = accountMapper.toAccount(createAccountRequest);
        account.setId(UUID.randomUUID().toString());
        rocksDb.save("account:" + account.getId(), account);
        return accountMapper.toGetAccountResponse(account);
    }

    public void deleteAccount(String accountId) {
        Account account = rocksDb.get("account:" + accountId, Account.class);
        if (account == null) {
            throw new AccountNotFoundException("Account not found");
        }
        account.setDeleted(true);
        account.setUpdatedAt(Instant.now());
        rocksDb.save("account:" + accountId, account);
    }
}
