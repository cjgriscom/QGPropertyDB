package com.quirkygaming.errorlib;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler<T extends Exception> {
	
	private HashSet<Class<?>> handledExceptions = new HashSet<Class<?>>();
	private HashSet<Class<?>> unwrappedExceptions = new HashSet<Class<?>>();
	private PrintStream log;
	private boolean printStackTrace = false;
	
	public static ErrorHandler<Exception> forwardHandled(Logger log, Class<?>... handledExceptions) {
		return forwardHandled(new PrintStream(new LoggerStream(log, Level.SEVERE)), handledExceptions);
	}
	public static ErrorHandler<RuntimeException> logAll(Logger log) {
		return logAll(new PrintStream(new LoggerStream(log, Level.SEVERE)));
	}
	public static ErrorHandler<RuntimeException> logAll(Logger log, boolean printStackTrace) {
		return logAll(new PrintStream(new LoggerStream(log, Level.SEVERE)), printStackTrace);
	}
	public static ErrorHandler<Exception> forwardHandled(PrintStream log, Class<?>... handledExceptions) {
		return new ErrorHandler<Exception>(log, false, Exception.class, handledExceptions);
	}
	public static ErrorHandler<RuntimeException> logAll(PrintStream log) {
		return logAll(log, false);
	}
	public static ErrorHandler<RuntimeException> logAll(PrintStream log, boolean printStackTrace) {
		return new ErrorHandler<RuntimeException>(log, false, printStackTrace, RuntimeException.class);
	}
	public static ErrorHandler<Exception> forwardHandled(Class<?>... handledExceptions) {
		return new ErrorHandler<Exception>(null, false, Exception.class, handledExceptions);
	}
	public static ErrorHandler<RuntimeException> throwAll() {
		return new ErrorHandler<RuntimeException>(null, true, RuntimeException.class);
	}
	
	private ErrorHandler(PrintStream log, boolean bypassMode, Class<T> mode, Class<?>... handledExceptions) {
		this.log = log;
		Collections.addAll(this.handledExceptions, handledExceptions);
	}
	
	private ErrorHandler(PrintStream log, boolean bypassMode, boolean printStackTrace, Class<T> mode, Class<?>... handledExceptions) {
		this(log, bypassMode, mode, handledExceptions);
		this.printStackTrace = printStackTrace;
	}

	
	public ErrorHandler<T> addHandledExceptions(Class<?>... exceptionTypes) {
		Collections.addAll(handledExceptions, exceptionTypes);
		return this;
	}
	
	public ErrorHandler<T> addExceptionsToUnwrap(Class<?>... exceptionTypes) {
		Collections.addAll(unwrappedExceptions, exceptionTypes);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public void handle(Exception e) throws T {
		Throwable t = e;
		if (unwrappedExceptions.contains(e.getClass())) {
			t = e.getCause();
		}
		if (handledExceptions.contains(t.getClass())) {
			throw (T)t;
		} else {
			if (log == null) {
				if (t instanceof RuntimeException) {
					throw (RuntimeException)t;
				} else {
					throw new RuntimeException(t);
				}
			} else {
				StackTraceElement ste = e.getStackTrace()[0];
				if (printStackTrace) e.printStackTrace(log);
				log.println("Caught " + e.getClass().getName() + ": " + e.getMessage() + " at line " + ste.getLineNumber() + " in " + ste.getClassName());
			}
		}
	}
}

class LoggerStream extends OutputStream {
	private Level level;
	private Logger l;
	private StringBuilder buffer = new StringBuilder();
	LoggerStream(Logger l, Level level) {
		this.l = l;
		this.level = level;
	}
	@Override
	public void write(int b) throws IOException {
		if (b == '\n') {
			l.log(level, buffer.toString());
			buffer = new StringBuilder();
		} else {
			buffer.append((char)b);
		}
	}
}