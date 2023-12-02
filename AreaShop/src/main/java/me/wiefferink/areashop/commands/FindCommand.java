package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.RegionGroup;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
public class FindCommand extends CommandAreaShop {

	@Inject
	private MessageBridge messageBridge;
	@Inject
	private Economy economy;
	@Inject
	private IFileManager fileManager;

	@Override
	public String getCommandStart() {
		return "areashop find";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.find")) {
			return "help-find";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.find")) {
			messageBridge.message(sender, "find-noPermission");
			return;
		}
		if(!(sender instanceof Player player)) {
			messageBridge.message(sender, "cmd-onlyByPlayer");
			return;
		}
		if(args.length <= 1 || args[1] == null || (!args[1].equalsIgnoreCase("buy") && !args[1].equalsIgnoreCase("rent"))) {
			messageBridge.message(sender, "find-help");
			return;
		}
		double balance = 0.0;
		if(economy != null) {
			balance = economy.getBalance(player);
		}
		double maxPrice = 0;
		boolean maxPriceSet = false;
		RegionGroup group = null;
		// Parse optional price argument
		if(args.length >= 3) {
			try {
				maxPrice = Double.parseDouble(args[2]);
				maxPriceSet = true;
			} catch(NumberFormatException e) {
				messageBridge.message(sender, "find-wrongMaxPrice", args[2]);
				return;
			}
		}
		// Parse optional group argument
		if(args.length >= 4) {
			group = fileManager.getGroup(args[3]);
			if(group == null) {
				messageBridge.message(sender, "find-wrongGroup", args[3]);
				return;
			}
		}

		// Find buy regions
		if(args[1].equalsIgnoreCase("buy")) {
			Collection<BuyRegion> regions = fileManager.getBuysRef();
			List<BuyRegion> results = new LinkedList<>();
			for(BuyRegion region : regions) {
				if(!region.isSold()
						&& ((region.getPrice() <= balance && !maxPriceSet) || (region.getPrice() <= maxPrice && maxPriceSet))
						&& (group == null || group.isMember(region))
						&& (region.getBooleanSetting("general.findCrossWorld") || player.getWorld().equals(region.getWorld()))) {
					results.add(region);
				}
			}
			if(!results.isEmpty()) {
				// Draw a random one
				BuyRegion region = results.get(ThreadLocalRandom.current().nextInt(results.size()));
				Message onlyInGroup = Message.empty();
				if(group != null) {
					onlyInGroup = Message.fromKey("find-onlyInGroup").replacements(args[3]);
				}

				// Teleport
				if(maxPriceSet) {
					messageBridge.message(player, "find-successMax", "buy", Utils.formatCurrency(maxPrice), onlyInGroup, region);
				} else {
					messageBridge.message(player, "find-success", "buy", Utils.formatCurrency(balance), onlyInGroup, region);
				}
				region.getTeleportFeature().teleportPlayer(player, region.getBooleanSetting("general.findTeleportToSign"), false);
			} else {
				Message onlyInGroup = Message.empty();
				if(group != null) {
					onlyInGroup = Message.fromKey("find-onlyInGroup").replacements(args[3]);
				}
				if(maxPriceSet) {
					messageBridge.message(player, "find-noneFoundMax", "buy", Utils.formatCurrency(maxPrice), onlyInGroup);
				} else {
					messageBridge.message(player, "find-noneFound", "buy", Utils.formatCurrency(balance), onlyInGroup);
				}
			}
		}

		// Find rental regions
		else {
			Collection<RentRegion> regions = fileManager.getRentsRef();
			List<RentRegion> results = new LinkedList<>();
			for(RentRegion region : regions) {
				if(!region.isRented()
						&& ((region.getPrice() <= balance && !maxPriceSet) || (region.getPrice() <= maxPrice && maxPriceSet))
						&& (group == null || group.isMember(region))
						&& (region.getBooleanSetting("general.findCrossWorld") || player.getWorld().equals(region.getWorld()))) {
					results.add(region);
				}
			}
			if(!results.isEmpty()) {
				// Draw a random one
				RentRegion region = results.get(ThreadLocalRandom.current().nextInt(results.size()));
				Message onlyInGroup = Message.empty();
				if(group != null) {
					onlyInGroup = Message.fromKey("find-onlyInGroup").replacements(args[3]);
				}

				// Teleport
				if(maxPriceSet) {
					messageBridge.message(player, "find-successMax", "rent", Utils.formatCurrency(maxPrice), onlyInGroup, region);
				} else {
					messageBridge.message(player, "find-success", "rent", Utils.formatCurrency(balance), onlyInGroup, region);
				}
				region.getTeleportFeature().teleportPlayer(player, region.getBooleanSetting("general.findTeleportToSign"), false);
			} else {
				Message onlyInGroup = Message.empty();
				if(group != null) {
					onlyInGroup = Message.fromKey("find-onlyInGroup").replacements(args[3]);
				}
				if(maxPriceSet) {
					messageBridge.message(player, "find-noneFoundMax", "rent", Utils.formatCurrency(maxPrice), onlyInGroup);
				} else {
					messageBridge.message(player, "find-noneFound", "rent", Utils.formatCurrency(balance), onlyInGroup);
				}
			}
		}

	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result.add("buy");
			result.add("rent");
		}
		return result;
	}

}



























