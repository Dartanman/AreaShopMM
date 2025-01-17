package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.interactivemessenger.processing.Message;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for generalising command classes.
 */
public abstract class CommandAreaShop {

	private final Map<String, CommandTime> lastUsed;

	protected CommandAreaShop() {
		lastUsed = new HashMap<>();
	}

	/**
	 * Check if this Command instance can execute the given command and arguments.
	 * @param command The command to check for execution
	 * @param args    The arguments to check
	 * @return true if it can execute the command, false otherwise
	 */
	public boolean canExecute(Command command, String[] args) {
		String commandString = command.getName() + " " + StringUtils.join(args, " ");
		if(commandString.length() > getCommandStart().length()) {
			return commandString.toLowerCase().startsWith(getCommandStart().toLowerCase() + " ");
		}
		return commandString.toLowerCase().startsWith(getCommandStart().toLowerCase());
	}

	/**
	 * Get a list of string to complete a command with (raw list, not matching ones not filtered out).
	 * @param toComplete The number of the argument that has to be completed
	 * @param start      The already given start of the command
	 * @param sender     The CommandSender that wants to tab complete
	 * @return A collection with all the possibilities for argument to complete
	 */
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		return new ArrayList<>();
	}

	/**
	 * Get the argument that comes after the base command that this command reacts to.
	 * @return The string that should be in front of the command for this class to act
	 */
	public abstract String getCommandStart();

	/**
	 * Returns the correct help string key to be used on the help page.
	 * @param target The CommandSender that the help message is for
	 * @return The help message key according to the permissions of the reciever
	 */
	public abstract String getHelp(CommandSender target);

	/**
	 * Execute a (sub)command if the conditions are met.
	 * @param sender The commandSender that executed the command
	 * @param args   The arguments that are given
	 */
	public abstract void execute(CommandSender sender, String[] args);

	/**
	 * Confirm a command.
	 * @param sender To confirm it for, or send a message to confirm
	 * @param args Command args
	 * @param message Message to send when confirmation is required
	 * @return true if confirmed, false if confirmation is required
	 */
	public boolean confirm(CommandSender sender, String[] args, Message message) {
		String command = "/" + getCommandStart() + " " + StringUtils.join(args, " ", 1, args.length);
		long now = System.currentTimeMillis();
		CommandTime last = lastUsed.get(sender.getName());
		if(last != null && last.command.equalsIgnoreCase(command) && last.time > (now - 1000 * 60)) {
			return true;
		}

		Message m = message.prefix().append(Message.fromKey("confirm-yes").replacements(command));
		send(m, sender);
		lastUsed.put(sender.getName(), new CommandTime(command, now));
		return false;
	}

	private void send(Message message, CommandSender target) {
		if(AreaShop.useMiniMessage())
		{
			if(message.get() == null || message.get().size() == 0 || (message.get().size() == 1 && message.get().get(0).length() == 0) || target == null) {
				return;
			}
			message.doReplacements();

			StringBuilder messageStr = new StringBuilder();
			for(String line : message.get())
			{
				messageStr.append(line);
			}

			MiniMessage mm = MiniMessage.miniMessage();
			TextComponent parsed = (TextComponent) mm.deserialize(messageStr.toString());
			try
			{
				Audience audience = (Audience) target;
				audience.sendMessage(parsed);
			}
			catch (ClassCastException e)
			{
				Bukkit.getLogger().severe("AreaShop sent a non-supported Object as the Audience for a Message!");
			}
		}
		else
		{
			message.send(target);
		}
	}

	private static class CommandTime {
		public final String command;
		public final long time;

		CommandTime(String command, long time) {
			this.command = command;
			this.time = time;
		}
	}

}
