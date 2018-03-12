package org.arx.util.jwt;

public class JWTDecodeException extends RuntimeException {
	private static final long serialVersionUID = -5693284083807274104L;

	/**
	 * Constructs a JWTDecodeException with no detail message.
	 */
	public JWTDecodeException() {
		super();
	}

	/**
	 * Constructs a JWTDecodeException with the specified detail message.
	 * @param message the detail message
	 */
	public JWTDecodeException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public JWTDecodeException(String message,Throwable cause) {
		super(message,cause);
	}
}
