package traincompat;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** 1.7.10 Forge OBJ loading for the classic track models */
public final class AdvancedModelLoader {
    private AdvancedModelLoader() {}

    public static IModelCustom loadModel(ResourceLocation resource) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                Minecraft.getInstance().getResourceManager().open(resource), StandardCharsets.UTF_8))) {
            return new WavefrontObject(resource.toString(), r);
        } catch (Exception e) {
            throw new RuntimeException("Traincraft: could not load model " + resource, e);
        }
    }
}
