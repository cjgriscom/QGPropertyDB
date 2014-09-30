package com.quirkygaming.propertylib;

public class MutableField<T> extends Field<T> {
	private final Field<T> field;
	
	public static <T> MutableField<T> newField(T initialValue) {
		return new MutableField<T>(new FieldImpl<T>(initialValue));
	}
	
	MutableField(Field<T> field) {
		this.field = field;
	}
	
	@Override
	public void set(T v) {
		field.set(v);
	}

	@Override
	public T get() {
		return field.get();
	}
	
	public Field<T> getImmutable() {
		return field;
	}
}
