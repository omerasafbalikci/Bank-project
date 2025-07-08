package org.bank.account.utilities.exceptions;

public class RocksDbException extends RuntimeException {
    public RocksDbException(String message) {
        super(message);
    }
}
