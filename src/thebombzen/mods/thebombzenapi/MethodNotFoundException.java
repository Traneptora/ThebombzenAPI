package thebombzen.mods.thebombzenapi;

public class MethodNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public MethodNotFoundException(){
		super();
	}
	
	public MethodNotFoundException(String message){
		super(message);
	}
	
	public MethodNotFoundException(String message, Throwable cause){
		super(message, cause);
	}
	
	public MethodNotFoundException(Throwable cause){
		super(cause);
	}
}
