package com.quirkygaming.errorlib;


public interface CustomHandler {
	/**
	 * Use to implement an event-driven exception handler.  
	 * @param thr The (possibly unwrapped) exception
	 * @return true if handled, false to wrap and rethrow as a RuntimeException
	 */
	public boolean handleException(Throwable thr);
}
