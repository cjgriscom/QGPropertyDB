package com.quirkygaming.propertylib;

import java.io.Serializable;

import com.quirkygaming.propertylib.PropertyObserver.EventType;

/**
 * An extension of Property<T> with the addition of a set method.
 *
 * @author  Chandler Griscom
 * @version 1.0
 */
public class MutableProperty<T> extends BoundProperty<T> implements Serializable {
	
	private static final long serialVersionUID = -6764130456697090580L;

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
		super(property);
	}
	
	/**
	 * Sets the value of this MutableProperty
	 */
	public void set(T v) {
		signal(EventType.SET);
		super.setInternal(v);
	}
	
	/**
	 * Returns an immutable (Property) version of this MutableProperty.
	 * 
	 * @return The immutable version
	 */
	public Property<T> getImmutable() {
		return super.getInternalProperty();
	}
	
	/**
	 * Construct (if necessary) and return a mutator for this Property
	 * @return The Mutator
	 */
	public Mutator getMutator() {
		if (mutator == null) {
			mutator = new Mutator();
		}
		return mutator;
	}
}
