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

//? >=1.21.11
//import net.minecraft.world.attribute.EnvironmentAttributeReader;

/**
 * This LevelReader exists to deceive the
 * {@code BlockBehaviour#canSurvive} to check if the block can survive without a support block below it
 */
public class DummyWorldView implements LevelReader {
    private final static DummyWorldView INSTANCE = new DummyWorldView();

    public static DummyWorldView getInstance() {
        return INSTANCE;
    }

    @Override
    public int getHeight(@NotNull Heightmap.Types heightmap, int x, int z) {
        return 0;
    }

    @Override
    public @NotNull BlockState getBlockState(@NotNull BlockPos pos) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public @Nullable ChunkAccess getChunk(int chunkX, int chunkZ, @NotNull ChunkStatus leastStatus, boolean create) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public int getSeaLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull DimensionType dimensionType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull RegistryAccess registryAccess() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull FeatureFlagSet enabledFeatures() {
        throw new UnsupportedOperationException();
    }

    //? if >= 1.21.11 {
    /*@Override
    public @NotNull EnvironmentAttributeReader environmentAttributes() {
        throw new UnsupportedOperationException();
    }
    *///?}

    @Override
    public float getShade(@NotNull Direction direction, boolean shaded) {
        return 0;
    }

    @Override
    public @NotNull LevelLightEngine getLightEngine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull WorldBorder getWorldBorder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<VoxelShape> getEntityCollisions(@Nullable Entity entity, @NotNull AABB box) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(@NotNull BlockPos pos) {
        return null;
    }

    @Override
    public @NotNull FluidState getFluidState(@NotNull BlockPos pos) {
        throw new UnsupportedOperationException();
    }
}
