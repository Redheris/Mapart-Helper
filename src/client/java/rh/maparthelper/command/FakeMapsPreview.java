package rh.maparthelper.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.NativeImageUtils;
import rh.maparthelper.mapart.MapartProcessing;

@Environment(EnvType.CLIENT)
public class FakeMapsPreview {
    public static boolean createFakeFramesFromMapart(MapartProcessing mapart, LocalPlayer player) {
        if (mapart.getNativeImage() == null) return false;

        removeFakeItemFrames((ClientLevel) player.level());
        int[][] maps = NativeImageUtils.divideImageByMaps(mapart.getNativeImage());

        for (int[] map : maps) {
            addFakeItemFrame(map, player);
        }

        return true;
    }

    public static void removeFakeItemFrames(ClientLevel world) {
        for (ItemFrame itemFrame : ClientCommandsContext.fakeItemFrames)
            world.removeEntity(itemFrame.getId(), Entity.RemovalReason.DISCARDED);
        ClientCommandsContext.fakeItemFrames.clear();
        ClientCommandsContext.fakeFramesBornTime = 0;
    }

    public static void addFakeItemFrame(int[] map, LocalPlayer player) {
        ClientLevel clientLevel = (ClientLevel) player.level();
        MapItemSavedData mapState = MapItemSavedData.createForClient((byte) 1, false, clientLevel.dimension());
        mapState.colors = new byte[map.length];
        for (int i = 0; i < map.length; i++) {
            MapColorEntry color = PaletteColors.getMapColorEntryByARGB(map[i]);
            mapState.colors[i] = color.mapColor().getPackedId(color.brightness());
        }

        ItemStack mapItem = new ItemStack(Items.FILLED_MAP);
        MapId mapId = new MapId(-1 - ClientCommandsContext.fakeItemFrames.size());
        mapItem.set(DataComponents.MAP_ID, mapId);

        ItemFrame itemFrame = new ItemFrame(clientLevel, player.blockPosition(), player.getMotionDirection().getOpposite());
        itemFrame.setItem(mapItem);
        itemFrame.setInvisible(true);

        clientLevel.overrideMapData(mapId, mapState);

        ClientCommandsContext.fakeItemFrames.add(itemFrame);
    }

    public static void showFakeFrames(LocalPlayer player, int width, int height) {
        @SuppressWarnings("resource") ClientLevel clientLevel = (ClientLevel) player.level();
        BlockPos.MutableBlockPos pos = player.blockPosition().mutable();
        Direction direction = player.getMotionDirection();
        if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            pos.move(direction);
        else
            pos.move(direction, 2);
        if (height == 1)
            pos.move(Direction.UP, 2);
        else
            pos.move(Direction.UP, height);

        Direction LEFT = Direction.WEST;
        switch (direction) {
            case EAST -> LEFT = Direction.NORTH;
            case SOUTH -> LEFT = Direction.EAST;
            case WEST -> LEFT = Direction.SOUTH;
        }
        double localOffset = Math.abs(player.position().get(LEFT.getAxis()) - pos.get(LEFT.getAxis()));
        double leftOffset;
        if (LEFT.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            leftOffset = width % 2 == 1 || localOffset < 0.5 ? 0 : -1;
        else
            leftOffset = width % 2 == 1 || localOffset < 0.5 ? -1 : 0;

        pos.move(LEFT, (int) (width / 2.0 + leftOffset));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemFrame itemFrame = ClientCommandsContext.fakeItemFrames.get(x + y * width);
                double posX = LEFT.getStepX() * 0.5;
                double posZ = LEFT.getStepZ() * 0.5;
                if (LEFT.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                    posX = pos.getX() - posX;
                    posZ = pos.getZ() - posZ;
                } else {
                    posX = pos.getX() + posX;
                    posZ = pos.getZ() + posZ;
                }
                posX -= direction.getStepX() * 0.03;
                posZ -= direction.getStepZ() * 0.03;

                double yOffset = height == 2 ? 0.5 : -0.5;
                itemFrame.setPosRaw(posX, pos.getY() + yOffset, posZ);
                clientLevel.addEntity(itemFrame);
                pos.move(LEFT.getOpposite());
            }
            pos.move(LEFT, width);
            pos.move(Direction.DOWN);
        }
        ClientCommandsContext.fakeFramesBornTime = clientLevel.getGameTime();
    }
}
