package com.quirkygaming.propertylib;

public class EnumPropertyMap<E extends Enum<E>, T> extends ProtectedEnumPropertyMap<E, T> {
	
	public EnumPropertyMap(Class<E> enumClass) {
		super(enumClass);
	}
	public EnumPropertyMap(Class<E> mainEnumClass, Class<?>[] otherEnums) {
		super(mainEnumClass, otherEnums);
	}
	
	public int size() {
		return super.size();
	}
	

	public T get(Enum<?> enumKey) {
		return super.get(enumKey);
	}
	
	public T set(Enum<?> enumKey, T value) {
		return super.set(enumKey, value);
	}
	
	public void clear() {
		super.clear();
	}
	
	public class Mutator {
		
	}
	
}