package org.bank.account.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bank.account.database.RocksDbService;
import org.bank.account.dto.requests.*;
import org.bank.account.dto.responses.GetAccountResponse;
import org.bank.account.dto.responses.GetTransactionResponse;
import org.bank.account.dto.responses.PagedResponse;
import org.bank.account.entity.*;
import org.bank.account.entity.Currency;
import org.bank.account.service.abstracts.AccountService;
import org.bank.account.utilities.exceptions.AccountBalanceException;
import org.bank.account.utilities.exceptions.AccountNotFoundException;
import org.bank.account.utilities.exceptions.BadRequestException;
import org.bank.account.utilities.mappers.AccountMapper;
import org.bank.account.utilities.mappers.TransactionMapper;
import org.rocksdb.RocksIterator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class AccountServiceImpl implements AccountService {
    private final RocksDbService rocksDb;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;

    @Override
    @Cacheable(value = "accounts", key = "#accountId", unless = "#result == null")
    public GetAccountResponse getAccountById(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new BadRequestException("Account ID must not be null or blank");
        }
        Account account = rocksDb.get("account:" + accountId, Account.class);
        if (account == null || account.isDeleted()) {
            throw new AccountNotFoundException("Account not found or deleted");
        }
        return accountMapper.toGetAccountResponse(account);
    }

    @Override
    public PagedResponse<GetAccountResponse> getAccountsFilteredSortedPaged(AccountFilterCriteria criteria, String sortField, String sortDirection, int page, int size) {
        if (criteria == null) {
            criteria = new AccountFilterCriteria();
        }
        RocksIterator iterator = rocksDb.getRocksIterator();
        List<Account> filtered = new ArrayList<>();

        for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
            String key = new String(iterator.key());
            if (!key.startsWith("account:")) continue;
            Account account = rocksDb.get(key, Account.class);
            if (account == null) continue;

            if (criteria.getId() != null && !criteria.getId().isBlank()
                    && !account.getId().equals(criteria.getId())) continue;
            if (criteria.getOwnerName() != null && !criteria.getOwnerName().isBlank()
                    && !account.getOwnerName().toLowerCase()
                    .contains(criteria.getOwnerName().toLowerCase())) continue;
            if (criteria.getAccountNumber() != null && !criteria.getAccountNumber().isBlank()
                    && !account.getAccountNumber().equals(criteria.getAccountNumber())) continue;
            if (criteria.getCurrency() != null && !criteria.getCurrency().isBlank()
                    && !account.getCurrency().name().equalsIgnoreCase(criteria.getCurrency())) continue;
            if (criteria.getStatus() != null && !criteria.getStatus().isBlank()
                    && !account.getStatus().name().equalsIgnoreCase(criteria.getStatus())) continue;
            if (criteria.getAccountType() != null && !criteria.getAccountType().isBlank()
                    && !account.getAccountType().name().equalsIgnoreCase(criteria.getAccountType())) continue;
            if (criteria.getCreatedAt() != null && !criteria.getCreatedAt().isBlank()
                    && account.getCreatedAt() != null
                    && !account.getCreatedAt().toString().startsWith(criteria.getCreatedAt())) continue;
            if (criteria.getUpdatedAt() != null && !criteria.getUpdatedAt().isBlank()
                    && account.getUpdatedAt() != null
                    && !account.getUpdatedAt().toString().startsWith(criteria.getUpdatedAt())) continue;
            if (criteria.getIsDeleted() && !account.isDeleted()) continue;
            if (!criteria.getIsDeleted() && account.isDeleted()) continue;
            if (criteria.getBalance() > 0 && account.getBalance() < criteria.getBalance()) continue;

            filtered.add(account);
        }
        Comparator<Account> comparator = switch (sortField != null ? sortField : "createdAt") {
            case "balance" -> Comparator.comparing(Account::getBalance);
            case "ownerName" -> Comparator.comparing(Account::getOwnerName);
            case "updatedAt" -> Comparator.comparing(Account::getUpdatedAt);
            default -> Comparator.comparing(Account::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(sortDirection)) comparator = comparator.reversed();
        filtered.sort(comparator);
        int totalItems = filtered.size();
        int from = Math.min(page * size, totalItems);
        int to = Math.min(from + size, totalItems);
        List<GetAccountResponse> content = filtered.subList(from, to).stream()
                .map(accountMapper::toGetAccountResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                page,
                (int) Math.ceil((double) totalItems / size),
                totalItems,
                size,
                page == 0,
                to == totalItems,
                to < totalItems,
                page > 0
        );
    }

    @Override
    @CachePut(value = "accounts", key = "#result != null ? #result.id : null", unless = "#result == null")
    public GetAccountResponse createAccount(CreateAccountRequest createAccountRequest) {
        boolean isValidCurrency = Arrays.stream(Currency.values())
                .anyMatch(currency -> currency == createAccountRequest.getCurrency());
        if (!isValidCurrency) {
            throw new BadRequestException("Currency is not valid");
        }
        boolean isValidAccountType = Arrays.stream(AccountType.values())
                .anyMatch(accountType -> accountType == createAccountRequest.getAccountType());
        if (!isValidAccountType) {
            throw new BadRequestException("Account type is not valid");
        }
        Account account = accountMapper.toAccount(createAccountRequest);
        account.setId(UUID.randomUUID().toString());
        rocksDb.save("account:" + account.getId(), account);
        return accountMapper.toGetAccountResponse(account);
    }

    @Override
    @CacheEvict(value = "accounts", key = "#accountId")
    public void deleteAccount(String accountId) {
        Account account = rocksDb.get("account:" + accountId, Account.class);
        if (account == null || account.isDeleted()) throw new AccountNotFoundException("Account not found or deleted");
        account.setDeleted(true);
        account.setStatus(AccountStatus.CLOSED);
        account.setUpdatedAt(Instant.now());
        rocksDb.save("account:" + accountId, account);
    }

    @Override
    @CachePut(value = "accounts", key = "#depositRequest.accountId")
    public GetTransactionResponse deposit(DepositRequest depositRequest) {
        if (depositRequest.getAmount() <= 0) throw new BadRequestException("Amount must be positive");
        Account account = rocksDb.get("account:" + depositRequest.getAccountId(), Account.class);
        if (account == null || account.isDeleted()) throw new AccountNotFoundException("Account not found or deleted");
        if (!account.getCurrency().equals(depositRequest.getCurrency()))
            throw new BadRequestException("Currencies do not match");
        account.setBalance(account.getBalance() + depositRequest.getAmount());
        account.setUpdatedAt(Instant.now());
        rocksDb.save("account:" + depositRequest.getAccountId(), account);
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                null,
                account.getId(),
                depositRequest.getAmount(),
                depositRequest.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESS,
                depositRequest.getDescription(),
                Instant.now(),
                Instant.now()
        );
        rocksDb.save("transaction:" + transaction.getId(), transaction);
        return transactionMapper.toGetTransactionResponse(transaction);
    }

    @Override
    @CachePut(value = "accounts", key = "#withdrawRequest.accountId")
    public GetTransactionResponse withdraw(WithdrawRequest withdrawRequest) {
        if (withdrawRequest.getAmount() <= 0) throw new BadRequestException("Amount must be positive");
        Account account = rocksDb.get("account:" + withdrawRequest.getAccountId(), Account.class);
        if (account == null || account.isDeleted()) throw new AccountNotFoundException("Account not found or deleted");
        if (!account.getCurrency().equals(withdrawRequest.getCurrency()))
            throw new BadRequestException("Currencies do not match");
        if (account.getBalance() < withdrawRequest.getAmount())
            throw new AccountBalanceException("Insufficient balance");

        account.setBalance(account.getBalance() - withdrawRequest.getAmount());
        account.setUpdatedAt(Instant.now());
        rocksDb.save("account:" + withdrawRequest.getAccountId(), account);
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                account.getId(),
                null,
                withdrawRequest.getAmount(),
                withdrawRequest.getCurrency(),
                TransactionType.WITHDRAW,
                TransactionStatus.SUCCESS,
                withdrawRequest.getDescription(),
                Instant.now(),
                Instant.now()
        );
        rocksDb.save("transaction:" + transaction.getId(), transaction);
        return transactionMapper.toGetTransactionResponse(transaction);
    }

    @Override
    @Caching(
            put = {
                    @CachePut(value = "accounts", key = "#transferRequest.fromAccountId"),
                    @CachePut(value = "accounts", key = "#transferRequest.toAccountId")
            }
    )
    public String transfer(TransferRequest transferRequest) {
        if (transferRequest.getAmount() <= 0) throw new BadRequestException("Amount must be positive");
        if (transferRequest.getFromAccountId().equals(transferRequest.getToAccountId()))
            throw new BadRequestException("Sender and receiver must differ");
        Account sender = rocksDb.get("account:" + transferRequest.getFromAccountId(), Account.class);
        Account receiver = rocksDb.get("account:" + transferRequest.getToAccountId(), Account.class);

        if (sender == null || sender.isDeleted()) throw new AccountNotFoundException("Sender not found or deleted");
        if (receiver == null || receiver.isDeleted())
            throw new AccountNotFoundException("Receiver not found or deleted");
        if (!sender.getCurrency().equals(transferRequest.getCurrency()) || !receiver.getCurrency().equals(transferRequest.getCurrency()))
            throw new BadRequestException("Currencies do not match");
        if (sender.getBalance() < transferRequest.getAmount())
            throw new AccountBalanceException("Insufficient balance");

        sender.setBalance(sender.getBalance() - transferRequest.getAmount());
        receiver.setBalance(receiver.getBalance() + transferRequest.getAmount());
        sender.setUpdatedAt(Instant.now());
        receiver.setUpdatedAt(Instant.now());
        rocksDb.save("account:" + transferRequest.getFromAccountId(), sender);
        rocksDb.save("account:" + transferRequest.getToAccountId(), receiver);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                sender.getId(),
                receiver.getId(),
                transferRequest.getAmount(),
                transferRequest.getCurrency(),
                TransactionType.TRANSFER,
                TransactionStatus.SUCCESS,
                transferRequest.getDescription(),
                Instant.now(),
                Instant.now()
        );
        rocksDb.save("transaction:" + transaction.getId(), transaction);
        return "Transfer completed successfully";
    }
}
