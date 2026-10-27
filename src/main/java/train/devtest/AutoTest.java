package train.devtest;

import train.render.TrainSmoke;
import train.blocks.catenary.CatenaryWireItem;
import train.Traincraft;
import train.blocks.TCBlocks;
import train.core.handlers.ConfigHandler;
import train.client.JukeboxClient;
import train.entity.rollingStock.EntityJukeBoxCart;
import train.blocks.bench.BlockTrainWorkbench;
import train.blocks.bench.TrainWorkbenchMenu;
import train.blocks.machines.MachineMenu;
import train.blocks.machines.MachineTile;
import com.mojang.blaze3d.platform.NativeImage;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.registry.TiMGenericRegistry;
import ebf.tim.utility.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.io.File;
import net.minecraft.util.Mth;
import train.client.RoofRideClient;
import train.blocks.TCBlocks;
import train.blocks.catenary.CatenaryMastBlock;
import train.blocks.catenary.CatenaryMastTile;
import train.blocks.catenary.Wires;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;

/**
 * Dev autotest, the 1.21 version of the 1.12 one: active when the game directory has a tctest.properties
 * (gradle's daemon keeps its environment, so the settings come from a file). It makes a flat creative world, runs
 * "use" lines that place blocks and transports, can seat the player and drive, logs the transports and takes
 * screenshots, then quits.
 *
 * keys: use=block_or_item@x,y,z[@yaw];... (positions relative to the spawn ground), cam=dx,dy,dz,yaw,pitch,
 * seat=1, drive=accelerator (TiM: negative is forwards), start=ticks before driving, ticks=total,
 * shots=from,to,every, log=every
 */
public class AutoTest {
    private static Properties p;
    private static boolean started;
    private static int tick = -1;
    private static BlockPos base;
    private static GenericRailTransport train;
    private static final java.util.List<GenericRailTransport> placed = new java.util.ArrayList<>();
    private static final java.util.List<net.minecraft.world.entity.Mob> MOBS = new java.util.ArrayList<>();
    private static final java.util.Map<net.minecraft.world.entity.Mob, Float> MOB_HEALTH = new java.util.HashMap<>();

