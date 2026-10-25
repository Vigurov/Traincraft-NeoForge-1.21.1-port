package train.library;

public class Info {
	/* Mod relevant information */
	public static final String modID = "traincraft";
	public static final String modName = "Traincraft";

	/* All the resources for the mod */
	public static final String resourceLocation = "traincraft";
	public static final String guiPrefix = "textures/gui/";
	public static final String bookPrefix = "textures/gui/book/";
	public static final String trainsPrefix = "textures/trains/";
	public static final String zeppelinTexturePrefix = "textures/zeppelin/";
	public static final String modelTexPrefix = "textures/models/";
	public static final String armorPrefix = "textures/armor/";
	public static final String villagerPrefix = "textures/villager/";

	/** the TMT OBJ loader takes this as a URL ("file:" is cut off) and loads it from the mod's resources */
	public static String getModelAddress(String name){
		return "file:/assets/traincraft/models/" + name;
	}
}
