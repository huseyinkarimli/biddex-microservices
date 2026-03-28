package com.biddex.company.exception;

public class UnauthorizedOperationException extends RuntimeException {

	public UnauthorizedOperationException(String message) {
		super(message);
	}
}
