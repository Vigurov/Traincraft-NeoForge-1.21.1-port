package traincompat;

/** 1.7.10 Forge OBJ model interface, as the classic track models use it */
public interface IModelCustom {
    String getType();
    void renderAll();
    void renderOnly(String... groupNames);
    void renderPart(String partName);
    void renderAllExcept(String... excludedGroupNames);
}
