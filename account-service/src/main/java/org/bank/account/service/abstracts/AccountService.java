package org.bank.account.service.abstracts;

import org.bank.account.dto.requests.*;
import org.bank.account.dto.responses.GetAccountResponse;
import org.bank.account.dto.responses.GetTransactionResponse;
import org.bank.account.dto.responses.PagedResponse;

public interface AccountService {
    GetAccountResponse getAccountById(String accountId);

    PagedResponse<GetAccountResponse> getAccountsFilteredSortedPaged(AccountFilterCriteria criteria, String sortField, String sortDirection, int page, int size);

    GetAccountResponse createAccount(CreateAccountRequest createAccountRequest);

    void deleteAccount(String accountId);

    GetTransactionResponse deposit(DepositRequest depositRequest);

    GetTransactionResponse withdraw(WithdrawRequest withdrawRequest);

    String transfer(TransferRequest transferRequest);
}
