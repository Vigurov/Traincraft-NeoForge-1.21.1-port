package ebf.tim.gui;

import ebf.tim.items.ItemTransport;
import ebf.tim.utility.Recipe;
import ebf.tim.utility.RecipeManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The TiM recipe book (1.12 GUICraftBook): two pages side by side; first the mods' info pages, then a page per
 * transport recipe with its 3x3 grid and result. Ingredients that can be one of several items cycle every second.
 * 1.12 scaled the book to a share of the screen; here it has a fixed size in GUI pixels like the vanilla book.
 */
public class GUICraftBook extends Screen {
    private static final ResourceLocation BOOK = ResourceLocation.withDefaultNamespace("textures/gui/book.png");
    private static final ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath("traincraft", "textures/gui/crafting_table.png");
    /** a page of book.png: u 26..166 (the binding at u 26), 192 high */
    private static final int PAGE_W = 140, PAGE_H = 192;
    /** the tables the transports are made on, in the order the book shows them */
    private static final String[] TABLES = {"traintable", "traintabletier1", "traintabletier2", "traintabletier3"};

    /** info pages by mod id, in the order they were added */
    private static final Map<String, List<String>> infoPages = new LinkedHashMap<>();
    /** the left page shown, kept between openings like in 1.12 */
    public static int page = 0;
    private static List<Object> pageData = null;

    private int left, top;
    private long frame = 0;
    private int cycle = 0;

    public GUICraftBook() {
        super(Component.translatable("item.traincraft.craftbook"));
    }

    public static void addPage(String modid, String text) {
        infoPages.computeIfAbsent(modid, k -> new ArrayList<>()).add(text);
        pageData = null;
    }

    /** a recipe page: the recipe and the table it is made on */
    private record RecipePage(Recipe recipe, String table) {}

    private static List<Object> pages() {
        if (pageData == null) {
            List<Object> pages = new ArrayList<>();
            for (List<String> p : infoPages.values()) pages.addAll(p);
            for (String table : TABLES) {
                for (Recipe r : RecipeManager.getRecipes(table)) {
                    if (!r.getresult().isEmpty() && r.getresult().get(0).getItem() instanceof ItemTransport) {
                        pages.add(new RecipePage(r, table));
                    }
                }
            }
            pageData = pages;
        }
        return pageData;
    }

    private static Object getPage(int n) {
        List<Object> p = pages();
        return n >= 0 && n < p.size() ? p.get(n) : null;
    }

    @Override
    protected void init() {
        left = (width - PAGE_W * 2) / 2;
        top = Math.max(2, (height - PAGE_H) / 2);
        if (page >= pages().size()) page = 0;
        addRenderableWidget(new PageButton(left + 20, top + PAGE_H - 30, false, b -> {
            if (page > 0) page -= 2;
        }, true));
        addRenderableWidget(new PageButton(left + PAGE_W * 2 - 43, top + PAGE_H - 30, true, b -> {
            if (page + 2 < pages().size()) page += 2;
        }, true));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        //the left page is the right one mirrored (u from 166 back to 26), so the binding is in the middle
        g.blit(BOOK, left, top, PAGE_W, PAGE_H, 26 + PAGE_W, 0, -PAGE_W, PAGE_H, 256, 256);
        g.blit(BOOK, left + PAGE_W, top, 26, 0, PAGE_W, PAGE_H);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        if (System.currentTimeMillis() - frame > 1000) {
            frame = System.currentTimeMillis();
            cycle++;
        }
        ItemStack hovered = ItemStack.EMPTY;
        for (int side = 0; side < 2; side++) {
            ItemStack h = renderPage(g, getPage(page + side), left + (side == 0 ? 18 : PAGE_W + 10), top + 16, mouseX, mouseY);
            if (!h.isEmpty()) hovered = h;
        }
        for (int side = 0; side < 2 && page + side < pages().size(); side++) {
            String n = (page + side + 1) + " / " + pages().size();
            g.drawString(font, n, left + PAGE_W / 2 + side * PAGE_W + (side == 0 ? 4 : -4) - font.width(n) / 2, top + PAGE_H - 42, 0xFF5A4A3A, false);
        }
        if (!hovered.isEmpty()) {
            g.renderTooltip(font, hovered, mouseX, mouseY);
        }
    }

    /** draws a page with its top left text corner at x, y; returns the stack under the mouse */
    private ItemStack renderPage(GuiGraphics g, Object p, int x, int y, int mouseX, int mouseY) {
        int w = PAGE_W - 28;
        if (p instanceof String text) {
            //1.12 laid the lines out for a wider page: shrink the text until its longest line fits
            String[] lines = text.split("\n");
            int longest = 1;
            for (String s : lines) longest = Math.max(longest, font.width(s));
            float scale = Math.min(1F, (float) w / longest);
            g.pose().pushPose();
            g.pose().translate(x, y + 3, 0);
            g.pose().scale(scale, scale, 1);
            for (int i = 0; i < lines.length; i++) {
                g.drawString(font, lines[i], 0, i * 10, 0xFF000000, false);
            }
            g.pose().popPose();
            return ItemStack.EMPTY;
        }
        if (!(p instanceof RecipePage rp)) {
            return ItemStack.EMPTY;
        }
        ItemStack hovered = ItemStack.EMPTY;
        List<ItemStack> results = rp.recipe.getresult();
        ItemStack result = results.get(cycle % results.size());
        int line = 0;
        for (FormattedCharSequence l : font.split(result.getHoverName(), w)) {
            g.drawString(font, l, x, y + 3 + line * 10, 0xFF000000, false);
            line++;
        }
        int gy = y + 40;
        List<List<ItemStack>> in = rp.recipe.getRecipeItems();
        for (int i = 0; i < 9; i++) {
            List<ItemStack> slot = i < in.size() ? in.get(i) : null;
            ItemStack s = slot == null || slot.isEmpty() ? ItemStack.EMPTY : slot.get(cycle % slot.size());
            int sx = x + (i % 3) * 18, sy = gy + (i / 3) * 18;
            if (slot(g, s, sx, sy, mouseX, mouseY)) hovered = s;
        }
        g.blit(ARROW, x + 58, gy + 18, 90, 35, 22, 15);
        if (slot(g, result, x + 86, gy + 18, mouseX, mouseY)) hovered = result;
        Component table = Component.translatable("block.traincraft.block." + rp.table);
        int ty = gy + 62;
        for (FormattedCharSequence l : font.split(table, w)) {
            g.drawString(font, l, x, ty, 0xFF404040, false);
            ty += 10;
        }
        return hovered;
    }

    /** a vanilla looking slot with the stack; true if the mouse is over it */
    private boolean slot(GuiGraphics g, ItemStack s, int x, int y, int mouseX, int mouseY) {
        g.fill(x, y, x + 18, y + 18, 0xFF373737);
        g.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        if (!s.isEmpty()) {
            g.renderItem(s, x + 1, y + 1);
            g.renderItemDecorations(font, s, x + 1, y + 1);
        }
        boolean over = mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18;
        if (over) {
            g.fill(x + 1, y + 1, x + 17, y + 17, 0x80FFFFFF);
        }
        return over && !s.isEmpty();
    }
}
