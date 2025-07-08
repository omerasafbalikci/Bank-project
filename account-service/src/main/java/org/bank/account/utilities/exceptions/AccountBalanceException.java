package org.bank.account.utilities.exceptions;

public class AccountBalanceException extends RuntimeException {
    public AccountBalanceException(String message) {
        super(message);
    }
}
