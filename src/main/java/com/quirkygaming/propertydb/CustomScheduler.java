package com.quirkygaming.propertydb;

/**
 * Use to implement a custom scheduler, for example, if you want writes to be synchronized with your main loop.
 * @author chandler
 *
 */
public interface CustomScheduler {
	/**
	 * Override to provide the "save clock" at a given period. This method will be called upon initialzation
	 * and implementors should set up a repeating synchronized event or async thread to run regularly
	 * @param token The initialization token; can be used to verify that the database is still active
	 * @param saveRoutine Invoke this runnable to perform the save sequence
	 */
	public void scheduleRepeatingTask(InitializationToken token, Runnable saveRoutine);
	
	/**
	 * Will be called when the database is disabled but the final save has not yet occured. 
	 * Use this method to clean up asynchronous threads if necessary, but don't force a save; that will be done internally.
	 */
	public void onDatabaseClose();
}
