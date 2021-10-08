package me.wiefferink.areashop.features.signs;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.bakedlibs.dough.blocks.ChunkPosition;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.ask.AddingRegionEvent;
import me.wiefferink.areashop.managers.FileManager;
import me.wiefferink.areashop.nms.BlockBehaviourHelper;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Materials;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.bukkitdo.Do;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SignListener implements Listener {

    private final BlockBehaviourHelper behaviourHelper;
    private final AreaShop plugin;
    private final SignManager signManager;

    public SignListener(@Nonnull BlockBehaviourHelper behaviourHelper, @Nonnull AreaShop plugin, SignManager signManager) {
        this.signManager = signManager;
        this.behaviourHelper = behaviourHelper;
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        Optional<SignCache> optional = this.signManager.getCacheForWorld(chunk.getWorld());
        if (optional.isEmpty()) {
            return;
        }
        final SignCache signCache = optional.get();
        Collection<RegionSign> chunkSigns = signCache.signsAtChunk(ChunkPosition.getAsLong(chunk.getX(), chunk.getZ()));
        if(chunkSigns.isEmpty()) {
            return;
        }
        chunkSigns.remove(null);
        Do.forAll(chunkSigns, RegionSign::update);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIndirectSignBreak(BlockPhysicsEvent event) {
        // Check if the block is a sign
        if(!Materials.isSign(event.getBlock().getType()) || behaviourHelper.isBlockValid(event.getBlock())) {
            return;
        }

        // Check if the sign is really the same as a saved rent
        final Optional<RegionSign> optionalSign = this.signManager.signFromLocation(event.getBlock().getLocation());
        if(optionalSign.isEmpty()) {
            return;
        }
        RegionSign regionSign = optionalSign.get();

        // Remove the sign so that it does not fall on the floor as an item (next region update will place it back when possible)
        AreaShop.debug("onIndirectSignBreak: Removed block of sign for", regionSign.getRegion().getName(), "at", regionSign.getStringLocation());
        event.getBlock().setType(Material.AIR);
        event.setCancelled(true);
    }



    @EventHandler(priority = EventPriority.HIGH)
    public void onSignBreak(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        // Check if it is a sign
        if(Materials.isSign(block.getType())) {
            // Check if the rent sign is really the same as a saved rent
            Optional<RegionSign> optional = signManager.signFromLocation(block.getLocation());
            if(optional.isEmpty()) {
                return;
            }
            RegionSign regionSign = optional.get();
            // Remove the sign of the rental region if the player has permission
            if(event.getPlayer().hasPermission("areashop.delsign")) {
                signManager.removeSign(regionSign);
                plugin.message(event.getPlayer(), "delsign-success", regionSign.getRegion());
            } else { // Cancel the breaking of the sign
                event.setCancelled(true);
                plugin.message(event.getPlayer(), "delsign-noPermission", regionSign.getRegion());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignClick(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Only listen to left and right clicks on blocks
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        // Only care about clicking blocks
        if(!Materials.isSign(block.getType())) {
            return;
        }

        // Check if this sign belongs to a region
        Optional<RegionSign> optional = signManager.signFromLocation(block.getLocation());
        if(optional.isEmpty()) {
            return;
        }
        RegionSign regionSign = optional.get();

        // Ignore players that are in sign link mode (which will handle the event itself)
        Player player = event.getPlayer();
        if(plugin.getSignlinkerManager().isInSignLinkMode(player)) {
            return;
        }

        // Get the clicktype
        GeneralRegion.ClickType clickType = null;
        if(player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            clickType = GeneralRegion.ClickType.SHIFTLEFTCLICK;
        } else if(!player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            clickType = GeneralRegion.ClickType.LEFTCLICK;
        } else if(player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            clickType = GeneralRegion.ClickType.SHIFTRIGHTCLICK;
        } else if(!player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            clickType = GeneralRegion.ClickType.RIGHTCLICK;
        }

        boolean ran = regionSign.runSignCommands(player, clickType);

        // Only cancel event if at least one command has been executed
        event.setCancelled(ran);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        if(event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if(!plugin.isReady()) {
            plugin.message(player, "general-notReady");
            return;
        }

        // Check if the sign is meant for this plugin
        if(event.getLine(0).contains(plugin.getConfig().getString("signTags.rent"))) {
            if(!player.hasPermission("areashop.createrent") && !player.hasPermission("areashop.createrent.member") && !player.hasPermission("areashop.createrent.owner")) {
                plugin.message(player, "setup-noPermissionRent");
                return;
            }

            // Get the other lines
            String secondLine = event.getLine(1);
            String thirdLine = event.getLine(2);
            String fourthLine = event.getLine(3);

            // Get the regionManager for accessing regions
            RegionManager regionManager = plugin.getRegionManager(event.getPlayer().getWorld());

            // If the secondLine does not contain a name try to find the region by location
            if(secondLine == null || secondLine.isEmpty()) {
                Set<ProtectedRegion> regions = plugin.getWorldGuardHandler().getApplicableRegionsSet(event.getBlock().getLocation());
                if(regions != null) {
                    boolean first = true;
                    ProtectedRegion candidate = null;
                    for(ProtectedRegion pr : regions) {
                        if(first) {
                            candidate = pr;
                            first = false;
                        } else {
                            if(pr.getPriority() > candidate.getPriority()) {
                                candidate = pr;
                            } else if(pr.getParent() != null && pr.getParent().equals(candidate)) {
                                candidate = pr;
                            } else {
                                plugin.message(player, "setup-couldNotDetect", candidate.getId(), pr.getId());
                                return;
                            }
                        }
                    }
                    if(candidate != null) {
                        secondLine = candidate.getId();
                    }
                }
            }

            boolean priceSet = fourthLine != null && !fourthLine.isEmpty();
            boolean durationSet = thirdLine != null && !thirdLine.isEmpty();
            // check if all the lines are correct
            if(secondLine == null || secondLine.isEmpty()) {
                plugin.message(player, "setup-noRegion");
                return;
            }
            ProtectedRegion region = regionManager.getRegion(secondLine);
            if(region == null) {
                plugin.message(player, "cmd-noRegion", secondLine);
                return;
            }

            FileManager.AddResult addResult = plugin.getFileManager().checkRegionAdd(player, regionManager.getRegion(secondLine), event.getPlayer().getWorld(), GeneralRegion.RegionType.RENT);
            if(addResult == FileManager.AddResult.BLACKLISTED) {
                plugin.message(player, "setup-blacklisted", secondLine);
            } else if(addResult == FileManager.AddResult.ALREADYADDED) {
                plugin.message(player, "setup-alreadyRentSign");
            } else if(addResult == FileManager.AddResult.ALREADYADDEDOTHERWORLD) {
                plugin.message(player, "setup-alreadyOtherWorld");
            } else if(addResult == FileManager.AddResult.NOPERMISSION) {
                plugin.message(player, "setup-noPermission", secondLine);
            } else if(thirdLine != null && !thirdLine.isEmpty() && !Utils.checkTimeFormat(thirdLine)) {
                plugin.message(player, "setup-wrongDuration");
            } else {
                double price = 0.0;
                if(priceSet) {
                    // Check the fourth line
                    try {
                        price = Double.parseDouble(fourthLine);
                    } catch(NumberFormatException e) {
                        plugin.message(player, "setup-wrongPrice");
                        return;
                    }
                }

                // Add rent to the FileManager
                final RentRegion rent = new RentRegion(secondLine, event.getPlayer().getWorld());
                boolean isMember = plugin.getWorldGuardHandler().containsMember(rent.getRegion(), player.getUniqueId());
                boolean isOwner = plugin.getWorldGuardHandler().containsOwner(rent.getRegion(), player.getUniqueId());
                boolean landlord = (!player.hasPermission("areashop.createrent")
                        && ((player.hasPermission("areashop.createrent.owner") && isOwner)
                        || (player.hasPermission("areashop.createrent.member") && isMember)));

                if(landlord) {
                    rent.setLandlord(player.getUniqueId(), player.getName());
                }
                if(priceSet) {
                    rent.setPrice(price);
                }
                if(durationSet) {
                    rent.setDuration(thirdLine);
                }
                rent.getSignsFeature().addSign(event.getBlock().getLocation(), event.getBlock().getType(), plugin.getBukkitHandler().getSignFacing(event.getBlock()), null);

                AddingRegionEvent addingRegionEvent = plugin.getFileManager().addRegion(rent);
                if (addingRegionEvent.isCancelled()) {
                    plugin.message(player, "general-cancelled", addingRegionEvent.getReason());
                    return;
                }

                rent.handleSchematicEvent(GeneralRegion.RegionEvent.CREATED);
                plugin.message(player, "setup-rentSuccess", rent);
                // Update the region after the event has written its lines
                Do.sync(rent::update);
            }
        } else if(event.getLine(0).contains(plugin.getConfig().getString("signTags.buy"))) {
            // Check for permission
            if(!player.hasPermission("areashop.createbuy") && !player.hasPermission("areashop.createbuy.member") && !player.hasPermission("areashop.createbuy.owner")) {
                plugin.message(player, "setup-noPermissionBuy");
                return;
            }

            // Get the other lines
            String secondLine = event.getLine(1);
            String thirdLine = event.getLine(2);

            // Get the regionManager for accessing regions
            RegionManager regionManager = plugin.getRegionManager(event.getPlayer().getWorld());

            // If the secondLine does not contain a name try to find the region by location
            if(secondLine == null || secondLine.isEmpty()) {
                Set<ProtectedRegion> regions = plugin.getWorldGuardHandler().getApplicableRegionsSet(event.getBlock().getLocation());
                if(regions != null) {
                    boolean first = true;
                    ProtectedRegion candidate = null;
                    for(ProtectedRegion pr : regions) {
                        if(first) {
                            candidate = pr;
                            first = false;
                        } else {
                            if(pr.getPriority() > candidate.getPriority()) {
                                candidate = pr;
                            } else if(pr.getParent() != null && pr.getParent().equals(candidate)) {
                                candidate = pr;
                            } else {
                                plugin.message(player, "setup-couldNotDetect", candidate.getId(), pr.getId());
                                return;
                            }
                        }
                    }
                    if(candidate != null) {
                        secondLine = candidate.getId();
                    }
                }
            }

            boolean priceSet = thirdLine != null && !thirdLine.isEmpty();
            // Check if all the lines are correct
            if(secondLine == null || secondLine.isEmpty()) {
                plugin.message(player, "setup-noRegion");
                return;
            }
            ProtectedRegion region = regionManager.getRegion(secondLine);
            if(region == null) {
                plugin.message(player, "cmd-noRegion", secondLine);
                return;
            }
            FileManager.AddResult addResult = plugin.getFileManager().checkRegionAdd(player, region, event.getPlayer().getWorld(), GeneralRegion.RegionType.BUY);
            if(addResult == FileManager.AddResult.BLACKLISTED) {
                plugin.message(player, "setup-blacklisted", secondLine);
            } else if(addResult == FileManager.AddResult.ALREADYADDED) {
                plugin.message(player, "setup-alreadyRentSign");
            } else if(addResult == FileManager.AddResult.ALREADYADDEDOTHERWORLD) {
                plugin.message(player, "setup-alreadyOtherWorld");
            } else if(addResult == FileManager.AddResult.NOPERMISSION) {
                plugin.message(player, "setup-noPermission", secondLine);
            } else {
                double price = 0.0;
                if(priceSet) {
                    // Check the fourth line
                    try {
                        price = Double.parseDouble(thirdLine);
                    } catch(NumberFormatException e) {
                        plugin.message(player, "setup-wrongPrice");
                        return;
                    }
                }

                // Add buy to the FileManager
                final BuyRegion buy = new BuyRegion(secondLine, event.getPlayer().getWorld());
                boolean isMember = plugin.getWorldGuardHandler().containsMember(buy.getRegion(), player.getUniqueId());
                boolean isOwner = plugin.getWorldGuardHandler().containsOwner(buy.getRegion(), player.getUniqueId());
                boolean landlord = (!player.hasPermission("areashop.createbuy")
                        && ((player.hasPermission("areashop.createbuy.owner") && isOwner)
                        || (player.hasPermission("areashop.createbuy.member") && isMember)));

                if(landlord) {
                    buy.setLandlord(player.getUniqueId(), player.getName());
                }
                if(priceSet) {
                    buy.setPrice(price);
                }
                buy.getSignsFeature().addSign(event.getBlock().getLocation(), event.getBlock().getType(), plugin.getBukkitHandler().getSignFacing(event.getBlock()), null);

                AddingRegionEvent addingRegionEvent = plugin.getFileManager().addRegion(buy);
                if (addingRegionEvent.isCancelled()) {
                    plugin.message(player, "general-cancelled", addingRegionEvent.getReason());
                    return;
                }

                buy.handleSchematicEvent(GeneralRegion.RegionEvent.CREATED);
                plugin.message(player, "setup-buySuccess", buy);
                // Update the region after the event has written its lines
                Do.sync(buy::update);
            }
        } else if(event.getLine(0).contains(plugin.getConfig().getString("signTags.add"))) {
            // Check for permission
            if(!player.hasPermission("areashop.addsign")) {
                plugin.message(player, "addsign-noPermission");
                return;
            }

            // Get the other lines
            String secondLine = event.getLine(1);
            String thirdLine = event.getLine(2);

            GeneralRegion region;
            if(secondLine != null && !secondLine.isEmpty()) {
                // Get region by secondLine of the sign
                region = plugin.getFileManager().getRegion(secondLine);
                if(region == null) {
                    plugin.message(player, "addSign-notRegistered", secondLine);
                    return;
                }
            } else {
                // Get region by sign position
                List<GeneralRegion> regions = Utils.getImportantRegions(event.getBlock().getLocation());
                if(regions.isEmpty()) {
                    plugin.message(player, "addsign-noRegions");
                    return;
                } else if(regions.size() > 1) {
                    plugin.message(player, "addsign-couldNotDetectSign", regions.get(0).getName(), regions.get(1).getName());
                    return;
                }
                region = regions.get(0);
            }

            if(thirdLine == null || thirdLine.isEmpty()) {
                region.getSignsFeature().addSign(event.getBlock().getLocation(), event.getBlock().getType(), plugin.getBukkitHandler().getSignFacing(event.getBlock()), null);
                plugin.message(player, "addsign-success", region);
            } else {
                region.getSignsFeature().addSign(event.getBlock().getLocation(), event.getBlock().getType(), plugin.getBukkitHandler().getSignFacing(event.getBlock()), thirdLine);
                plugin.message(player, "addsign-successProfile", thirdLine, region);
            }

            // Update the region later because this event will do it first
            Do.sync(region::update);
        }
    }

}
