package rh.maparthelper.conversion.schematic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.MapartHelperClient;

public class NbtSchematicUtils {
    protected static NbtCompound createMapArtBaseNbt(int mapWidth, int mapHeight) {
        NbtCompound nbt = new NbtCompound();
        int width = mapWidth * 128;
        int height = mapHeight * 128;

        //==========  Aux block  =============
        NbtList list = new NbtList();
        NbtCompound blockEntry = new NbtCompound();
        blockEntry.put("Name", NbtString.of(MapartHelper.config.conversionSettings.auxBlock));
        list.add(blockEntry);
        nbt.put("palette", list);
        //====================================
        //===========   Size   ===============
        NbtList size = new NbtList();
        size.add(NbtInt.of(width));
        size.add(NbtInt.of(MapartHelperClient.conversionConfig.useAuxBlocks == 1 ? 2 : 1));
        size.add(NbtInt.of(height + 1));
        nbt.put("size", size);
        //====================================
        //========  Northern line  ===========
        NbtList blocks = new NbtList();
        for (int i = 0; i < size.getInt(0, 1); i++) {
            NbtCompound entry = new NbtCompound();
            NbtList pos = new NbtList();
            pos.add(NbtInt.of(i));
            pos.add(NbtInt.of(0));
            pos.add(NbtInt.of(0));
            entry.put("pos", pos);
            entry.put("state", NbtInt.of(0));
            blocks.add(entry);
        }
        nbt.put("blocks", blocks);
        //====================================
        //===========   Info   ==============
        String author = MinecraftClient.getInstance().getSession().getUsername() + " // by MapArt Helper";
        nbt.putString("author", author);
        nbt.putInt("DataVersion", 3105);
        //====================================

        return nbt;
    }
}
