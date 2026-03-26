package rh.maparthelper.server;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteColors;

import java.util.ArrayList;
import java.util.List;

//? if >=26.1 {
/*import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStackTemplate;
*///}

public class MapCreator {
    public static void getMapsForMapart(int[][] maps, int mapartWidth, String mapartName, ServerLevel world, ServerPlayer player) {
        if (maps == null) return;
        //? <26.1
        List<ItemStack> mapItems = new ArrayList<>();
        //? >= 26.1
        //List<ItemStackTemplate> mapItems = new ArrayList<>();

        for (int i = 0; i < maps.length; i++) {
            MapId mapIdComponent = createMap(maps[i], world);
            MutableComponent itemName = Component.literal(mapartName).withStyle(ChatFormatting.GOLD);
            if (maps.length > 1)
                itemName.append(Component.literal(" [" + i % mapartWidth + " " + i / mapartWidth + "]").withStyle(ChatFormatting.GRAY));
            //? <26.1
            mapItems.add(getMapItemStack(mapIdComponent, itemName));
            //? >= 26.1
            //mapItems.add(getMapItemStackTemplate(mapIdComponent, itemName));
        }
        if (mapItems.size() == 1) {
            //? <26.1
            player.addItem(mapItems.getFirst());
            //? >= 26.1
            //player.addItem(mapItems.getFirst().create());
        } else {
            ItemStack bundleItem = new ItemStack(Items.YELLOW_BUNDLE);
            BundleContents bundleContent = new BundleContents(mapItems);
            bundleItem.set(DataComponents.BUNDLE_CONTENTS, bundleContent);
            Component itemName = Component.literal(mapartName).withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(" " + mapartWidth + "x" + (mapItems.size() / mapartWidth)).withStyle(ChatFormatting.GRAY));
            bundleItem.set(DataComponents.ITEM_NAME, itemName);
            player.addItem(bundleItem);
        }
    }

    public static ItemStack getMapItemStack(MapId mapIdComponent, Component itemName) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        itemStack.set(DataComponents.MAP_ID, mapIdComponent);
        itemStack.set(DataComponents.ITEM_NAME, itemName);
        return itemStack;
    }

    //? if >=26.1 {
    /*public static ItemStackTemplate getMapItemStackTemplate(MapId mapIdComponent, Component itemName) {
        DataComponentPatch patch = DataComponentPatch.builder()
                .set(DataComponents.MAP_ID, mapIdComponent)
                .set(DataComponents.ITEM_NAME, itemName)
                .build();
        return new ItemStackTemplate(Items.FILLED_MAP, patch);
    }
    *///?}

    public static MapId createMap(int[] colors, ServerLevel world) {
        MapItemSavedData mapState = MapItemSavedData.createForClient((byte) 0, true, world.dimension());
        mapState.colors = new byte[colors.length];
        for (int i = 0; i < colors.length; i++) {
            MapColorEntry color = PaletteColors.getMapColorEntryByARGB(colors[i]);
            assert color != null;
            mapState.colors[i] = color.mapColor().getPackedId(color.brightness());
        }
        MapId mapIdComponent = world.getFreeMapId();
        world.setMapData(mapIdComponent, mapState);
        return mapIdComponent;
    }

    public static MapId createMap(byte[] mapColors, ServerLevel world) {
        MapItemSavedData mapState = MapItemSavedData.createForClient((byte) 0, true, world.dimension());
        mapState.colors = new byte[mapColors.length];
        System.arraycopy(mapColors, 0, mapState.colors, 0, mapColors.length);
        MapId mapIdComponent = world.getFreeMapId();
        world.setMapData(mapIdComponent, mapState);
        return mapIdComponent;
    }
}
