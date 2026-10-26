package train.blocks.catenary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Every wire strung between masts, substations and generators, with the shape it hangs in: for drawing them, and
 * for finding the one a player hits (any wire breaks where it is hit, wherever along it).
 */
public final class Wires {
    private Wires() {}

    public enum Kind { SPAN, FEEDER, INPUT }

    /** a wire: what it joins (a, b), and the points along it as it hangs */
    public record Wire(Kind kind, BlockPos a, BlockPos b, List<Vec3> points) {}

    /** the points of a cable sagging from a to b */
    public static List<Vec3> sag(Vec3 a, Vec3 b) {
        double len = a.distanceTo(b);
        int n = Math.max(4, (int) Math.ceil(len * 2));
        double sag = Math.min(0.6, 0.015 * len * len / 4 + 0.1);
        List<Vec3> l = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            l.add(a.add(b.subtract(a).scale(t)).add(0, -4 * sag * t * (1 - t), 0));
        }
        return l;
    }

    /** a span's messenger sag (the contact wire under it is level) */
    public static double spanSag(double len) {
        return Math.min(0.45, 0.012 * len * len / 4 + 0.08);
    }

    /** the points of a span: down its contact wire, then back along its messenger */
    private static List<Vec3> span(Vec3 a, Vec3 b) {
        double len = a.distanceTo(b), sag = spanSag(len);
        int n = Math.max(4, (int) Math.ceil(len * 2));
        List<Vec3> l = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            l.add(a.add(b.subtract(a).scale(t)).add(0, CatenaryGeometry.CONTACT_Y, 0));
        }
        for (int i = n; i >= 0; i--) {
            double t = i / (double) n;
            l.add(a.add(b.subtract(a).scale(t)).add(0, CatenaryGeometry.MESSENGER_Y - 4 * sag * t * (1 - t), 0));
        }
        return l;
    }

    /** the wires within r blocks of p */
    public static List<Wire> near(Level level, Vec3 p, double r) {
        List<Wire> out = new ArrayList<>();
        for (CatenaryMastTile m : CatenaryMastTile.loaded(level)) {
            if (m.isRemoved() || m.getLevel() != level) continue;
            BlockPos pos = m.getBlockPos();
            if (pos.distToCenterSqr(p) > (r + CatenaryMastTile.MAX_SPAN) * (r + CatenaryMastTile.MAX_SPAN)) continue;
            for (BlockPos o : m.links) {
                if (o.asLong() < pos.asLong()) continue;
                var os = level.getBlockState(o);
                if (!(os.getBlock() instanceof CatenaryMastBlock)) continue;
                out.add(new Wire(Kind.SPAN, pos, o, span(CatenaryGeometry.anchor(pos, m.getBlockState(), 0), CatenaryGeometry.anchor(o, os, 0))));
            }
        }
        for (SubstationTile s : SubstationTile.loaded(level)) {
            if (s.isRemoved() || s.getLevel() != level) continue;
            if (s.getBlockPos().distToCenterSqr(p) > (r + 24) * (r + 24)) continue;
            for (BlockPos m : s.feeders) {
                if (level.getBlockEntity(m) instanceof CatenaryMastTile mast) {
                    out.add(new Wire(Kind.FEEDER, s.getBlockPos(), m, sag(s.feederPoint(), mast.feederPoint())));
                }
            }
            int i = 0, n = s.inputs.size();
            for (BlockPos g : s.inputs) {
                out.add(new Wire(Kind.INPUT, s.getBlockPos(), g, sag(generatorPoint(level, g), s.inputPoint(i++, n))));
            }
        }
        return out;
    }

    /** where a power line leaves a generator: a windmill's tower under its wheel, anything else its top */
    public static Vec3 generatorPoint(Level level, BlockPos g) {
        boolean windmill = level.getBlockEntity(g) instanceof train.blocks.machines.MachineTile m && m.kind.equals("windmill");
        return new Vec3(g.getX() + 0.5, g.getY() + (windmill ? 1.25 : 1.0), g.getZ() + 0.5);
    }

    /** the wire a ray from 'from' along 'dir' hits first within reach (thick as 'width'), and where along it */
    public record Hit(Wire wire, Vec3 at, double distance) {}

    public static Hit pick(Level level, Vec3 from, Vec3 dir, double reach, double width) {
        Hit best = null;
        Vec3 d = dir.normalize();
        for (Wire w : near(level, from, reach)) {
            List<Vec3> pts = w.points();
            for (int i = 0; i + 1 < pts.size(); i++) {
                Vec3 a = pts.get(i), b = pts.get(i + 1);
                //the closest points of the ray and the segment
                Vec3 u = b.subtract(a), w0 = from.subtract(a);
                double uu = u.dot(u), ud = u.dot(d), uw = u.dot(w0), dw = d.dot(w0);
                double den = uu - ud * ud;
                double s = den < 1e-9 ? 0 : Math.max(0, Math.min(1, (uw - ud * dw) / den));
                double t = Math.max(0, s * ud - dw);
                Vec3 onSeg = a.add(u.scale(s)), onRay = from.add(d.scale(t));
                if (t > reach || onSeg.distanceTo(onRay) > width) continue;
                if (best == null || t < best.distance()) best = new Hit(w, onSeg, t);
            }
        }
        return best;
    }

    /** cuts a wire (server): the two ends let go of each other; true if there was one */
    public static boolean cut(Level level, Kind kind, BlockPos a, BlockPos b) {
        switch (kind) {
            case SPAN -> {
                if (level.getBlockEntity(a) instanceof CatenaryMastTile ma && ma.links.contains(b)) {
                    ma.unlink(b);
                    if (level.getBlockEntity(b) instanceof CatenaryMastTile mb) mb.unlink(a);
                    return true;
                }
            }
            case FEEDER -> {
                if (level.getBlockEntity(a) instanceof SubstationTile s && s.feeders.contains(b)) {
                    s.removeFeeder(b);
                    if (level.getBlockEntity(b) instanceof CatenaryMastTile m) m.setFeeder(null);
                    return true;
                }
            }
            case INPUT -> {
                if (level.getBlockEntity(a) instanceof SubstationTile s && s.inputs.contains(b)) {
                    s.removeInput(b);
                    return true;
                }
            }
        }
        return false;
    }
}
