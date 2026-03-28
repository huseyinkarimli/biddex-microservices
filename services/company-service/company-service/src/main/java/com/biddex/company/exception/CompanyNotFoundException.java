package com.biddex.company.exception;

public class CompanyNotFoundException extends RuntimeException {

	public CompanyNotFoundException(String message) {
		super(message);
	}
}
