package me.wiefferink.areashop.managers;

import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.commands.AddCommand;
import me.wiefferink.areashop.commands.AddfriendCommand;
import me.wiefferink.areashop.commands.AddsignCommand;
import me.wiefferink.areashop.commands.BuyCommand;
import me.wiefferink.areashop.commands.CommandAreaShop;
import me.wiefferink.areashop.commands.DelCommand;
import me.wiefferink.areashop.commands.DelfriendCommand;
import me.wiefferink.areashop.commands.DelsignCommand;
import me.wiefferink.areashop.commands.FindCommand;
import me.wiefferink.areashop.commands.GroupaddCommand;
import me.wiefferink.areashop.commands.GroupdelCommand;
import me.wiefferink.areashop.commands.GroupinfoCommand;
import me.wiefferink.areashop.commands.GrouplistCommand;
import me.wiefferink.areashop.commands.HelpCommand;
import me.wiefferink.areashop.commands.ImportCommand;
import me.wiefferink.areashop.commands.InfoCommand;
import me.wiefferink.areashop.commands.LinksignsCommand;
import me.wiefferink.areashop.commands.MeCommand;
import me.wiefferink.areashop.commands.MessageCommand;
import me.wiefferink.areashop.commands.ReloadCommand;
import me.wiefferink.areashop.commands.RentCommand;
import me.wiefferink.areashop.commands.ResellCommand;
import me.wiefferink.areashop.commands.SchematiceventCommand;
import me.wiefferink.areashop.commands.SellCommand;
import me.wiefferink.areashop.commands.SetTransferCommand;
import me.wiefferink.areashop.commands.SetdurationCommand;
import me.wiefferink.areashop.commands.SetlandlordCommand;
import me.wiefferink.areashop.commands.SetownerCommand;
import me.wiefferink.areashop.commands.SetpriceCommand;
import me.wiefferink.areashop.commands.SetrestoreCommand;
import me.wiefferink.areashop.commands.SetteleportCommand;
import me.wiefferink.areashop.commands.StackCommand;
import me.wiefferink.areashop.commands.StopresellCommand;
import me.wiefferink.areashop.commands.TeleportCommand;
import me.wiefferink.areashop.commands.TransferCommand;
import me.wiefferink.areashop.commands.UnrentCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Singleton
public class CommandManager extends Manager implements CommandExecutor, TabCompleter {
	private final List<CommandAreaShop> commands;
	private final AreaShop plugin;
	private final MessageBridge messageBridge;

	/**
	 * Constructor.
	 */
	@Inject
	CommandManager(@Nonnull AreaShop plugin, @Nonnull MessageBridge messageBridge, @Nonnull
				   Injector injector) {
		this.plugin = plugin;
		this.messageBridge = messageBridge;
		commands = new ArrayList<>(34);
		commands.add(new HelpCommand(this));
		commands.add(injector.getInstance(RentCommand.class));
		commands.add(injector.getInstance(UnrentCommand.class));
		commands.add(injector.getInstance(BuyCommand.class));
		commands.add(injector.getInstance(SellCommand.class));
		commands.add(injector.getInstance(TransferCommand.class));
		commands.add(injector.getInstance(MeCommand.class));
		commands.add(injector.getInstance(InfoCommand.class));
		commands.add(injector.getInstance(TeleportCommand.class));
		commands.add(injector.getInstance(SetteleportCommand.class));
		commands.add(injector.getInstance(AddfriendCommand.class));
		commands.add(injector.getInstance(DelfriendCommand.class));
		commands.add(injector.getInstance(FindCommand.class));
		commands.add(injector.getInstance(ResellCommand.class));
		commands.add(injector.getInstance(StopresellCommand.class));
		commands.add(injector.getInstance(SetrestoreCommand.class));
		commands.add(injector.getInstance(SetpriceCommand.class));
		commands.add(injector.getInstance(SetownerCommand.class));
		commands.add(injector.getInstance(SetdurationCommand.class));
		commands.add(injector.getInstance(SetTransferCommand.class));
		commands.add(injector.getInstance(ReloadCommand.class));
		commands.add(injector.getInstance(GroupaddCommand.class));
		commands.add(injector.getInstance(GroupdelCommand.class));
		commands.add(injector.getInstance(GrouplistCommand.class));
		commands.add(injector.getInstance(GroupinfoCommand.class));
		commands.add(injector.getInstance(SchematiceventCommand.class));
		commands.add(injector.getInstance(AddCommand.class));
		commands.add(injector.getInstance(DelCommand.class));
		commands.add(injector.getInstance(AddsignCommand.class));
		commands.add(injector.getInstance(DelsignCommand.class));
		commands.add(injector.getInstance(LinksignsCommand.class));
		commands.add(injector.getInstance(StackCommand.class));
		commands.add(injector.getInstance(SetlandlordCommand.class));
		commands.add(injector.getInstance(MessageCommand.class));
		commands.add(injector.getInstance(ImportCommand.class));

		// Register commands in bukkit
		plugin.getCommand("AreaShop").setExecutor(this);
		plugin.getCommand("AreaShop").setTabCompleter(this);
	}

