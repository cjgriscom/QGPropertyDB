package com.quirkygaming.errorlib;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler<T extends Exception> {
	
	private HashSet<Class<?>> handledExceptions = new HashSet<Class<?>>();
	private HashSet<Class<?>> unwrappedExceptions = new HashSet<Class<?>>();
	private CustomHandler ch = null;
	private Class<T> mode;
	
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
		return new ErrorHandler<Exception>(new LogHandler(log, false), Exception.class, handledExceptions);
	}
	public static ErrorHandler<RuntimeException> logAll(PrintStream log) {
		return logAll(log, false);
	}
	public static ErrorHandler<RuntimeException> logAll(PrintStream log, boolean printStackTrace) {
		return new ErrorHandler<RuntimeException>(new LogHandler(log, printStackTrace), RuntimeException.class, RuntimeException.class);
	}
	public static ErrorHandler<Exception> forwardHandled(Class<?>... handledExceptions) {
		return new ErrorHandler<Exception>(new ThrowAll(), Exception.class, handledExceptions);
	}
	public static ErrorHandler<RuntimeException> throwAll() {
		return new ErrorHandler<RuntimeException>(new ThrowAll(), RuntimeException.class);
	}
	public static ErrorHandler<RuntimeException> customHandler(CustomHandler handler) {
		return new ErrorHandler<RuntimeException>(handler, RuntimeException.class);
	}
	
	private ErrorHandler(CustomHandler ch, Class<T> mode, Class<?>... handledExceptions) {
		this.ch = ch;
		this.mode = mode;
		validateClasses(handledExceptions);
		Collections.addAll(this.handledExceptions, handledExceptions);
	}
	
	public ErrorHandler<T> addHandledExceptions(Class<?>... exceptionTypes) {
		validateClasses(exceptionTypes);
		Collections.addAll(handledExceptions, exceptionTypes);
		return this;
	}
	
	public ErrorHandler<T> addExceptionsToUnwrap(Class<?>... exceptionTypes) {
		validateClasses(exceptionTypes);
		Collections.addAll(unwrappedExceptions, exceptionTypes);
		return this;
	}
	
	public void tryCatch(Runnable r) throws T {
		try {
			r.run();
		} catch (Exception e) {
			handle(e);
		}
	}
	
	private void throwAsRuntimeExp(Throwable t) throws RuntimeException {
		if (t instanceof RuntimeException) {
			throw (RuntimeException)t;
		} else {
			throw new RuntimeException(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void handle(Throwable t) throws T {
		
		// Unwrap exception causes if requested
		if (unwrappedExceptions.contains(t.getClass())) {
			t = t.getCause();
		}
		
		// Forward exceptions that the client will handle (Exception mode)
		if (handledExceptions.contains(t.getClass())) {
			throw (T)t;
		}
		
		// Pass to CustomHandler
		boolean handled = ch.handleException(t);
		if (!handled) throwAsRuntimeExp(t);
	}
	
	// Make sure provided classes can be thrown
	private void validateClasses(Class<?>[] exceptionTypes) throws IllegalArgumentException {
		for (Class<?> c : exceptionTypes) {
			if (!mode.isAssignableFrom(c)) {
				throw formatIllegalArgumentException(c);
			}
		}
	}
	
	private RuntimeException formatIllegalArgumentException(Class<?> illegal) {
		RuntimeException exp = new IllegalArgumentException(
				"ErrorHandler<" + mode.getSimpleName() + "> "
				+ "cannot handle " + illegal.getName());
		
		// Sift stack trace back to the culprit
		int stackStart = 0;
		StackTraceElement[] trace = exp.getStackTrace();
		for (StackTraceElement e : trace) {
			if (e.getClassName().equals(ErrorHandler.class.getName())) stackStart++;
			else break;
		}
		exp.setStackTrace(Arrays.copyOfRange(trace, stackStart, trace.length));
		return exp;
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

class ThrowAll implements CustomHandler {
	@Override
	public boolean handleException(Throwable t) {
		return false;
	}
}

class LogHandler implements CustomHandler {
	boolean printStackTrace;
	PrintStream log;
	
	public LogHandler(PrintStream log, boolean printStackTrace) {
		this.printStackTrace = printStackTrace;
		this.log = log;
	}
	
	@Override
	public boolean handleException(Throwable thr) {
		StackTraceElement ste = thr.getStackTrace()[0];
		if (printStackTrace) thr.printStackTrace(log);
		log.println("Caught " + thr.getClass().getName() + ": " + thr.getMessage() + " at line " + ste.getLineNumber() + " in " + ste.getClassName());
		return true;
	}
}