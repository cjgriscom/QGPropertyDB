package com.quirkygaming.propertydb;

public final class InitializationToken {
	
	InitializationToken() {}
	
	public boolean valid() {
		return PropertyDB.tokenIsValid(this);
	}
}
