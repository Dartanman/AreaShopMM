package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.managers.IFileManager;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class SetTransferCommand extends CommandAreaShop {

    @Inject
    private MessageBridge messageBridge;
    @Inject
    private IFileManager fileManager;
    @Inject
    private Plugin plugin;

    @Override
    public String getCommandStart() {
        return "areashop settransfer";
    }

    @Override
    public String getHelp(CommandSender target) {
        if (target.hasPermission("areashop.settransfer")) {
            return "help-settransfer";
        }
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("areashop.settransfer")) {
            messageBridge.message(sender, "settransfer-noPermission");
            return;
        }
        if (args.length <= 2 || args[1] == null || args[2] == null) {
            messageBridge.message(sender, "settransfer-help");
            return;
        }
        GeneralRegion region = fileManager.getRegion(args[1]);
        if (region == null) {
            messageBridge.message(sender, "settransfer-notRegistered", args[1]);
            return;
        }
        boolean value;
        if (args[2].equalsIgnoreCase("true")) {
            value = true;
        } else if (args[2].equalsIgnoreCase("false")) {
            value = false;
        } else {
            messageBridge.message(sender, "settransfer-invalidSetting", args[2]);
            return;
        }
        region.setTransferEnabled(value);
        messageBridge.message(sender, "settransfer-success", args[2], region);
        region.update();
    }

    @Override
    public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
        List<String> result = new ArrayList<>();
        if (toComplete == 2) {
            result = fileManager.getRegionNames();
        } else if (toComplete == 3) {
            result.add("true");
            result.add("false");
        }
        return result;
    }
}
