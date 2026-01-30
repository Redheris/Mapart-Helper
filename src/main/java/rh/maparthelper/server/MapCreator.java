package rh.maparthelper.server;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rh.maparthelper.colors.MapColorEntry;
import rh.maparthelper.config.palette.PaletteColors;

import java.util.ArrayList;
import java.util.List;

public class MapCreator {
    public static void getMapsForMapart(int[][] maps, int mapartWidth, String mapartName, ServerWorld world, ServerPlayerEntity player) {
        if (maps == null) return;
        List<ItemStack> mapItems = new ArrayList<>();

        for (int i = 0; i < maps.length; i++) {
            MapIdComponent mapIdComponent = createMap(maps[i], world);
            MutableText itemName = Text.literal(mapartName).formatted(Formatting.GOLD);
            if (maps.length > 1)
                itemName.append(Text.literal(" [" + i % mapartWidth + " " + i / mapartWidth + "]").formatted(Formatting.GRAY));
            mapItems.add(getMapItemStack(mapIdComponent, itemName));
        }
        if (mapItems.size() == 1) {
            player.giveItemStack(mapItems.getFirst());
        } else {
            ItemStack bundleItem = new ItemStack(Items.YELLOW_BUNDLE);
            BundleContentsComponent bundleContent = new BundleContentsComponent(mapItems);
            bundleItem.set(DataComponentTypes.BUNDLE_CONTENTS, bundleContent);
            Text itemName = Text.literal(mapartName).formatted(Formatting.GOLD)
                    .append(Text.literal(" " + mapartWidth + "x" + (mapItems.size() / mapartWidth)).formatted(Formatting.GRAY));
            bundleItem.set(DataComponentTypes.ITEM_NAME, itemName);
            player.giveItemStack(bundleItem);
        }
    }

    private static ItemStack getMapItemStack(MapIdComponent mapIdComponent, Text itemName) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        itemStack.set(DataComponentTypes.MAP_ID, mapIdComponent);
        itemStack.set(DataComponentTypes.ITEM_NAME, itemName);
        return itemStack;
    }

    private static MapIdComponent createMap(int[] colors, ServerWorld world) {
        MapState mapState = MapState.of((byte) 0, true, world.getRegistryKey());
        mapState.colors = new byte[colors.length];
        for (int i = 0; i < colors.length; i++) {
            MapColorEntry color = PaletteColors.getMapColorEntryByARGB(colors[i]);
            assert color != null;
            mapState.colors[i] = color.mapColor().getRenderColorByte(color.brightness());
        }
        MapIdComponent mapIdComponent = world.increaseAndGetMapId();
        world.putMapState(mapIdComponent, mapState);
        return mapIdComponent;
    }
}
