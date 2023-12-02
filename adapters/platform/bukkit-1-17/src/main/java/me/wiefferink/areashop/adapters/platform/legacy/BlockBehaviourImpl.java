package me.wiefferink.areashop.adapters.platform.legacy;

import me.wiefferink.areashop.platform.adapter.BlockBehaviourHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;

import java.util.Objects;

public class BlockBehaviourImpl implements BlockBehaviourHelper {

    @Override
    public boolean canPlace(Location location, BlockData blockData) {
        Objects.requireNonNull(location.getWorld(), "Null World!");
        final BlockPos blockPos = new BlockPos(location.getX(), location.getY(), location.getZ());
        final CraftWorld craftWorld = (CraftWorld) location.getWorld();
        final ServerLevel world = craftWorld.getHandle();
        final CraftBlockData craftBlockData = (CraftBlockData) blockData;
        final BlockState ibd = craftBlockData.getState();
        return ibd.canSurvive(world, blockPos);
    }

    @Override
    public boolean isBlockValid(Block block) {
        final CraftBlock craftBlock = (CraftBlock) block;
        final CraftWorld craftWorld = (CraftWorld) craftBlock.getWorld();
        final ServerLevel world = craftWorld.getHandle();
        final BlockState ibd = craftBlock.getNMS();
        return ibd.canSurvive(world, craftBlock.getPosition());
    }

    @Override
    public boolean isBlockStateValid(org.bukkit.block.BlockState blockState) {
        final CraftBlockState craftBlockState = (CraftBlockState) blockState;
        final BlockState ibd = craftBlockState.getHandle();
        return ibd.canSurvive(craftBlockState.getBlock().getCraftWorld().getHandle(), craftBlockState.getPosition());
    }

}