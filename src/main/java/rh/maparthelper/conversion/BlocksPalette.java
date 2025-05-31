package rh.maparthelper.conversion;

import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import rh.maparthelper.MapartHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BlocksPalette {
    // Lists of block classes for blocking/enabling by configs
    private static final Class<?>[] NEED_WATER_BLOCKS;
    private static final Class<?>[] MEANINGLESS_BLOCKS;
    private static final Class<?>[] CREATIVE_BLOCKS;
    private static final Class<?>[] GROWABLE_BLOCKS;
    private static final Class<?>[] GRASS_LIKE_BLOCKS;
    private static final Class<?>[] BUILD_DECOR_BLOCKS;

    private static HashMap<MapColor, ArrayList<Block>> palette = new HashMap<>();

    public static void initColors() {
        palette = new HashMap<>();
        for (Block block : Registries.BLOCK) {
            BlockState state = block.getDefaultState();
            MapColor color = state.getMapColor(null, null);
            if (color == MapColor.CLEAR)
                continue;
            if (color != null) {
                if (!palette.containsKey(color))
                    palette.put(color, new ArrayList<>());
                if (useBlockInPalette(block) && block != Blocks.BEDROCK && block != Blocks.REINFORCED_DEEPSLATE)
                    palette.get(color).add(block);
            }
        }

        ItemStack[] toolItems = {
                new ItemStack(Items.NETHERITE_SWORD),
                new ItemStack(Items.NETHERITE_AXE),
                new ItemStack(Items.NETHERITE_PICKAXE),
                new ItemStack(Items.NETHERITE_SHOVEL),
                new ItemStack(Items.NETHERITE_HOE),
                new ItemStack(Items.SHEARS)
        };
        for (MapColor key : palette.keySet()) {
            palette.get(key).sort((b1, b2) ->
                    Float.compare(getRoughMinBreakingSpeed(b1, toolItems), getRoughMinBreakingSpeed(b2, toolItems))
            );
        }
    }

    private static boolean useBlockInPalette(Block block) {
        var useInPalette = MapartHelper.config.commonConfiguration.useInPalette;

        if (useInPalette.anyBlocks) return true;
        if (matchesAny(block, MEANINGLESS_BLOCKS)) return false;

        if (useInPalette.onlySolid) return block.getClass() == Block.class;
        if (useInPalette.onlyCarpets) return matchesAny(block, CarpetBlock.class, PaleMossCarpetBlock.class);

        if (!useInPalette.blocksWithEntities && block instanceof BlockWithEntity) return false;
        if (!useInPalette.buildDecorBlocks && matchesAny(block, BUILD_DECOR_BLOCKS)) return false;
        if (!useInPalette.creativeBlocks && matchesAny(block, CREATIVE_BLOCKS)) return false;
        if (!useInPalette.needWaterBlocks && matchesAny(block, NEED_WATER_BLOCKS)) return false;
        if (!useInPalette.growableBlocks && matchesAny(block, GROWABLE_BLOCKS)) return false;
        return useInPalette.grassLikeBlocks || !matchesAny(block, GRASS_LIKE_BLOCKS);
    }


    public static Block[] getBlocksOfColor(MapColor color) {
        return palette.get(color).toArray(new Block[0]);
    }

    public static void setBlocksFromPalette(World world) {
        int startX = 64;
        for (int x = startX; x < startX + 128; x++)
            for (int z = -64; z < 64; z++)
                world.setBlockState(new BlockPos(x, -61, z), Blocks.GRASS_BLOCK.getDefaultState());
        BlockPos pos = new BlockPos(startX, -61, -64);
        for (MapColor color : palette.keySet()) {
            if (color == MapColor.CLEAR)
                continue;
            for (Block block : palette.get(color)) {
                world.setBlockState(pos, getDefaultPaletteState(block), 18);
                pos = pos.east();
                if (pos.getX() == startX + 128) {
                    pos = pos.west(128);
                    pos = pos.south();
                }
            }
            pos = pos.west(pos.getX() - startX);
            pos = pos.south();
        }
    }

    public static BlockState getDefaultPaletteState(Block block) {
        BlockState state = block.getDefaultState();
        state = state.withIfExists(Properties.WATERLOGGED, false);
        state = state.withIfExists(Properties.DOWN, true);
        state = state.withIfExists(Properties.PERSISTENT, true);
        return state;
    }

    private static float getRoughMinBreakingSpeed (Block block, ItemStack[] tools) {
        BlockState state = block.getDefaultState();
        float hardness = block.getHardness();
        if (hardness < 0) return Float.POSITIVE_INFINITY;

        double[] toolsSpeed = Arrays.stream(tools).mapToDouble(t -> t.getMiningSpeedMultiplier(state)).toArray();
        float maxSpeed = (float) Arrays.stream(toolsSpeed).max().orElse(0.0);
        if (maxSpeed <= 0) return Float.POSITIVE_INFINITY;

        return hardness / maxSpeed;
    }

    private static boolean matchesAny(Block block, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(block)) return true;
        }
        return false;
    }

    static {
        NEED_WATER_BLOCKS = new Class[]{
                CoralBlockBlock.class,
                CoralBlock.class,
                CoralFanBlock.class,
                CoralWallFanBlock.class,
                KelpBlock.class,
                SeagrassBlock.class,
                TallSeagrassBlock.class,
                KelpPlantBlock.class
        };
        MEANINGLESS_BLOCKS = new Class[]{
                BedBlock.class,
                DoorBlock.class,
                FrostedIceBlock.class,
                BubbleColumnBlock.class,
                FrogspawnBlock.class,
                LilyPadBlock.class,
                SnifferEggBlock.class,
                TurtleEggBlock.class,
                FlowerPotBlock.class,
                WeepingVinesBlock.class,
                CaveVines.class,
                SporeBlossomBlock.class,
                PistonHeadBlock.class,
                PistonExtensionBlock.class,
                CactusBlock.class,
                CocoaBlock.class,
                FireBlock.class,
                SoulFireBlock.class,
                ChorusPlantBlock.class,
                ChorusFlowerBlock.class,
                BigDripleafStemBlock.class,
                HangingMossBlock.class,
                HangingRootsBlock.class,
                LavaCauldronBlock.class,
                LeveledCauldronBlock.class,
                FarmlandBlock.class,
                DirtPathBlock.class,
                HeavyCoreBlock.class
        };
        CREATIVE_BLOCKS = new Class[]{
                OperatorBlock.class,
                StructureVoidBlock.class,
                SpawnerBlock.class,
                TrialSpawnerBlock.class,
                VaultBlock.class,
                EndPortalFrameBlock.class,
                Portal.class,
                BuddingAmethystBlock.class,
                DragonEggBlock.class,
                InfestedBlock.class
        };
        GROWABLE_BLOCKS = new Class[]{
                PlantBlock.class,
                AbstractPlantPartBlock.class,
                BambooBlock.class,
                BambooShootBlock.class,
                SugarCaneBlock.class
        };
        GRASS_LIKE_BLOCKS = new Class[]{
                GrassBlock.class,
                PlantBlock.class,
                CoralBlock.class,
                CoralFanBlock.class,
                CoralWallFanBlock.class,
                DeadCoralBlock.class,
                DeadCoralFanBlock.class,
                DeadCoralWallFanBlock.class,
                BigDripleafBlock.class,
                PointedDripstoneBlock.class
        };
        BUILD_DECOR_BLOCKS = new Class[]{
                FenceBlock.class,
                FenceGateBlock.class,
                WallBlock.class,
                BannerBlock.class,
                StairsBlock.class,
                TrapdoorBlock.class,
                LanternBlock.class,
                CandleBlock.class,
                LightningRodBlock.class,
                CarvedPumpkinBlock.class
        };
    }
}
