package thebombzen.mods.thebombzenapi.configuration;

public enum SingleMultiBoolean {
	NEVER("Never"),
	ALWAYS("Always"),
	SINGLEPLAYER_ONLY("Singleplayer Only"),
	MULTIPLAYER_ONLY("Multiplayer Only");
	
	public static final String[] singleMultiStrings = new String[]{"Never", "Always", "Singleplayer Only", "Multiplayer Only"};
	
	private String info;
	
	private SingleMultiBoolean(String info){
		this.info = info;
	}
	
	@Override
	public String toString(){
		return info;
	}
}
