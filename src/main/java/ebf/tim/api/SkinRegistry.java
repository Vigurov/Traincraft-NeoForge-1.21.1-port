package ebf.tim.api;

import ebf.tim.entities.GenericRailTransport;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;



public class SkinRegistry {

    public static boolean forceSkinRegister=false,debugSkinRegistration=true;
    private static Map<String, Map<String, TransportSkin>> transports = new HashMap<String, Map<String, TransportSkin>>();
    public static Map<String, TransportSkin> getTransportSkins(Class c){
        return transports.containsKey(c.getName())?transports.get(c.getName()):null;
    }

    public static void addSkin(Class c, String modid, String textureURI, String name, String description){
        addSkinRecolor(c,modid,textureURI,null, null, null,name,description);
    }

    public static void addSkin(Class c, String modid, String textureURI, String bogieTextureURI, String name, String description){
        addSkinRecolor(c,modid,textureURI,new String[]{bogieTextureURI}, null,null,name,description);
    }
    public static void addSkin(Class c, String modid, String textureURI, String bogieTextureURI, String subBogieTextureURI, String name, String description){
        addSkinRecolor(c.getName(),modid,textureURI,new String[]{bogieTextureURI}, new String[]{subBogieTextureURI},null,null,name,description);
    }

    public static void addSkin(Class c, String modid, String textureURI, @Nullable String[] bogieTextureURIs, String name, String description){
        addSkinRecolor(c,modid,textureURI,bogieTextureURIs,null,null,name,description);
    }

    public static void addSkin(Class c, String modid, String textureURI, @Nullable String[] bogieTextureURIs,@Nullable String[] subBogieTextureURIs, String name, String description){
        addSkinRecolor(c.getName(),modid,textureURI,bogieTextureURIs,subBogieTextureURIs,null,null,name,description);
    }

    public static void addSkin(String c, String modid, String textureURI, String name, String description){
        addSkinRecolor(c,modid,textureURI,null,null, null,name,description);
    }

    public static void addSkin(String c, String modid, String textureURI, String bogieTextureURI, String name, String description){
        addSkinRecolor(c,modid,textureURI,new String[]{bogieTextureURI},null,null,name,description);
    }

    public static void addSkin(String c, String modid, String textureURI, String bogieTextureURI, String subBogieTextureURI, String name, String description){
        addSkinRecolor(c,modid,textureURI,new String[]{bogieTextureURI},new String[]{subBogieTextureURI},null,null,name,description);
    }

    public static void addSkin(String c, String modid, String textureURI, @Nullable String[] bogieTextureURIs, String name, String description){
        addSkinRecolor(c,modid,textureURI,bogieTextureURIs,null,null,name,description);
    }

    public static void addSkin(String c, String modid, String textureURI, @Nullable String[] bogieTextureURIs,  @Nullable String[] subBogieTextureURIs, String name, String description){
        addSkinRecolor(c,modid,textureURI,bogieTextureURIs,subBogieTextureURIs,null,null,name,description);
    }

    public static void addSkinRecolor(Class c,String modid, String textureURI, @Nullable int[] recolorFrom, @Nullable int[] recolorTo, String skinName, String skinDescription){
        addSkinRecolor(c,modid,textureURI,null,null,recolorFrom,recolorTo,skinName,skinDescription);
    }

    public static void addSkinRecolor(Class c,String modid, String textureURI, String[] bogieTextureURI, @Nullable int[] recolorFrom, @Nullable int[] recolorTo, String skinName, String skinDescription){
        addSkinRecolor(c,modid,textureURI,bogieTextureURI,null,recolorFrom,recolorTo,skinName,skinDescription);
    }

    public static void addSkinRecolor(String c,String modid, String textureURI, String[] bogieTextureURI, @Nullable int[] recolorFrom, @Nullable int[] recolorTo, String skinName, String skinDescription){
        addSkinRecolor(c,modid,textureURI,bogieTextureURI,null,recolorFrom,recolorTo,skinName,skinDescription);
    }

    public static void addSkinRecolor(Class c,String modid, String textureURI, String[] bogieTextureURI,String[] subBogieTextureURI, @Nullable int[] recolorFrom, @Nullable int[] recolorTo, String skinName, String skinDescription){
        addSkinRecolor(c.getName(),modid,textureURI,bogieTextureURI,subBogieTextureURI,recolorFrom,recolorTo,skinName,skinDescription);
    }

    public static void addSkinRecolor(String c,String modid, String textureURI,@Nullable String[] bogieTextureURI,@Nullable String[] subBogieTextureURI, @Nullable int[] recolorFrom, @Nullable int[] recolorTo, String skinName, String skinDescription){

        if(isLoaded(modid) || forceSkinRegister) {
            if (!transports.containsKey(c)) {
                transports.put(c, new HashMap<String, TransportSkin>());
            }
            if(transports.get(c).containsKey(modid + ":" + skinName)){
                System.out.println(String.join(" ", "ERROR", "Duplicate TransportSkin entry: " + skinName, "In entity: " + c, "Overriding original entry"));
            }

            TransportSkin s = new TransportSkin(modid, textureURI,skinName,skinDescription);
            s.setId(transports.get(c).size());
            if(bogieTextureURI!=null){
                s.setBogieTextures(bogieTextureURI);
            }
            if(subBogieTextureURI!=null){
                s.setSubBogieTextures(bogieTextureURI);
            }
            s.setRecolorsFrom(recolorFrom);
            s.setRecolorsTo(recolorTo);

            transports.get(c).put(modid + ":" + skinName, s);
        }
    }


    public static void addSkin(Class c, TransportSkin s){
        addSkin(c.getName(),new TransportSkin(s.modid,s.texture, s.name,s.description)
                .setRecolorsFrom(s.colorsFrom).setRecolorsTo(s.colorsTo)
                .setBogieTextures(s.bogieTextures).setSubBogieTextures(s.subBogieTextures));
    }

    public static void addSkin(String c, TransportSkin s){

        if(isLoaded(s.modid) || forceSkinRegister) {
            if (!transports.containsKey(c)) {
                transports.put(c, new HashMap<String, TransportSkin>());
            }
            if(transports.get(c).containsKey(s.modid + ":" + s.name)){
                System.out.println(String.join(" ", "ERROR", "Duplicate TransportSkin entry: " + s.name, "In entity: " + c, "Overriding original entry"));
            }
            s.setId(transports.get(c).size());
            transports.get(c).put(s.modid + ":" + s.name, s);
        }
    }



    public static TransportSkin getSkin(GenericRailTransport entity, Player player, boolean isPaintBucket, String internalResourceURI){
        if (entity.getSkinList(player, isPaintBucket)==null || !entity.getSkinList(player, isPaintBucket).containsKey(internalResourceURI)){
            return null;
        }
        return entity.getSkinList(player, isPaintBucket).get(internalResourceURI);
    }

    private static boolean isLoaded(String modid){
        return ModList.get()==null || ModList.get().isLoaded(modid);
    }
}
