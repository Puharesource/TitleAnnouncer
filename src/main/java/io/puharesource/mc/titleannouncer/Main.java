package io.puharesource.mc.titleannouncer;

import io.puharesource.mc.titleannouncer.commands.CommandTa;
import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.ConfigFile;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.ActionbarTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin {
    private ConfigFile configFile;

    private List<Object> hoveringMessages = new ArrayList<>();
    private List<Object> actionbarMessages = new ArrayList<>();

    private long hoveringInterval;
    private long actionbarInterval;

    private int hoveringId = -1;
    private int actionbarId = -1;

    public void onEnable() {
        configFile = new ConfigFile(this, getDataFolder(), "config", true);
        getCommand("ta").setExecutor(new CommandTa(this));

        reload();
    }

    public void reload() {
        if (hoveringMessages.size() > 0)
            hoveringMessages.clear();
        if (actionbarMessages.size() > 0)
            actionbarMessages.clear();

        if (hoveringId != -1)
            Bukkit.getScheduler().cancelTask(hoveringId);
        if (actionbarId != -1)
            Bukkit.getScheduler().cancelTask(actionbarId);

        hoveringId = -1;
        actionbarId = -1;

        configFile.load();

        hoveringInterval = configFile.getConfig().getInt("settings.hovering.interval") * 20;
        actionbarInterval = configFile.getConfig().getInt("settings.actionbar.interval") * 20;

        for (String message : configFile.getConfig().getStringList("messages.hovering")) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            Object object;
            if (message.contains("\\n")) {
                String[] messages = message.replace("\\n", "\n").split("\n", 2);

                if (messages[0].startsWith("animation:") || messages[1].startsWith("animation:")) {
                    object = new TitleAnimation(
                            (messages[0].toLowerCase().startsWith("animation:") ? Config.getAnimation(messages[0].substring("animation:".length())) == null ? messages[0] : Config.getAnimation(messages[0].substring("animation:".length())) : messages[0]),
                            (messages[1].toLowerCase().startsWith("animation:") ? Config.getAnimation(messages[1].substring("animation:".length())) == null ? messages[1] : Config.getAnimation(messages[1].substring("animation:".length())) : messages[1])
                    );
                } else object = new TitleObject(messages[0], messages[1]);


            } else {
                if (message.toLowerCase().startsWith("animation:")) {
                    object = Config.getAnimation(message.substring("animation:".length()));
                    if (object == null)
                        object = new TitleObject(message, TitleObject.TitleType.TITLE);
                    else object = new TitleAnimation((FrameSequence) object, "");
                } else object = new TitleObject(message, TitleObject.TitleType.TITLE);
            }

            if (object instanceof TitleObject)
                ((TitleObject) object).setFadeIn(configFile.getConfig().getInt("settings.hovering.fade-in")).setStay(configFile.getConfig().getInt("settings.hovering.stay")).setFadeOut(configFile.getConfig().getInt("settings.hovering.fade-out"));

            hoveringMessages.add(object);
        }

        for (String message : configFile.getConfig().getStringList("messages.actionbar")) {
            Object object;
            if (message.toLowerCase().startsWith("animation:")) {
                object = Config.getAnimation(message.substring("animation:".length()));
                if (object == null)
                    object = new ActionbarTitleObject(message);
                else object = new ActionbarTitleAnimation((FrameSequence) object);
                actionbarMessages.add(object);
            } else actionbarMessages.add(new ActionbarTitleObject(ChatColor.translateAlternateColorCodes('&', message)));
        }

        if (hoveringMessages.size() > 0) {
            hoveringId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                int i = 0;

                @Override
                public void run() {
                    for (Player player : getServer().getOnlinePlayers()) {
                        Object message = hoveringMessages.get(i);
                        if (message instanceof TitleObject)
                            ((TitleObject) message).send(player);
                        else ((TitleAnimation) message).send(player);
                    }

                    if (hoveringMessages.size() - 1 > i)
                        i++;
                    else i = 0;
                }
            }, hoveringInterval, hoveringInterval).getTaskId();
        }

        if (actionbarMessages.size() > 0) {
            actionbarId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                int i = 0;

                @Override
                public void run() {
                    for (Player player : getServer().getOnlinePlayers()) {
                        Object message = actionbarMessages.get(i);
                        if (message instanceof ActionbarTitleObject)
                            ((ActionbarTitleObject) message).send(player);
                        else ((ActionbarTitleAnimation) message).send(player);
                    }

                    if (actionbarMessages.size() - 1 > i)
                        i++;
                    else i = 0;
                }
            }, actionbarInterval, actionbarInterval).getTaskId();
        }
    }
}
