package thebombzen.mods.thebombzenapi;

public class FieldNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public FieldNotFoundException(){
		super();
	}
	
	public FieldNotFoundException(String message){
		super(message);
	}
	
	public FieldNotFoundException(String message, Throwable cause){
		super(message, cause);
	}
	
	public FieldNotFoundException(Throwable cause){
		super(cause);
	}
}
