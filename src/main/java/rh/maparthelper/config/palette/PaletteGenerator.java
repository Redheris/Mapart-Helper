package rh.maparthelper.config.palette;

import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.colors.MapColorEntry;

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
        PaletteColors.argbMapColors.clear();

        for (Block block : Registries.BLOCK) {
            BlockState state = block.getDefaultState();
            MapColor color = state.getMapColor(null, null);
            if (color == MapColor.CLEAR)
                continue;
            if (color != null) {
                boolean useCreativeBlocks = MapartHelper.commonConfig.useInPalette.creativeBlocks;
                if (useBlockInPalette(block) && (useCreativeBlocks || block != Blocks.BEDROCK && block != Blocks.REINFORCED_DEEPSLATE && block != Blocks.PETRIFIED_OAK_SLAB)) {
                    if (!palette.containsKey(color.id))
                        palette.put(color.id, new ArrayList<>());
                    palette.get(color.id).add(block);
                }
            }
        }

        initARGBMapColor(palette);

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

    public static void initARGBMapColor(Map<Integer, List<Block>> palette) {
        for (int colorId : palette.keySet()) {
            if (!palette.get(colorId).isEmpty())
                addARGBMapColorEntries(MapColor.get(colorId));
        }
    }

    private static void addARGBMapColorEntries(MapColor mapColor) {
        if (mapColor == MapColor.WATER_BLUE) {
            PaletteColors.argbMapColors.put(
                    mapColor.getRenderColor(MapColor.Brightness.NORMAL),
                    new MapColorEntry(mapColor, MapColor.Brightness.NORMAL)
            );
        } else {
            MapColor.Brightness[] brightnesses = new MapColor.Brightness[]{
                    MapColor.Brightness.LOW,
                    MapColor.Brightness.NORMAL,
                    MapColor.Brightness.HIGH
            };
            for (MapColor.Brightness brightness : brightnesses) {
                int argb = mapColor.getRenderColor(brightness);
                MapColorEntry entry = new MapColorEntry(mapColor, brightness);
                PaletteColors.argbMapColors.put(argb, entry);
            }
        }
    }

    private static boolean useBlockInPalette(Block block) {
        var useInPalette = MapartHelper.commonConfig.useInPalette;

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

    public static BlockState getDefaultPaletteState(Block block) {
        BlockState state = block.getDefaultState();
        state = state.withIfExists(Properties.WATERLOGGED, false);
        state = state.withIfExists(Properties.DOWN, true);
        state = state.withIfExists(Properties.PERSISTENT, true);
        return state;
    }

    private static float getBlockScore(Block block, ItemStack[] tools) {
        float breakTime = getRoughMinBreakingSpeed(block, tools);
        float typePenalty = 0f;
        BlockState blockState = block.getDefaultState();

        if (block == Blocks.PACKED_ICE) typePenalty -= 3.0f;
        if (block == Blocks.DIORITE) typePenalty -= 3.0f;
        else if (isWool(blockState)) typePenalty -= 5.0f;
        else if (blockState.isIn(BlockTags.TERRACOTTA)) typePenalty -= 4.5f;
        else if (blockState.isIn(BlockTags.LEAVES)) typePenalty -= 3.0f;

        if (blockState.isIn(BlockTags.PLANKS) || blockState.isIn(BlockTags.WOODEN_SLABS)) typePenalty -= 0.1f;
        else if (block == Blocks.BROWN_MUSHROOM_BLOCK) typePenalty += 0.5f;
        else if (block instanceof ScaffoldingBlock) typePenalty += 2.0f;
        else if (block instanceof CarpetBlock) typePenalty += 1.0f;
        else if (block instanceof FallingBlock) typePenalty += 1.5f;
        else if (blockState.isIn(BlockTags.PRESSURE_PLATES)) typePenalty += 0.3f;
        else if (blockState.isIn(BlockTags.SLABS)) typePenalty += 2.0f;
        else if (FUNCTIONAL_BLOCKS.contains(block) || block instanceof BlockWithEntity) typePenalty += 4.0f;

        return breakTime + typePenalty;
    }

    private static boolean isWool(BlockState blockState) {
        return blockState.isIn(BlockTags.WOOL) || blockState.isIn(BlockTags.WOOL_CARPETS);
    }

    private static float getRoughMinBreakingSpeed(Block block, ItemStack[] tools) {
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

    public static PalettePresetsConfig.PalettePreset getDefaultPreset() {
        Map<MapColor, Block> palette = PaletteConfigManager.completePalette.palette.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> MapColor.get(entry.getKey()),
                        entry -> entry.getValue().getFirst()
                ));
        palette.put(MapColor.PALE_GREEN, Blocks.GRASS_BLOCK);
        palette.remove(MapColor.WATER_BLUE);
        palette.put(MapColor.OAK_TAN, Blocks.OAK_PLANKS);
        palette.put(MapColor.GOLD, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        palette.put(MapColor.SPRUCE_BROWN, Blocks.SPRUCE_PLANKS);

        return new PalettePresetsConfig.PalettePreset(palette);
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
                SugarCaneBlock.class,
                VineBlock.class
        };
        GRASS_LIKE_BLOCKS = new Class[]{
                PlantBlock.class,
                TallPlantBlock.class,
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
