package com.quirkygaming.propertylib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A generic wrapper object that allows construction with an initial value, 
 * and allows subsequent value retrievals with the get() method.  A mutator
 * can be used by the owning class to provide a means to set the value.  
 * The MutableProperty class is an extension of this class that contains a set() method.
 * <p>
 * <b>Example implementation:</b>
 * <br> - Construction as a class property:
 * {@code
 * private Mutator mutator = new Mutator();
 * public final Property<Integer> integerProperty = Property.newProperty(mutator, 42);
 * }
 * <br> - Getting:
 * {@code
 * integerProperty.get(); // Returns current value of integerProperty
 * }
 * <br> - Setting internally with mutator:
 * {@code
 * mutator.set(integerProperty, 256); // Sets integerProperty to 256
 * }
 *
 * @author  Chandler Griscom
 * @version 1.0
 */
public abstract class Property<T> {
	
	Mutator mutator = null;
	
	/**
	 * Constructs a new Property with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the Property as well as its type.
	 * @return The newly constructed Property
	 */
	public static <T> Property<T> newProperty(T initialValue) {
		return new PropertyImpl<T>(initialValue);
	}
	
	/**
	 * Constructs a new Property with type T as specified by initialValue.
	 * 
	 * @param mutator An existing Mutator object, to which permission will be given to set this property.
	 * @param initialValue Provides the initial value of the Property as well as its type.
	 * @return The newly constructed Property
	 */
	public static <T> Property<T> newProperty(Mutator mutator, T initialValue) {
		Property<T> f = new PropertyImpl<T>(initialValue);
		f.mutator = mutator;
		return f;
	}
	
	/**
	 * An extension of Property that returns a clone of the property contents upon calling of the get() method.
	 * Java's Cloneable interface must be properly implemented for this to work at all.
	 * Constructs a new clone-on-get with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the Property as well as its type.
	 * @return The newly constructed Property
	 */
	public static <T extends Cloneable> Property<T> newCloningProperty(T initialValue) {
		return new CloningProperty<T>(initialValue);
	}
	
	/**
	 * An extension of Property that returns a clone of the property contents upon calling of the get() method.
	 * Java's Cloneable interface must be properly implemented for this to work at all.
	 * Constructs a new clone-on-get Property with type T as specified by initialValue.
	 * 
	 * @param mutator An existing Mutator object, to which permission will be given to set this property
	 * or get the internal (non-clone) value.
	 * @param initialValue Provides the initial value of the Property as well as its type.
	 * @return The newly constructed Property
	 */
	public static <T extends Cloneable> Property<T> newCloningProperty(Mutator mutator, T initialValue) {
		Property<T> f = newCloningProperty(initialValue);
		f.mutator = mutator;
		return f;
	}
	
	/**
	 * An extension of Property that returns a Property whose value is bound to another one.
	 * 
	 * @param mutator An existing Mutator object, to which permission will be given to set this property
	 * or get the internal (non-clone) value.
	 * @param initialValue Provides the initial value of the Property as well as its type.
	 * @return The newly constructed Property
	 */
	public static <T extends Cloneable> Property<T> newBoundProperty(Mutator mutator, Property<T> otherProperty) {
		BoundProperty<T> f = new BoundProperty<T>(otherProperty);
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
	 * Checks if the contents of this property equals an object.
	 */
	@Override
	public boolean equals(Object other) {
		return get().equals(other);
	}
	
	/**
	 * Checks if the contents of this property equals the contents of another property.
	 */
	public boolean equals(Property<?> other) {
		return get().equals(other.get());
	}	
}

class PropertyImpl<T> extends Property<T> {
	PropertyImpl(T initialValue) {
		property = initialValue;
	}

	T property;
	
	void setInternal(T v) {
		property = v;
	}
	T getInternal() {
		return property;
	}
	
	public T get() {
		return getInternal();
	}
	
	@Override
	public String toString() {
		return property.toString();
	}
}

class CloningProperty<T extends Cloneable> extends PropertyImpl<T> {
	
	/**
	 * Constructs a new CloningProperty with type T as specified by initialValue.
	 * 
	 * @param initialValue Provides the initial value of the CloningProperty as well as its type.
	 * @return The newly constructed CloningProperty
	 */
	public static <T extends Cloneable> CloningProperty<T> newCloningProperty(T initialValue) {
		return new CloningProperty<T>(initialValue);
	}
	
	private Method cloneMethod;
	
	CloningProperty(T initialValue) {
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
			T result = (T) cloneMethod.invoke(property);
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

class BoundProperty<T> extends Property<T> {
	private final Property<T> property;
	
	BoundProperty(Property<T> property) {
		this.property = property;
	}
	
	@Override
	public T get() {
		return property.get();
	}
	
	Property<T> getInternalProperty() {
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

