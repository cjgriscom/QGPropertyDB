package com.quirkygaming.propertydb;

/**
 * Thrown when someone attempts to use an invalid InitializationToken or re-initialize the database if it's already in use
 * @author chandler
 *
 */
public class IllegalInitializationException extends RuntimeException {

	private static final long serialVersionUID = -1783570975073317879L;
	public IllegalInitializationException(String msg) {
		super(msg);
	}
}
