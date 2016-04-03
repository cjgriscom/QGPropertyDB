package com.quirkygaming.propertydb;

/**
 * Use to implement a custom scheduler, for example, if you want writes to be synchronized with your main loop.
 * @author chandler
 *
 */
public interface CustomScheduler {
	/**
	 * Override 
	 * @param token The initialization token; can be used to verify that the database is still active
	 * @param period_millis Passes the period requested by PropertyDB.initialize; can be ignored for custom implementations (but make sure you use a reasonable period)
	 * @param saveRoutine Invoke this runnable to perform the save sequence
	 */
	public void scheduleRepeatingTask(InitializationToken token, int period_millis, Runnable saveRoutine);
	
	/**
	 * Use this method to clean up asynchronous threads if necessary.
	 */
	public void onDatabaseClose();
}
