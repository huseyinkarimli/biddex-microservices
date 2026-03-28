package com.biddex.auth;

public class RefreshTokenMismatchException extends RuntimeException {

	public RefreshTokenMismatchException(String message) {
		super(message);
	}
}
