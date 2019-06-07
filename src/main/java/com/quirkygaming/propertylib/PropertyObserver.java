package com.quirkygaming.propertylib;

/**
 * An interface that supplies a method used for listening to changes to a Property.
 *
 * @author  Chandler Griscom
 * @version 1.0
 */
public interface PropertyObserver<T> {
	/**
	 * Event types
	 * @author chandler
	 */
	public static enum EventType {
		GET,
		SET,
		UPDATE,
	}
	
	/**
	 * When an event occurs, this method will be called in registered observers.
	 * 
	 * @param modifiedProperty The property that triggered the event
	 * @param type The type of event that occured
	 */
	public void onChange(Property<T> modifiedProperty, EventType type);
}
