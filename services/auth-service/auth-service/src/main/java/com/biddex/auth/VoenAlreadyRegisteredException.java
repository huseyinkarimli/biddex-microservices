package com.biddex.auth;

public class VoenAlreadyRegisteredException extends RuntimeException {

	public VoenAlreadyRegisteredException(String message) {
		super(message);
	}
}
