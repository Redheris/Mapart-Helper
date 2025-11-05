package rh.maparthelper.util;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rh.maparthelper.MapartHelper;

public class ParticleUtils {

    public static void spawnParticle(World world, ParticleEffect particle, Vec3d pos) {
        world.addParticleClient(particle, pos.x, pos.y, pos.z, 0, 0, 0);
    }

    public static void drawStraightLine(World world, double x1, double y1, double z1,
                                        double x2, double y2, double z2, ParticleEffect particle, double step) {
        int steps = (int) (Math.max(
                Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)),
                Math.abs(z2 - z1)
        ) / step);

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;
            world.addParticleClient(particle, x, y, z, 0, 0, 0);
        }
    }

    public static void drawSelectionBox(World world, Vec3d pos1, Vec3d pos2, Direction direction, double step) {
        if (world == null) return;

        DustParticleEffect particle = new DustParticleEffect(MapartHelper.commonConfig.selectionColor, 0.3f);
        double xMin = Math.min(pos1.x, pos2.x) - 0.5;
        double xMax = Math.max(pos1.x, pos2.x) + 0.5;
        double yMin = Math.min(pos1.y, pos2.y) - 0.5;
        double yMax = Math.max(pos1.y, pos2.y) + 0.5;
        double zMin = Math.min(pos1.z, pos2.z) - 0.5;
        double zMax = Math.max(pos1.z, pos2.z) + 0.5;

        switch (direction.getAxis()) {
            case X -> {
                drawStraightLine(world, pos1.x, yMin, zMin, pos1.x, yMin, zMax, particle, step);
                drawStraightLine(world, pos1.x, yMax, zMin, pos1.x, yMax, zMax, particle, step);
                drawStraightLine(world, pos1.x, yMin, zMin, pos1.x, yMax, zMin, particle, step);
                drawStraightLine(world, pos1.x, yMin, zMax, pos1.x, yMax, zMax, particle, step);
            }
            case Y -> {
                drawStraightLine(world, xMin, pos1.y, zMin, xMax, pos1.y, zMin, particle, step);
                drawStraightLine(world, xMin, pos1.y, zMax, xMax, pos1.y, zMax, particle, step);
                drawStraightLine(world, xMin, pos1.y, zMin, xMin, pos1.y, zMax, particle, step);
                drawStraightLine(world, xMax, pos1.y, zMin, xMax, pos1.y, zMax, particle, step);
            }
            case Z -> {
                drawStraightLine(world, xMin, yMin, pos1.z, xMax, yMin, pos1.z, particle, step);
                drawStraightLine(world, xMin, yMax, pos1.z, xMax, yMax, pos1.z, particle, step);
                drawStraightLine(world, xMin, yMin, pos1.z, xMin, yMax, pos1.z, particle, step);
                drawStraightLine(world, xMax, yMin, pos1.z, xMax, yMax, pos1.z, particle, step);
            }
        }
    }
}
