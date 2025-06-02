package rh.maparthelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;
import rh.maparthelper.SessionVariables;

public class ClientTickHandler {

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            assert client.world != null;

            var pos1 = SessionVariables.selectedPos1;
            var pos2 = SessionVariables.selectedPos2;
            var direction = SessionVariables.selectedDirection;

            if (pos1 == null) return;
            Vec3d offset = pos1.offset(direction, 0.05);

            if (pos2 != null) {
                ParticleUtils.drawSelectionBox(client.world, offset, pos2.offset(direction, 0.05), direction, 0.08);
            } else {
                ParticleUtils.spawnParticle(client.world, new DustParticleEffect(0xbb00aa, 1.0f), offset);
            }
        });
    }
}
