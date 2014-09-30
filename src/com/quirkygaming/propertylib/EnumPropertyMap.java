package com.quirkygaming.propertylib;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class EnumPropertyMap<E extends Enum<E>, T> {
	
	private static final HashMap<Class<?>, Enum<?>[]> enumCache = new HashMap<Class<?>, Enum<?>[]>();
	
	private HashMap<Class<?>, Integer> positions = null;
	private boolean multiEnums = false;
	
	private final Class<E> mainEnumClass;
	
	private final int size;
	private final Object[] items;
	
	public EnumPropertyMap(Class<E> enumClass) {
		size = getEnumValues(enumClass).length;
		items = new Object[size];
		mainEnumClass = enumClass;
	}
	
	public EnumPropertyMap(Class<E> mainEnumClass, Class<E>... otherEnums) {
		int currentPos = getEnumValues(mainEnumClass).length;
		
		multiEnums = true;
		positions = new HashMap<Class<?>, Integer>();

		for (Class<?> ec : otherEnums) {
			positions.put(ec, currentPos);
			currentPos += getEnumValues(ec).length;
		}
		
		size = currentPos;
		items = new Object[size];
		this.mainEnumClass = mainEnumClass;
	}
	
	private Enum<?>[] getEnumValues(Class<?> enumClass) {
		try {
			if (!enumCache.containsKey(enumClass)) {
				Enum<?>[] vals = (Enum<?>[]) enumClass.getMethod("values").invoke(null);
				enumCache.put(enumClass, vals);
				return vals;
			} else {
				return enumCache.get(enumClass);
			}
		} catch (IllegalArgumentException e) {
			throw getEx(e, enumClass.getName());
		} catch (SecurityException e) {
			throw getEx(e, enumClass.getName());
		} catch (IllegalAccessException e) {
			throw getEx(e, enumClass.getName());
		} catch (InvocationTargetException e) {
			throw getEx(e, enumClass.getName());
		} catch (NoSuchMethodException e) {
			throw getEx(e, enumClass.getName());
		} catch (ClassCastException e) {
			throw getEx(e, enumClass.getName());
		}
	}
	private RuntimeException getEx(Exception e, String name) {
		return new RuntimeException("Runtime error while processing enum " + name + " .", e);
	}

	public int size() {
		return size;
	}
	
	int getIndex(E enumKey) {
		if (multiEnums) {
			Integer position = positions.get(enumKey.getClass());
			if (position == null) return -1;
			return positions.get(enumKey.getClass()) + enumKey.ordinal();
		}
		if (mainEnumClass != enumKey.getClass()) return -1;
		return enumKey.ordinal();
	}

	public T get(E enumKey) {
		return get(getIndex(enumKey));
	}
	@SuppressWarnings("unchecked")
	T get(int index) {
		if (index == -1) return null;
		return (T) items[index];
	}
	
	protected T set(E enumKey, T value) {
		return set(getIndex(enumKey), value);
	}
	protected T set(int index, T value) {
		if (index == -1) return null;
		items[index] = value;
		return value;
	}
	
	public void clear() {
		for (int i = 0; i < size; i++) {
			items[i] = null;
		}
	}
	
	public class Mutator {
		
	}
	
}