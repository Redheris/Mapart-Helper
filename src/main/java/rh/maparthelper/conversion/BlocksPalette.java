package rh.maparthelper.conversion;

import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BlocksPalette {
    // Lists of block classes for blocking/enabling by configs
    private static final Class<?>[] NEED_WATER_BLOCKS = {
            CoralBlock.class,
            CoralBlockBlock.class,
            CoralFanBlock.class,
            CoralWallFanBlock.class,
            KelpBlock.class,
            SeagrassBlock.class,
            TallSeagrassBlock.class,
            KelpPlantBlock.class
    };
    private static final Class<?>[] MEANINGLESS_BLOCKS = {
            BedBlock.class,
            DoorBlock.class,
            FrostedIceBlock.class,
            BubbleColumnBlock.class,
            FrogspawnBlock.class,
            SnifferEggBlock.class,
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
            LeveledCauldronBlock.class
    };
    private static final Class<?>[] CREATIVE_BLOCKS = {
            OperatorBlock.class,
            StructureVoidBlock.class,
            SpawnerBlock.class,
            TrialSpawnerBlock.class,
            VaultBlock.class,
            EndPortalFrameBlock.class,
            Portal.class,
            BuddingAmethystBlock.class
    };
    private static final Class<?>[] GROWABLE_BLOCKS = {
            PlantBlock.class,
            AbstractPlantPartBlock.class,
            BambooBlock.class,
            BambooShootBlock.class,
            SugarCaneBlock.class
    };
    private static final Class<?>[] GRASS_BLOCKS = {
            GrassBlock.class,
            PlantBlock.class
    };

    private static HashMap<MapColor, Set<Block>> palette = new HashMap<>();

    public static void initColors() {
        palette = new HashMap<>();
        for (Block block : Registries.BLOCK) {
            BlockState state = block.getDefaultState();
            MapColor color = state.getMapColor(null, null);
            if (color == MapColor.CLEAR)
                continue;
            if (color != null) {
                if (!palette.containsKey(color))
                    palette.put(color, new HashSet<>());
                if (useBlockInPalette(block) && block != Blocks.BEDROCK && block != Blocks.REINFORCED_DEEPSLATE)
                    palette.get(color).add(block);
            }
        }
        // Debug output of the palette
//        System.out.println(palette);
//        for (var s : palette.entrySet())
//            System.out.println(s);
    }

    private static boolean useBlockInPalette(Block block) {
        if (Arrays.stream(CREATIVE_BLOCKS).anyMatch(blockClass -> blockClass.isInstance(block)))
            return false;
        if (Arrays.stream(NEED_WATER_BLOCKS).anyMatch(blockClass -> blockClass.isInstance(block)))
            return false;
        if (Arrays.stream(MEANINGLESS_BLOCKS).anyMatch(blockClass -> blockClass.isInstance(block)))
            return false;
        if (Arrays.stream(GROWABLE_BLOCKS).anyMatch(blockClass -> blockClass.isInstance(block)))
            return false;
        return Arrays.stream(GRASS_BLOCKS).noneMatch(blockClass -> blockClass.isInstance(block));
    }

    public static Block[] getBlocksOfColor(MapColor color) {
        Object[] array = palette.keySet().stream().map(key -> key.id).toArray();
        System.out.println(array.length);
        System.out.println(Arrays.toString(array));
        return palette.get(color).toArray(new Block[0]);
    }

    public static void setBlocksFromPalette(World world) {
        int startX = 64;
        BlockPos pos = new BlockPos(startX, -61, -64);
        for (int x = startX; x < startX + 128; x++)
            for (int z = -64; z < 64; z++)
                world.setBlockState(new BlockPos(x, -61, z), Blocks.GRASS_BLOCK.getDefaultState());
        for (MapColor color : palette.keySet()) {
            if (color == MapColor.CLEAR)
                continue;
            for (Block block : palette.get(color)) {
//                System.out.print(block.getTranslationKey() + " ");
                BlockState blockState = block.getDefaultState().withIfExists(Properties.WATERLOGGED, false).withIfExists(Properties.DOWN, true);
                world.setBlockState(pos, blockState, Block.FORCE_STATE);
                pos = pos.east();
                if (pos.getX() == startX + 128) {
                    pos = pos.west(128);
                    pos = pos.south();
//                    System.out.println();
                }
            }
            pos = pos.west(pos.getX() - startX);
            pos = pos.south();
//            System.out.println();
        }
    }
}
