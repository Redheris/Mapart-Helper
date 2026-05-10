package rh.maparthelper.util;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rh.maparthelper.MapartHelper;

public class ParticleUtils {

    public static void spawnParticle(Level world, ParticleOptions particle, Vec3 pos) {
        world.addParticle(particle, true, true, pos.x, pos.y, pos.z, 0, 0, 0);
    }

    public static void drawStraightLine(Level world, double x1, double y1, double z1,
                                        double x2, double y2, double z2, ParticleOptions particle, double step) {
        int steps = (int) (Math.max(
                Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)),
                Math.abs(z2 - z1)
        ) / step);

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;
            world.addParticle(particle, true, true, x, y, z, 0, 0, 0);
        }
    }

    public static void drawSelectionBox(Level world, Vec3 pos1, Vec3 pos2, Direction direction) {
        if (world == null) return;

        float scale = pos1.distanceTo(pos2) > 4 ? 0.6f : 0.4f;
        DustParticleOptions particle = new DustParticleOptions(MapartHelper.commonConfig().selectionColor.getRGB(), scale);

        double xMin = Math.min(pos1.x, pos2.x) - 0.5;
        double xMax = Math.max(pos1.x, pos2.x) + 0.5;
        double stepX = xMax - xMin > 10 ? 0.35 : 0.2;

        double yMin = Math.min(pos1.y, pos2.y) - 0.5;
        double yMax = Math.max(pos1.y, pos2.y) + 0.5;
        double stepY = yMax - yMin > 10 ? 0.35 : 0.2;

        double zMin = Math.min(pos1.z, pos2.z) - 0.5;
        double zMax = Math.max(pos1.z, pos2.z) + 0.5;
        double stepZ = zMax - zMin > 10 ? 0.35 : 0.2;

        switch (direction.getAxis()) {
            case X -> {
                drawStraightLine(world, pos1.x, yMin, zMin, pos1.x, yMin, zMax, particle, stepZ);
                drawStraightLine(world, pos1.x, yMax, zMin, pos1.x, yMax, zMax, particle, stepZ);
                drawStraightLine(world, pos1.x, yMin, zMin, pos1.x, yMax, zMin, particle, stepY);
                drawStraightLine(world, pos1.x, yMin, zMax, pos1.x, yMax, zMax, particle, stepY);
            }
            case Y -> {
                drawStraightLine(world, xMin, pos1.y, zMin, xMax, pos1.y, zMin, particle, stepX);
                drawStraightLine(world, xMin, pos1.y, zMax, xMax, pos1.y, zMax, particle, stepX);
                drawStraightLine(world, xMin, pos1.y, zMin, xMin, pos1.y, zMax, particle, stepZ);
                drawStraightLine(world, xMax, pos1.y, zMin, xMax, pos1.y, zMax, particle, stepZ);
            }
            case Z -> {
                drawStraightLine(world, xMin, yMin, pos1.z, xMax, yMin, pos1.z, particle, stepX);
                drawStraightLine(world, xMin, yMax, pos1.z, xMax, yMax, pos1.z, particle, stepX);
                drawStraightLine(world, xMin, yMin, pos1.z, xMin, yMax, pos1.z, particle, stepY);
                drawStraightLine(world, xMax, yMin, pos1.z, xMax, yMax, pos1.z, particle, stepY);
            }
        }
    }
}
