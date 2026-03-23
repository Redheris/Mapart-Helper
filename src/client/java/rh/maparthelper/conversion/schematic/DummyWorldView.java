package rh.maparthelper.conversion.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DummyWorldView implements LevelReader {
    private final static DummyWorldView INSTANCE = new DummyWorldView();

    public static DummyWorldView getInstance() {
        return INSTANCE;
    }

    @Override
    public int getHeight(Heightmap.Types heightmap, int x, int z) {
        return 0;
    }

    @Override
    public @NotNull BlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.defaultBlockState();
    }


    @Override
    public @Nullable ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        return null;
    }

    @Override
    public boolean hasChunk(int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public @NotNull BiomeManager getBiomeManager() {
        return null;
    }

    @Override
    public @NotNull Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        return null;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public @NotNull DimensionType dimensionType() {
        return null;
    }

    @Override
    public @NotNull RegistryAccess registryAccess() {
        return null;
    }

    @Override
    public @NotNull FeatureFlagSet enabledFeatures() {
        return null;
    }

    @Override
    public float getShade(Direction direction, boolean shaded) {
        return 0;
    }

    @Override
    public @NotNull LevelLightEngine getLightEngine() {
        return null;
    }

    @Override
    public @NotNull WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public @NotNull List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB box) {
        return List.of();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public @NotNull FluidState getFluidState(BlockPos pos) {
        return null;
    }
}
