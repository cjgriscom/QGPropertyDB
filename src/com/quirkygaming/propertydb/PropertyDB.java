package com.quirkygaming.propertydb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertylib.MutableProperty;
import com.quirkygaming.propertylib.Property;
import com.quirkygaming.propertylib.PropertyObserver;
import com.quirkygaming.propertylib.PropertyObserver.EventType;

public final class PropertyDB {
	
	private static PropertyDB INSTANCE;
	
	private InitializationToken token = null;
	
	private CustomScheduler scheduler;
	
	private HashMap<MutableProperty<?>, DBEntry<?,?>> entries = new HashMap<MutableProperty<?>, DBEntry<?,?>>();
	private Set<DBEntry<?,?>> waiting = Collections.synchronizedSet(new LinkedHashSet<DBEntry<?,?>>());
	private Object saveLock = new Object();
	
	private PropertyDB(){}
	
	public static InitializationToken initializeDB(int period_millis) throws IllegalInitializationException {
		return initializeDB(period_millis, new DefaultScheduler());
	}
	
	public static InitializationToken initializeDB(int period_millis, CustomScheduler scheduler) throws IllegalInitializationException {
		if (INSTANCE == null) {
			assert debug("Initialized DB with " + period_millis + "ms period");
			INSTANCE = new PropertyDB();
			INSTANCE.token = new InitializationToken();
			INSTANCE.scheduler = scheduler;
			scheduler.scheduleRepeatingTask(INSTANCE.token, period_millis, new Runnable(){
				final InitializationToken token = INSTANCE.token;
				
				public void run() {
					assert debug("Async Write");
					assert debug_sleep(100);
					if (tokenIsValid(token)) {
						INSTANCE.saveProperties();
					}
					assert debug("Done Async Write");
				}
			});
			
			return INSTANCE.token;
		} else throw new IllegalInitializationException("PropertyDB already initialized!");
	}
	
	public static void closeDatabase(InitializationToken token) {
		if (tokenIsValid(token)) {
			assert debug("Closing DB");
			INSTANCE.token = null;
			INSTANCE.scheduler.onDatabaseClose();
			assert debug("CLOSING SAVE");
			INSTANCE.saveProperties();
			INSTANCE = null;
			assert debug("CLOSED");
		} else {
			throw new IllegalInitializationException("Invalid initialization token!");
		}
	}
	
	
	public static void forceSave(InitializationToken token) {
		if (tokenIsValid(token)) {
			assert debug("Forcing save...");
			INSTANCE.saveProperties();
		} else {
			throw new IllegalInitializationException("Invalid initialization token!");
		}
	}
	
	public static boolean initialized() {
		return INSTANCE != null;
	}
	public static boolean tokenIsValid(InitializationToken token) {
		return initialized() && token != null && token == INSTANCE.token;
	}
	
	private static class DBEntry<T, E extends Exception> {
		
		MutableProperty<T> mutable;
		File location;
		ErrorHandler<E> handler;
		String fieldName; long version;
		
		synchronized void save() {
			try {
				if (!location.exists()) {
					assert debug("Mkdirs for " + fieldName);
					new File(location.getParent()).mkdirs();
				}
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(location));
				oos.writeObject(mutable);
				oos.close();
			} catch (FileNotFoundException e) {
				//TODO Don't really like this
				try {handler.handle(new DatabaseException("FileNotFoundException while saving property: " + fieldName + " version " + version));} catch (Exception e1) {}
			} catch (IOException e) {
				try {handler.handle(new DatabaseException("IOException while saving property: " + fieldName + " version " + version));} catch (Exception e1) {}
			}
		}
		
		public int hashCode() {
			return mutable.hashCode();
		}
	}
	
	private synchronized void saveProperties() {
		final Set<DBEntry<?,?>> saveQueue = new LinkedHashSet<DBEntry<?,?>>();
		
		/*
		 * Perform transfer -- this ensures that 'waiting' is never locked very long 
		 * for insertions while the queue is being saved
		 */
		synchronized (waiting) { // Transfer all saves to saveQueue
			if (waiting.size() == 0) return; 
			saveQueue.addAll(waiting);
			waiting.clear();
		}
		synchronized (saveLock) { // Process saveQueue
			for (DBEntry<?,?> entry : saveQueue) {
				assert debug("Saving " + entry.fieldName);
				entry.save();
			}
			saveQueue.clear();
		}
	}
	
	private static File getPropertyLocation(String fieldName, long version, File directory) {
		return new File(directory.getAbsolutePath(), fieldName + "_" + version + ".property");
	}
	
	public static boolean propertyExists(String fieldName, long version, File directory) {
		return getPropertyLocation(fieldName, version, directory).exists();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Serializable, E extends Exception> MutableProperty<T> initiateProperty(File directory, final String fieldName, final long version, T initialValue, final ErrorHandler<E> handler) throws E {
		if (!initialized()) throw new IllegalInitializationException("Database not initialized!");
		
		final MutableProperty<T> property;
		final File location = getPropertyLocation(fieldName, version, directory);
		
		try {
			if (location.exists()) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(location));
				Object obj = ois.readObject();
				ois.close();
				property = (MutableProperty<T>) obj;
				assert debug("Loaded " + fieldName);
			} else {
				assert debug("Created " + fieldName);
				property = MutableProperty.newProperty(initialValue);
			}
		} catch (ClassCastException e) {
			handler.handle(new DatabaseException("ClassCastException while loading property: " + fieldName + " version " + version));
			return null;
		} catch (ClassNotFoundException e) {
			handler.handle(new DatabaseException("ClassNotFoundException while loading property: " + fieldName + " version " + version));
			return null;
		} catch (IOException e) {
			handler.handle(new DatabaseException("IOException while loading property: " + fieldName + " version " + version));
			return null;
		}
		
		final DBEntry<T, E> entry = new DBEntry<T, E>();
		entry.fieldName = fieldName;
		entry.version = version;
		entry.mutable = property;
		entry.handler = handler;
		entry.location = location;
		
		INSTANCE.entries.put(property, entry);
		
		property.addObserver(new PropertyObserver<T>() {
			private final InitializationToken token = INSTANCE.token;
			
			public void onChange(Property<T> modifiedProperty, EventType type) {
				if (tokenIsValid(token)) {
					synchronized (INSTANCE.waiting) {
						INSTANCE.waiting.add(entry);
						assert debug("Caught " + type + " for " + entry.fieldName);
					}
				}
			}
		}, EventType.SET, EventType.UPDATE);
		
		return property;
	}
	
	static boolean debug(String msg) {System.out.println(msg); return true;}
	static boolean debug_sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
}

final class DefaultScheduler implements CustomScheduler {
	
	Thread t;
	Object savelock = new Object();
	boolean saving = false;
	
	
	@Override
	public void scheduleRepeatingTask(final InitializationToken token, final int period_millis, final Runnable r) {
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (token.valid()) {
					try {
						Thread.sleep(period_millis);
						assert PropertyDB.debug("Completed sleep; Token:" + token.valid());
						synchronized (savelock) {
							saving = true;
							r.run();
							saving = false;
						}
						
					} catch (InterruptedException e) {
						assert PropertyDB.debug("THREAD INTERRUPTED; Token:" + token.valid());
						break;
					}
				}
			}
		});
		t.start();
	}
	@Override
	public synchronized void onDatabaseClose() {
		if (t == null) return;
		if (!saving) {
			assert PropertyDB.debug("Calling Interrupt");
			t.interrupt();
		} else assert PropertyDB.debug("NOT calling Interrupt");
		
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t = null;
	}
}
