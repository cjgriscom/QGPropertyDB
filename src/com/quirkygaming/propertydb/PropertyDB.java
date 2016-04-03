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

/**
 * Main API class; meant for static access
 * @author chandler
 *
 */
public final class PropertyDB {
	
	// Stores current instance;
	private static PropertyDB INSTANCE;
	
	// Stores the current token used to control this instance
	private InitializationToken token = null;
	
	// "Clock" provider and close-handler
	private CustomScheduler scheduler;
	
	// Stores running list of entries as initialized by users
	private HashMap<MutableProperty<?>, DBEntry<?,?>> entries = new HashMap<MutableProperty<?>, DBEntry<?,?>>();
	
	// Keeps track of elements waiting to be serialized on next clock pulse
	private Set<DBEntry<?,?>> waiting = Collections.synchronizedSet(new LinkedHashSet<DBEntry<?,?>>());
	
	// Ensures that two saves never run concurrently
	private Object saveLock = new Object();
	
	private PropertyDB(){}
	
	/**
	 * This method should be called by some authoritative controller of a program to
	 * initialize the database before users start registering their properties.
	 * It returns an InitializationToken which can be used to close the database
	 * safely when your application is closing.
	 * 
	 * @param period_millis The time between every asynchronous write
	 * @return The token used to control the database, usually the main loop of a program or a Bukkit plugin
	 * @throws IllegalInitializationException if the DB is already initialized
	 */
	public static InitializationToken initializeDB(int period_millis) throws IllegalInitializationException {
		return initializeDB(period_millis, new DefaultScheduler());
	}
	
	/**
	 * This method should be called by some authoritative controller of a program to
	 * initialize the database before users start registering their properties.
	 * It returns an InitializationToken which can be used to close the database
	 * safely when your application is closing.
	 * 
	 * @param period_millis The time between every asynchronous write
	 * @param scheduler Use to implement a custom scheduler, for example, if you want writes to be synchronized with your main loop.
	 * @return The token used to control the database, usually the main loop of a program or a Bukkit plugin
	 * @throws IllegalInitializationException if the DB is already initialized
	 */
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
	
	/**
	 * Safely writes and closes the database and ties up threads
	 * @param token The token passed to the database manager who initialized the database
	 * @throws IllegalInitializationException if the token is invalid
	 */
	public static void closeDatabase(InitializationToken token) throws IllegalInitializationException {
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
	
	/**
	 * Forces the database to write to disk
	 * @param token The token passed to the database manager who initialized the database
	 * @throws IllegalInitializationException if the token is invalid
	 */
	public static void forceSave(InitializationToken token) throws IllegalInitializationException {
		if (tokenIsValid(token)) {
			assert debug("Forcing save...");
			INSTANCE.saveProperties();
		} else {
			throw new IllegalInitializationException("Invalid initialization token!");
		}
	}
	
	/**
	 * Check if the database has been initialized
	 * @return True if initialized
	 */
	public static boolean initialized() {
		return INSTANCE != null;
	}
	
	/**
	 * Check if your token is valid
	 * @param token The token passed to the database manager who initialized the database
	 * @return True if valid
	 */
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
	
	// TODO add version checking method
	
	/**
	 * Checks if a property exists before loading it
	 * @param directory Location in which properties are stored (can be different for different properties)
	 * @param fieldName Name of the property
	 * @param version Version, used for checking existence of previous versions
	 * @return
	 */
	public static boolean propertyExists(File directory, String fieldName, long version) {
		return getPropertyLocation(fieldName, version, directory).exists();
	}
	
	/**
	 * Creates or loads the specified property
	 * @param directory Location in which properties are stored (can be different for different properties)
	 * @param fieldName Name of the property
	 * @param version Version, used for checking existence of previous versions
	 * @param initialValue Initial value if the property doesn't exist
	 * @param handler An error handler; use the presets in ErrorLib or make your own to handle DatabaseException
	 * @return A PropertyLib MutableProperty with the desired type
	 * @throws E Will throw a DatabaseException if the loading criteria don't match the file
	 */
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
	
	// Assertion debugging methods
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

/**
 * The default scheduler; runs asynchronously at a specified period and joins threads when closing
 */
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
		synchronized (savelock) {
			if (!saving) { // Interrupt sleep
				assert PropertyDB.debug("Calling Interrupt");
				t.interrupt();
			} else assert PropertyDB.debug("NOT calling Interrupt"); // Should never happen
		}
		
		try {
			t.join(); // Join thread
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t = null;
	}
}
