package com.quirkygaming.propertylib;

import java.util.HashSet;
import java.util.Set;

public class Mutator {
	
	private Set<Field<?>> fieldSet = new HashSet<Field<?>>();
	
	public Mutator() {
		
	}
	
	void internalAddPermission(Field<?> field) {
		fieldSet.add(field);
	}
	
	public <T> T set(Field<T> field, T value) {
		if (field instanceof MutableField || fieldSet.contains(field)) {
			field.set(value);
			return value;
		} else {
			throw new RuntimeException("Caller attempted to illegally set field with mutator");
		}
	}
	
}
