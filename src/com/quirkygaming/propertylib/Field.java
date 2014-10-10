package com.quirkygaming.propertylib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A generic wrapper object that allows construction with an initial value, 
 * and allows subsequent value retrievals with the get() method.  A mutator
 * can be used by the owning class to provide a means to set the value.  
 * The MutableField class is an extension of this class that contains a set() method.
 * <p>
 * <b>Example implementation:</b>
 * <br> - Construction as a class field:
 * {@code
 * private Mutator mutator = new Mutator();
 * public final Field<Integer> integerField = Field.newField(mutator, 42);
 * }
 * <br> - Getting:
 * {@code
 * integerField.get(); // Returns current value of integerField
 * }
 * <br> - Setting internally with mutator:
 * {@code
 * mutator.set(integerField, 256); // Sets integerField to 256
 * }
 *
 * @author  CJ Griscom
 * @version 1.0
 */
public abstract class Field<T> {
	
	Mutator mutator = null;
	
	/**
	 * Constructs a new Field with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the Field as well as its type.
	 * @return The newly constructed Field
	 */
	public static <T> Field<T> newField(T initialValue) {
		return new FieldImpl<T>(initialValue);
	}
	
	/**
	 * Constructs a new Field with type T as specified by initialValue.
	 * 
	 * @param mutator An existing Mutator object, to which permission will be given to set this field.
	 * @param initialValue Provides the initial value of the Field as well as its type.
	 * @return The newly constructed Field
	 */
	public static <T> Field<T> newField(Mutator mutator, T initialValue) {
		Field<T> f = new FieldImpl<T>(initialValue);
		f.mutator = mutator;
		return f;
	}
	
	/**
	 * An extension of Field that returns a clone of the field contents upon calling of the get() method.
	 * Java's Cloneable interface must be properly implemented for this to work at all.
	 * Constructs a new clone-on-get with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the CloningField as well as its type.
	 * @return The newly constructed CloningField
	 */
	public static <T extends Cloneable> CloningField<T> newCloningField(T initialValue) {
		return new CloningField<T>(initialValue);
	}
	
	/**
	 * An extension of Field that returns a clone of the field contents upon calling of the get() method.
	 * Java's Cloneable interface must be properly implemented for this to work at all.
	 * Constructs a new clone-on-get Field with type T as specified by initialValue.
	 * 
	 * @param mutator An existing Mutator object, to which permission will be given to set this field
	 * or get the internal (non-clone) value.
	 * @param initialValue Provides the initial value of the CloningField as well as its type.
	 * @return The newly constructed CloningField
	 */
	public static <T extends Cloneable> CloningField<T> newCloningField(Mutator mutator, T initialValue) {
		CloningField<T> f = new CloningField<T>(initialValue);
		f.mutator = mutator;
		return f;
	}
	
	abstract void setInternal(T v);
	abstract T getInternal();
	
	/**
	 * Gets the current value.
	 * 
	 * @return The value
	 */
	public abstract T get();
	
	/**
	 * Checks if the contents of this field equals an object.
	 */
	@Override
	public boolean equals(Object other) {
		return get().equals(other);
	}
	
	/**
	 * Checks if the contents of this field equals the contents of another field.
	 */
	public boolean equals(Field<?> other) {
		return get().equals(other.get());
	}	
}

class FieldImpl<T> extends Field<T> {
	FieldImpl(T initialValue) {
		field = initialValue;
	}

	T field;
	
	void setInternal(T v) {
		field = v;
	}
	T getInternal() {
		return field;
	}
	
	public T get() {
		return getInternal();
	}
	
	@Override
	public String toString() {
		return field.toString();
	}
}

class CloningField<T extends Cloneable> extends FieldImpl<T> {
	
	/**
	 * Constructs a new CloningField with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the CloningField as well as its type.
	 * @return The newly constructed CloningField
	 */
	public static <T extends Cloneable> CloningField<T> newCloningField(T initialValue) {
		return new CloningField<T>(initialValue);
	}
	
	private Method cloneMethod;
	
	CloningField(T initialValue) {
		super(initialValue);
		try {
			cloneMethod = initialValue.getClass().getMethod("clone");
			if (!cloneMethod.isAccessible()) cloneMethod.setAccessible(true);
		} catch (SecurityException e) {
			checkException(e, true);
		} catch (NoSuchMethodException e) {
			checkException(e, true);
		}
	}
	
	/**
	 * Gets a clone of the current value.
	 * 
	 * @return The cloned value
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		try {
			T result = (T) cloneMethod.invoke(field);
			if (result == null) throw new NullPointerException("clone() method returned null");
			return result;
		} catch (IllegalArgumentException e) {
			checkException(e, false);
		} catch (IllegalAccessException e) {
			checkException(e, true);
		} catch (InvocationTargetException e) {
			boolean implError = false;
			Throwable e2 = e.getCause(); // clone() returned an error, get cause.
			if (e2 instanceof CloneNotSupportedException) implError = true;
			checkException(e, implError);
		} catch (ClassCastException e) {
			checkException(e, false);
		} catch (NullPointerException e) {
			checkException(e, false);
		}
		
		return null;
	}
	
	private void checkException(Throwable e, boolean implError) {
		if (implError) {
			throw new RuntimeException("Error in implementation of Cloneable", e);
		} else {
			throw new RuntimeException("Unexpected exception encountered during cloning process", e);
		}
	}

}
