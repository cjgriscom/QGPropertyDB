package com.quirkygaming.propertylib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ProtectedEnumPropertyMap<E extends Enum<E>, T> {
	
	private static final HashMap<Class<?>, Enum<?>[]> enumCache = new HashMap<Class<?>, Enum<?>[]>();
	
	private HashMap<Class<?>, Integer> positions = null;
	private boolean multiEnums = false;
	
	private final Class<E> mainEnumClass;
	
	private final int size;
	private final Object[] items;
	
	protected ProtectedEnumPropertyMap(Class<E> enumClass) {
		size = getEnumValues(enumClass).length;
		items = new Object[size];
		mainEnumClass = enumClass;
	}
	
	protected ProtectedEnumPropertyMap(Class<E> mainEnumClass, Class<?>... otherEnums) {
		int currentPos = getEnumValues(mainEnumClass).length;
		
		multiEnums = true;
		positions = new HashMap<Class<?>, Integer>();

		for (Class<?> ec : otherEnums) {
			if (!ec.isEnum()) throw new RuntimeException(ec.getName() + " is not an enum");
			positions.put(ec, currentPos);
			currentPos += getEnumValues(ec).length;
		}
		
		size = currentPos;
		items = new Object[size];
		this.mainEnumClass = mainEnumClass;
	}
	
	private Enum<?>[] getEnumValues(Class<?> enumClass) {
		
		if (!enumCache.containsKey(enumClass)) {
			Method m;
			try {
				m=enumClass.getMethod("values");
				if (!m.isAccessible()) m.setAccessible(true);
				Enum<?>[] vals = (Enum<?>[]) m.invoke(null);
				enumCache.put(enumClass, vals);
				return vals;
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
		} else {
			return enumCache.get(enumClass);
		}
		
	}
	private RuntimeException getEx(Exception e, String name) {
		return new RuntimeException("Runtime error while processing enum " + name, e);
	}
	private RuntimeException getCastMismatch(Exception e) {
		return new RuntimeException("Internal casting mismatch in EnumPropertyMap; check implementation consistency", e);
	}

	protected int size() {
		return size;
	}
	
	int getIndex(Enum<?> enumKey) {
		if (mainEnumClass == enumKey.getClass()) return enumKey.ordinal();
		if (multiEnums) {
			Integer position = positions.get(enumKey.getClass());
			if (position == null) return -1;
			return positions.get(enumKey.getClass()) + enumKey.ordinal();
		}
		return -1;
	}

	protected T get(Enum<?> enumKey) {
		return get(getIndex(enumKey));
	}
	
	protected <X> X getAsType(Enum<?> enumKey) {
		return get(getIndex(enumKey));
	}
	@SuppressWarnings("unchecked")
	<X> X get(int index) {
		if (index == -1) return null;
		try {
			return (X) items[index];
		} catch (ClassCastException e) {
			throw getCastMismatch(e);
		}
	}
	
	protected T set(Enum<?> enumKey, T value) {
		return set(getIndex(enumKey), value);
	}
	protected <X> X setAsType(Enum<?> enumKey, X value) {
		return set(getIndex(enumKey), value);
	}
	<X> X set(int index, X value) {
		if (index == -1) return null;
		try {
			items[index] = value;
			return value;
		} catch (ClassCastException e) {
			throw getCastMismatch(e);
		}
	}
	
	protected void clear() {
		for (int i = 0; i < size; i++) {
			items[i] = null;
		}
	}
}