package com.quirkygaming.propertylib;

public abstract class Field<T> {
	protected Field(T initialValue) {}
	
	public static <T> Field<T> newField(T initialValue) {
		return new FieldImpl<T>(initialValue);
	}
	
	public static <T, E extends Enum<E>> Field<T> newField(ProtectedEnumPropertyMap<?, T> map, E property) {
		return new MappedFieldImpl<T>(map, property);
	}
	
	public static <T, E extends Enum<E>> Field<T> newWeaklyLinkedField(ProtectedEnumPropertyMap<?, ?> map, E property, Class<T> type) {
		return new WeakMappedFieldImpl<T>(map, property);
	}
	
	protected abstract void set(T v);
	
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
	protected FieldImpl(T initialValue) {
		super(initialValue);
		field = initialValue;
	}

	private T field;
	
	protected void set(T v) {
		field = v;
	}
	
	public T get() {
		return field;
	}
}

class MappedFieldImpl<T> extends Field<T> {

	private final int index;
	private ProtectedEnumPropertyMap<?, T> map;
	
	protected <E extends Enum<E>> MappedFieldImpl(ProtectedEnumPropertyMap<?, T> map, E property) {
		super(null);
		this.map = map;
		this.index = map.getIndex(property);
	}

	@Override
	protected void set(T v) {
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
	
	protected <E extends Enum<E>, X extends Object> WeakMappedFieldImpl(ProtectedEnumPropertyMap<?, X> map, E property) {
		super(null);
		this.map = map;
		this.index = map.getIndex(property);
	}

	@Override
	protected void set(T v) {
		map.set(index, v);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		return (T) map.get(index);
	}
	
}