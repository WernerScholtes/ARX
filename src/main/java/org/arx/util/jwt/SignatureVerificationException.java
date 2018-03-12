package org.arx.util.jwt;

/**
 * A runtime exception that is thrown if the signature or a JWT token is invalid.
 */
public class SignatureVerificationException extends RuntimeException {
	private static final long serialVersionUID = -4866920812814332361L;

	/**
	 * Constructs a SignatureVerificationException with no detail message.
	 */
	public SignatureVerificationException() {
		super();
	}

	/**
	 * Constructs a SignatureVerificationException with the specified detail message.
	 * @param message the detail message
	 */
	public SignatureVerificationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public SignatureVerificationException(String message,Throwable cause) {
		super(message,cause);
	}
}
