package org.arx.util.jwt;

/**
 * A runtime exception that is thrown if a JWT token has expired.
 */
public class TokenExpiredException extends RuntimeException {
	private static final long serialVersionUID = 529099543388296925L;

	/**
	 * Constructs a TokenExpiredException with no detail message.
	 */
	public TokenExpiredException() {
		super();
	}

	/**
	 * Constructs a TokenExpiredException with the specified detail message.
	 * @param message the detail message
	 */
	public TokenExpiredException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public TokenExpiredException(String message,Throwable cause) {
		super(message,cause);
	}
}
