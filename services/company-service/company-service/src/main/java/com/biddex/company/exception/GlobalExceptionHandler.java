package com.biddex.company.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CompanyNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(CompanyNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
	}

	@ExceptionHandler(UserProfileNotFoundException.class)
	public ResponseEntity<ApiError> handleProfileNotFound(UserProfileNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
	}

	@ExceptionHandler({UnauthorizedOperationException.class})
	public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedOperationException ex, HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request);
	}

	@ExceptionHandler(DuplicateRatingException.class)
	public ResponseEntity<ApiError> handleDuplicateRating(DuplicateRatingException ex, HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
		if (ex.getMessage() != null && ex.getMessage().contains("uk_rating_tender_direction")) {
			return build(HttpStatus.CONFLICT, "Conflict", "Rating already submitted for this tender and direction", request);
		}
		return build(HttpStatus.CONFLICT, "Conflict", "Data constraint violation", request);
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

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid path or query parameter: " + ex.getName(), request);
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
