package com.quirkygaming.propertylib;

/**
 * An extension of Property<T> with the addition of a set method.
 *
 * @author  CJ Griscom
 * @version 1.0
 */
public class MutableProperty<T> extends Property<T> {
	private final Property<T> property;
	
	/**
	 * Constructs a new MutableProperty with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the MutableProperty as well as its type.
	 * @return The newly constructed MutableProperty
	 */
	public static <T> MutableProperty<T> newProperty(T initialValue) {
		return new MutableProperty<T>(new PropertyImpl<T>(initialValue));
	}
	
	/**
	 * Constructs a new clone-on-get MutableProperty with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the MutableProperty as well as its type.
	 * @return The newly constructed MutableProperty
	 */
	public static <T extends Cloneable> MutableProperty<T> newClonableProperty(T initialValue) {
		return new MutableProperty<T>(new CloningProperty<T>(initialValue));
	}
	
	MutableProperty(Property<T> property) {
		this.property = property;
	}
	
	/**
	 * Sets the value of this MutableProperty
	 */
	public void set(T v) {
		property.setInternal(v);
	}
	
	/**
	 * Gets the current value.
	 * 
	 * @return The value
	 */
	@Override
	public T get() {
		return property.get();
	}
	
	/**
	 * Returns an immutable (Property) version of this MutableProperty.
	 * 
	 * @return The immutable version
	 */
	public Property<T> getImmutable() {
		return property;
	}

	@Override
	void setInternal(T v) {
		property.setInternal(v);
	}

	@Override
	T getInternal() {
		return property.getInternal();
	}
	
	@Override
	public String toString() {
		return property.toString();
	}
}
