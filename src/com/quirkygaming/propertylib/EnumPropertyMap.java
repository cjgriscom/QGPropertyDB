package com.quirkygaming.propertylib;

public class EnumPropertyMap<E extends Enum<E>, T> extends ProtectedEnumPropertyMap<E, T> {
	
	public EnumPropertyMap(Class<E> enumClass) {
		super(enumClass);
	}
	public EnumPropertyMap(Class<E> mainEnumClass, Class<?>[] otherEnums) {
		super(mainEnumClass, otherEnums);
	}
	
	public <N extends Enum<N>> Field<T> getField(N property) {
		return super.getField(property);
	}
	public <X, N extends Enum<N>> Field<X> getWeaklyLinkedField(N property, Class<X> type) {
		return super.getWeaklyLinkedField(property, type);
	}
	
	public <N extends Enum<N>> MutableField<T> getMutableField(N property) {
		return super.getMutableField(property);
	}
	public <X, N extends Enum<N>> MutableField<X> getWeaklyLinkedMutableField(N property, Class<X> type) {
		return super.getWeaklyLinkedMutableField(property, type);
	}
	
	public int size() {
		return super.size();
	}
	

	public T get(Enum<?> enumKey) {
		return super.get(enumKey);
	}
	public <X> X getAsType(Enum<?> enumKey) {
		return super.getAsType(enumKey);
	}
	
	public T set(Enum<?> enumKey, T value) {
		return super.set(enumKey, value);
	}
	public <X> X setAsType(Enum<?> enumKey, X value) {
		return super.setAsType(enumKey, value);
	}
	
	public void clear() {
		super.clear();
	}
	
	public class Mutator {
		
	}
	
}