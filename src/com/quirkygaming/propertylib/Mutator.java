package com.quirkygaming.propertylib;

/**
 * A class used for the purpose of internally mutating Field objects.
 *
 * @author  CJ Griscom
 * @version 1.0
 */
public class Mutator {
	
	//private Set<Field<?>> fieldSet = new HashSet<Field<?>>();
	
	/**
	 * No-arg constructor
	 */
	public Mutator() {
		
	}
	
	/**
	 * Use this method to get the value of a field -- mainly intended for the purpose of
	 * getting the internal value of a cloning Field.  The field MUST have been constructed 
	 * with a reference to this mutator, otherwise the mutator will throw a runtime exception.
	 * 
	 * @param field The field to be accessed
	 * @return The field's internal value
	 */
	public <T> T get(Field<T> field) {
		if (field instanceof MutableField || field.mutator == this) {
			
			return field.getInternal();
		} else {
			throw new RuntimeException("Caller attempted to illegally get internal field with mutator");
		}
	}
	
	/**
	 * Use this method to set the value of an immutable field.  The field MUST have been constructed 
	 * with a reference to this mutator, otherwise the mutator will throw a runtime exception.
	 * 
	 * @param field The field to be modified
	 * @param value The new value
	 * @return The new value
	 */
	public <T> T set(Field<T> field, T value) {
		if (field instanceof MutableField || field.mutator == this) {
			field.setInternal(value);
			return value;
		} else {
			throw new RuntimeException("Caller attempted to illegally set field with mutator");
		}
	}
	
}
