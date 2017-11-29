package com.zw.p2p.invest.exception;

/**
 * Created by lijin on 15/2/1.
 */
public class InvestException extends Exception{
    public InvestException(String message) {
        super(message);
    }

    public InvestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvestException() {
    }
}
