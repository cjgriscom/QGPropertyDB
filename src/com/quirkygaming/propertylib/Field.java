package com.quirkygaming.propertylib;

public abstract class Field<T> {
	public static <T> Field<T> newField(T initialValue) {
		return new FieldImpl<T>(initialValue);
	}
	
	public static <T> Field<T> newField(Mutator m, T initialValue) {
		Field<T> f = new FieldImpl<T>(initialValue);
		m.internalAddPermission(f);
		return f;
	}
	
	abstract void set(T v);
	
	public abstract T get();
	
	@Override
	public boolean equals(Object other) {
		return get().equals(other);
	}
	
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

class MappedFieldImpl<T> extends Field<T> {

	private final int index;
	private ProtectedEnumPropertyMap<?, T> map;
	
	<E extends Enum<E>> MappedFieldImpl(ProtectedEnumPropertyMap<?, T> map, E property) {
		this.map = map;
		this.index = map.getIndex(property);
	}

	@Override
	void set(T v) {
		map.set(index, v);
	}

	@Override
	public T get() {
		return map.get(index);
	}
	
}

class WeakMappedFieldImpl<T> extends Field<T> {

	private final int index;
	private ProtectedEnumPropertyMap<?, ?> map;
	
	<E extends Enum<E>, X extends Object> WeakMappedFieldImpl(ProtectedEnumPropertyMap<?, X> map, E property) {
		this.map = map;
		this.index = map.getIndex(property);
	}

	@Override
	void set(T v) {
		map.set(index, v);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		return (T) map.get(index);
	}
	
}