package rh.maparthelper.conversion;

import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.joml.Vector2i;
import rh.maparthelper.MapUtils;
import rh.maparthelper.MapartHelper;
import rh.maparthelper.conversion.colors.ColorUtils;

import java.util.*;
import java.util.stream.Collectors;

public class BlocksPalette {
    // Lists of block classes for blocking/enabling by configs
    private static final Class<?>[] NEED_WATER_BLOCKS;
    private static final Class<?>[] MEANINGLESS_BLOCKS;
    private static final Class<?>[] CREATIVE_BLOCKS;
    private static final Class<?>[] GROWABLE_BLOCKS;
    private static final Class<?>[] GRASS_LIKE_BLOCKS;
    private static final Class<?>[] BUILD_DECOR_BLOCKS;

    private static final Map<MapColor, ArrayList<Block>> palette = new TreeMap<>(Comparator.comparingInt(o -> o.id));
    private static final Map<Integer, MapColorEntry> argbMapColors = new HashMap<>();
    private static final Map<Integer, MapColorEntry> cachedClosestColors = new HashMap<>();

    public static void initColors() {
        palette.clear();
        argbMapColors.clear();

        for (Block block : Registries.BLOCK) {
            BlockState state = block.getDefaultState();
            MapColor color = state.getMapColor(null, null);
            if (color == MapColor.CLEAR)
                continue;
            if (color != null) {
                if (!palette.containsKey(color))
                    palette.put(color, new ArrayList<>());
                boolean useCreativeBlocks = MapartHelper.config.commonConfiguration.useInPalette.creativeBlocks;
                if (useBlockInPalette(block) && (useCreativeBlocks || block != Blocks.BEDROCK && block != Blocks.REINFORCED_DEEPSLATE))
                    palette.get(color).add(block);
            }
        }

        for (MapColor color : palette.keySet()) {
            if (!palette.get(color).isEmpty())
                addARGBMapColorEntries(color);
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

    public static MapColorEntry getMapColorEntryByARGB(int argb) {
        return argbMapColors.get(argb);
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


    public static ArrayList<Block> getBlocksOfColor(MapColor color) {
        return palette.get(color);
    }

    public static MapColor[] getMapColors() {
        return palette.keySet().toArray(new MapColor[0]);
    }

    public static Map<MapColor, Block> getDefaultPalette() {
        return palette.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getFirst()
                ));
    }

    private static MapColorEntry getClosestColor3D(int argb) {
        MapColorEntry closest = new MapColorEntry(MapColor.CLEAR, MapColor.Brightness.NORMAL);
        double minDist = Integer.MAX_VALUE;

        for (MapColor color : palette.keySet()) {
            for (int brightId = 0; brightId < 3; brightId++) {
                MapColor.Brightness brightness;
                brightness = color == MapColor.WATER_BLUE ? MapColor.Brightness.NORMAL : MapColor.Brightness.validateAndGet(brightId);
                int current = color.getRenderColor(brightness);

                if (current == argb) return new MapColorEntry(color, brightness);

                double dist = ColorUtils.colorDistance(argb, current);
                if (dist < minDist) {
                    minDist = dist;
                    closest = new MapColorEntry(color, brightness);
                }

                if (color == MapColor.WATER_BLUE) break;
            }
        }

        return closest;
    }

    private static MapColorEntry getClosestColor2D(int argb) {
        MapColor closest = MapColor.CLEAR;
        double minDist = Integer.MAX_VALUE;

        for (MapColor color : palette.keySet()) {
            int current = color.getRenderColor(MapColor.Brightness.NORMAL);
            if (current == argb) return new MapColorEntry(color, MapColor.Brightness.NORMAL);
            double dist = ColorUtils.colorDistance(argb, current);
            if (dist < minDist) {
                minDist = dist;
                closest = color;
            }
        }

        return new MapColorEntry(closest, MapColor.Brightness.NORMAL);
    }

    public static MapColorEntry getClosestColor(int argb, boolean use3D) {
        if (use3D)
            return cachedClosestColors.computeIfAbsent(argb, BlocksPalette::getClosestColor3D);
        return cachedClosestColors.computeIfAbsent(argb, BlocksPalette::getClosestColor2D);
    }

    public static void clearColorCache(){
        cachedClosestColors.clear();
    }

    public static void placeBlocksFromPalette(World world, int playerX, int y, int playerZ) {
        Vector2i startPos = MapUtils.getMapAreaStartPos(playerX, playerZ);
        int startX = startPos.x;
        int startZ = startPos.y;

        int maxLen = palette.values().stream().mapToInt(ArrayList::size).max().orElse(0);

        BlockPos.Mutable pos = new BlockPos.Mutable(startX, y, startZ);
        for (int x = 0; x < maxLen; x++) {
            for (int z = -1; z < palette.size(); z++) {
                world.setBlockState(pos.add(x, 0, z), Blocks.GRASS_BLOCK.getDefaultState());
            }
        }

        pos.set(startX, y, startZ);
        for (MapColor color : palette.keySet()) {
            for (Block block : palette.get(color)) {
                world.setBlockState(pos, getDefaultPaletteState(block), 18);
                pos = pos.move(Direction.EAST);
                if (pos.getX() == startX + 128) {
                    pos = pos.move(Direction.WEST, 128);
                    pos = pos.move(Direction.SOUTH);
                }
            }
            pos = pos.move(Direction.WEST, pos.getX() - startX);
            pos = pos.move(Direction.SOUTH);
        }
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

    public record MapColorEntry(MapColor mapColor, MapColor.Brightness brightness) {}
}
