package rh.maparthelper.conversion.schematic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.UseAuxBlocks;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PaletteGenerator;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

import java.util.ArrayList;
import java.util.List;

public class MapartSchematicBuilder {
    private final NbtCompound nbt = new NbtCompound();
    private final NbtList blocks = new NbtList();
    private final int[][] mapColors;
    private final int xSize;
    private int ySize = 1;
    private final int zSize;
    private final List<Block> blocks_palette = new ArrayList<>();
    private final boolean[] prevRowSupportBlock;
    private List<List<Integer>> heightsZX;

    private final StaircaseStyles staircaseStyle = MapartHelper.conversionSettings.staircaseStyle;
    private final UseAuxBlocks useAuxBlocks = MapartHelper.conversionSettings.useAuxBlocks;
    private final Block auxBlock = MapartHelper.conversionSettings.auxBlock;

    public MapartSchematicBuilder(int[] mapColors, int mapsWidth, int mapsHeight) {
        this.xSize = mapsWidth * 128;
        this.zSize = mapsHeight * 128 + 1;
        this.prevRowSupportBlock = new boolean[xSize];
        this.mapColors = new int[zSize - 1][xSize];
        for (int z = 0; z < zSize - 1; z++) {
            System.arraycopy(mapColors, z * xSize, this.mapColors[z], 0, xSize);
        }
        addBaseMetadata();
    }

    public NbtCompound build() {
        heightsZX = staircaseStyle.getStaircase(mapColors);
        for (int z = 0; z < zSize; z++) {
            for (int x = 0; x < xSize; x++) {
                int y = staircaseStyle == StaircaseStyles.FLAT_2D ? 0 : heightsZX.get(z).get(x);
                if (z == 0)
                    addBlock(auxBlock, x, y, z);
                else {
                    MapColor mapColor = PaletteColors.getMapColorEntryByARGB(mapColors[z - 1][x]).mapColor();
                    if (mapColor != MapColor.CLEAR)
                        addColor(mapColor, x, y, z);
                }
            }
        }
        addSize();
        addPaletteAndBlocks();
        return nbt;
    }

    private int getBlockMaterialId(Block block) {
        int ind = blocks_palette.indexOf(block);
        if (ind != -1) return ind;
        blocks_palette.add(block);
        return blocks_palette.size() - 1;
    }

    private void addColor(MapColor mapColor, int x, int y, int z) {
        Block block = PaletteConfigManager.presetsConfig.getBlockOfMapColor(mapColor);
        addBlock(block, x, y, z);
        if (useAuxBlocks != UseAuxBlocks.NO_AUX)
            addAuxBlocksForColor(block, x, y, z);
    }

    private void addAuxBlocksForColor(Block colorBlock, int colorX, int colorY, int colorZ) {
        boolean needsSupport = needsAuxBlock(colorBlock);
        if (colorY > 0 && needsSupport)
            addBlock(auxBlock, colorX, colorY - 1, colorZ);

        if (useAuxBlocks == UseAuxBlocks.ALL && staircaseStyle != StaircaseStyles.FLAT_2D) {
            int yDiff = heightsZX.get(colorZ - 1).get(colorX) - colorY;
            if (yDiff == 1) {
                if (needsSupport && colorY > 0)
                    addBlock(auxBlock, colorX, colorY - 1, colorZ - 1);
                if (!prevRowSupportBlock[colorX])
                    addBlock(auxBlock, colorX, colorY, colorZ - 1);
            } else if (yDiff == -1) {
                if (!needsSupport && colorY > 0)
                    addBlock(auxBlock, colorX, colorY - 1, colorZ);
                if (prevRowSupportBlock[colorX] && colorY > 1)
                    addBlock(auxBlock, colorX, colorY - 2, colorZ);
            } else if (yDiff == 0 && colorY > 0) {
                if (needsSupport) {
                    if (!prevRowSupportBlock[colorX] && (colorZ == 1 || heightsZX.get(colorZ - 2).get(colorX) - colorY != -1))
                        addBlock(auxBlock, colorX, colorY - 1, colorZ - 1);
                } else if (prevRowSupportBlock[colorX])
                    addBlock(auxBlock, colorX, colorY - 1, colorZ);
            }
            prevRowSupportBlock[colorX] = needsSupport;
        }
    }

    private void addBlock(Block block, int x, int y, int z) {
        NbtCompound entry = new NbtCompound();
        NbtList pos = new NbtList();
        pos.add(NbtInt.of(x));
        pos.add(NbtInt.of(y));
        pos.add(NbtInt.of(z));
        entry.put("pos", pos);
        int blockId = getBlockMaterialId(block);
        entry.put("state", NbtInt.of(blockId));
        blocks.add(entry);
        ySize = Math.max(y + 1, ySize);
    }

    private void addPaletteAndBlocks() {
        NbtList palette = new NbtList();
        for (Block block : blocks_palette) {
            BlockState blockState = PaletteGenerator.getDefaultPaletteState(block);
            NbtCompound blockEntry = NbtHelper.fromBlockState(blockState);
            palette.add(blockEntry);
        }
        nbt.put("palette", palette);
        nbt.put("blocks", blocks);
    }

    private void addSize() {
        NbtList size = new NbtList();
        size.add(NbtInt.of(xSize));
        size.add(NbtInt.of(ySize));
        size.add(NbtInt.of(zSize));
        nbt.put("size", size);
    }

    private void addBaseMetadata() {
        String author = MinecraftClient.getInstance().getSession().getUsername() + " // using Mapart Helper";
        nbt.putString("author", author);
        NbtHelper.putDataVersion(nbt);
    }

    public static boolean needsAuxBlock(Block block) {
        if (MapartHelper.conversionSettings.useAuxBlocks == UseAuxBlocks.NO_AUX) return false;
        boolean canPlaceAtAir = block.getDefaultState().canPlaceAt(DummyWorldView.getInstance(), BlockPos.ORIGIN);
        boolean hasNoCollision = block.getDefaultState().getCollisionShape(null, null) == VoxelShapes.empty();
        return !canPlaceAtAir || block instanceof FallingBlock || hasNoCollision;
    }
}
