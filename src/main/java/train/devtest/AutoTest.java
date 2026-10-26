package train.devtest;

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
        log("autotest on: " + p);
    }

    private static int i(String key, int def) {
        return p.containsKey(key) ? Integer.parseInt(p.getProperty(key).trim()) : def;
    }

    private static void log(String s) {
        System.out.println("[TCTEST] " + s);
    }

    private static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!started && mc.screen instanceof TitleScreen) {
            started = true;
            String name = "tctest";
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
        if (p.containsKey("fleet") && base != null) {
            //fleet=accel,startTick  every locomotive placed (gallery) gets its fuel at 30, starts at 40 and drives
            //from startTick on; logs at the end what each did
            String[] f = p.getProperty("fleet").split(",");
            int accel = Integer.parseInt(f[0]), go = Integer.parseInt(f[1]);
            if (tick == 30) server.execute(() -> {
                for (GenericRailTransport t : placed) {
                    if (!(t instanceof EntityTrainCore)) continue;
                    var types = t.getTypes();
                    if (types.contains(ebf.tim.TrainsInMotion.transportTypes.STEAM)) {
                        put(t, 400, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, 64));
                        put(t, 401, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
                    } else if (types.contains(ebf.tim.TrainsInMotion.transportTypes.DIESEL)) {
                        put(t, 400, new net.minecraft.world.item.ItemStack(ebf.tim.registry.TiMItems.dieselCanister.get(), 16));
                    } else if (types.contains(ebf.tim.TrainsInMotion.transportTypes.ELECTRIC)) {
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
                    if (t.getTypes().contains(ebf.tim.TrainsInMotion.transportTypes.STEAM)) {
                        var slot = t.getSlotIndexByID(401);
                        if (slot != null && !slot.getHasStack()) slot.setStack(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET));
                    }
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
                b.append(String.format(" [%s x=%.2f v=%.3f f=%s b=%s c=%d]", t.getClass().getSimpleName().replace("Entity", ""),
                        t.getX() - base.getX(), t.getVelocity(), t.frontLinkedID, t.backLinkedID, t.getConsist().size()));
            }
            log("consist t=" + tick + b);
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
                        sp.teleportTo(level, base.getX() + x + 0.5, base.getY(), base.getZ() - 6.5, 0, 6);
                    });
                }
                if (phase == rowEvery - 3) {
                    String name = rowNames.get(k);
                    Screenshot.grab(mc.gameDirectory, "row-" + name + ".png", mc.getMainRenderTarget(), c -> {});
                }
            }
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
                level.setBlock(at.below(), Blocks.STONE.defaultBlockState(), 3);
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
        if (BuiltInRegistries.BLOCK.containsKey(id)) {
            Block b = BuiltInRegistries.BLOCK.get(id);
            int len = a.length > 2 ? Integer.parseInt(a[2]) : 1;
            String axis = a.length > 3 ? a[3] : "x";
            for (int n = 0; n < len; n++) {
                BlockPos at = axis.equals("x") ? pos.offset(n, 0, 0) : pos.offset(0, 0, n);
                level.setBlock(at.below(), Blocks.STONE.defaultBlockState(), 3);
                level.setBlock(at, b.defaultBlockState(), 3);
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
            level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
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
