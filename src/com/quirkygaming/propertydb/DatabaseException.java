package com.quirkygaming.propertydb;

/**
 * Thrown when errors occur related to loading or saving properties. 
 * @author chandler
 *
 */
public class DatabaseException extends Exception {

	private static final long serialVersionUID = -6523214340878538617L;
	public DatabaseException(String msg) {
		super(msg);
	}
	
}
