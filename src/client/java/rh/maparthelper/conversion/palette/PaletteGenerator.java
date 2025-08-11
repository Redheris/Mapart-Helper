package rh.maparthelper.conversion.palette;

import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import rh.maparthelper.MapartHelper;

import java.util.*;

import static java.util.Map.entry;

public class PaletteGenerator {
    // Lists of block classes for blocking/enabling by configs
    private static final Class<?>[] NEED_WATER_BLOCKS;
    private static final Class<?>[] MEANINGLESS_BLOCKS;
    private static final Class<?>[] CREATIVE_BLOCKS;
    private static final Class<?>[] GROWABLE_BLOCKS;
    private static final Class<?>[] GRASS_LIKE_BLOCKS;
    private static final Class<?>[] BUILD_DECOR_BLOCKS;

    static final Map<Integer, MapColorEntry> argbMapColors = new HashMap<>();

    public static void initColors(Map<Integer, List<Block>> palette) {
        palette.clear();
        argbMapColors.clear();

        for (Block block : Registries.BLOCK) {
            BlockState state = block.getDefaultState();
            MapColor color = state.getMapColor(null, null);
            if (color == MapColor.CLEAR)
                continue;
            if (color != null) {
                if (!palette.containsKey(color.id))
                    palette.put(color.id, new ArrayList<>());
                boolean useCreativeBlocks = MapartHelper.config.commonConfiguration.useInPalette.creativeBlocks;
                if (useBlockInPalette(block) && (useCreativeBlocks || block != Blocks.BEDROCK && block != Blocks.REINFORCED_DEEPSLATE))
                    palette.get(color.id).add(block);
            }
        }

        for (int colorId : palette.keySet()) {
            if (!palette.get(colorId).isEmpty())
                addARGBMapColorEntries(MapColor.get(colorId));
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
            palette.get(colorId).sort((b1, b2) ->
                    Float.compare(getRoughMinBreakingSpeed(b1, toolItems), getRoughMinBreakingSpeed(b2, toolItems))
            );
        }
    }

