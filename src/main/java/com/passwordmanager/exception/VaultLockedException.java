package com.passwordmanager.exception;

public class VaultLockedException extends RuntimeException {
    public VaultLockedException(String message) {
        super(message);
    }
}
