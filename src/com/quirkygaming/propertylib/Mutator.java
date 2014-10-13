package com.quirkygaming.propertylib;

/**
 * A class used for the purpose of internally mutating Property objects.
 *
 * @author  Chandler Griscom
 * @version 1.0
 */
public class Mutator {
	
	/**
	 * No-arg constructor
	 */
	public Mutator() {
		
	}
	
	/**
	 * Use this method to get the value of a Property -- mainly intended for the purpose of
	 * getting the internal value of a clone-on-get Property.  The Property MUST have been constructed 
	 * with a reference to this mutator, otherwise the mutator will throw a runtime exception.
	 * 
	 * @param property The Property to be accessed
	 * @return The Property's internal value
	 */
	public <T> T get(Property<T> property) {
		if (property instanceof MutableProperty || property.mutator == this) {
			
			return property.getInternal();
		} else {
			throw new RuntimeException("Caller attempted to illegally get internal property with mutator");
		}
	}
	
	/**
	 * Use this method to set the value of an immutable Property.  The Property MUST have been constructed 
	 * with a reference to this mutator, otherwise the mutator will throw a runtime exception.
	 * 
	 * @param property The Property to be modified
	 * @param value The new value
	 * @return The new value
	 */
	public <T> T set(Property<T> property, T value) {
		if (property instanceof MutableProperty || property.mutator == this) {
			property.setInternal(value);
			return value;
		} else {
			throw new RuntimeException("Caller attempted to illegally set property with mutator");
		}
	}
	
}
