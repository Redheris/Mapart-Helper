package rh.maparthelper.conversion.schematic;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.Shapes;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.UseAuxBlocks;
import rh.maparthelper.config.palette.PaletteColors;
import rh.maparthelper.config.palette.PaletteConfigManager;
import rh.maparthelper.config.palette.PaletteGenerator;
import rh.maparthelper.conversion.staircases.StaircaseStyles;

import java.util.ArrayList;
import java.util.List;

public class MapartSchematicBuilder {
    private final CompoundTag nbt = new CompoundTag();
    private final ListTag blocks = new ListTag();
    private final int[][] mapColors;
    private final int xSize;
    private int ySize = 1;
    private final int zSize;
    private final List<Block> blocks_palette = new ArrayList<>();
    private final boolean[] prevRowSupportBlock;
    private List<List<Integer>> heightsZX;

    private final StaircaseStyles staircaseStyle = MapartHelper.conversionConfig().getStaircaseStyle();
    private final UseAuxBlocks useAuxBlocks = MapartHelper.conversionConfig().getUseAuxBlocks();
    private final Block auxBlock = MapartHelper.conversionConfig().getAuxBlock();

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

    public CompoundTag build() {
        if (staircaseStyle != StaircaseStyles.FLAT_2D)
            heightsZX = staircaseStyle.getStaircase(mapColors);
        for (int z = 0; z < zSize; z++) {
            for (int x = 0; x < xSize; x++) {
                int y = staircaseStyle == StaircaseStyles.FLAT_2D ? 0 : heightsZX.get(z).get(x);
                if (z == 0)
                    addBlock(auxBlock, x, y, z);
                else {
                    MapColor mapColor = PaletteColors.getMapColorEntryByARGB(mapColors[z - 1][x]).mapColor();
                    if (mapColor != MapColor.NONE)
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
        boolean needsSupport = shouldPlaceAuxBlock(colorBlock);
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
        CompoundTag entry = new CompoundTag();
        ListTag pos = new ListTag();
        pos.add(IntTag.valueOf(x));
        pos.add(IntTag.valueOf(y));
        pos.add(IntTag.valueOf(z));
        entry.put("pos", pos);
        int blockId = getBlockMaterialId(block);
        entry.put("state", IntTag.valueOf(blockId));
        blocks.add(entry);
        ySize = Math.max(y + 1, ySize);
    }

    private void addPaletteAndBlocks() {
        ListTag palette = new ListTag();
        for (Block block : blocks_palette) {
            BlockState blockState = PaletteGenerator.getDefaultPaletteState(block);
            CompoundTag blockEntry = NbtUtils.writeBlockState(blockState);
            palette.add(blockEntry);
        }
        nbt.put("palette", palette);
        nbt.put("blocks", blocks);
    }

    private void addSize() {
        ListTag size = new ListTag();
        size.add(IntTag.valueOf(xSize));
        size.add(IntTag.valueOf(ySize));
        size.add(IntTag.valueOf(zSize));
        nbt.put("size", size);
    }

    private void addBaseMetadata() {
        String author = Minecraft.getInstance().getUser().getName() + " // using Mapart Helper";
        nbt.putString("author", author);
        NbtUtils.addCurrentDataVersion(nbt);
    }

    public static boolean shouldPlaceAuxBlock(Block block) {
        if (MapartHelper.conversionConfig().getUseAuxBlocks() == UseAuxBlocks.NO_AUX || block.defaultBlockState().isAir())
            return false;
        return needsAuxBlock(block);
    }

    public static boolean needsAuxBlock(Block block) {
        boolean canPlaceAtAir = block.defaultBlockState().canSurvive(DummyWorldView.getInstance(), BlockPos.ZERO);
        boolean hasNoCollision = block.defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) == Shapes.empty();
        return !canPlaceAtAir || block instanceof FallingBlock || hasNoCollision;
    }
}
