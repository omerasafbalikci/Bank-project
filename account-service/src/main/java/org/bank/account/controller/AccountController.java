package org.bank.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bank.account.dto.requests.*;
import org.bank.account.dto.responses.GetAccountResponse;
import org.bank.account.dto.responses.GetTransactionResponse;
import org.bank.account.dto.responses.PagedResponse;
import org.bank.account.service.abstracts.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
@Log4j2
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/id/{id}")
    public ResponseEntity<GetAccountResponse> getAccountById(@PathVariable String id) {
        log.trace("Entering getAccountById method in AccountController class");
        log.info("Fetching account by id: {}", id);
        GetAccountResponse response = this.accountService.getAccountById(id);
        log.info("Account with id {} retrieved successfully", id);
        log.trace("Exiting getAccountById method in AccountController class");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filtered-and-sorted")
    public ResponseEntity<PagedResponse<GetAccountResponse>> getAccountsFilteredSortedPaged(
            @RequestBody(required = false) AccountFilterCriteria criteria,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.trace("Entering getAccountsFilteredSortedPaged method in AccountController class");
        log.info("Fetching accounts with filters - page: {}, size: {}, sortField: {}, sortDirection: {}",
                page, size, sortField, sortDirection);
        PagedResponse<GetAccountResponse> response = this.accountService.getAccountsFilteredSortedPaged(criteria, sortField, sortDirection, page, size);
        log.info("Users filtered and sorted fetched successfully");
        log.trace("Exiting getAccountsFilteredSortedPaged method in AccountController class");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GetAccountResponse> createAccount(@RequestBody @Valid CreateAccountRequest createAccountRequest) {
        log.trace("Entering createAccount method in AccountController class");
        log.info("Creating a new account with details: {}", createAccountRequest);
        GetAccountResponse response = this.accountService.createAccount(createAccountRequest);
        log.info("Account created successfully");
        log.trace("Exiting createAccount method in AccountController class");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable String id) {
        log.trace("Entering deleteAccount method in AccountController class");
        log.info("Deleting account with id: {}", id);
        this.accountService.deleteAccount(id);
        log.info("Account with id {} deleted successfully", id);
        log.trace("Exiting deleteAccount method in AccountController class");
        return ResponseEntity.ok("Account has been successfully deleted.");
    }

    @PostMapping("/deposit")
    public ResponseEntity<GetTransactionResponse> deposit(@RequestBody @Valid DepositRequest depositRequest) {
        log.trace("Entering deposit method in AccountController class");
        log.info("Processing deposit request: {}", depositRequest);
        GetTransactionResponse response = this.accountService.deposit(depositRequest);
        log.info("Deposit completed successfully for account: {}", depositRequest.getAccountId());
        log.trace("Exiting deposit method in AccountController class");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<GetTransactionResponse> withdraw(@RequestBody @Valid WithdrawRequest withdrawRequest) {
        log.trace("Entering withdraw method in AccountController class");
        log.info("Processing withdraw request: {}", withdrawRequest);
        GetTransactionResponse response = this.accountService.withdraw(withdrawRequest);
        log.info("Withdraw completed successfully for account: {}", withdrawRequest.getAccountId());
        log.trace("Exiting withdraw method in AccountController class");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody @Valid TransferRequest transferRequest) {
        log.trace("Entering transfer method in AccountController class");
        log.info("Processing transfer request from {} to {}, amount: {}",
                transferRequest.getFromAccountId(),
                transferRequest.getToAccountId(),
                transferRequest.getAmount());
        String result = this.accountService.transfer(transferRequest);
        log.info("Transfer completed successfully");
        log.trace("Exiting transfer method in AccountController class");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