    private static void addARGBMapColorEntries(MapColor mapColor) {
        if (mapColor == MapColor.WATER_BLUE) {
            argbMapColors.put(
                    mapColor.getRenderColor(MapColor.Brightness.NORMAL),
                    new MapColorEntry(mapColor, MapColor.Brightness.NORMAL)
            );
        } else {
            MapColor.Brightness[] brightnesses = new MapColor.Brightness[] {
                    MapColor.Brightness.LOW,
                    MapColor.Brightness.NORMAL,
                    MapColor.Brightness.HIGH
            };
            for (MapColor.Brightness brightness : brightnesses) {
                int argb = mapColor.getRenderColor(brightness);
                MapColorEntry entry = new MapColorEntry(mapColor, brightness);
                argbMapColors.put(argb, entry);
            }
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

    public static BlockState getDefaultPaletteState(Block block) {
        BlockState state = block.getDefaultState();
        state = state.withIfExists(Properties.WATERLOGGED, false);
        state = state.withIfExists(Properties.DOWN, true);
        state = state.withIfExists(Properties.PERSISTENT, true);
        return state;
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

    public static Map<MapColor, Block> getDefaultPreset() {
        return Map.<MapColor, Block>ofEntries(
                entry(MapColor.PALE_GREEN, Blocks.GRASS_BLOCK),
                entry(MapColor.PALE_YELLOW, Blocks.SANDSTONE),
                entry(MapColor.WHITE_GRAY, Blocks.MUSHROOM_STEM),
                entry(MapColor.BRIGHT_RED, Blocks.REDSTONE_BLOCK),
                entry(MapColor.PALE_PURPLE, Blocks.PACKED_ICE),
                entry(MapColor.IRON_GRAY, Blocks.PALE_OAK_LEAVES),
                entry(MapColor.DARK_GREEN, Blocks.OAK_LEAVES),
                entry(MapColor.WHITE, Blocks.WHITE_WOOL),
                entry(MapColor.LIGHT_BLUE_GRAY, Blocks.CLAY),
                entry(MapColor.DIRT_BROWN, Blocks.DIRT),
                entry(MapColor.STONE_GRAY, Blocks.STONE),
                entry(MapColor.OAK_TAN, Blocks.OAK_PLANKS),
                entry(MapColor.OFF_WHITE, Blocks.DIORITE),
                entry(MapColor.ORANGE, Blocks.ORANGE_WOOL),
                entry(MapColor.MAGENTA, Blocks.MAGENTA_WOOL),
                entry(MapColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL),
                entry(MapColor.YELLOW, Blocks.YELLOW_WOOL),
                entry(MapColor.LIME, Blocks.LIME_WOOL),
                entry(MapColor.PINK, Blocks.PINK_WOOL),
                entry(MapColor.GRAY, Blocks.GRAY_WOOL),
                entry(MapColor.LIGHT_GRAY, Blocks.PALE_MOSS_BLOCK),
                entry(MapColor.CYAN, Blocks.CYAN_WOOL),
                entry(MapColor.PURPLE, Blocks.PURPLE_WOOL),
                entry(MapColor.BLUE, Blocks.BLUE_WOOL),
                entry(MapColor.BROWN, Blocks.BROWN_WOOL),
                entry(MapColor.GREEN, Blocks.MOSS_BLOCK),
                entry(MapColor.RED, Blocks.NETHER_WART_BLOCK),
                entry(MapColor.BLACK, Blocks.BLACK_WOOL),
                entry(MapColor.GOLD, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE),
                entry(MapColor.DIAMOND_BLUE, Blocks.PRISMARINE_BRICKS),
                entry(MapColor.LAPIS_BLUE, Blocks.LAPIS_BLOCK),
                entry(MapColor.EMERALD_GREEN, Blocks.EMERALD_BLOCK),
                entry(MapColor.SPRUCE_BROWN, Blocks.SPRUCE_PLANKS),
                entry(MapColor.DARK_RED, Blocks.NETHERRACK),
                entry(MapColor.TERRACOTTA_WHITE, Blocks.WHITE_TERRACOTTA),
                entry(MapColor.TERRACOTTA_ORANGE, Blocks.ORANGE_TERRACOTTA),
                entry(MapColor.TERRACOTTA_MAGENTA, Blocks.MAGENTA_TERRACOTTA),
                entry(MapColor.TERRACOTTA_LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA),
                entry(MapColor.TERRACOTTA_YELLOW, Blocks.YELLOW_TERRACOTTA),
                entry(MapColor.TERRACOTTA_LIME, Blocks.LIME_TERRACOTTA),
                entry(MapColor.TERRACOTTA_PINK, Blocks.PINK_TERRACOTTA),
                entry(MapColor.TERRACOTTA_GRAY, Blocks.GRAY_TERRACOTTA),
                entry(MapColor.TERRACOTTA_LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA),
                entry(MapColor.TERRACOTTA_CYAN, Blocks.CYAN_TERRACOTTA),
                entry(MapColor.TERRACOTTA_PURPLE, Blocks.PURPLE_TERRACOTTA),
                entry(MapColor.TERRACOTTA_BLUE, Blocks.BLUE_TERRACOTTA),
                entry(MapColor.TERRACOTTA_BROWN, Blocks.BROWN_TERRACOTTA),
                entry(MapColor.TERRACOTTA_GREEN, Blocks.GREEN_TERRACOTTA),
                entry(MapColor.TERRACOTTA_RED, Blocks.RED_TERRACOTTA),
                entry(MapColor.TERRACOTTA_BLACK, Blocks.BLACK_TERRACOTTA),
                entry(MapColor.DULL_RED, Blocks.CRIMSON_NYLIUM),
                entry(MapColor.DULL_PINK, Blocks.CRIMSON_PLANKS),
                entry(MapColor.DARK_CRIMSON, Blocks.CRIMSON_HYPHAE),
                entry(MapColor.TEAL, Blocks.WARPED_NYLIUM),
                entry(MapColor.DARK_AQUA, Blocks.WARPED_PLANKS),
                entry(MapColor.DARK_DULL_PINK, Blocks.WARPED_HYPHAE),
                entry(MapColor.BRIGHT_TEAL, Blocks.WARPED_WART_BLOCK),
                entry(MapColor.DEEPSLATE_GRAY, Blocks.DEEPSLATE),
                entry(MapColor.RAW_IRON_PINK, Blocks.RAW_IRON_BLOCK),
                entry(MapColor.LICHEN_GREEN, Blocks.GLOW_LICHEN)
        );
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
