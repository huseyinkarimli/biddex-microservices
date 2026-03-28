package com.biddex.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleUserExists(UserAlreadyExistsException ex, HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
	}

	@ExceptionHandler(VoenAlreadyRegisteredException.class)
	public ResponseEntity<ApiError> handleVoenRegistered(VoenAlreadyRegisteredException ex, HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
	}

	@ExceptionHandler({InvalidTokenException.class, TokenExpiredException.class, RefreshTokenMismatchException.class})
	public ResponseEntity<ApiError> handleToken(RuntimeException ex, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
	}

	@ExceptionHandler(InvalidVoenException.class)
	public ResponseEntity<ApiError> handleInvalidVoen(InvalidVoenException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return build(HttpStatus.BAD_REQUEST, "Bad Request", msg, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ApiError> handleUserNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error", request);
	}

	private static ResponseEntity<ApiError> build(HttpStatus status, String error, String message, HttpServletRequest request) {
		ApiError body = ApiError.builder()
				.timestamp(Instant.now())
				.status(status.value())
				.error(error)
				.message(message)
				.path(request.getRequestURI())
				.build();
		return ResponseEntity.status(status).body(body);
	}
}
