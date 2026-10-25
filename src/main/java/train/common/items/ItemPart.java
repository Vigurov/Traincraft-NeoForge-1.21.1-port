package train.common.items;

import net.minecraft.world.item.Item;

/**
 * @author canitzp
 */
public class ItemPart extends Item {
    protected String iconName = "";
    protected String folder = "parts";

    public ItemPart(String iconName){
        super(new Item.Properties());
        this.iconName = iconName;
    }

    public ItemPart overridePath(String newFolder){
        this.folder = newFolder;
        return this;
    }
}
