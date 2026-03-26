package rh.maparthelper.config.palette;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.Shapes;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.config.CommonConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class PaletteGenerator {
    // Lists of block classes for blocking/enabling by configs
    private static final Class<?>[] NEED_WATER_BLOCKS;
    private static final Class<?>[] MEANINGLESS_BLOCKS;
    private static final Class<?>[] CREATIVE_BLOCKS;
    private static final Class<?>[] GROWABLE_BLOCKS;
    private static final Class<?>[] GRASS_LIKE_BLOCKS;
    private static final Class<?>[] BUILD_DECOR_BLOCKS;
    private static final List<Block> FUNCTIONAL_BLOCKS;

    public static void initColors(Map<Integer, List<Block>> palette) {
        palette.clear();

        for (Block block : BuiltInRegistries.BLOCK) {
            BlockState state = block.defaultBlockState();
            MapColor color = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            if (color == MapColor.NONE)
                continue;
            boolean useCreativeBlocks = MapartHelper.commonConfig().creativeBlocks;
            if (useBlockInPalette(block) && (useCreativeBlocks || block != Blocks.BEDROCK && block != Blocks.REINFORCED_DEEPSLATE && block != Blocks.PETRIFIED_OAK_SLAB)) {
                if (!palette.containsKey(color.id))
                    palette.put(color.id, new ArrayList<>());
                palette.get(color.id).add(block);
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
        for (int colorId : palette.keySet()) {
            palette.get(colorId).sort(Comparator.comparingDouble(b -> getBlockScore(b, toolItems)));
        }
    }

    private static boolean useBlockInPalette(Block block) {
        CommonConfiguration config = MapartHelper.commonConfig();

        if (config.anyBlocks) return true;
        if (matchesAny(block, MEANINGLESS_BLOCKS)) return false;

        if (config.onlySolid && !matchesAny(block, CREATIVE_BLOCKS)) {
            BlockState state = block.defaultBlockState();
            if (state.isSolidRender()) return true;
            return state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) == Shapes.block();
        }
        if (config.onlyCarpets) return matchesAny(block, CarpetBlock.class, MossyCarpetBlock.class);

        if (!config.entityBlocks && block instanceof EntityBlock) return false;
        if (!config.buildDecorBlocks && matchesAny(block, BUILD_DECOR_BLOCKS)) return false;
        if (!config.creativeBlocks && matchesAny(block, CREATIVE_BLOCKS)) return false;
        if (!config.needWaterBlocks && matchesAny(block, NEED_WATER_BLOCKS)) return false;
        if (!config.growableBlocks && matchesAny(block, GROWABLE_BLOCKS)) return false;
        return config.grassLikeBlocks || !matchesAny(block, GRASS_LIKE_BLOCKS);
    }

    public static BlockState getDefaultPaletteState(Block block) {
        BlockState state = block.defaultBlockState();
        state = state.trySetValue(BlockStateProperties.WATERLOGGED, false);
        state = state.trySetValue(BlockStateProperties.DOWN, true);
        state = state.trySetValue(BlockStateProperties.PERSISTENT, true);
        return state;
    }

    private static float getBlockScore(Block block, ItemStack[] tools) {
        float breakTime = getRoughMinBreakingSpeed(block, tools);
        float typePenalty = 0f;
        BlockState blockState = block.defaultBlockState();

        if (block == Blocks.PACKED_ICE) typePenalty -= 3.0f;
        if (block == Blocks.DIORITE) typePenalty -= 3.0f;
        else if (isWool(blockState)) typePenalty -= 5.0f;
        else if (blockState.is(BlockTags.TERRACOTTA)) typePenalty -= 4.5f;
        else if (blockState.is(BlockTags.LEAVES)) typePenalty -= 3.0f;

        if (blockState.is(BlockTags.PLANKS) || blockState.is(BlockTags.WOODEN_SLABS)) typePenalty -= 0.1f;
        else if (block == Blocks.BROWN_MUSHROOM_BLOCK) typePenalty += 0.5f;
        else if (block instanceof ScaffoldingBlock) typePenalty += 2.0f;
        else if (block instanceof CarpetBlock) typePenalty += 1.0f;
        else if (block instanceof FallingBlock) typePenalty += 1.5f;
        else if (blockState.is(BlockTags.PRESSURE_PLATES)) typePenalty += 0.3f;
        else if (blockState.is(BlockTags.SLABS)) typePenalty += 2.0f;
        else if (FUNCTIONAL_BLOCKS.contains(block) || block instanceof EntityBlock) typePenalty += 4.0f;

        return breakTime + typePenalty;
    }

    private static boolean isWool(BlockState blockState) {
        return blockState.is(BlockTags.WOOL) || blockState.is(BlockTags.WOOL_CARPETS);
    }

    private static float getRoughMinBreakingSpeed(Block block, ItemStack[] tools) {
        BlockState state = block.defaultBlockState();
        float hardness = block.defaultDestroyTime();
        if (hardness < 0) return Float.POSITIVE_INFINITY;

        double[] toolsSpeed = Arrays.stream(tools).mapToDouble(t -> t.getDestroySpeed(state)).toArray();
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

    public static PalettePresetsConfig.PalettePreset getDefaultPreset() {
        Map<Integer, List<Block>> completePalette = PaletteConfigManager.completePalette.palette;
        Map<MapColor, Block> palette = completePalette.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        entry -> MapColor.byId(entry.getKey()),
                        entry -> entry.getValue().isEmpty() ? Blocks.AIR : entry.getValue().getFirst()
                ));

        palette.remove(MapColor.WATER);

        List<Block> blocks = completePalette.get(MapColor.GRASS.id);
        if (blocks != null && blocks.contains(Blocks.GRASS_BLOCK))
            palette.replace(MapColor.GRASS, Blocks.GRASS_BLOCK);

        blocks = completePalette.get(MapColor.WOOD.id);
        if (blocks != null && blocks.contains(Blocks.OAK_PLANKS))
            palette.replace(MapColor.WOOD, Blocks.OAK_PLANKS);

        blocks = completePalette.get(MapColor.GOLD.id);
        if (blocks != null && blocks.contains(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE))
            palette.replace(MapColor.GOLD, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);

        blocks = completePalette.get(MapColor.PODZOL.id);
        if (blocks != null && blocks.contains(Blocks.SPRUCE_PLANKS))
            palette.replace(MapColor.PODZOL, Blocks.SPRUCE_PLANKS);

        return new PalettePresetsConfig.PalettePreset(palette);
    }

    static {
        NEED_WATER_BLOCKS = new Class[]{
                CoralBlock.class,
                CoralPlantBlock.class,
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
                WaterlilyBlock.class,
                SnifferEggBlock.class,
                TurtleEggBlock.class,
                FlowerPotBlock.class,
                WeepingVinesBlock.class,
                CaveVines.class,
                SporeBlossomBlock.class,
                PistonHeadBlock.class,
                MovingPistonBlock.class,
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
                LayeredCauldronBlock.class,
                FarmBlock.class,
                DirtPathBlock.class,
                HeavyCoreBlock.class
        };
        CREATIVE_BLOCKS = new Class[]{
                GameMasterBlock.class,
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
                VegetationBlock.class,
                GrowingPlantBlock.class,
                BambooStalkBlock.class,
                BambooSaplingBlock.class,
                SugarCaneBlock.class,
                VineBlock.class
        };
        GRASS_LIKE_BLOCKS = new Class[]{
                VegetationBlock.class,
                DoublePlantBlock.class,
                CoralPlantBlock.class,
                CoralFanBlock.class,
                CoralWallFanBlock.class,
                BaseCoralPlantBlock.class,
                BaseCoralFanBlock.class,
                BaseCoralWallFanBlock.class,
                BigDripleafBlock.class,
                PointedDripstoneBlock.class
        };
        BUILD_DECOR_BLOCKS = new Class[]{
                FenceBlock.class,
                FenceGateBlock.class,
                WallBlock.class,
                BannerBlock.class,
                StairBlock.class,
                TrapDoorBlock.class,
                LanternBlock.class,
                CandleBlock.class,
                LightningRodBlock.class,
                CarvedPumpkinBlock.class
        };
        FUNCTIONAL_BLOCKS = List.of(
                Blocks.CRAFTING_TABLE,
                Blocks.ANVIL,
                Blocks.CHIPPED_ANVIL,
                Blocks.DAMAGED_ANVIL,
                Blocks.GRINDSTONE,
                Blocks.STONECUTTER,
                Blocks.NOTE_BLOCK,
                Blocks.LOOM,
                Blocks.CARTOGRAPHY_TABLE,
                Blocks.FLETCHING_TABLE,
                Blocks.SMITHING_TABLE,
                Blocks.FURNACE
        );
    }
}
