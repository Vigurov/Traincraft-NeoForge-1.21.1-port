package train.core.helpers;

import javazoom.jl.decoder.JavaLayerHook;

import java.io.InputStream;

/** JLayer's tables (sfd.ser) from the mod's assets/traincraft/jlayer (1.12 read them through the resource manager) */
public class JLayerHook implements JavaLayerHook {
	@Override
	public InputStream getResourceAsStream(String name) {
		return JLayerHook.class.getClassLoader().getResourceAsStream("assets/traincraft/jlayer/" + name);
	}
}
