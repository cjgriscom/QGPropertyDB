package com.quirkygaming.propertylib;

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
	 * @param m An existing Mutator object, to which permission will be given to set this field.
	 * @param initialValue Provides the initial value of the Field as well as its type.
	 * @return The newly constructed Field
	 */
	public static <T> Field<T> newField(Mutator mutator, T initialValue) {
		Field<T> f = new FieldImpl<T>(initialValue);
		f.mutator = mutator;
		return f;
	}
	
	abstract void set(T v);
	
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

	private T field;
	
	void set(T v) {
		field = v;
	}
	
	public T get() {
		return field;
	}
}