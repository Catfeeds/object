package com.zw.p2p.repay.exception;

public class RepayException extends Exception {
	public RepayException(String msg) {
		super(msg);
	}

	public RepayException(String msg, Throwable e) {
		super(msg, e);
	}
}
