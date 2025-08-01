package rh.maparthelper.conversion.schematic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import rh.maparthelper.MapartHelperClient;
import rh.maparthelper.conversion.BlocksPalette;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.conversion.pallete.gson.PaletteConfigManager;
import rh.maparthelper.conversion.staircases.Flat2DStaircase;
import rh.maparthelper.conversion.staircases.IMapartStaircase;

import java.util.List;
import java.util.Objects;

public class NbtSchematicUtils {
    private static NbtCompound createMapArtBaseNbt(int mapWidth, int mapHeight) {
        NbtCompound nbt = new NbtCompound();
        int width = mapWidth * 128;
        int height = mapHeight * 128;

        addPaletteToNbt(nbt);

        NbtList size = new NbtList();
        size.add(NbtInt.of(width));
        size.add(NbtInt.of(MapartHelperClient.conversionConfig.useAuxBlocks == 1 ? 2 : 1));
        size.add(NbtInt.of(height + 1));
        nbt.put("size", size);

        NbtList blocks = new NbtList();
        nbt.put("blocks", blocks);

        String author = MinecraftClient.getInstance().getSession().getUsername() + " // by Mapart Helper";
        nbt.putString("author", author);
        NbtHelper.putDataVersion(nbt);

        return nbt;
    }

    private static void addPaletteToNbt(NbtCompound nbt) {
        NbtList palette = new NbtList();

        // TODO: Not very hard-coding, but it's not very nice to save the whole palette
        List<Block> blocks = PaletteConfigManager.palettePresetsConfig.getCurrentPreset().getBlocks();
        Block auxBlock = Registries.BLOCK.get(Identifier.of(MapartHelperClient.conversionConfig.auxBlock));
        if (!blocks.contains(auxBlock))
            blocks.addFirst(auxBlock);

        for (Block block : blocks) {
            BlockState blockState = BlocksPalette.getDefaultPaletteState(block);
            NbtCompound blockEntry = NbtHelper.fromBlockState(blockState);
            palette.add(blockEntry);
        }

        nbt.put("palette", palette);
    }

    protected static void addBlockToNbt(NbtCompound nbt, int x, int y, int z, Block block) {
        NbtList blocks = Objects.requireNonNull(nbt.get("blocks")).asNbtList().orElse(new NbtList());
        List<Block> blockIds = PaletteConfigManager.palettePresetsConfig.getCurrentPreset().getBlocks();

        NbtCompound entry = new NbtCompound();
        NbtList pos = new NbtList();
        pos.add(NbtInt.of(x));
        pos.add(NbtInt.of(y));
        pos.add(NbtInt.of(z));
        entry.put("pos", pos);
        entry.put("state", NbtInt.of(blockIds.indexOf(block)));
        blocks.add(entry);

        nbt.put("blocks", blocks);
    }

    protected static void addBlockToNbt(NbtCompound nbt, int x, int y, int z, String blockId) {
        Block block = Registries.BLOCK.get(Identifier.of(blockId));
        addBlockToNbt(nbt, x, y, z, block);
    }

    protected static void addColorToNbt(NbtCompound nbt, int x, int y, int z, MapColor color) {
        Block block = PaletteConfigManager.palettePresetsConfig.getCurrentPreset().getBlockByMapColor(color);
        addBlockToNbt(nbt, x, y, z, block);
    }

    protected static NbtCompound createMapArtNbt() {
        int mapWidth = CurrentConversionSettings.width;
        int mapHeight = CurrentConversionSettings.height;
        NbtCompound nbt = createMapArtBaseNbt(mapWidth, mapHeight);

        if (CurrentConversionSettings.guiMapartImage.getImage() != null) {
            int[] colorsRaw = CurrentConversionSettings.guiMapartImage.getImage().copyPixelsArgb();
            int width = mapWidth * 128;
            int height = mapHeight * 128;

            int[][] colors = new int[height][width];
            for (int y = 0; y < height; y++) {
                System.arraycopy(colorsRaw, y * width, colors[y], 0, width);
            }

            // TODO: Hard-coded :p
            IMapartStaircase staircase = new Flat2DStaircase();
            var converted = staircase.getStaircase(colors);

            for (int x = 0; x < converted.getFirst().size(); x++) {
                addBlockToNbt(nbt, x, converted.getFirst().get(x), 0, MapartHelperClient.conversionConfig.auxBlock);
            }

            for (int z = 1; z < converted.size(); z++) {
                for (int x = 0; x < converted.getFirst().size(); x++) {
                    MapColor color = BlocksPalette.getMapColorEntryByARGB(colors[z - 1][x]).mapColor();
                    addColorToNbt(nbt, x, converted.get(z).get(x), z, color);
                }
            }
        }
        return nbt;
    }
}
