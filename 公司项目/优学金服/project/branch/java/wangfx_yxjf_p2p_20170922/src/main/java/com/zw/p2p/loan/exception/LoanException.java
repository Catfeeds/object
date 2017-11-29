package com.zw.p2p.loan.exception;

/**
 * Created by lijin on 15/2/1.
 */
public class LoanException extends Exception{
    public LoanException(String message) {
        super(message);
    }

    public LoanException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanException() {
    }
}
