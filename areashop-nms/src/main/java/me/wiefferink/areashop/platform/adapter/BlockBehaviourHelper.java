package me.wiefferink.areashop.platform.adapter;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

public interface BlockBehaviourHelper {

    boolean canPlace(Location location, BlockData blockData);

    boolean isBlockValid(Block block);

    boolean isBlockStateValid(BlockState blockState);

}