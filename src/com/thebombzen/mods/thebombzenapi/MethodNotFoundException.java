package com.thebombzen.mods.thebombzenapi;

/**
 * An unchecked exception for ThebombzenAPI's own reflection classes.
 * It is used if there is an error accessing a method by name.
 * This Exception should not be thrown at runtime except in a development environment,
 * assuming everything was done correctly and thus it's an unchecked wrapper.
 * @author thebombzen
 */
public class MethodNotFoundException extends RuntimeException {
	
	/**
	 * Throwable is serializable so we need this or the compiler will whine.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct an empty MethodNotFoundException.
	 */
	public MethodNotFoundException(){
		super();
	}
	
	/**
	 * Construct a MethodNotFoundException with the given informational messsage.
	 * @param message A short bit of information describing the Exception.
	 */
	public MethodNotFoundException(String message){
		super(message);
	}
	
	/**
	 * Construct a MethodNotFoundException with the given informational messsage
	 * and with the given Throwable as the cause. This means that this exception wraps that one.
	 * @param message A short bit of information describing the Exception.
	 * @param cause The exception to wrap with this one.
	 */
	public MethodNotFoundException(String message, Throwable cause){
		super(message, cause);
	}
	
	/**
	 * Construct a MethodNotFoundException with the given Throwable as the cause.
	 * This means that this exception wraps that one.
	 * @param cause The exception to wrap with this one.
	 */
	public MethodNotFoundException(Throwable cause){
		super(cause);
	}
}
