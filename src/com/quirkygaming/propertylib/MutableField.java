package com.quirkygaming.propertylib;

/**
 * An extension of Field<T> with the addition of a set method.
 *
 * @author  CJ Griscom
 * @version 1.0
 */
public class MutableField<T> extends Field<T> {
	private final Field<T> field;
	
	/**
	 * Constructs a new MutableField with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the MutableField as well as its type.
	 * @return The newly constructed MutableField
	 */
	public static <T> MutableField<T> newField(T initialValue) {
		return new MutableField<T>(new FieldImpl<T>(initialValue));
	}
	
	/**
	 * Constructs a new clone-on-get MutableField with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the MutableField as well as its type.
	 * @return The newly constructed MutableField
	 */
	public static <T extends Cloneable> MutableField<T> newClonableField(T initialValue) {
		return new MutableField<T>(new CloningField<T>(initialValue));
	}
	
	MutableField(Field<T> field) {
		this.field = field;
	}
	
	/**
	 * Sets the value of this MutableField
	 */
	public void set(T v) {
		field.setInternal(v);
	}
	
	/**
	 * Gets the current value.
	 * 
	 * @return The value
	 */
	@Override
	public T get() {
		return field.get();
	}
	
	/**
	 * Returns an immutable (Field) version of this MutableField.
	 * 
	 * @return The immutable version
	 */
	public Field<T> getImmutable() {
		return field;
	}

	@Override
	void setInternal(T v) {
		field.setInternal(v);
	}

	@Override
	T getInternal() {
		return field.getInternal();
	}
	
	@Override
	public String toString() {
		return field.toString();
	}
}