    public static void init() {
        File f = new File(Minecraft.getInstance().gameDirectory, "tctest.properties");
        if (!f.exists()) {
            return;
        }
        p = new Properties();
        try (FileReader r = new FileReader(f)) {
            p.load(r);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        NeoForge.EVENT_BUS.addListener(AutoTest::onTick);
        //the integrated server stops while the game is paused: a run whose window lost focus (someone working in
        //another window) had every train standing still
        Minecraft.getInstance().options.pauseOnLostFocus = false;
        GenericRailTransport.debugCollide = p.containsKey("debugcollide");
        ebf.tim.render.Pantographs.debug = p.containsKey("pantolog");
        //what hurts the player during a test
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e) -> {
            if (e.getEntity() instanceof net.minecraft.world.entity.player.Player) {
                log("player hurt t=" + tick + " by " + e.getSource().getMsgId() + " " + e.getSource().getEntity() + " amount=" + e.getNewDamage());
            }
        });
        if (p.containsKey("soundlog")) {
            //soundlog=1  logs the sounds the client plays (but the steps and the like)
            NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.sound.PlaySoundEvent e) -> {
                if (e.getSound() != null && e.getSound().getLocation().getNamespace().equals("traincraft")) {
                    log("sound t=" + tick + " " + e.getSound().getLocation());
                }
            });
        }
        if (p.containsKey("itemsheet")) {
            //itemsheet=scale  every item of the Traincraft tabs drawn over the screen in a grid on slot grey, a new
            //page every 20 ticks from tick 40 (take shots=40,..,20)
            NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RenderGuiEvent.Post e) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || tick < 40) return;
                if (sheet == null) {
                    net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(mc.player.connection.enabledFeatures(), true, mc.level.registryAccess());
                    sheet = new java.util.ArrayList<>();
                    sheet.addAll(Traincraft.tcTab.get().getDisplayItems());
                    sheet.addAll(Traincraft.timCraftingTab.get().getDisplayItems());
                    log("itemsheet " + sheet.size() + " items");
                }
                var g = e.getGuiGraphics();
                float scale = Float.parseFloat(p.getProperty("itemsheet"));
                int w = g.guiWidth(), h = g.guiHeight();
                g.fill(0, 0, w, h, 0xFF8B8B8B);
                int cell = 18;
                int cols = (int) (w / scale / cell), rows = (int) (h / scale / cell);
                int page = (tick - 40) / 20, from = page * cols * rows;
                g.pose().pushPose();
                g.pose().scale(scale, scale, 1);
                for (int n = from; n < Math.min(sheet.size(), from + cols * rows); n++) {
                    int x = ((n - from) % cols) * cell + 1, y = ((n - from) / cols) * cell + 1;
                    g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
                    g.fill(x, y, x + 17, y + 17, 0xFFFFFFFF);
                    g.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
                    g.renderItem(sheet.get(n), x, y);
                    g.renderItemDecorations(mc.font, sheet.get(n), x, y);
                }
                g.pose().popPose();
            });
        }
        log("autotest on: " + p);
    }

    private static java.util.List<net.minecraft.world.item.ItemStack> sheet;

    private static int i(String key, int def) {
        return p.containsKey(key) ? Integer.parseInt(p.getProperty(key).trim()) : def;
    }

    /**
     * remote=host:port  joins that (dedicated) server instead of making a world; remotecmd=tick:command|tick:command
     * sends chat commands (without the slash) at those ticks; every remotelog ticks (default 20) logs the transports
     * this client sees; quits after ticks
     */
    private static void remoteTick(Minecraft mc) {
        if (!started && mc.screen instanceof TitleScreen) {
            started = true;
            String addr = p.getProperty("remote");
            log("joining " + addr + " as " + mc.getUser().getName() + " " + mc.getUser().getProfileId());
            net.minecraft.client.gui.screens.ConnectScreen.startConnecting(mc.screen, mc,
                    net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(addr),
                    new net.minecraft.client.multiplayer.ServerData("tctest", addr, net.minecraft.client.multiplayer.ServerData.Type.OTHER), false, null);
            return;
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        tick++;
        if (tick == 1) log("joined, player at " + mc.player.blockPosition());
        for (String c : p.getProperty("remotecmd", "").split("\\|")) {
            int at = c.indexOf(':');
            if (at > 0 && Integer.parseInt(c.substring(0, at).trim()) == tick) {
                String cmd = c.substring(at + 1).trim();
                mc.player.connection.sendCommand(cmd);
                log("cmd " + cmd);
            }
        }
        //remotekeys=tick:key:on|tick:key:off|tick:ignition  holds or releases forward/back/jump, or presses ignition
        for (String c : p.getProperty("remotekeys", "").split("\\|")) {
            String[] k = c.trim().split(":");
            if (k.length < 2 || Integer.parseInt(k[0]) != tick) continue;
            boolean on = k.length < 3 || k[2].equals("on");
            switch (k[1]) {
                case "forward" -> mc.options.keyUp.setDown(on);
                case "back" -> mc.options.keyDown.setDown(on);
                case "jump" -> mc.options.keyJump.setDown(on);
                case "ignition" -> net.minecraft.client.KeyMapping.click(ebf.tim.utility.ClientKeyHandler.KeyIgnition.getKey());
                default -> log("unknown key " + k[1]);
            }
            log("key " + c.trim() + " vehicle=" + mc.player.getVehicle());
        }
        if (tick % i("remotelog", 20) == 0) {
            StringBuilder b = new StringBuilder();
            for (var e : mc.level.entitiesForRendering()) {
                if (e instanceof GenericRailTransport t) {
                    b.append(String.format(" [%s %.2f,%.2f,%.2f yaw=%.1f v=%.3f run=%s derail=%s f=%s b=%s]", t.getClass().getSimpleName().replace("Entity", ""),
                            t.getX(), t.getY(), t.getZ(), t.getYRot(), t.getVelocity(), t.getBoolean(GenericRailTransport.boolValues.RUNNING),
                            t.getBoolean(GenericRailTransport.boolValues.DERAILED), t.frontLinkedID, t.backLinkedID));
                }
            }
            log("remote t=" + tick + b);
        }
        if (p.containsKey("shots")) {
            String[] sh = p.getProperty("shots").split(",");
            int from = Integer.parseInt(sh[0]), to = Integer.parseInt(sh[1]), every = Integer.parseInt(sh[2]);
            if (tick >= from && tick <= to && (tick - from) % every == 0) {
                net.minecraft.client.Screenshot.grab(mc.gameDirectory, "tc-" + tick + ".png", mc.getMainRenderTarget(), m -> {});
            }
        }
        if (tick == i("ticks", 200)) {
            log("done");
            mc.stop();
        }
    }

    private static void log(String s) {
        System.out.println("[TCTEST] " + s);
    }

    private static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (p.containsKey("remote")) {
            remoteTick(mc);
            return;
        }
        if (!started && mc.screen instanceof TitleScreen) {
            started = true;
            String name = "tctest";
            if (p.containsKey("keepworld")) {
                //keepworld=1  opens the world the last run left (saved when it quit) instead of a new one
                mc.createWorldOpenFlows().openWorld(name, () -> log("could not open the kept world"));
                return;
            }
            try {
                Path dir = mc.gameDirectory.toPath().resolve("saves").resolve(name);
                if (Files.exists(dir)) {
                    try (var walk = Files.walk(dir)) {
                        walk.sorted(Comparator.reverseOrder()).forEach(x -> x.toFile().delete());
                    }
                }
            } catch (Exception e) {
                log("could not delete old world " + e);
            }
            GameRules rules = new GameRules();
            rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
            rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
            rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
            LevelSettings settings = new LevelSettings(name, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel(name, settings, new WorldOptions(1234L, false, false),
                    reg -> reg.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                    mc.screen);
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            return;
        }
        tick++;
        if (mc.isPaused() && tick % 20 == 0) log("paused at " + tick + ": the server is not ticking");
        ServerLevel level = mc.getSingleplayerServer().overworld();
        ServerPlayer sp = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
        if (sp == null) {
            return;
        }
        var server = mc.getSingleplayerServer();
        if (tick == 20) {
            //world changes run on the server thread (off it the level hands out no block entities)
            server.execute(() -> {
                level.setDayTime(i("time", 6000));
                base = BlockPos.containing(sp.getX(), sp.getY(), sp.getZ());
                log("base " + base);
                if (p.containsKey("adopt")) {
                    //adopt=1  the transports already in the (kept) world are the test's, locomotive first
                    for (var e : level.getAllEntities()) {
                        if (e instanceof GenericRailTransport t) {
                            if (t instanceof EntityTrainCore && train == null) train = t;
                            placed.add(t);
                        }
                    }
                    placed.sort(Comparator.comparingInt(t -> t instanceof EntityTrainCore ? 0 : 1));
                    log("adopted " + placed.size());
                }
                var mods = level.registryAccess().registryOrThrow(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.BIOME_MODIFIERS);
                log("config: biome modifiers " + mods.keySet().stream().filter(k -> k.getNamespace().equals("traincraft")).sorted().toList()
                        + ", steam recipes " + ebf.tim.utility.RecipeManager.getRecipes("traintabletier1").stream()
                        .filter(r -> r.getresult().get(0).getItem() instanceof ebf.tim.items.ItemTransport it && it.info() != null
                                && it.info().getTypes().contains(ebf.tim.TrainsInMotion.transportTypes.STEAM)).count()
                        + ", windmill radius " + ConfigHandler.windmillCheckRadius());
                for (String use : p.getProperty("use", "").split(";")) {
                    if (!use.isBlank()) run(level, sp, use.trim());
                }
                if (p.containsKey("gallery")) {
                    //gallery=from,to[,length]  transports from..to of the registry, each in the middle of its own line
                    //(24 blocks unless given), 5 blocks apart
                    String[] g = p.getProperty("gallery").split(",");
                    int from = Integer.parseInt(g[0]), to = Math.min(Integer.parseInt(g[1]), TiMGenericRegistry.TRANSPORTS.size() - 1);
                    int len = g.length > 2 ? Integer.parseInt(g[2]) : 24;
                    int placed = 0;
                    for (int n = from; n <= to; n++) {
                        int z = (n - from) * 5;
                        run(level, sp, "minecraft:rail@0,0," + z + "@" + len + "@x");
                        var id = BuiltInRegistries.ENTITY_TYPE.getKey(TiMGenericRegistry.TRANSPORTS.get(n).get());
                        int had = AutoTest.placed.size();
                        run(level, sp, id + "@" + (len / 2) + ",0," + z + "@-4");
                        if (AutoTest.placed.size() > had) placed++;
                    }
                    log("gallery placed " + placed + " of " + (to - from + 1));
                }
                if (p.containsKey("row")) {
                    //row=from,to,spacing  transports from..to of the registry one after another on one TiM rail line
                    //or row=spacing:index;index;...  just those
                    java.util.List<Integer> ids = new java.util.ArrayList<>();
                    int sp2;
                    if (p.getProperty("row").contains(":")) {
                        String[] g = p.getProperty("row").split(":");
                        sp2 = Integer.parseInt(g[0]);
                        for (String n : g[1].split(";")) ids.add(Integer.parseInt(n.trim()));
                    } else {
                        String[] g = p.getProperty("row").split(",");
                        int from = Integer.parseInt(g[0]), to = Math.min(Integer.parseInt(g[1]), TiMGenericRegistry.TRANSPORTS.size() - 1);
                        sp2 = Integer.parseInt(g[2]);
                        for (int n = from; n <= to; n++) ids.add(n);
                    }
                    run(level, sp, "traincraft:rail@0,0,0@" + (ids.size() * sp2 + 10) + "@x");
                    for (int k2 = 0; k2 < ids.size(); k2++) {
                        var id = BuiltInRegistries.ENTITY_TYPE.getKey(TiMGenericRegistry.TRANSPORTS.get(ids.get(k2)).get());
                        int x = 5 + k2 * sp2;
                        run(level, sp, id + "@" + x + ",0,0@-4");
                        rowNames.add(id.getPath());
                        rowX.add(x);
                    }
                }
                if (p.containsKey("hidegui")) mc.options.hideGui = true;
                //fov=degrees  a narrow view from far away draws nearly without perspective, for measuring
                if (p.containsKey("fov")) mc.options.fov().set(Integer.parseInt(p.getProperty("fov")));
                if (p.containsKey("cam")) {
                    String[] c = p.getProperty("cam").split(",");
                    sp.getAbilities().flying = true;
                    sp.onUpdateAbilities();
                    sp.teleportTo(level, base.getX() + Double.parseDouble(c[0]), base.getY() + Double.parseDouble(c[1]), base.getZ() + Double.parseDouble(c[2]),
                            Float.parseFloat(c[3]), Float.parseFloat(c[4]));
                }
            });
        }
        int start = i("start", 60);
        if (tick == start - 20 && train != null && i("seat", 0) == 1) {
            server.execute(() -> log("seat " + sp.startRiding(train)));
            if (i("view", 1) == 3) {
                mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            }
        }
        if (tick == 30 && p.containsKey("craft")) {
            //craft=table:transport-or-item:take  fills the table's inputs from the recipe of the given result,
            //lists the outputs, takes the first one and shows what is left in the inputs
            server.execute(() -> {
                String[] a = p.getProperty("craft").split(";");
                for (String c : a) {
                    String[] x = c.split("@");
                    var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("traincraft:block." + x[0]));
                    BlockPos at = base.offset(-3, 0, 2 + java.util.Arrays.asList(a).indexOf(c) * 2);
                    level.setBlock(at, block.defaultBlockState(), 3);
                    var te = (ebf.tim.blocks.TileEntityStorage) level.getBlockEntity(at);
                    var result = new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(x[1])));
                    var recipe = ebf.tim.utility.RecipeManager.getRecipe(result, x[0]);
                    log("craft " + c + " recipe " + (recipe == null ? "NONE" : recipe.input.stream().map(l -> l == null ? "-" : l.get(0).toString()).toList()));
                    if (recipe == null) continue;
                    int i = 0;
                    for (var slot : te.inventory) {
                        if (!slot.isCraftingInput()) continue;
                        if (i < recipe.input.size() && recipe.input.get(i) != null) slot.setStack(recipe.input.get(i).get(0).copyWithCount(recipe.input.get(i).get(0).getCount() * 2));
                        i++;
                    }
                    te.markDirty();
                    log("outputs " + te.inventory.stream().filter(ebf.tim.utility.ItemStackSlot::isCraftingOutput).map(s2 -> s2.getStackOrEmpty().toString()).toList());
                    var out = te.inventory.stream().filter(s2 -> s2.isCraftingOutput() && s2.getHasStack()).findFirst();
                    if (p.containsKey("tablegui") && x[0].equals(p.getProperty("tablegui"))) {
                        //show it with the inputs back in place
                        sp.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, pl) -> new ebf.tim.gui.TableMenu(id, inv, te), block.getName()), buf -> { buf.writeBlockPos(at); buf.writeUtf(te.table); });
                        continue;
                    }
                    if (out.isPresent()) {
                        var taken = out.get().getStackOrEmpty().copy();
                        out.get().setStack(net.minecraft.world.item.ItemStack.EMPTY);
                        te.onCrafting(taken);
                        log("took " + taken + ", inputs left " + te.inventory.stream().filter(ebf.tim.utility.ItemStackSlot::isCraftingInput).map(s2 -> s2.getStackOrEmpty().toString()).toList());
                    }
                }
            });
        }
        if (tick == 30 && p.containsKey("machine")) {
            //machine=kind|slot:item:count,...|gui   places the machine, fills its slots, logs it every 100 ticks
            server.execute(() -> {
                String[] a = p.getProperty("machine").split("\\|");
                //kind[@x,y,z]: the position defaults to beside the spawn
                BlockPos at = base.offset(-3, 0, 2);
                if (a[0].contains("@")) {
                    String[] xyz = a[0].split("@")[1].split(",");
                    at = base.offset(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
                    a[0] = a[0].split("@")[0];
                }
                level.setBlock(at, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("traincraft:block." + a[0])).defaultBlockState(), 3);
                var te = (MachineTile) level.getBlockEntity(at);
                if (a.length > 1) for (String gv : a[1].split(",")) {
                    String[] g = gv.split(":");
                    te.slot(Integer.parseInt(g[0])).setStack(new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(g[1] + ":" + g[2])), Integer.parseInt(g[3])));
                }
                if (a.length > 2 && a[2].equals("power")) level.setBlock(level.getBlockState(at.above()).isAir() ? at.above() : at.below(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
                if (a.length > 2 && a[2].equals("power")) te.powered = true;
                machineAt = at;
                if (p.containsKey("machinegui")) {
                    sp.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, pl) -> new MachineMenu(id, inv, te), level.getBlockState(at).getBlock().getName()),
                            buf -> { buf.writeBlockPos(te.getBlockPos()); buf.writeUtf(te.kind); });
                }
            });
        }
        if (p.containsKey("lanterngui") && tick == Integer.parseInt(p.getProperty("lanterngui"))) {
            //the colour screen of the first lantern of the use= list
            for (String use : p.getProperty("use", "").split(";")) {
                if (!use.startsWith("traincraft:block.lantern")) continue;
                String[] xyz = use.split("@")[1].split(",");
                if (mc.level.getBlockEntity(base.offset(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]))) instanceof train.blocks.lantern.LanternTile lt) {
                    mc.setScreen(new train.client.gui.LanternScreen(lt));
                }
                break;
            }
        }
        if (tick == 30 && p.containsKey("wear")) {
            //wear=item[#rrggbb],...  puts the clothes on (dyed if a colour is given)
            server.execute(() -> {
                for (String w : p.getProperty("wear").split(",")) {
                    String[] a = w.split("#");
                    var stack = new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(a[0])));
                    if (a.length > 1) stack.set(net.minecraft.core.component.DataComponents.DYED_COLOR,
                            new net.minecraft.world.item.component.DyedItemColor(Integer.parseInt(a[1], 16), true));
                    sp.setItemSlot(sp.getEquipmentSlotForItem(stack), stack);
                    log("wear " + stack + " in " + sp.getEquipmentSlotForItem(stack));
                }
            });
        }
        if (tick == 25 && p.containsKey("inv")) {
            //inv=item[#rrggbb],...  into the player's inventory from slot 9 on (the main rows)
            server.execute(() -> {
                String[] g = p.getProperty("inv").split(",");
                for (int n = 0; n < g.length; n++) {
                    String[] a = g[n].split("#");
                    var stack = new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(a[0])));
                    if (a.length > 1) stack.set(net.minecraft.core.component.DataComponents.DYED_COLOR,
                            new net.minecraft.world.item.component.DyedItemColor(Integer.parseInt(a[1], 16), true));
                    sp.getInventory().setItem(9 + n, stack);
                }
            });
        }
        if (p.containsKey("lamp") && train != null) {
            //lamp=tick[,tick...]  switches the driven transport's lamps
            for (String l : p.getProperty("lamp").split(",")) {
                if (tick == Integer.parseInt(l.trim())) server.execute(() -> train.interact(sp, false, false, 5));
            }
        }
        if (p.containsKey("lightscan") && base != null && tick % 20 == 0) {
            //lightscan=1  counts the light blocks around the spawn
            server.execute(() -> {
                StringBuilder b = new StringBuilder();
                int n = 0;
                for (BlockPos q : BlockPos.betweenClosed(base.offset(-10, -2, -10), base.offset(70, 4, 10))) {
                    var st = level.getBlockState(q);
                    if (st.is(Blocks.LIGHT)) {
                        n++;
                        b.append(" ").append(q.getX() - base.getX()).append(",").append(q.getY() - base.getY()).append(",").append(q.getZ() - base.getZ())
                                .append("=").append(st.getValue(net.minecraft.world.level.block.LightBlock.LEVEL));
                    }
                }
                log("lights t=" + tick + " n=" + n + b + (train == null ? "" : String.format(" train x=%.1f", train.getX() - base.getX())));
            });
        }
        if (p.containsKey("horn") && train != null) {
            //horn=tick[,tick...]
            for (String h : p.getProperty("horn").split(",")) {
                if (tick == Integer.parseInt(h.trim())) server.execute(() -> train.interact(sp, false, false, 9));
            }
        }
        if (p.containsKey("jukebox") && train instanceof EntityJukeBoxCart jb) {
            //jukebox=url  plays it from tick 40; logs the client player every 40 ticks
            if (tick == 40) server.execute(() -> jb.recievePacket(p.getProperty("jukebox"), true));
            if (p.containsKey("jukeboxgui") && tick == Integer.parseInt(p.getProperty("jukeboxgui"))
                    && mc.level.getEntity(jb.getId()) instanceof EntityJukeBoxCart cj) {
                EntityJukeBoxCart.openScreen.accept(cj);
            }
            if (tick % 40 == 0 && mc.level.getEntity(jb.getId()) instanceof EntityJukeBoxCart cj) {
                log("jukebox t=" + tick + " playing=" + cj.isPlaying() + " url=" + cj.getStreamURL() + " " + JukeboxClient.status(cj));
            }
        }
        if (p.containsKey("rclick") && tick == Integer.parseInt(p.getProperty("rclick").split("@")[1])) {
            //rclick=index@tick[@sneak]  the player right clicks that placed transport (empty hand); logs what opened
            String[] r = p.getProperty("rclick").split("@");
            GenericRailTransport t = placed.get(Integer.parseInt(r[0]));
            server.execute(() -> {
                sp.setShiftKeyDown(r.length > 2);
                var res = t.interact(sp, net.minecraft.world.InteractionHand.MAIN_HAND);
                sp.setShiftKeyDown(false);
                log("rclick " + t.getClass().getSimpleName() + " sneak=" + (r.length > 2) + " -> " + res + " menu=" + sp.containerMenu.getClass().getSimpleName()
                        + " riding=" + (sp.getVehicle() == t));
            });
        }
        if (p.containsKey("load") && tick == 40) {
            //load=index:count|index:count  fills that many inventory slots of those placed transports with logs (on
            //the server only, no screen opened: what the clients see of it comes through the synced cargo count)
            server.execute(() -> {
                for (String q : p.getProperty("load").split("\\|")) {
                    String[] a = q.split(":");
                    GenericRailTransport t = placed.get(Integer.parseInt(a[0]));
                    int n = Integer.parseInt(a[1]), done = 0;
                    for (var s : t.inventory) {
                        if (done >= n) break;
                        s.setStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_LOG, 64));
                        done++;
                    }
                    log("load " + t.getClass().getSimpleName() + " slots " + t.inventory.size() + " filled " + done + " cargo " + t.cargoCount());
                }
            });
        }
        if (p.containsKey("fuelall")) {
            //fuelall=1  fuel for every placed locomotive at 30, engines on at 40 (no driving)
            if (tick == 30) server.execute(() -> {
                for (GenericRailTransport t : placed) {
                    if (t.getTypes().contains(ebf.tim.TrainsInMotion.transportTypes.DIESEL)) put(t, 400, new net.minecraft.world.item.ItemStack(ebf.tim.registry.TiMItems.dieselCanister.get(), 16));
                }
            });
            if (tick == 40) server.execute(() -> {
                for (GenericRailTransport t : placed) if (t instanceof EntityTrainCore && t != train && !t.getBoolean(GenericRailTransport.boolValues.RUNNING)) t.interact(sp, false, false, 8);
            });
        }
        if (p.containsKey("fleet") && base != null) {
            //fleet=accel,startTick  every locomotive placed (gallery) gets its fuel at 30, starts at 40 and drives
            //from startTick on; logs at the end what each did
            String[] f = p.getProperty("fleet").split(",");
            int go = Integer.parseInt(f[1]);
            //fleet=accel,startTick,coastTick  the throttle is closed again at coastTick (rolling on at a crawl)
            //fleet=accel,startTick,coastTick,reverseTick  from reverseTick the throttle the other way
            //...,reverseTick,backAgainTick  and from backAgainTick the first way again
            int accel = f.length > 4 && tick >= Integer.parseInt(f[4]) ? Integer.parseInt(f[0])
                    : f.length > 3 && tick >= Integer.parseInt(f[3]) ? -Integer.parseInt(f[0])
                    : f.length > 2 && tick >= Integer.parseInt(f[2]) ? 0 : Integer.parseInt(f[0]);
            if (tick == 30) server.execute(() -> {
                //nofuel=index,index  those placed locomotives get nothing (they must not move however they are driven)
                java.util.Set<String> nofuel = java.util.Set.of(p.getProperty("nofuel", "").split(","));
                for (GenericRailTransport t : placed) {
                    if (!(t instanceof EntityTrainCore)) continue;
                    if (nofuel.contains(String.valueOf(placed.indexOf(t)))) {
                        fleetStart.put(t, t.position());
                        continue;
                    }
                    var types = t.getTypes();
                    if (types.contains(ebf.tim.TrainsInMotion.transportTypes.STEAM)) {
                        put(t, 400, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, 64));
                        put(t, 401, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
                    } else if (types.contains(ebf.tim.TrainsInMotion.transportTypes.DIESEL)) {
                        put(t, 400, new net.minecraft.world.item.ItemStack(ebf.tim.registry.TiMItems.dieselCanister.get(), 16));
                    } else if (types.contains(ebf.tim.TrainsInMotion.transportTypes.ELECTRIC) && !p.containsKey("nocharge")) {
                        //nocharge=1  electrics get nothing: they run on what the wire gives them
                        put(t, 400, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.REDSTONE_BLOCK, 16));
                    }
                    fleetStart.put(t, t.position());
                }
            });
            if (tick == 40) server.execute(() -> {
                for (GenericRailTransport t : placed) if (t instanceof EntityTrainCore) t.interact(sp, false, false, 8);
            });
            if (tick >= go && (tick - go) % 40 == 0) server.execute(() -> {
                //steam ones only run once the boiler is hot: ask again until they do
                for (GenericRailTransport t : placed) {
                    if (t instanceof EntityTrainCore c && c.getBoolean(GenericRailTransport.boolValues.RUNNING)) c.accelerator = accel;
                    else if (t instanceof EntityTrainCore) t.interact(sp, false, false, 8);
                }
            });
            //water as it is used up (steam), one bucket at a time like a player would
            if (tick % 40 == 0) server.execute(() -> {
                for (GenericRailTransport t : placed) {
                    if (java.util.Set.of(p.getProperty("nofuel", "").split(",")).contains(String.valueOf(placed.indexOf(t)))) continue;
                    if (t.getTypes().contains(ebf.tim.TrainsInMotion.transportTypes.STEAM)) {
                        var slot = t.getSlotIndexByID(401);
                        if (slot != null && !slot.getHasStack()) slot.setStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
                    }
                }
            });
            if (p.containsKey("trackpath") && tick % 5 == 0 && tick >= go) server.execute(() -> {
                //trackpath=1  where each locomotive's front bogie is, and on what
                for (GenericRailTransport t : placed) {
                    if (!(t instanceof EntityTrainCore) || t.frontBogie == null) continue;
                    String st = t.frontBogie.debugState();
                    String on = st.substring(st.lastIndexOf(' ', st.indexOf(" last=") > 0 ? st.indexOf(" last=") - 1 : st.length() - 1) + 1).replace(")", "");
                    log(String.format("path %d z=%.0f x=%.2f dz=%.2f y=%.2f derailed=%s v=%.3f %s", tick, t.getZ() - base.getZ(), t.getX() - base.getX(),
                            t.frontBogie.posZ - base.getZ(), t.frontBogie.posY - base.getY(), t.getBoolean(GenericRailTransport.boolValues.DERAILED), t.getVelocity(), st.substring(st.indexOf("v=") + 2).replaceAll("^[-0-9,.]+ ", "")));
                }
            });
            if (tick == i("ticks", 200) - 2) server.execute(() -> {
                int moved = 0, total = 0;
                for (GenericRailTransport t : placed) {
                    if (!(t instanceof EntityTrainCore c)) continue;
                    total++;
                    var from = fleetStart.get(t);
                    double d = from == null ? 0 : from.distanceTo(t.position());
                    if (d > 1) moved++;
                    log(String.format("fleet %-28s ticks=%d entityTicking=%s moved=%5.1f running=%s derailed=%s v=%.3f boiler=%d tank=%s removed=%s",
                            BuiltInRegistries.ENTITY_TYPE.getKey(t.getType()).getPath(), t.tickCount, level.isPositionEntityTicking(t.blockPosition()), d, c.getBoolean(GenericRailTransport.boolValues.RUNNING),
                            c.getBoolean(GenericRailTransport.boolValues.DERAILED), c.getVelocity(), c.entityData().get(GenericRailTransport.BOILER_HEAT) / 100,
                            java.util.Arrays.stream(c.getTankInfo() == null ? new GenericRailTransport.FluidTankInfo[0] : c.getTankInfo())
                                    .map(x -> x == null || x.fluid == null ? "-" : x.fluid.getName() + "=" + x.fluid.amount).toList(), c.isRemoved()));
                }
                log("fleet moved " + moved + " of " + total);
            });
        }
        if (tick == 25 && p.containsKey("railscan")) server.execute(() -> {
            //railscan=1  every classic track block near the spawn: position, kind, its piece and facing
            StringBuilder b = new StringBuilder();
            for (BlockPos q : BlockPos.betweenClosed(base.offset(-5, -1, -40), base.offset(40, 3, 180))) {
                var be = level.getBlockEntity(q);
                if (be instanceof train.common.tile.TileTCRail r) {
                    b.append(String.format(" %d,%d,%d:%s/%d", q.getX() - base.getX(), q.getY() - base.getY(), q.getZ() - base.getZ(), r.getTrackName(), r.getFacing()));
                    if (p.getProperty("railscan").equals("2")) b.append(String.format("[meta=%d on=%s r=%.2f c=%.2f,%.2f]", r.getBlockMeta(), r.getSwitchState(), r.r,
                            r.cx - base.getX(), r.cz - base.getZ()));
                } else if (be instanceof train.common.tile.TileTCRailGag) {
                    b.append(String.format(" %d,%d,%d:gag", q.getX() - base.getX(), q.getY() - base.getY(), q.getZ() - base.getZ()));
                }
            }
            log("railscan" + b);
        });
        if (tick == 30 && p.containsKey("roofscan")) {
            roofScan(mc);
        }
        if (tick == 30 && p.containsKey("measure")) {
            measureAll(mc);
        }
        if (tick == 100 && p.containsKey("camsmoke")) {
            //camsmoke=dist  the camera that far beside the first chimney of the first locomotive here, looking at it
            for (var e : mc.level.entitiesForRendering()) {
                if (!(e instanceof EntityTrainCore t)) continue;
                var at = TrainSmoke.worldSmokePoint(t);
                if (at == null) continue;
                double d = Double.parseDouble(p.getProperty("camsmoke"));
                double cx = at.x, cy = at.y + 0.4, cz = at.z - d;
                server.execute(() -> sp.connection.teleport(cx, cy - 1.62, cz, 0, 8));
                log(String.format("camsmoke %s at %.2f,%.2f,%.2f", t.getClass().getSimpleName(), at.x, at.y, at.z));
                break;
            }
        }
        if (tick == 30 && p.containsKey("shapecheck")) {
            shapeCheck(mc);
        }
        if (tick == 30 && p.containsKey("smokecheck")) {
            smokeCheck(mc);
        }
        if (tick == 30 && p.containsKey("recipeaudit")) {
            server.execute(() -> recipeAudit(level));
        }
        if (p.containsKey("bell") && tick == Integer.parseInt(p.getProperty("bell")) && train != null) {
            server.execute(() -> train.interact(sp, false, false, 17));
        }
        if (tick == 25 && p.containsKey("hand")) {
            //hand=item  in the main hand
            server.execute(() -> sp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(p.getProperty("hand"))))));
        }
        if (p.containsKey("toggle") && tick == Integer.parseInt(p.getProperty("toggle").split("@")[1])) {
            //toggle=x,y,z@tick  right clicks the block there (a switch stand)
            server.execute(() -> {
                String[] xyz = p.getProperty("toggle").split("@")[0].split(",");
                BlockPos at = base.offset(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
                var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(at), net.minecraft.core.Direction.UP, at, false);
                log("toggle " + at + " -> " + level.getBlockState(at).useWithoutItem(level, sp, hit));
            });
        }
        if (p.containsKey("probe") && tick % 40 == 0 && base != null) {
            //probe=x,y,z;...  logs switch/signal strengths and classic switch track states there
            server.execute(() -> {
                StringBuilder b = new StringBuilder();
                for (String pr : p.getProperty("probe").split(";")) {
                    String[] xyz = pr.split(",");
                    BlockPos at = base.offset(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
                    var be = level.getBlockEntity(at);
                    b.append(" [").append(pr).append(" ").append(be == null ? "-" : be.getClass().getSimpleName());
                    if (be instanceof ebf.tim.blocks.TileSwitch t) b.append(" strength=").append(t.getStrength()).append(" facing=").append(t.facing());
                    if (be instanceof train.common.tile.TileTCRail r) b.append(" ").append(r.getTrackName()).append(" switch=").append(r.getSwitchState());
                    b.append("]");
                }
                log("probe t=" + tick + b);
            });
        }
        if (tick == 40 && p.containsKey("viewfront")) {
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
        }
        if (p.containsKey("suittest")) {
            //suittest=1  the composite suit against poison, blindness and fire; logs what is left and the wear
            if (tick == 50) server.execute(() -> {
                sp.setGameMode(GameType.SURVIVAL);
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 400));
                sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 400));
                sp.igniteForSeconds(10);
                //no natural regeneration, so only the chest piece heals
                server.setDifficulty(Difficulty.NORMAL, true);
                level.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, server);
                sp.setHealth(14);
                log("suittest start health=" + sp.getHealth() + " effects=" + sp.getActiveEffects() + " fire=" + sp.getRemainingFireTicks());
            });
            if (tick == 60 || tick == 260) server.execute(() -> {
                StringBuilder b = new StringBuilder();
                for (var s : sp.getArmorSlots()) b.append(" ").append(s.getItem()).append(" dmg=").append(s.getDamageValue()).append("/").append(s.getMaxDamage());
                log("suittest t=" + tick + " health=" + sp.getHealth() + " effects=" + sp.getActiveEffects() + " fire=" + sp.getRemainingFireTicks() + b);
            });
            if (tick == 70) server.execute(() -> {
                //a 20 block fall: the boots take it
                sp.teleportTo(sp.getX(), sp.getY() + 20, sp.getZ());
                sp.fallDistance = 0;
            });
        }
        if (p.containsKey("book") && tick == Integer.parseInt(p.getProperty("book").split(":")[0])) {
            //book=tick[:page]  opens the recipe book
            String[] a = p.getProperty("book").split(":");
            ebf.tim.gui.GUICraftBook.page = a.length > 1 ? Integer.parseInt(a[1]) : 0;
            mc.setScreen(new ebf.tim.gui.GUICraftBook());
        }
        if (tick == 30 && p.containsKey("bench")) {
            //bench=item,-,item,...  (9 grid slots) places a train workbench, fills it and opens it
            server.execute(() -> {
                BlockPos at = base.offset(-3, 0, 2);
                level.setBlock(at, TCBlocks.PART_TABLE.get().defaultBlockState(), 3);
                var te = (BlockTrainWorkbench.Tile) level.getBlockEntity(at);
                String[] g = p.getProperty("bench").split(",");
                for (int n = 0; n < 9 && n < g.length; n++) {
                    if (!g[n].equals("-")) {
                        String[] a = g[n].split("#");
                        var stack = new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(a[0])));
                        if (a.length > 1) stack.set(net.minecraft.core.component.DataComponents.DYED_COLOR,
                                new net.minecraft.world.item.component.DyedItemColor(Integer.parseInt(a[1], 16), true));
                        te.items.set(n, stack);
                    }
                }
                te.setChanged();
                sp.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, pl) -> new TrainWorkbenchMenu(id, inv, te.items, te,
                        net.minecraft.world.inventory.ContainerLevelAccess.create(level, at)), net.minecraft.network.chat.Component.translatable("container.traincraft.trainworkbench")));
                log("bench result " + sp.containerMenu.getSlot(0).getItem() + " color=" + sp.containerMenu.getSlot(0).getItem().get(net.minecraft.core.component.DataComponents.DYED_COLOR));
            });
        }
        if (machineAt != null && tick % 100 == 0) {
            server.execute(() -> {
                if (level.getBlockEntity(machineAt) instanceof MachineTile te) {
                    log("machine t=" + tick + " lit=" + level.getBlockState(machineAt).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT) + " burn=" + te.burnTime + " cook=" + te.cookTime
                            + " tank=" + (te.tank == null ? "-" : te.tank.getName() + ":" + te.tank.amount) + " energy=" + te.energy.getEnergyStored()
                            + " slots=" + te.inventory.stream().map(s2 -> s2.getSlotID() + "=" + s2.getStackOrEmpty()).toList());
                }
            });
        }
        if (tick == start - 15 && p.containsKey("give") && train != null) {
            //give=slotId:item:count;... into the driven transport's inventory
            server.execute(() -> {
                for (String gv : p.getProperty("give").split(";")) {
                    String[] a = gv.split(":");
                    var slot = train.getSlotIndexByID(Integer.parseInt(a[0]));
                    var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(a[1] + ":" + a[2]));
                    if (slot != null) slot.setStack(new net.minecraft.world.item.ItemStack(item, Integer.parseInt(a[3])));
                    log("give " + gv + " -> " + (slot == null ? "no slot" : slot.getStackOrEmpty()));
                }
            });
        }
        if (p.containsKey("gui") && tick == Integer.parseInt(p.getProperty("gui")) && train != null) {
            server.execute(() -> train.interact(sp, false, false, 1));
        }
        if (train != null && base != null && tick % 20 == 0 && train.getTankCapacity() != null && p.containsKey("tanks")) {
            StringBuilder b = new StringBuilder();
            for (var t : train.getTankInfo()) b.append(t == null || t.fluid == null ? " -" : " " + t.fluid.getName() + "=" + t.fluid.amount + "/" + t.capacity);
            log("tanks t=" + tick + b + " running=" + train.getBoolean(GenericRailTransport.boolValues.RUNNING)
                    + " boiler=" + train.entityData().get(GenericRailTransport.BOILER_HEAT) + " slots=" + train.inventory.stream().map(x -> x.getSlotID() + ":" + x.getStackOrEmpty()).toList());
        }
        if (tick == start - 10 && p.containsKey("couple")) {
            //open the couplers of everything placed
            server.execute(() -> {
                for (GenericRailTransport t : placed) t.interact(sp, false, false, 7);
                log("couplers open on " + placed.size());
            });
        }
        if (p.containsKey("stop") && tick == Integer.parseInt(p.getProperty("stop")) && train instanceof EntityTrainCore core) {
            //stop=tick  throttle off and the brake on
            server.execute(() -> {
                core.accelerator = 0;
                if (!core.getBoolean(GenericRailTransport.boolValues.BRAKE)) core.interact(sp, false, false, 15);
                log("stop");
            });
        }
        if (p.containsKey("reverse") && tick == Integer.parseInt(p.getProperty("reverse")) && train instanceof EntityTrainCore) {
            server.execute(() -> {
                ((EntityTrainCore) train).accelerator = -((EntityTrainCore) train).accelerator;
                log("reverse " + ((EntityTrainCore) train).accelerator);
            });
        }
        if (tick == start && train instanceof EntityTrainCore && p.containsKey("drive")) {
            server.execute(() -> {
                train.interact(sp, false, false, 8); //engine
                int acc = i("drive", 0);
                ((EntityTrainCore) train).accelerator = acc;
                log("drive " + acc + " running=" + train.getBoolean(GenericRailTransport.boolValues.RUNNING));
            });
        }
        int every = i("log", 10);
        if (train != null && base != null && tick >= 20 && tick % every == 0 && placed.size() > 1) {
            StringBuilder b = new StringBuilder();
            for (GenericRailTransport t : placed) {
                b.append(String.format(" [%s x=%.2f z=%.2f v=%.3f f=%s b=%s c=%d]", t.getClass().getSimpleName().replace("Entity", ""),
                        t.getX() - base.getX(), t.getZ() - base.getZ(), t.getVelocity(), t.frontLinkedID, t.backLinkedID, t.getConsist().size()));
            }
            log("consist t=" + tick + b);
            //what the client draws: the rendered (interpolated) positions of the same transports
            StringBuilder cb = new StringBuilder();
            float pt = mc.getTimer().getGameTimeDeltaPartialTick(false);
            for (GenericRailTransport t : placed) {
                var ce = mc.level.getEntity(t.getId());
                cb.append(String.format(" x=%.3f", ce == null ? Double.NaN : net.minecraft.util.Mth.lerp(pt, ce.xo, ce.getX()) - base.getX()));
            }
            log("client t=" + tick + cb);
        }
        if (train != null && base != null && tick >= 20 && tick % every == 0) {
            GenericRailTransport client = (GenericRailTransport) mc.level.getEntity(train.getId());
            log(String.format("t=%d pos=(%.2f,%.2f,%.2f) yaw=%.1f pitch=%.1f v=%.3f acc=%d derail=%s front=%s back=%s client=%s",
                    tick, train.getX() - base.getX(), train.getY() - base.getY(), train.getZ() - base.getZ(), train.getYRot(), train.getXRot(),
                    train.getVelocity(), train.getAccelerator(), train.getBoolean(GenericRailTransport.boolValues.DERAILED),
                    train.frontBogie == null ? "-" : train.frontBogie.debugState(), train.backBogie == null ? "-" : train.backBogie.debugState(),
                    client == null ? "none" : String.format("(%.2f,%.2f,%.2f yaw=%.1f yOff=%s)", client.getX() - base.getX(), client.getY() - base.getY(), client.getZ() - base.getZ(), client.getYRot(),
                            client.backBogie == null ? "-" : String.format("%.4f", client.backBogie.yOffset))));
        }
        if (p.containsKey("drift") && base != null) {
            //drift=tick  how far each placed transport went from where it was put (nothing drives them), at that tick
            if (tick == 21) server.execute(() -> { for (GenericRailTransport t : placed) driftStart.put(t, t.position()); });
            if (tick == Integer.parseInt(p.getProperty("drift"))) server.execute(() -> {
                int n = 0;
                for (GenericRailTransport t : placed) {
                    var from = driftStart.get(t);
                    double d = from == null ? -1 : from.distanceTo(t.position());
                    if (d > 0.05) {
                        n++;
                        log(String.format("drift %-34s %.2f derailed=%s pitch=%.1f riders=%s", BuiltInRegistries.ENTITY_TYPE.getKey(t.getType()).getPath(), d,
                                t.getBoolean(GenericRailTransport.boolValues.DERAILED), t.getXRot(), t.getPassengers().size()));
                    }
                }
                log("drift: " + n + " of " + placed.size() + " moved; player rides " + (sp.getVehicle() == null ? "nothing" : sp.getVehicle().getType()));
            });
        }
        if (p.containsKey("row") && p.containsKey("rowshots") && !rowNames.isEmpty()) {
            //rowshots=every  the camera goes from one transport of the row to the next, a picture of each from the side
            int rowEvery = Integer.parseInt(p.getProperty("rowshots")), k = (tick - 40) / rowEvery, phase = (tick - 40) % rowEvery;
            if (tick >= 40 && k < rowNames.size()) {
                if (phase == 0) {
                    int x = rowX.get(k);
                    server.execute(() -> {
                        sp.getAbilities().flying = true;
                        sp.onUpdateAbilities();
                        //rowdist=blocks  how far from the track the camera stands
                        double dist = Double.parseDouble(p.getProperty("rowdist", "6.5"));
                        sp.teleportTo(level, base.getX() + x + 0.5, base.getY() + Double.parseDouble(p.getProperty("rowheight", "0")), base.getZ() - dist,
                                0, Float.parseFloat(p.getProperty("rowpitch", "6")));
                    });
                }
                if (phase == rowEvery - 3) {
                    String name = rowNames.get(k);
                    Screenshot.grab(mc.gameDirectory, "row-" + name + ".png", mc.getMainRenderTarget(), c -> {});
                }
            }
        }
        if (p.containsKey("roof") && base != null) {
            //roof=index,tick  the player (survival, not flying) is dropped onto that placed transport and stays; where
            //they are on it (along, across, height) is logged every 10 ticks, as long as they are near it
            String[] r = p.getProperty("roof").split(",");
            int t0 = Integer.parseInt(r[1]);
            if (tick == t0 && r.length > 3) {
                //roof=index,tick,frameFrom,frameTo  also the camera every frame between those ticks
                int f0 = Integer.parseInt(r[2]), f1 = Integer.parseInt(r[3]);
                NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RenderFrameEvent.Post e) -> {
                    if (tick < f0 || tick > f1 || mc.player == null) return;
                    var cam = mc.gameRenderer.getMainCamera();
                    float pt = e.getPartialTick().getGameTimeDeltaPartialTick(false);
                    var ct = mc.level.getEntity(placed.get(Integer.parseInt(r[0])).getId());
                    log(String.format("frame t=%d pt=%.2f yaw=%.3f x=%.4f z=%.4f trainx=%.4f trainz=%.4f turned=%.2f", tick, pt, cam.getYRot(), cam.getPosition().x - base.getX(),
                            cam.getPosition().z - base.getZ(), ct == null ? 0 : Mth.lerp(pt, ct.xo, ct.getX()) - base.getX(), ct == null ? 0 : Mth.lerp(pt, ct.zo, ct.getZ()) - base.getZ(),
                            RoofRideClient.turned));
                });
            }
            if (tick >= t0 && placed.size() > Integer.parseInt(r[0])) {
                var t = placed.get(Integer.parseInt(r[0]));
                if (tick == t0) server.execute(() -> {
                    sp.setGameMode(GameType.SURVIVAL);
                    sp.getAbilities().flying = false;
                    sp.onUpdateAbilities();
                    sp.connection.teleport(t.getX(), t.getY() + 3, t.getZ(), 0, 0);
                });
                if (tick % 10 == 0) {
                    var ct = mc.level.getEntity(t.getId());
                    if (ct != null) {
                        double a = Math.toRadians(ct.getYRot()), ux = Math.cos(a), uz = Math.sin(a);
                        double ox = mc.player.getX() - ct.getX(), oz = mc.player.getZ() - ct.getZ();
                        log(String.format("roof t=%d vehicle=%s along=%.2f across=%.2f up=%.2f onGround=%s trainYaw=%.1f playerYaw=%.1f", tick, mc.player.getVehicle(), ox * ux + oz * uz,
                                -ox * uz + oz * ux, mc.player.getY() - ct.getY(), mc.player.onGround(), ct.getYRot(), mc.player.getYRot()));
                    }
                }
            }
        }
        if (p.containsKey("stand") && base != null) {
            //stand=x,z,tick  the player stands there (survival, on the ground), seen from the front (F5 twice)
            String[] st = p.getProperty("stand").split(",");
            int t0 = Integer.parseInt(st[2]);
            if (tick == t0) server.execute(() -> {
                sp.setGameMode(GameType.SURVIVAL);
                sp.getAbilities().flying = false;
                sp.onUpdateAbilities();
                sp.connection.teleport(base.getX() + Double.parseDouble(st[0]), base.getY() + 0.5, base.getZ() + Double.parseDouble(st[1]), 0, -50);
            });
            if (tick == t0 + 2) {
                mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
                mc.options.hideGui = true;
            }
            if (tick == t0 + 30) log(String.format("stand: feet at y=%.4f above the ground, onGround=%s", mc.player.getY() - base.getY(), mc.player.onGround()));
        }
        if (p.containsKey("breaktest") && base != null) {
            //breaktest=tick  a survival player hits every placed transport (its collision part, as a click does) once
            //every 12 ticks, 8 times; breakat=x,y,z;... blocks it then breaks by hand; then the items on the ground
            int t0 = Integer.parseInt(p.getProperty("breaktest"));
            if (tick == t0) server.execute(() -> {
                sp.setGameMode(GameType.SURVIVAL);
                log("survival");
            });
            if (tick > t0 && tick <= t0 + 96 && (tick - t0) % 12 == 0) server.execute(() -> {
                for (GenericRailTransport t : placed) {
                    if (t.isRemoved()) continue;
                    var parts = t.getParts();
                    net.minecraft.world.entity.Entity target = parts != null && parts.length > 0 ? parts[0] : t;
                    sp.teleportTo(t.getX(), t.getY(), t.getZ() - 2.5);
                    sp.resetAttackStrengthTicker();
                    sp.attack(target);
                    log(String.format("hit %s via %s removed=%s", t.getClass().getSimpleName(), target.getClass().getSimpleName(), t.isRemoved()));
                }
            });
            if (tick == t0 + 110 && p.containsKey("breakat")) server.execute(() -> {
                for (String q : p.getProperty("breakat").split(";")) {
                    String[] c = q.split(",");
                    BlockPos at = base.offset(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
                    var before = level.getBlockState(at).getBlock();
                    sp.teleportTo(at.getX() + 0.5, at.getY(), at.getZ() - 1.5);
                    boolean ok = sp.gameMode.destroyBlock(at);
                    log("break " + q + " " + before + " -> " + ok + " now " + level.getBlockState(at).getBlock());
                }
            });
            if (tick == t0 + 140) server.execute(() -> {
                var items = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(base).inflate(60));
                java.util.Map<String, Integer> sum = new java.util.TreeMap<>();
                for (var it : items) sum.merge(BuiltInRegistries.ITEM.getKey(it.getItem().getItem()).toString(), it.getItem().getCount(), Integer::sum);
                log("dropped " + sum);
                long left = level.getEntitiesOfClass(GenericRailTransport.class, new net.minecraft.world.phys.AABB(base).inflate(60)).size();
                log("transports left " + left);
            });
        }
        if (p.containsKey("tickcheck") && base != null && tick == Integer.parseInt(p.getProperty("tickcheck"))) {
            //tickcheck=tick  how many ticks each placed transport has lived, and whether its spot is entity-ticking
            server.execute(() -> {
                for (GenericRailTransport t : placed) {
                    log(String.format("tickcheck %s tickCount=%d entityTicking=%s inLevel=%s removed=%s chunk=%s", t.getClass().getSimpleName(), t.tickCount,
                            level.isPositionEntityTicking(t.blockPosition()), level.getEntity(t.getId()) == t, t.isRemoved(),
                            level.getChunkSource().getChunkNow(t.blockPosition().getX() >> 4, t.blockPosition().getZ() >> 4) != null));
                }
            });
        }
        if (p.containsKey("feeder") && base != null && tick == 31) {
            //feeder=x,y,z>x,y,z  strings a feeder cable, substation to mast, with the item as a player clicks
            server.execute(() -> {
                var stack = new net.minecraft.world.item.ItemStack(TCBlocks.FEEDER_CABLE.get(), 16);
                sp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
                for (String q : p.getProperty("feeder").split(">")) {
                    String[] c = q.split(",");
                    BlockPos at = base.offset(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
                    var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(at), net.minecraft.core.Direction.UP, at, false);
                    log("feeder click " + q + " -> " + stack.useOn(new net.minecraft.world.item.context.UseOnContext(level, sp, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit)));
                }
            });
        }
        if (p.containsKey("wire") && base != null && tick == 30) {
            //wire=x,y,z>x,y,z>x,y,z...  strings the catenary wire mast to mast with the item, as a player clicks
            server.execute(() -> {
                var stack = new net.minecraft.world.item.ItemStack(TCBlocks.CATENARY_WIRE.get(), 64);
                sp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
                for (String q : p.getProperty("wire").split(">")) {
                    String[] c = q.split(",");
                    BlockPos at = base.offset(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
                    var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(at), net.minecraft.core.Direction.UP, at, false);
                    var r = stack.useOn(new net.minecraft.world.item.context.UseOnContext(level, sp, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit));
                    log("wire click " + q + " -> " + r);
                }
                //the cantilevers as made, and whether each span keeps over the track
                String[] qs = p.getProperty("wire").split(">");
                for (int i = 0; i < qs.length; i++) {
                    String[] c = qs[i].split(",");
                    BlockPos at = base.offset(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
                    if (!(level.getBlockEntity(at) instanceof CatenaryMastTile m)) continue;
                    String over = "";
                    if (i + 1 < qs.length) {
                        String[] d = qs[i + 1].split(",");
                        if (level.getBlockEntity(base.offset(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2]))) instanceof CatenaryMastTile n) {
                            over = " span to " + qs[i + 1] + " over track=" + CatenaryWireItem.overTrack(level, m.contactPoint(), n.contactPoint())
                                    + CatenaryWireItem.offPoints(level, m.contactPoint(), n.contactPoint());
                        }
                    }
                    var cp = m.contactPoint();
                    log(String.format("mast %s reach=%.2f contact=(%.2f,%.2f)%s", qs[i], m.reach, cp.x - base.getX(), cp.z - base.getZ(), over));
                }
            });
        }
        //feedmast=x,y,z,fe[,untilTick]  that block (a substation) gets fe every tick, a big generator beside it, until then
        String[] feedSpec = p.containsKey("feedmast") ? p.getProperty("feedmast").split(",") : null;
        if (feedSpec != null && base != null && (feedSpec.length < 5 || tick <= Integer.parseInt(feedSpec[4]))) {
            String[] f = feedSpec;
            server.execute(() -> {
                BlockPos at = base.offset(Integer.parseInt(f[0]), Integer.parseInt(f[1]), Integer.parseInt(f[2]));
                var cap = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, at, null);
                if (cap != null) cap.receiveEnergy(Integer.parseInt(f[3]), false);
            });
        }
        if (p.containsKey("wirelog") && base != null && tick % 5 == 0) {
            //wirelog=1  each electric: where, how far across the nearest contact wire is and how high over its rails,
            //the wire height its pantographs get, and whether it pulls
            server.execute(() -> {
                for (GenericRailTransport t : placed) {
                    if (!(t instanceof EntityTrainCore c) || t.frontBogie == null || !t.getTypes().contains(ebf.tim.TrainsInMotion.transportTypes.ELECTRIC)) continue;
                    double railTop = t.getY() + (t.frontBogie.yOffset + t.backBogie.yOffset) * 0.5;
                    double[] n = CatenaryMastTile.nearestSpan(level, t.getX(), railTop, t.getZ());
                    log(String.format("wire t=%d x=%.2f y=%.2f z=%.2f across=%.2f above=%.2f height=%.2f pull=%s v=%.3f", tick, t.getX() - base.getX(), railTop - base.getY(),
                            t.getZ() - base.getZ(), n[0], n[1], t.entityData().get(GenericRailTransport.WIRE_HEIGHT), c.hasTraction(), t.getVelocity()));
                }
            });
        }
        if (p.containsKey("catlog") && base != null && tick % 20 == 0) {
            //catlog=1  the energy at each mast (by x), and each placed transport's tank and engine
            server.execute(() -> {
                StringBuilder b = new StringBuilder();
                for (BlockPos q : BlockPos.betweenClosed(base.offset(-8, -1, -8), base.offset(140, 2, 8))) {
                    if (level.getBlockEntity(q) instanceof train.blocks.catenary.SubstationTile st) {
                        b.append(String.format(" sub%d=%d", q.getX() - base.getX(), st.energy.getEnergyStored()));
                    }
                }
                for (GenericRailTransport t : placed) {
                    b.append(String.format(" | %s x=%.1f v=%.3f run=%s pull=%s tank=%s", t.getClass().getSimpleName().replace("Entity", ""), t.getX() - base.getX(), t.getVelocity(),
                            t.getBoolean(GenericRailTransport.boolValues.RUNNING), t instanceof EntityTrainCore c ? c.hasTraction() : "-", t.getTankInfo() == null ? "-" : java.util.Arrays.stream(t.getTankInfo())
                                    .map(x -> x == null || x.fluid == null ? "0" : String.valueOf(x.fluid.amount)).toList()));
                }
                log("cat t=" + tick + b);
            });
        }
        if (p.containsKey("hitat") && base != null) {
            //hitat=tick:x,y,z:px,py,pz  the player stands at p (relative, survival) looking at x,y,z and attacks once
            for (String h : p.getProperty("hitat").split(";")) {
                String[] q = h.split(":");
                int at = Integer.parseInt(q[0]);
                String[] t = q[1].split(","), f = q[2].split(",");
                net.minecraft.world.phys.Vec3 target = net.minecraft.world.phys.Vec3.atLowerCornerOf(base).add(Double.parseDouble(t[0]), Double.parseDouble(t[1]), Double.parseDouble(t[2]));
                net.minecraft.world.phys.Vec3 feet = net.minecraft.world.phys.Vec3.atLowerCornerOf(base).add(Double.parseDouble(f[0]), Double.parseDouble(f[1]), Double.parseDouble(f[2]));
                if (tick == at) server.execute(() -> {
                    sp.setGameMode(GameType.SURVIVAL);
                    net.minecraft.world.phys.Vec3 d = target.subtract(feet.add(0, sp.getEyeHeight(), 0));
                    float yaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z)), pitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
                    sp.connection.teleport(feet.x, feet.y, feet.z, yaw, pitch);
                });
                if (tick == at + 10) {
                    net.minecraft.client.KeyMapping.click(mc.options.keyAttack.getKey());
                    log("attack at " + q[1]);
                }
            }
        }
        if (p.containsKey("breakblock") && base != null) {
            //breakblock=tick:x,y,z;...  the (survival) player breaks that block, as if mined
            for (String h : p.getProperty("breakblock").split(";")) {
                String[] q = h.split(":"), c = q[1].split(",");
                if (tick != Integer.parseInt(q[0])) continue;
                BlockPos at = base.offset(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
                server.execute(() -> {
                    sp.setGameMode(GameType.SURVIVAL);
                    var before = level.getBlockState(at).getBlock();
                    log("break " + q[1] + " " + before + " -> " + sp.gameMode.destroyBlock(at));
                });
            }
        }
        if (p.containsKey("wiredump") && base != null) {
            //wiredump=tick,tick  every wire, and the wire items lying about
            for (String at : p.getProperty("wiredump").split(",")) {
                if (Integer.parseInt(at.trim()) != tick) continue;
                server.execute(() -> {
                    var ws = Wires.near(level, net.minecraft.world.phys.Vec3.atCenterOf(base), 64);
                    StringBuilder b = new StringBuilder();
                    for (var w : ws) {
                        var mid = w.points().get(w.kind() == Wires.Kind.SPAN ? w.points().size() / 4 : w.points().size() / 2);
                        b.append(String.format(java.util.Locale.ROOT, " %s(%d..%d mid %.2f,%.2f,%.2f)", w.kind(), w.a().getX() - base.getX(), w.b().getX() - base.getX(),
                                mid.x - base.getX(), mid.y - base.getY(), mid.z - base.getZ()));
                    }
                    int items = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(base).inflate(40)).size();
                    StringBuilder parts = new StringBuilder();
                    for (int y = 0; y <= 5; y++) parts.append(' ').append(BuiltInRegistries.BLOCK.getKey(level.getBlockState(base.offset(4, y, -1)).getBlock()).getPath());
                    log("wires t=" + tick + b + " | items on the ground " + items + " | over mast 4:" + parts);
                });
            }
        }
        if (p.containsKey("statedump") && base != null) {
            //statedump=tick[,tick...]  every placed transport: where, which way, coupled to which (by uuid), engine,
            //fuel, derailed
            for (String at : p.getProperty("statedump").split(",")) {
                if (Integer.parseInt(at.trim()) != tick) continue;
                server.execute(() -> {
                    for (GenericRailTransport t : placed) {
                        var f = t.frontLinkedID == null ? null : level.getEntity(t.frontLinkedID);
                        var b = t.backLinkedID == null ? null : level.getEntity(t.backLinkedID);
                        var slot = t.getSlotIndexByID(400);
                        log(String.format("state %s %s at %.2f,%.2f,%.2f yaw=%.1f front=%s back=%s run=%s derailed=%s flung=%s/%s fuelSlot=%s tank=%s couplers=%s/%s",
                                t.getClass().getSimpleName().replace("Entity", ""), t.getUUID().toString().substring(0, 8),
                                t.getX(), t.getY(), t.getZ(), t.getYRot(),
                                f == null ? "-" : f.getUUID().toString().substring(0, 8), b == null ? "-" : b.getUUID().toString().substring(0, 8),
                                t.getBoolean(GenericRailTransport.boolValues.RUNNING), t.getBoolean(GenericRailTransport.boolValues.DERAILED),
                                t.frontBogie != null && t.frontBogie.flung, t.backBogie != null && t.backBogie.flung,
                                slot == null ? "-" : slot.getStackOrEmpty(), t.getTankInfo() == null ? "-" : java.util.Arrays.stream(t.getTankInfo())
                                        .map(x -> x == null || x.fluid == null ? "-" : x.fluid.getName() + "=" + x.fluid.amount).toList(),
                                t.getBoolean(GenericRailTransport.boolValues.COUPLINGFRONT), t.getBoolean(GenericRailTransport.boolValues.COUPLINGBACK)));
                    }
                });
            }
        }
        if (p.containsKey("dropon") && tick >= Integer.parseInt(p.getProperty("dropon").split("@")[1]) && !placed.isEmpty()) {
            //dropon=index@tick  drops the player 2.5 above the middle of that placed transport; logs their height over
            //its rails' top (its position) every 4 ticks for 40
            String[] d = p.getProperty("dropon").split("@");
            int at = Integer.parseInt(d[1]);
            GenericRailTransport t = placed.get(Integer.parseInt(d[0]));
            if (tick == at) server.execute(() -> sp.connection.teleport(t.getX(), t.getY() + 2.5, t.getZ(), d.length > 2 ? Float.parseFloat(d[2]) : sp.getYRot(), 0));
            //dropon=index@tick@yaw  then walks that way from 20 ticks after the drop, for 20
            if (d.length > 2 && tick == at + 20) mc.options.keyUp.setDown(true);
            if (d.length > 2 && tick == at + 40) mc.options.keyUp.setDown(false);
            if (tick > at && tick <= at + 40 && tick % 4 == 0) server.execute(() -> log(String.format("dropon t=%d over %.3f across %.2f ground=%s",
                    tick, sp.getY() - t.getY() - t.entityData().get(GenericRailTransport.BOGIE_Y_OFFSET), Math.hypot(sp.getX() - t.getX(), sp.getZ() - t.getZ()), sp.onGround())));
        }
        if (p.containsKey("stroll")) {
            //stroll=x,z,yaw,fromTick,toTick  the player stands there (from the base, on the ground), holds forward and
            //its height is logged every 2 ticks: what it walks over
            String[] w = p.getProperty("stroll").split(",");
            int from = Integer.parseInt(w[3]), to = Integer.parseInt(w[4]);
            if (tick == from) server.execute(() -> {
                sp.getAbilities().flying = false;
                sp.onUpdateAbilities();
                sp.connection.teleport(base.getX() + Double.parseDouble(w[0]), base.getY(), base.getZ() + Double.parseDouble(w[1]), Float.parseFloat(w[2]), 0);
            });
            //stroll=...,jump  holds jump too; stroll=...,drop  drops them there from 2 blocks up, no walking
            boolean drop = w.length > 5 && w[5].equals("drop");
            if (tick == from + 2 && !drop) mc.options.keyUp.setDown(true);
            if (tick == from + 2 && w.length > 5 && w[5].equals("jump")) mc.options.keyJump.setDown(true);
            if (tick == from && drop) server.execute(() -> sp.connection.teleport(sp.getX(), base.getY() + 2.5, sp.getZ(), sp.getYRot(), 0));
            if (tick == to) { mc.options.keyUp.setDown(false); mc.options.keyJump.setDown(false); }
            if (tick >= from && tick <= to && tick % 2 == 0) server.execute(() ->
                    log(String.format("stroll t=%d x=%.2f y=%.3f z=%.2f ground=%s", tick, sp.getX(), sp.getY() - base.getY(), sp.getZ(), sp.onGround())));
        }
        if (p.containsKey("walk") && tick >= Integer.parseInt(p.getProperty("walk").split(",")[1])) {
            //walk=index,fromTick,toTick  the player stands behind that placed transport (on a line along x) and walks
            //into it, holding forward; its speed is logged every 2 ticks
            String[] w = p.getProperty("walk").split(",");
            int from = Integer.parseInt(w[1]), to = Integer.parseInt(w[2]);
            var t = placed.get(Integer.parseInt(w[0]));
            if (tick == from) server.execute(() -> {
                double behind = t.getX() - (ebf.tim.utility.TransportDims.of(t) != null ? ebf.tim.utility.TransportDims.of(t).back() : 3) - 1.0;
                sp.getAbilities().flying = false;
                sp.onUpdateAbilities();
                sp.connection.teleport(behind, t.getY(), t.getZ(), -90, 0);
                log("walk from " + behind + " to " + t.getX());
            });
            if (tick == from + 2) mc.options.keyUp.setDown(true);
            if (tick == to) mc.options.keyUp.setDown(false);
            if (tick >= from && tick <= to + 40 && tick % 2 == 0) server.execute(() ->
                    log(String.format("walk t=%d car x=%.3f dx=%.4f player x=%.3f", tick, t.getX() - base.getX(), t.getX() - t.xo, sp.getX() - base.getX())));
        }
        if (p.containsKey("kick")) {
            //kick=index,speed,tick[;index,speed,tick...]  gives that placed transport a speed along +x
            for (String one : p.getProperty("kick").split(";")) {
                String[] k = one.trim().split(",");
                if (tick != Integer.parseInt(k[2])) continue;
                server.execute(() -> {
                    var t = placed.get(Integer.parseInt(k[0]));
                    double v = Double.parseDouble(k[1]);
                    t.frontBogie.velocity[0] = v; t.frontBogie.velocity[1] = 0;
                    t.backBogie.velocity[0] = v; t.backBogie.velocity[1] = 0;
                    log("kick " + t.getType() + " " + v);
                });
            }
        }
        if (p.containsKey("mobs") && base != null) {
            //mobs=type@x,z[,y];...  mobs that cannot walk standing there from tick 30; their health is logged as it changes
            if (tick == 30) server.execute(() -> {
                for (String m : p.getProperty("mobs").split(";")) {
                    String[] a = m.trim().split("@");
                    String[] xz = a[1].split(",");
                    var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(a[0]));
                    if (type.create(level) instanceof net.minecraft.world.entity.Mob mob) {
                        //it keeps its AI (a mob without one is not moved by pushes) but cannot walk off
                        mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100000, 255, false, false));
                        mob.moveTo(base.getX() + Double.parseDouble(xz[0]) + 0.5, base.getY() + (xz.length > 2 ? Double.parseDouble(xz[2]) : 0),
                                base.getZ() + Double.parseDouble(xz[1]) + 0.5, 0, 0);
                        level.addFreshEntity(mob);
                        MOBS.add(mob);
                        log("mob " + a[0] + " at " + a[1] + " health " + mob.getHealth());
                    }
                }
            });
            if (tick > 30) server.execute(() -> {
                for (var mob : MOBS) {
                    float h = mob.isAlive() ? mob.getHealth() : 0;
                    Float was = MOB_HEALTH.put(mob, h);
                    if (tick % 50 == 0) log(String.format("mob t=%d %s at x=%.2f y=%.2f z=%.2f", tick, mob.getType().toShortString(),
                            mob.getX() - base.getX(), mob.getY() - base.getY(), mob.getZ() - base.getZ()));
                    if (was == null || was != h) log(String.format("mob t=%d %s z=%.0f health %.1f -> %.1f at x=%.2f y=%.2f",
                            tick, mob.getType().toShortString(), mob.getZ() - base.getZ(), was == null ? Float.NaN : was, h, mob.getX() - base.getX(), mob.getY() - base.getY()));
                }
            });
        }
        if (p.containsKey("standon") && train != null) {
            //standon=tick  drops the player (not flying) onto the middle of the first transport, then tries to walk
            //it sideways through the next one; logs where it ends up
            int t0 = Integer.parseInt(p.getProperty("standon"));
            if (tick == t0) server.execute(() -> {
                sp.setGameMode(GameType.SURVIVAL);
                sp.getAbilities().flying = false;
                sp.onUpdateAbilities();
                sp.teleportTo(level, train.getX(), train.getY() + 5, train.getZ(), 0, 0);
            });
            if (tick == t0 + 40) server.execute(() -> log(String.format("standon: player y above transport %.2f (on it if > 1)", sp.getY() - train.getY())));
            if (tick >= t0 && tick <= t0 + 100 && tick % 10 == 0) {
                var ct = (GenericRailTransport) mc.level.getEntity(train.getId());
                StringBuilder bb = new StringBuilder();
                if (ct != null && ct.getParts() != null) for (var part : ct.getParts()) bb.append(String.format(" [%.2f..%.2f y%.2f..%.2f z%.2f..%.2f]", part.getBoundingBox().minX - ct.getX(), part.getBoundingBox().maxX - ct.getX(),
                        part.getBoundingBox().minY - ct.getY(), part.getBoundingBox().maxY - ct.getY(), part.getBoundingBox().minZ - ct.getZ(), part.getBoundingBox().maxZ - ct.getZ()));
                log(String.format("standon t=%d client player rel (%.2f,%.2f,%.2f) boxes%s collidable=%s", tick, mc.player.getX() - train.getX(), mc.player.getY() - train.getY(), mc.player.getZ() - train.getZ(), bb,
                        ct == null || ct.getParts().length == 0 ? "-" : ct.getParts()[0].canBeCollidedWith()));
            }
            if (tick == t0 + 45) server.execute(() -> sp.teleportTo(level, train.getX(), train.getY(), train.getZ() - 3, 0, 0));
            if (tick > t0 + 50 && tick < t0 + 100) {
                mc.options.keyUp.setDown(true);
            }
            if (tick == t0 + 100) {
                mc.options.keyUp.setDown(false);
                server.execute(() -> log(String.format("standon: walked from z-3 towards it, now z %.2f relative (blocked if < -0.5)", sp.getZ() - train.getZ())));
            }
        }
        if (p.containsKey("camjoint") && base != null) {
            //camjoint=k,tick  the camera beside the joint between the k-th and the next placed transport
            String[] cj = p.getProperty("camjoint").split(",");
            int k = Integer.parseInt(cj[0]);
            if (tick == Integer.parseInt(cj[1]) && placed.size() > k + 1) server.execute(() -> {
                var a = placed.get(k);
                var b2 = placed.get(k + 1);
                double mx = (a.getX() + b2.getX()) / 2, mz = (a.getZ() + b2.getZ()) / 2;
                double dx = b2.getX() - a.getX(), dz = b2.getZ() - a.getZ(), l = Math.max(1e-3, Math.sqrt(dx * dx + dz * dz));
                //off to the side of the joint, looking at it
                double cx = mx - dz / l * 3.2, cz = mz + dx / l * 3.2;
                float yaw = (float) (Math.toDegrees(Math.atan2(cz - mz, cx - mx)) + 90);
                sp.getAbilities().flying = true;
                sp.onUpdateAbilities();
                sp.teleportTo(level, cx, a.getY() - 1.0, cz, yaw, 8);
            });
        }
        if (p.containsKey("shots")) {
            String[] s = p.getProperty("shots").split(",");
            int from = Integer.parseInt(s[0]), to = Integer.parseInt(s[1]), step = Integer.parseInt(s[2]);
            if (tick >= from && tick <= to && (tick - from) % step == 0) {
                Screenshot.grab(mc.gameDirectory, "tc-" + tick + ".png", mc.getMainRenderTarget(), c -> log("shot " + tick));
            }
        }
        if (tick == i("ticks", 200)) {
            log("done");
            mc.stop();
        }
    }

    private static BlockPos machineAt;

    /**
     * measure=1: every transport drawn into nothing on the client, its vertices collected: the lowest point (as
     * a + s * the bogie height, from two heights) and how far the model reaches past the centre to the front and
     * the back (along the line between the bogies). Written as tools/transport_dims.json.
     */
    /**
     * roofscan=1  for every electric locomotive the height of its model along it (every eighth of a block): the
     * pantographs stand out as bumps over the roof
     */
    private static void roofScan(Minecraft mc) {
        for (var holder : TiMGenericRegistry.TRANSPORTS) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(holder.get()).getPath();
            GenericRailTransport t = holder.get().create(mc.level);
            if (t == null || !t.getTypes().contains(ebf.tim.TrainsInMotion.transportTypes.ELECTRIC)) continue;
            t.setPos(0, 0, 0);
            float[] rp = t.rotationPoints();
            t.frontBogie = new ebf.tim.entities.EntityBogie(t, rp[1], 0, 0, true);
            t.backBogie = new ebf.tim.entities.EntityBogie(t, rp[0], 0, 0, false);
            double dir = rp[1] - rp[0] >= 0 ? 1 : -1;
            t.frontBogie.yOffset = t.backBogie.yOffset = 0.125f;
            var verts = new java.util.ArrayList<org.joml.Vector3f>();
            fexcraft.tmt.slim.RenderContext.begin(new com.mojang.blaze3d.vertex.PoseStack(), null, 0, null);
            fexcraft.tmt.slim.RenderContext.measure = verts;
            ebf.tim.render.RenderWagon.measuring = true;
            try {
                ebf.tim.render.RenderWagon.doRender(t, t.getYRot(), 0, t.backBogie.yOffset, false, null);
            } catch (Exception e) {
                log("roof " + id + " failed " + e);
                continue;
            } finally {
                ebf.tim.render.RenderWagon.measuring = false;
                fexcraft.tmt.slim.RenderContext.measure = null;
                fexcraft.tmt.slim.RenderContext.end();
            }
            java.util.TreeMap<Integer, Float> top = new java.util.TreeMap<>();
            float lo = Float.MAX_VALUE;
            for (var v : verts) lo = Math.min(lo, v.y);
            for (var v : verts) top.merge((int) Math.floor(v.x * dir * 8), v.y - lo, Math::max);
            StringBuilder b = new StringBuilder();
            for (var e : top.entrySet()) b.append(String.format(java.util.Locale.ROOT, " %.2f:%.2f", e.getKey() / 8f, e.getValue()));
            log("roof " + id + " #" + TiMGenericRegistry.TRANSPORTS.indexOf(holder) + b);
        }
    }

    /**
     * smokecheck=1: for every locomotive with smoke, where it comes out against the model: the top of what is under
     * it (the highest vertices within 0.45 across, their middle), how far across from that middle the smoke starts
     * and how high over that top. A chimney's smoke should start at its middle, at its top
     */
    private static void smokeCheck(Minecraft mc) {
        for (var holder : TiMGenericRegistry.TRANSPORTS) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(holder.get()).getPath();
            try {
                if (!(holder.get().create(mc.level) instanceof EntityTrainCore t)) continue;
                t.setPos(0, 0, 0);
                float[] rp = t.rotationPoints();
                t.frontBogie = new ebf.tim.entities.EntityBogie(t, rp[1], 0, 0, true);
                t.backBogie = new ebf.tim.entities.EntityBogie(t, rp[0], 0, 0, false);
                double dir = rp[1] - rp[0] >= 0 ? 1 : -1;
                float railY = 0.3f;
                var pts = TrainSmoke.smokePoints(t, railY);
                if (pts.isEmpty()) continue;
                t.frontBogie.yOffset = t.backBogie.yOffset = railY;
                t.renderData.needsModelUpdate = true;
                var verts = new java.util.ArrayList<org.joml.Vector3f>();
                fexcraft.tmt.slim.RenderContext.begin(new com.mojang.blaze3d.vertex.PoseStack(), null, 0, null);
                fexcraft.tmt.slim.RenderContext.measure = verts;
                ebf.tim.render.RenderWagon.measuring = true;
                try {
                    ebf.tim.render.RenderWagon.doRender(t, t.getYRot(), 0, railY, false, null);
                } finally {
                    ebf.tim.render.RenderWagon.measuring = false;
                    fexcraft.tmt.slim.RenderContext.measure = null;
                    fexcraft.tmt.slim.RenderContext.end();
                }
                StringBuilder b = new StringBuilder(), fix = new StringBuilder();
                for (double[] q : pts) {
                    double sx = q[0] * dir, sy = q[1], sz = q[2] * dir;
                    float top = -Float.MAX_VALUE;
                    for (var v : verts) if (Math.hypot(v.x - sx, v.z - sz) < 0.45) top = Math.max(top, v.y);
                    if (top == -Float.MAX_VALUE) { b.append(String.format(java.util.Locale.ROOT, " [nothing under the smoke at %.2f,%.2f,%.2f]", sx, sy, sz)); continue; }
                    double cx = 0, cz = 0; int n = 0;
                    for (var v : verts) if (Math.hypot(v.x - sx, v.z - sz) < 0.45 && v.y >= top - 0.06) { cx += v.x; cz += v.z; n++; }
                    cx /= n; cz /= n;
                    b.append(String.format(java.util.Locale.ROOT, " [across %.2f (along %+.2f side %+.2f) over top %+.2f]", Math.hypot(sx - cx, sz - cz), (sx - cx) * dir, (sz - cz) * dir, sy - top));
                    //the correction to the model-frame point (x backwards along, y down, z across) that puts it on
                    //the middle of the top, a little over it; across only when one chimney is under it and it is off
                    double across = Math.hypot(sx - cx, sz - cz);
                    boolean move = pts.size() == 1 && across > 0.08;
                    double dx = move ? -(cx - sx) * dir : 0, dz = move ? (cz - sz) * dir : 0, dy = sy - (top + 0.05);
                    fix.append(String.format(java.util.Locale.ROOT, "%s{%.3ff, %.3ff, %.3ff}", fix.length() == 0 ? "" : ", ", dx, dy, dz));
                }
                log(String.format("smoke %-34s%s", id, b));
                if (p.getProperty("smokecheck").equals(id)) {
                    //the roof line along it near the first chimney's side position: highest vertex per quarter block
                    double sx = pts.get(0)[0] * dir, sz = pts.get(0)[2] * dir;
                    StringBuilder pr = new StringBuilder();
                    for (double x = sx - 3; x <= sx + 3; x += 0.25) {
                        float hi = -9;
                        for (var v : verts) if (Math.abs(v.z - sz) < 0.6 && v.x >= x && v.x < x + 0.25) hi = Math.max(hi, v.y);
                        pr.append(String.format(java.util.Locale.ROOT, " %+.2f:%.2f", (x - sx) * dir, hi));
                    }
                    log(String.format(java.util.Locale.ROOT, "roof of %s (smoke at y %.2f, along from it):%s", id, pts.get(0)[1], pr));
                }
                log(String.format(java.util.Locale.ROOT, "smokefix adjust(%s.class, new float[][]{%s});", t.getClass().getSimpleName(), fix));
            } catch (Throwable e) {
                log("smoke " + id + " failed: " + e);
            }
        }
        log("smokecheck done");
    }

    /**
     * shapecheck=1: every transport's body as drawn, with its bogies along x: how long (from the middle to each end)
     * and wide it is, how high its sides go, and how high the highest thing inside it is (0.35 in from the sides
     * and 0.4 from the ends): an open-top car has that well under its sides, a covered one has its roof there
     */
    private static void shapeCheck(Minecraft mc) {
        for (var holder : TiMGenericRegistry.TRANSPORTS) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(holder.get()).getPath();
            try {
                GenericRailTransport t = holder.get().create(mc.level);
                t.setPos(0, 0, 0);
                float[] rp = t.rotationPoints();
                t.frontBogie = new ebf.tim.entities.EntityBogie(t, rp[1], 0, 0, true);
                t.backBogie = new ebf.tim.entities.EntityBogie(t, rp[0], 0, 0, false);
                float railY = 0.3f;
                t.frontBogie.yOffset = t.backBogie.yOffset = railY;
                t.renderData.needsModelUpdate = true;
                var verts = new java.util.ArrayList<org.joml.Vector3f>();
                var faces = new java.util.ArrayList<org.joml.Vector3f[]>();
                fexcraft.tmt.slim.RenderContext.begin(new com.mojang.blaze3d.vertex.PoseStack(), null, 0, null);
                fexcraft.tmt.slim.RenderContext.measure = verts;
                fexcraft.tmt.slim.RenderContext.measureFaces = faces;
                ebf.tim.render.RenderWagon.measuring = true;
                try {
                    ebf.tim.render.RenderWagon.doRender(t, t.getYRot(), 0, railY, false, null);
                } finally {
                    ebf.tim.render.RenderWagon.measuring = false;
                    fexcraft.tmt.slim.RenderContext.measure = null;
                    fexcraft.tmt.slim.RenderContext.measureFaces = null;
                    fexcraft.tmt.slim.RenderContext.end();
                }
                if (verts.isEmpty()) continue;
                float x0 = Float.MAX_VALUE, x1 = -Float.MAX_VALUE, w = 0, top = -Float.MAX_VALUE, lo = Float.MAX_VALUE;
                for (var v : verts) { x0 = Math.min(x0, v.x); x1 = Math.max(x1, v.x); w = Math.max(w, Math.abs(v.z)); top = Math.max(top, v.y); lo = Math.min(lo, v.y); }
                //what a ray down meets first: across the body (z) in the middle third of its length, every 0.05
                StringBuilder prof = new StringBuilder();
                java.util.List<Float> mid = new java.util.ArrayList<>();
                for (float z = -w; z <= w + 1e-4; z += 0.05f) {
                    java.util.List<Float> hits = new java.util.ArrayList<>();
                    for (float x = x0 + (x1 - x0) / 3; x <= x1 - (x1 - x0) / 3; x += 0.1f) hits.add(rayDown(faces, x, z));
                    hits.sort(null);
                    float h = hits.get(hits.size() / 2);
                    prof.append(String.format(java.util.Locale.ROOT, " %.2f", h == -Float.MAX_VALUE ? -1f : h - railY));
                    if (Math.abs(z) < w * 0.5f) mid.add(h);
                }
                mid.sort(null);
                float inside = mid.isEmpty() ? -Float.MAX_VALUE : mid.get(mid.size() / 2);
                log(String.format(java.util.Locale.ROOT, "shape %-34s %-26s len %.2f..%.2f halfwidth %.2f top %.2f inside %.2f hitbox %s across:%s",
                        id, t.getClass().getSimpleName(), x0, x1, w, top - railY, inside - railY, java.util.Arrays.toString(t.getHitboxSize()), prof));
                if (inside != -Float.MAX_VALUE && !(t instanceof EntityTrainCore)) {
                    float floor = inside - railY;
                    //the sides: what the ray meets near each edge (the highest within 0.2 of it)
                    float sideL = -Float.MAX_VALUE, sideR = -Float.MAX_VALUE, thick = 0;
                    for (float z = -w; z <= w + 1e-4; z += 0.025f) {
                        float h = -Float.MAX_VALUE;
                        for (float x = x0 + (x1 - x0) / 3; x <= x1 - (x1 - x0) / 3; x += 0.2f) h = Math.max(h, rayDown(faces, x, z));
                        if (z < -w + 0.2f) sideL = Math.max(sideL, h);
                        if (z > w - 0.2f) sideR = Math.max(sideR, h);
                        if (h > inside + 0.3f && Math.abs(z) > w - 0.25f) thick = Math.max(thick, w - Math.abs(z));
                    }
                    float wall = Math.min(sideL, sideR) - railY;
                    //along the middle: where the floor is, and the ends' height
                    float in0 = Float.MAX_VALUE, in1 = -Float.MAX_VALUE, endTop = -Float.MAX_VALUE;
                    for (float x = x0; x <= x1 + 1e-4; x += 0.025f) {
                        float h = rayDown(faces, x, 0);
                        if (Math.abs(h - inside) < 0.06f) { in0 = Math.min(in0, x); in1 = Math.max(in1, x); }
                    }
                    for (float x = x0; x <= x1 + 1e-4; x += 0.025f) if (x < in0 || x > in1) endTop = Math.max(endTop, rayDown(faces, x, 0));
                    float halfIn = Math.min(-in0, in1);
                    log(String.format(java.util.Locale.ROOT, "walkdiag %-30s floor %.2f sides %.2f/%.2f ends %.2f in %.2f..%.2f thick %.2f top %.2f",
                            id, floor, sideL - railY, sideR - railY, endTop - railY, in0, in1, thick, top - railY));
                    float bodyHalf = Math.min(-x0, x1);
                    //a wall runs along the side; stakes only stand here and there
                    int high = 0, all = 0;
                    for (float x = -bodyHalf + 0.1f; x <= bodyHalf - 0.1f; x += 0.05f) {
                        for (float side : new float[]{-1, 1}) {
                            all++;
                            float h = -Float.MAX_VALUE;
                            for (float in = 0.01f; in <= 0.25f; in += 0.025f) h = Math.max(h, rayDown(faces, x, side * (w - in)));
                            if (h - railY > floor + 0.3f) high++;
                        }
                    }
                    if (all > 0 && high < 0.7f * all) wall = floor;
                    boolean ends = endTop != -Float.MAX_VALUE && endTop - railY > floor + 0.35f && halfIn > 0.2f && halfIn < bodyHalf - 0.05f;
                    if (wall > floor + 0.35f && floor < 1.2f) {
                        //walls on both sides; the ends where the floor stops, or the body's ends when the floor runs on
                        float t2 = Math.max(0.08f, thick + 0.025f);
                        log(String.format(java.util.Locale.ROOT, "walkshape tub(\"%s\", %.3ff, %.3ff, %.3ff, %.3ff, %.3ff);", id, floor,
                                ends ? Math.min(wall, endTop - railY) : wall, w, t2, ends ? halfIn : bodyHalf - t2));
                    } else if (floor < 1.0f) {
                        log(String.format(java.util.Locale.ROOT, "walkshape flat(\"%s\", %.3ff);", id, floor));
                    }
                }
            } catch (Throwable e) {
                log("shape " + id + " failed: " + e);
            }
        }
        log("shapecheck done");
    }

    /** the height of the first face a vertical ray down at (x, z) meets, or -MAX when none */
    private static float rayDown(java.util.List<org.joml.Vector3f[]> faces, float x, float z) {
        float best = -Float.MAX_VALUE;
        for (var f : faces) {
            //a fan of triangles from the first corner
            for (int i = 1; i + 1 < f.length; i++) {
                var a = f[0]; var b = f[i]; var c = f[i + 1];
                float d = (b.z - c.z) * (a.x - c.x) + (c.x - b.x) * (a.z - c.z);
                if (Math.abs(d) < 1e-7f) continue;
                float l1 = ((b.z - c.z) * (x - c.x) + (c.x - b.x) * (z - c.z)) / d;
                float l2 = ((c.z - a.z) * (x - c.x) + (a.x - c.x) * (z - c.z)) / d;
                float l3 = 1 - l1 - l2;
                if (l1 < -1e-4f || l2 < -1e-4f || l3 < -1e-4f) continue;
                best = Math.max(best, l1 * a.y + l2 * b.y + l3 * c.y);
            }
        }
        return best;
    }

    private static void measureAll(Minecraft mc) {
        StringBuilder json = new StringBuilder("{\n");
        int n = 0;
        for (var holder : TiMGenericRegistry.TRANSPORTS) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(holder.get()).getPath();
            try {
                GenericRailTransport t = holder.get().create(mc.level);
                t.setPos(0, 0, 0);
                float[] rp = t.rotationPoints();
                t.frontBogie = new ebf.tim.entities.EntityBogie(t, rp[1], 0, 0, true);
                t.backBogie = new ebf.tim.entities.EntityBogie(t, rp[0], 0, 0, false);
                double fx = rp[1] - rp[0];
                double dir = fx >= 0 ? 1 : -1;
                float[] minY = new float[2];
                double front = 0, back = 0;
                float[] hs = {0.3f, 0.4f};
                for (int k = 0; k < 2; k++) {
                    t.frontBogie.yOffset = t.backBogie.yOffset = hs[k];
                    t.renderData.needsModelUpdate = true;
                    var verts = new java.util.ArrayList<org.joml.Vector3f>();
                    fexcraft.tmt.slim.RenderContext.begin(new com.mojang.blaze3d.vertex.PoseStack(), null, 0, null);
                    fexcraft.tmt.slim.RenderContext.measure = verts;
                    ebf.tim.render.RenderWagon.measuring = true;
                    try {
                        ebf.tim.render.RenderWagon.doRender(t, t.getYRot(), 0, t.backBogie.yOffset, false, null);
                    } finally {
                        ebf.tim.render.RenderWagon.measuring = false;
                        fexcraft.tmt.slim.RenderContext.measure = null;
                        fexcraft.tmt.slim.RenderContext.end();
                    }
                    float lo = Float.MAX_VALUE;
                    double f = -Double.MAX_VALUE, b = -Double.MAX_VALUE;
                    for (var v : verts) {
                        lo = Math.min(lo, v.y);
                        f = Math.max(f, v.x * dir);
                        b = Math.max(b, -v.x * dir);
                    }
                    minY[k] = lo;
                    if (k == 0 && p.getProperty("measure").equals(id)) {
                        //what is at the ends: the vertices within 0.1 of the front/back, by height
                        StringBuilder eb = new StringBuilder();
                        for (var v : verts) if (v.x * dir > f - 0.1 || -v.x * dir > b - 0.1) eb.append(String.format(java.util.Locale.ROOT, " (%.2f,%.2f,%.2f)", v.x * dir, v.y, v.z));
                        log("ends of " + id + ":" + eb);
                        java.util.TreeMap<Integer, float[]> rows = new java.util.TreeMap<>();
                        for (var v : verts) { int yb = (int) Math.floor(v.y * 4); float[] r = rows.computeIfAbsent(yb, q -> new float[]{9, -9}); r[0] = (float) Math.min(r[0], v.x * dir); r[1] = (float) Math.max(r[1], v.x * dir); }
                        StringBuilder rb = new StringBuilder();
                        for (var e : rows.entrySet()) rb.append(String.format(java.util.Locale.ROOT, " y%.2f:[%.2f..%.2f]", e.getKey() / 4f, e.getValue()[0], e.getValue()[1]));
                        log("extent by height " + id + ":" + rb);
                    }
                    front = f;
                    back = b;
                    if (verts.isEmpty()) throw new IllegalStateException("no vertices");
                }
                double s = (minY[1] - minY[0]) / (hs[1] - hs[0]), a = minY[0] - s * hs[0];
                json.append(String.format(java.util.Locale.ROOT, "%s\"%s\": {\"a\": %.4f, \"s\": %.4f, \"front\": %.4f, \"back\": %.4f}",
                        n++ == 0 ? "" : ",\n", id, a, s, front, back));
                log(String.format(java.util.Locale.ROOT, "measure %-32s a=%.3f s=%.3f front=%.3f back=%.3f hitbox=%.2f", id, a, s, front, back, t.getHitboxSize()[0]));
            } catch (Throwable e) {
                log("measure " + id + " failed: " + e);
            }
        }
        //the rails: the highest point of their geometry (the top of the railheads), block-relative
        json.append(",\n\"_rails\": {");
        int rn = 0;
        var cam = mc.gameRenderer.getMainCamera();
        for (BlockPos q : BlockPos.betweenClosed(base.offset(-2, -1, -2), base.offset(40, 2, 6))) {
            var be = mc.level.getBlockEntity(q);
            if (be == null) continue;
            var rend = mc.getBlockEntityRenderDispatcher().getRenderer(be);
            if (rend == null) continue;
            float[] hi = {-Float.MAX_VALUE}, lo = {Float.MAX_VALUE};
            net.minecraft.client.renderer.MultiBufferSource cap = type -> new com.mojang.blaze3d.vertex.VertexConsumer() {
                public com.mojang.blaze3d.vertex.VertexConsumer addVertex(float x, float y, float z) { hi[0] = Math.max(hi[0], y); lo[0] = Math.min(lo[0], y); return this; }
                public com.mojang.blaze3d.vertex.VertexConsumer setColor(int r, int g, int b, int a) { return this; }
                public com.mojang.blaze3d.vertex.VertexConsumer setUv(float u, float v) { return this; }
                public com.mojang.blaze3d.vertex.VertexConsumer setUv1(int u, int v) { return this; }
                public com.mojang.blaze3d.vertex.VertexConsumer setUv2(int u, int v) { return this; }
                public com.mojang.blaze3d.vertex.VertexConsumer setNormal(float x, float y, float z) { return this; }
            };
            try {
                ((net.minecraft.client.renderer.blockentity.BlockEntityRenderer) rend).render(be, 0, new com.mojang.blaze3d.vertex.PoseStack(), cap, 0xF000F0, 0);
            } catch (Throwable e) {
                log("rail measure failed " + be + " " + e);
                continue;
            }
            if (hi[0] == -Float.MAX_VALUE) continue;
            String kind = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).getPath();
            log(String.format(java.util.Locale.ROOT, "rail %s at %s top=%.4f bottom=%.4f", kind, q.subtract(base), hi[0], lo[0]));
            json.append(String.format(java.util.Locale.ROOT, "%s\"%s\": %.4f", rn++ == 0 ? "" : ", ", kind + "@" + q.subtract(base).toShortString().replace(", ", "_"), hi[0]));
        }
        json.append("}");
        json.append("\n}\n");
        try {
            Files.writeString(Path.of(System.getProperty("user.home"), "src/tc-neo/tools/transport_dims.json"), json.toString());
        } catch (Exception e) {
            log("measure write failed " + e);
        }
        log("measured " + n);
    }
    private static final java.util.Map<GenericRailTransport, net.minecraft.world.phys.Vec3> driftStart = new java.util.HashMap<>();
    private static final java.util.List<String> rowNames = new java.util.ArrayList<>();
    private static final java.util.List<Integer> rowX = new java.util.ArrayList<>();
    private static final java.util.Map<GenericRailTransport, net.minecraft.world.phys.Vec3> fleetStart = new java.util.HashMap<>();

    private static void put(GenericRailTransport t, int slotId, net.minecraft.world.item.ItemStack stack) {
        var slot = t.getSlotIndexByID(slotId);
        if (slot != null) slot.setStack(stack);
    }

    /**
     * recipeaudit=1: which Traincraft items survival can't get. Starts from the vanilla items, what the mod's
     * ores drop and what its machines make, then adds whatever a crafting/smelting recipe or a TiM table recipe
     * makes from things already got, until nothing changes.
     */
    private static void recipeAudit(ServerLevel level) {
        java.util.Set<net.minecraft.world.item.Item> got = new java.util.HashSet<>();
        for (var item : BuiltInRegistries.ITEM) {
            String ns = BuiltInRegistries.ITEM.getKey(item).getNamespace();
            if (ns.equals("minecraft")) got.add(item);
        }
        //ores of the mod: what they drop to a diamond pickaxe
        var pick = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
        for (var block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals("traincraft")) continue;
            String n = id.getPath();
            if (n.contains("ore") || n.contains("petroleum") || n.contains("oilsand")) {
                for (var d : Block.getDrops(block.defaultBlockState(), level, base, null, null, pick)) got.add(d.getItem());
                log("audit ore " + n + " drops " + Block.getDrops(block.defaultBlockState(), level, base, null, null, pick));
            }
        }
        var rm = level.getRecipeManager();
        boolean changed = true;
        int rounds = 0;
        while (changed && rounds++ < 50) {
            changed = false;
            //machines: distillation (diesel, fuel oil, plastic) and the open hearth furnace (steel)
            if (got.contains(TCBlocks.DISTIL.get().asItem())) {
                for (var it : new net.minecraft.world.item.Item[]{ebf.tim.registry.TiMItems.dieselCanister.get(), ebf.tim.registry.TiMOres.dustPlastic.get()}) changed |= got.add(it);
                for (var f : ebf.tim.registry.TiMFluids.ALL) changed |= got.add(f.bucket.get());
            }
            if (got.contains(TCBlocks.HEARTH_FURNACE.get().asItem()) && got.contains(ebf.tim.registry.TiMItems.graphite.get())) {
                changed |= got.add(ebf.tim.registry.TiMOres.ingotSteel.get());
            }
            for (var holder : rm.getRecipes()) {
                var r = holder.value();
                var out = r.getResultItem(level.registryAccess());
                if (out.isEmpty() || got.contains(out.getItem())) continue;
                boolean ok = !r.getIngredients().isEmpty();
                for (var ing : r.getIngredients()) {
                    if (ing.isEmpty()) continue;
                    boolean any = false;
                    for (var st : ing.getItems()) if (got.contains(st.getItem())) { any = true; break; }
                    if (!any) { ok = false; break; }
                }
                if (ok) changed |= got.add(out.getItem());
            }
            for (String table : new String[]{"traintable", "traintabletier1", "traintabletier2", "traintabletier3"}) {
                var tableItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse("traincraft:block." + table));
                if (!got.contains(tableItem)) continue;
                for (var r : ebf.tim.utility.RecipeManager.getRecipes(table)) {
                    boolean ok = true;
                    for (var slot : r.getRecipeItems()) {
                        if (slot == null) continue;
                        boolean any = false;
                        for (var st : slot) if (st.isEmpty() || got.contains(st.getItem())) { any = true; break; }
                        if (!any) { ok = false; break; }
                    }
                    if (ok) for (var res : r.getresult()) changed |= got.add(res.getItem());
                }
            }
            if (got.contains(TiMBlocksItem("block.railtable"))) changed |= got.add(ebf.tim.registry.TiMBlocks.railItem());
        }
        java.util.List<String> missing = new java.util.ArrayList<>();
        for (var item : BuiltInRegistries.ITEM) {
            var id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals("traincraft") && !got.contains(item)) missing.add(id.getPath());
        }
        java.util.Collections.sort(missing);
        log("audit rounds=" + rounds + " got " + got.stream().filter(i -> BuiltInRegistries.ITEM.getKey(i).getNamespace().equals("traincraft")).count()
                + " traincraft items, missing " + missing.size() + ": " + missing);
    }

    private static net.minecraft.world.item.Item TiMBlocksItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("traincraft", name));
    }

    /** block@x,y,z  or  rail line: block@x,y,z@len@axis  or  transport item@x,y,z@playerSide */
    private static void run(ServerLevel level, ServerPlayer sp, String use) {
        String[] a = use.split("@");
        String[] xyz = a[1].split(",");
        BlockPos pos = base.offset(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
        ResourceLocation id = ResourceLocation.parse(a[0]);
        if (id.equals(ResourceLocation.fromNamespaceAndPath("traincraft", "rail"))) {
            //TiM rail: iron, gravel ballast, oak ties
            int len = a.length > 2 ? Integer.parseInt(a[2]) : 1;
            String axis = a.length > 3 ? a[3] : "x";
            for (int n = 0; n < len; n++) {
                BlockPos at = axis.equals("x") ? pos.offset(n, 0, 0) : pos.offset(0, 0, n);
                if (level.getBlockState(at.below()).isAir()) level.setBlock(at.below(), Blocks.STONE.defaultBlockState(), 3);
                net.minecraft.world.item.ItemStack stack = ebf.tim.items.ItemRail.setStackData(
                        new net.minecraft.world.item.ItemStack(ebf.tim.registry.TiMBlocks.railItem()),
                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT),
                        new net.minecraft.world.item.ItemStack(Blocks.GRAVEL), new net.minecraft.world.item.ItemStack(Blocks.OAK_LOG), null);
                ((ebf.tim.items.ItemRail) ebf.tim.registry.TiMBlocks.railItem()).placeTrack(stack, level, at.getX(), at.getY(), at.getZ(), null);
            }
            StringBuilder dump = new StringBuilder();
            for (int n = 0; n < len; n++) {
                BlockPos at = axis.equals("x") ? pos.offset(n, 0, 0) : pos.offset(0, 0, n);
                var be = level.getBlockEntity(at);
                dump.append(level.getBlockState(at).getBlock() == ebf.tim.registry.TiMBlocks.RAIL.get() ? "R" : "-")
                        .append(be instanceof ebf.tim.blocks.RailTileEntity r ? r.getMeta() + (r.getData().isEmpty() ? "e" : "") : (be == null ? "null" : be.getClass().getSimpleName())).append(' ');
            }
            log("placed " + len + " TiM rails from " + pos + " along " + axis + ": " + dump);
            return;
        }
        if (id.equals(ResourceLocation.fromNamespaceAndPath("traincraft", "catenary_mast"))) {
            //catenary_mast@x,y,z@facing  (the way its cantilever points: north/east/south/west)
            var facing = net.minecraft.core.Direction.byName(a.length > 2 ? a[2] : "east");
            if (!level.getBlockState(pos).isAir()) {
                //a player could not put it there either
                log("mast at " + pos + " not placed: " + level.getBlockState(pos).getBlock());
                return;
            }
            if (level.getBlockState(pos.below()).isAir()) level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(pos, TCBlocks.CATENARY_MAST.get().defaultBlockState().setValue(CatenaryMastBlock.FACING, facing), 3);
            log("mast at " + pos + " facing " + facing);
            return;
        }
        if (BuiltInRegistries.BLOCK.containsKey(id)) {
            Block b = BuiltInRegistries.BLOCK.get(id);
            int len = a.length > 2 ? Integer.parseInt(a[2]) : 1;
            String axis = a.length > 3 ? a[3] : "x";
            for (int n = 0; n < len; n++) {
                BlockPos at = axis.equals("x") ? pos.offset(n, 0, 0) : pos.offset(0, 0, n);
                if (level.getBlockState(at.below()).isAir()) level.setBlock(at.below(), Blocks.STONE.defaultBlockState(), 3);
                var st = b.defaultBlockState();
                //block@x,y,z@len@axis@facing
                if (a.length > 4 && st.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                    st = st.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.byName(a[4]));
                }
                level.setBlock(at, st, 3);
            }
            log("placed " + len + " " + id + " from " + pos + " along " + axis);
            return;
        }
        if (BuiltInRegistries.ITEM.get(id) instanceof train.common.items.ItemTCRail track) {
            //classic track piece: used on the ground block under the position, the player looking along yaw
            float yaw = a.length > 2 ? Float.parseFloat(a[2]) : 0;
            sp.setYRot(yaw);
            sp.setYHeadRot(yaw);
            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(track, 64);
            sp.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
            if (level.getBlockState(pos.below()).isAir()) level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos.below()), net.minecraft.core.Direction.UP, pos.below(), false);
            var result = track.useOn(new net.minecraft.world.item.context.UseOnContext(level, sp, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit));
            log("use " + id + " at " + pos + " yaw " + yaw + " -> " + result + " " + level.getBlockState(pos).getBlock());
            return;
        }
        for (var type : TiMGenericRegistry.TRANSPORTS) {
            if (BuiltInRegistries.ENTITY_TYPE.getKey(type.get()).equals(id)) {
                GenericRailTransport t = type.get().create(level);
                t.setOwner(sp.getUUID());
                //the placement code picks the direction from where the player stands
                double side = a.length > 2 ? Double.parseDouble(a[2]) : -5;
                sp.teleportTo(pos.getX() + 0.5 + side, pos.getY(), pos.getZ() + 0.5 + side * 0.3);
                boolean ok = CommonUtil.placeOnRail(t, sp, null, level, pos.getX(), pos.getY(), pos.getZ());
                log("transport " + id + " at " + pos + " placed=" + ok + " yaw=" + t.getYRot());
                if (ok) {
                    placed.add(t);
                    //the driven one: the first locomotive placed
                    if (!(train instanceof EntityTrainCore)) train = t;
                }
                return;
            }
        }
        log("unknown " + use);
    }
}
