package fexcraft.tmt.slim;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public abstract class ModelPoolEntry {

	public String name;
	public TexturedVertex[] vertices;
	public TexturedPolygon[] faces;
	public Map<String, TextureGroup> textures;
	protected TextureGroup texture;

	public InputStream checkValidPath(String path){
		InputStream in = getClass().getResourceAsStream(path);
		if (in == null && path.contains("!/")) {
			// a jar URL ("file:/.../mod.jar!/assets/..."): load the part inside the jar from the classpath
			in = getClass().getResourceAsStream(path.substring(path.indexOf("!/") + 1));
		}
		if (in == null && path.startsWith("file:")) {
			try {
				in = new java.io.FileInputStream(new java.io.File(new java.net.URI(path)));
			} catch (Exception ignored) {
			}
		}
		return in;
	}

	public abstract void getModel(InputStream file);

	/**
	 * Sets the current texture group, which is used to switch the
	 * textures on a per-model base. Do note that any model that is
	 * rendered afterwards will use the same texture. To counter it,
	 * set a default texture, either at initialization or before
	 * rendering.
	 * @param groupName The name of the texture group. If the texture
	 * group doesn't exist, it creates a new group automatically.
	 */
	protected void setTextureGroup(String groupName){
		if(textures.size() == 0 || !textures.containsKey(groupName)){
			textures.put(groupName, new TextureGroup());
		}
		texture = textures.get(groupName);
	}

}
