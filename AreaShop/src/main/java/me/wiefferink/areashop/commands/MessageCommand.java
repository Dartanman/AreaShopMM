package me.wiefferink.areashop.commands;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class MessageCommand extends CommandAreaShop {

	@Inject
	private MessageBridge messageBridge;
	
	@Override
	public String getCommandStart() {
		return "areashop message";
	}

	@Override
	public String getHelp(CommandSender target) {
		// Internal command, no need to show in the help list
		return null;
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if(!sender.hasPermission("areashop.message")) {
			messageBridge.message(sender, "message-noPermission");
			return;
		}

		if(args.length < 3) {
			messageBridge.message(sender, "message-help");
			return;
		}

		Player player = Bukkit.getPlayer(args[1]);
		if(player == null) {
			messageBridge.message(sender, "message-notOnline", args[1]);
			return;
		}

		String[] messageArgs = new String[args.length - 2];
		System.arraycopy(args, 2, messageArgs, 0, args.length - 2);
		String message = StringUtils.join(messageArgs, " ");

		Message m = Message.fromString(message);
		send(m, player);
	}

	private void send(Message message, Object target) {
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

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			for(Player player : Utils.getOnlinePlayers()) {
				result.add(player.getName());
			}
		}
		return result;
	}

}










