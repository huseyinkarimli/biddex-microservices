package com.biddex.company.exception;

public class UserProfileNotFoundException extends RuntimeException {

	public UserProfileNotFoundException(String message) {
		super(message);
	}
}
