package me.wiefferink.areashop.tools;

import com.google.inject.Singleton;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.interactivemessenger.Log;
import me.wiefferink.interactivemessenger.generators.ConsoleGenerator;
import me.wiefferink.interactivemessenger.generators.TellrawGenerator;
import me.wiefferink.interactivemessenger.parsers.YamlParser;
import me.wiefferink.interactivemessenger.processing.Message;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class SimpleMessageBridge implements MessageBridge {

    /**
     * Send a message to a target without a prefix.
     * @param target       The target to send the message to
     * @param key          The key of the language string
     * @param replacements The replacements to insert in the message
     */
    @Override
    public void messageNoPrefix(Object target, String key, Object... replacements) {
        Message m = Message.fromKey(key).replacements(replacements);
        send(m, target);
    }

    public static void send(Message message, Object target) {
        if(AreaShop.useMiniMessage()) {

            Audience audience = null;
            try {
                audience = (Audience) target;
            }
            catch (ClassCastException e) {
                Bukkit.getLogger().severe("AreaShop sent a non-supported Object as the Audience for a Message!");
            }

            if(message.get() == null || message.get().size() == 0 || (message.get().size() == 1 && message.get().get(0).length() == 0) || target == null) {
                return;
            }
            message.doReplacements();

            StringBuilder messageStr = new StringBuilder();
            for(String line : message.get()) {
                messageStr.append(line);
            }

            MiniMessage mm = MiniMessage.miniMessage();
            Component parsed = mm.deserialize(messageStr.toString());
            audience.sendMessage(parsed);
        } else {
            message.send(target);
        }
    }

    /**
     * Send a message to a target, prefixed by the default chat prefix.
     * @param target       The target to send the message to
     * @param key          The key of the language string
     * @param replacements The replacements to insert in the message
     */
    @Override
    public void message(Object target, String key, Object... replacements) {
        Message m = Message.fromKey(key).prefix().replacements(replacements);
        send(m, target);
    }

}
