package com.quirkygaming.propertydb;

/**
 * This class is used for security purposes by the API. The initializer of the database will be passed
 * an InitializationToken which can be used to force saves and stop the database. Possessing a token
 * effectively grants administrative access to its owner. 
 * @author chandler
 *
 */
public final class InitializationToken {
	
	InitializationToken() {}
	/**
	 * Checks if this token can still control the database
	 * @return True if valid
	 */
	public boolean valid() {
		return PropertyDB.tokenIsValid(this);
	}
}
