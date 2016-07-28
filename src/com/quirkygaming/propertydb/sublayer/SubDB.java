package com.quirkygaming.propertydb.sublayer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.DatabaseException;
import com.quirkygaming.propertydb.InitializationToken;
import com.quirkygaming.propertydb.PropertyDB;
import com.quirkygaming.propertylib.MutableProperty;

public final class SubDB<E extends Exception> {
	
	boolean closed = false;
	Map<String, MutableProperty<?>> fieldMap = new TreeMap<>();
	Map<MutableProperty<?>, String> fieldMapReverse = new HashMap<>();
	
	String name;
	File directory;
	ErrorHandler<E> handler;
	
	MutableProperty<TreeMap<String, SubEntryData>> index;
	private static long ROOT_VERSION = 1L;
	
	public SubDB(String name, File directory, ErrorHandler<E> handler) throws E {
		this.name = name; this.directory = directory; this.handler = handler;
		
		index = PropertyDB.initiateProperty(
				directory, "SubDB_" + name, ROOT_VERSION, new TreeMap<String, SubEntryData>(), handler);
	}
	
	public String getName() {return name;}
	
	private synchronized boolean closed() throws E {
		if (closed) {
			handler.handle(new DatabaseException("SubDB has been destroyed!"));
			return true;
		}
		return false;
	}
	
	public void destroySubDB() throws E {
		if (closed()) return;
		// Delete loaded properties
		Object[] copy = fieldMapReverse.keySet().toArray();
		for (Object o : copy) {
			deleteProperty((MutableProperty<?>) o);
		}
		// Delete unloaded properties
		copy = index.get().keySet().toArray();
		for (Object o : copy) {
			deleteProperty((String) o);
		}
		closed = true;
		PropertyDB.deleteProperty(directory, "SubDB_" + name, ROOT_VERSION, handler);

	}
	
	public boolean propertyExists(String fieldName) throws E {
		if (closed) return false;
		return index.get().containsKey(fieldName);
	}
	
	public boolean propertyExists(String fieldName, long version) throws E {
		if (closed) return false;
		return PropertyDB.propertyExists(directory, wrapName(fieldName), version);
	}
	
	public long propertyVersion(String fieldName) throws E {
		if (closed()) return -1;
		if (!index.get().containsKey(fieldName)) {
			handler.handle(new DatabaseException("Subdatabase property " + fieldName + " does not exist!"));
			return -1L;
		} else {
			return index.get().get(fieldName).version;
		}
	}
	
	public <T extends Serializable> MutableProperty<T> initiateProperty(String fieldName, long version, T initialValue) throws E {
		if (closed()) return null;
		SubEntryData data = new SubEntryData(version);

		index.get().put(fieldName, data);
		index.update();

		MutableProperty<T> mutable = PropertyDB.initiateProperty(directory, wrapName(fieldName), version, initialValue,
				handler);

		fieldMap.put(fieldName, mutable);
		fieldMapReverse.put(mutable, fieldName); // Insert into cache maps

		return mutable;
	}

	@SuppressWarnings("unchecked")
	public <T extends Serializable> MutableProperty<T> getLoadedProperty(String fieldName) throws E {
		if (closed()) return null;
		if (!fieldMap.containsKey(fieldName)) {
			handler.handle(new DatabaseException("Subdatabase property " + fieldName + " is not loaded!"));
			return null;
		} else {
			try {
				return (MutableProperty<T>) fieldMap.get(fieldName);
			} catch (ClassCastException e) {
				handler.handle(new DatabaseException("ClassCastException while retrieving Property: " + fieldName, e));
				return null;
			}
		}
	}
	
	public <T extends Serializable> MutableProperty<T> getOrInitiateProperty(String fieldName, long version, T initialValue) throws E {
		if (isLoaded(fieldName)) return getLoadedProperty(fieldName);
		else return initiateProperty(fieldName, version, initialValue);
	}
	
	// This loads and unloads a property, returning its value
	public <T extends Serializable> T getAndCloseProperty(String fieldName, long version, T initialValue) throws E {
		if (closed()) return null;
		MutableProperty<T> property = getOrInitiateProperty(fieldName, version, initialValue);
		unloadProperty(property);
		return property.get();
	}
	
	public boolean isLoaded(String fieldName) {
		if (closed) return false;
		return fieldMap.containsKey(fieldName);
	}
	
	public void deleteProperty(String fieldName) throws E {
		if (closed()) return;
		if (!index.get().containsKey(fieldName)) {
			handler.handle(new DatabaseException("Subdatabase property " + fieldName + " does not exist!"));
			return;
		}
		
		long version = propertyVersion(fieldName);
		
		PropertyDB.deleteProperty(directory, wrapName(fieldName), version, handler);

		index.get().remove(fieldName); // Remove from index
		index.update();
		fieldMapReverse.remove(fieldMap.remove(fieldName)); // Remove from both cache maps
	}
	
	public void deleteProperty(MutableProperty<?> property) throws E {
		if (closed()) return;
		if (!fieldMapReverse.containsKey(property)) {
			handler.handle(new DatabaseException("Requested property does not exist in this subdatabase!"));
		} else {
			PropertyDB.deleteProperty(property, handler);
			index.get().remove(fieldMapReverse.get(property)); // Remove from index
			index.update();
			fieldMap.remove(fieldMapReverse.remove(property)); // Remove from both cache maps
		}
	}
	
	public void unloadProperty(String fieldName) throws E {
		if (closed()) return;
		if (!fieldMap.containsKey(fieldName)) {
			handler.handle(new DatabaseException("Subdatabase property " + fieldName + " is not loaded!"));
			return;
		}
		
		PropertyDB.unloadProperty(fieldMap.get(fieldName), handler);

		fieldMapReverse.remove(fieldMap.remove(fieldName)); // Remove from both cache maps
	}
	
	public void unloadProperty(MutableProperty<?> property) throws E {
		if (closed()) return;
		if (!fieldMapReverse.containsKey(property)) {
			handler.handle(new DatabaseException("Requested property does not exist in this subdatabase!"));
		} else {
			PropertyDB.unloadProperty(property, handler);
			fieldMap.remove(fieldMapReverse.remove(property)); // Remove from both cache maps
		}
	}
	
	public List<String> getPropertyList() throws E {
		if (closed()) return null;
		return new ArrayList<String>(index.get().keySet());
	}
	
	private String wrapName(String fieldName) {return "SubDB_" + name + "_" + fieldName;}
}

class SubEntryData implements Serializable {
	private static final long serialVersionUID = -4473280399672895854L;
	long version;
	
	SubEntryData(long version) {this.version = version;}
}