	/**
	 * Get the list with AreaShop commands.
	 * @return The list with AreaShop commands
	 */
	public List<CommandAreaShop> getCommands() {
		return commands;
	}

	/**
	 * Shows the help page for the CommandSender.
	 * @param target The CommandSender to show the help to
	 */
	public void showHelp(CommandSender target) {
		if(!target.hasPermission("areashop.help")) {
			messageBridge.message(target, "help-noPermission");
			return;
		}
		// Add all messages to a list
		ArrayList<String> messages = new ArrayList<>();
		messageBridge.message(target, "help-header");
		messageBridge.message(target, "help-alias");
		for(CommandAreaShop command : commands) {
			String help = command.getHelp(target);
			if(help != null && !help.isEmpty()) {
				messages.add(help);
			}
		}
		// Send the messages to the target
		for(String message : messages) {
			messageBridge.messageNoPrefix(target, message);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(!plugin.isReady()) {
			messageBridge.message(sender, "general-notReady");
			return true;
		}

		// Redirect `/as info player <player>` to `/as me <player>`
		if(args.length == 3 && "info".equals(args[0]) && "player".equals(args[1])) {
			args[0] = "me";
			args[1] = args[2];
		}

		// Execute command
		boolean executed = false;
		for(int i = 0; i < commands.size() && !executed; i++) {
			if(commands.get(i).canExecute(command, args)) {
				commands.get(i).execute(sender, args);
				executed = true;
			}
		}

		// Show help
		if (!executed) {
			if (args.length == 0) {
				this.showHelp(sender);
			} else {
				// Indicate that the '/as updaterents' and '/as updatebuys' commands are removed
				if("updaterents".equalsIgnoreCase(args[0]) || "updatebuys".equalsIgnoreCase(args[0])) {
					messageBridge.message(sender, "reload-updateCommandChanged");
				} else {
					messageBridge.message(sender, "cmd-notValid");
				}
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> result = new ArrayList<>();
		if(!sender.hasPermission("areashop.tabcomplete")) {
			return result;
		}
		int toCompleteNumber = args.length;
		String toCompletePrefix = args[args.length - 1].toLowerCase();
		//AreaShop.debug("toCompleteNumber=" + toCompleteNumber + ", toCompletePrefix=" + toCompletePrefix + ", length=" + toCompletePrefix.length());
		if(toCompleteNumber == 1) {
			for(CommandAreaShop c : commands) {
				String begin = c.getCommandStart();
				result.add(begin.substring(begin.indexOf(' ') + 1));
			}
		} else {
			String[] start = new String[args.length];
			start[0] = command.getName();
			System.arraycopy(args, 0, start, 1, args.length - 1);
			for(CommandAreaShop c : commands) {
				if(c.canExecute(command, args)) {
					result = c.getTabCompleteList(toCompleteNumber, start, sender);
				}
			}
		}
		// Filter and sort the results
		if(!result.isEmpty()) {
			SortedSet<String> set = new TreeSet<>();
			for(String suggestion : result) {
				if(suggestion.toLowerCase().startsWith(toCompletePrefix)) {
					set.add(suggestion);
				}
			}
			result.clear();
			result.addAll(set);
		}
		//AreaShop.debug("Tabcomplete #" + toCompleteNumber + ", prefix="+ toCompletePrefix + ", result=" + result.toString());
		return result;
	}
}

















