package rh.maparthelper.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.conversion.mapart.ConvertedMapartImage;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.NativeImageUtils;
import rh.maparthelper.colors.MapColorEntry;

@Environment(EnvType.CLIENT)
public class FakeMapsPreview {
    public static boolean createFakeFramesFromMapart(ConvertedMapartImage mapart, ClientPlayerEntity player) {
        if (CurrentConversionSettings.guiMapartImage == null)
            return false;
        removeFakeItemFrames(player.clientWorld);
        int[][] maps = NativeImageUtils.divideMapartByMaps(mapart);
        if (maps == null) return false;
        for (int[] map : maps) {
            addFakeItemFrame(map, player);
        }
        return true;
    }

    public static void removeFakeItemFrames(ClientWorld world) {
        for (ItemFrameEntity itemFrame : ClientCommandsContext.fakeItemFrames)
            world.removeEntity(itemFrame.getId(), Entity.RemovalReason.DISCARDED);
        ClientCommandsContext.fakeItemFrames.clear();
        ClientCommandsContext.fakeFramesBornTime = 0;
    }

    public static void addFakeItemFrame(int[] map, ClientPlayerEntity player) {
        MapState mapState = MapState.of((byte) 1, false, null);
        mapState.colors = new byte[map.length];
        boolean use3D = MapartHelper.conversionSettings.use3D();
        boolean useDithering = MapartHelper.conversionSettings.useDithering();
        for (int i = 0; i < map.length; i++) {
            MapColorEntry color = PaletteColors.getClosestColor(map[i], use3D, useDithering);
            mapState.colors[i] = color.mapColor().getRenderColorByte(color.brightness());
        }

        ItemStack mapItem = new ItemStack(Items.FILLED_MAP);
        MapIdComponent mapId = new MapIdComponent(-1 - ClientCommandsContext.fakeItemFrames.size());
        mapItem.set(DataComponentTypes.MAP_ID, mapId);

        ItemFrameEntity itemFrame = new ItemFrameEntity(player.clientWorld, player.getBlockPos(), player.getMovementDirection().getOpposite());
        itemFrame.setHeldItemStack(mapItem);
        itemFrame.setInvisible(true);

        player.clientWorld.putClientsideMapState(mapId, mapState);

        ClientCommandsContext.fakeItemFrames.add(itemFrame);
    }

    public static void showFakeFrames(ClientPlayerEntity player, int width, int height) {
        BlockPos.Mutable pos = player.getBlockPos().mutableCopy();
        Direction direction = player.getMovementDirection();
        if (direction.getDirection() == Direction.AxisDirection.NEGATIVE)
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
        pos.move(LEFT, (int)(width / 2.0));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ItemFrameEntity itemFrame = ClientCommandsContext.fakeItemFrames.get(x + y * width);
                double posX = LEFT.getOffsetX() * 0.5;
                double posZ = LEFT.getOffsetZ() * 0.5;
                if (LEFT.getDirection() == Direction.AxisDirection.NEGATIVE) {
                    posX = pos.getX() - posX;
                    posZ = pos.getZ() - posZ;
                } else {
                    posX = pos.getX() + posX;
                    posZ = pos.getZ() + posZ;
                }
                posX -= direction.getOffsetX() * 0.03;
                posZ -= direction.getOffsetZ() * 0.03;

                itemFrame.setPos(posX, pos.getY() - 0.5, posZ);
                player.clientWorld.addEntity(itemFrame);
                pos.move(LEFT.getOpposite());
            }
            pos.move(LEFT, width);
            pos.move(Direction.DOWN);
        }
        ClientCommandsContext.fakeFramesBornTime = player.clientWorld.getTime();
    }
}
