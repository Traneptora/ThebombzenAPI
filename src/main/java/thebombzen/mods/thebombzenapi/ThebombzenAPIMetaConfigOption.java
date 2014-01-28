package thebombzen.mods.thebombzenapi;

/**
 * This is the implementation of ThebombzenAPIConfigOption for ThebombzenAPI.
 * This has no enum constants and thus the instance methods are meaningless.
 * Do not model your config options after this.
 * @author thebombzen
 */
public enum ThebombzenAPIMetaConfigOption implements ThebombzenAPIConfigOption {
	;

	private ThebombzenAPIMetaConfigOption(){
		
	}
	
	@Override
	public int getDefaultToggleIndex() {
		return -1;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public String[] getFiniteStringOptions() {
		return null;
	}

	@Override
	public String[] getInfo() {
		return null;
	}

	@Override
	public int getOptionType() {
		return -1;
	}

	@Override
	public String getShortInfo() {
		return null;
	}
	
}
