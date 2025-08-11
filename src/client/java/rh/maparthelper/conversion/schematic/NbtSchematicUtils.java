package rh.maparthelper.conversion.schematic;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.shape.VoxelShapes;
import rh.maparthelper.MapartHelperClient;
import rh.maparthelper.conversion.CurrentConversionSettings;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.config.palette.PaletteGenerator;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

import java.util.*;

public class NbtSchematicUtils {
    private static final Map<Integer, Integer> material_list = new HashMap<>();
    private static final List<Block> blocks_list = new ArrayList<>();

    public static List<Map.Entry<Block, Integer>> getMaterialList(boolean fromMostFreq) {
        Map<Block, Integer> blocksCount = new HashMap<>();
        material_list.forEach((k, v) -> blocksCount.put(blocks_list.get(k), v));
        List<Map.Entry<Block, Integer>> list = blocksCount.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .toList();
        return fromMostFreq ? list.reversed() : list;
    }

    private static int getBlockMaterialId(Block block) {
        int ind = blocks_list.indexOf(block);

        if (ind != -1) return ind;

        blocks_list.add(block);
        return blocks_list.size() - 1;

    }

    private static NbtCompound createMapartBaseNbt() {
        NbtCompound nbt = new NbtCompound();

        String author = MinecraftClient.getInstance().getSession().getUsername() + " // by Mapart Helper";
        nbt.putString("author", author);
        NbtHelper.putDataVersion(nbt);

        return nbt;
    }

    private static void addSizeToNbt(NbtCompound nbt, int x, int y, int z) {
        NbtList size = new NbtList();
        size.add(NbtInt.of(x));
        size.add(NbtInt.of(y));
        size.add(NbtInt.of(z));
        nbt.put("size", size);
    }

    private static void addPaletteToNbt(NbtCompound nbt) {
        NbtList palette = new NbtList();

        for (Block block : blocks_list) {
            BlockState blockState = PaletteGenerator.getDefaultPaletteState(block);
            NbtCompound blockEntry = NbtHelper.fromBlockState(blockState);
            palette.add(blockEntry);
        }

        nbt.put("palette", palette);
    }

    protected static void addBlockToNbt(NbtCompound nbt, int x, int y, int z, Block block) {
        NbtList blocks = nbt.contains("blocks") ? Objects.requireNonNull(nbt.get("blocks")).asNbtList().orElse(new NbtList()) : new NbtList();

        NbtCompound entry = new NbtCompound();
        NbtList pos = new NbtList();
        pos.add(NbtInt.of(x));
        pos.add(NbtInt.of(y));
        pos.add(NbtInt.of(z));
        entry.put("pos", pos);
        int blockId = getBlockMaterialId(block);
        entry.put("state", NbtInt.of(blockId));
        blocks.add(entry);

        material_list.merge(blockId, 1, Integer::sum);

        nbt.put("blocks", blocks);
    }

    protected static void addColorToNbt(NbtCompound nbt, int x, int y, int z, MapColor color) {
        Block block = PaletteConfigManager.palettePresetsConfig.getBlockOfMapColor(color);
        addBlockToNbt(nbt, x, y, z, block);
        if (y == 0) return;
        int usingAuxMode = MapartHelperClient.conversionConfig.useAuxBlocks;
        if (usingAuxMode == 0 && (block instanceof FallingBlock || block.getDefaultState().getCollisionShape(null, null) == VoxelShapes.empty()))
            addBlockToNbt(nbt, x, y - 1, z, MapartHelperClient.conversionConfig.auxBlock);
    }

    protected static NbtCompound createMapartNbt(int[] map, int mapsWidth, int mapsHeight) {
        material_list.clear();
        blocks_list.clear();
        NbtCompound nbt = createMapartBaseNbt();

        int width = mapsWidth * 128;
        int height = mapsHeight * 128;

        int[][] colors = new int[height][width];
        for (int y = 0; y < height; y++) {
            System.arraycopy(map, y * width, colors[y], 0, width);
        }

        StaircaseStyles staircase = MapartHelperClient.conversionConfig.staircaseStyle;
        if (staircase == StaircaseStyles.FLAT_2D) {
            for (int x = 0; x < width; x++) {
                addBlockToNbt(nbt, x, 0, 0, MapartHelperClient.conversionConfig.auxBlock);
            }
            for (int z = 1; z < height + 1; z++) {
                for (int x = 0; x < width; x++) {
                    MapColor color = PaletteColors.getMapColorEntryByARGB(colors[z - 1][x]).mapColor();
                    if (color != MapColor.CLEAR)
                        addColorToNbt(nbt, x, 0, z, color);
                    else
                        addBlockToNbt(nbt, x, 0, z, Blocks.GLASS);
                }
            }
            addSizeToNbt(nbt, width, 1, height + 1);
        } else {
            int maxHeight = 1;
            List<List<Integer>> converted = staircase.getStaircase(colors);

            for (int x = 0; x < converted.getFirst().size(); x++) {
                int y = converted.getFirst().get(x);
                maxHeight = Math.max(y, maxHeight);

                addBlockToNbt(nbt, x, y, 0, MapartHelperClient.conversionConfig.auxBlock);
            }
            for (int z = 1; z < converted.size(); z++) {
                for (int x = 0; x < converted.getFirst().size(); x++) {
                    int y = converted.get(z).get(x);
                    maxHeight = Math.max(y, maxHeight);

                    MapColor color = PaletteColors.getMapColorEntryByARGB(colors[z - 1][x]).mapColor();
                    if (color != MapColor.CLEAR)
                        addColorToNbt(nbt, x, y, z, color);
                    else
                        addBlockToNbt(nbt, x, 0, z, Blocks.GLASS);
                }
            }
            addSizeToNbt(nbt, width, maxHeight + 1, height + 1);
        }


        addPaletteToNbt(nbt);

        return nbt;
    }

    protected static NbtCompound createMapartNbt() {
        int mapsWidth = CurrentConversionSettings.getWidth();
        int mapsHeight = CurrentConversionSettings.getHeight();
        if (CurrentConversionSettings.guiMapartImage.getImage() != null) {
            return createMapartNbt(CurrentConversionSettings.guiMapartImage.getImage().copyPixelsArgb(), mapsWidth, mapsHeight);
        }
        return createMapartBaseNbt();
    }
}
