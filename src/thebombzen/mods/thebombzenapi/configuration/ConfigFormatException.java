package thebombzen.mods.thebombzenapi.configuration;

public class ConfigFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ConfigFormatException(String info){
		super(info);
	}
	
	public ConfigFormatException(String info, Throwable cause){
		super(info, cause);
	}
	
}
