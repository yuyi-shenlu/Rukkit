/*
 * Copyright 2020-2022 RukkitDev Team and contributors.
 *
 * This project uses GNU Affero General Public License v3.0.You can find this license in the following link.
 * 本项目使用 GNU Affero General Public License v3.0 许可证，你可以在下方链接查看:
 *
 * https://github.com/RukkitDev/Rukkit/blob/master/LICENSE
 */

/*
 * Copyright 2020-2022 RukkitDev Team and contributors.
 */

package cn.rukkit.plugin.internal;

import cn.rukkit.Rukkit;
import cn.rukkit.event.EventHandler;
import cn.rukkit.event.EventListener;
import cn.rukkit.event.player.PlayerJoinEvent;
import cn.rukkit.event.player.PlayerLeftEvent;
import cn.rukkit.plugin.PluginConfig;
import cn.rukkit.util.LangUtil;

import java.text.MessageFormat;

public class BasePlugin extends InternalRukkitPlugin implements EventListener {

    @EventHandler
    public void onPlayerJoinTip(PlayerJoinEvent event) {
        Rukkit.getConnectionManager().broadcastServerMessage(MessageFormat.format(LangUtil.getString("rukkit.playerJoin"), event.getPlayer().name));
    }

    @EventHandler
    public void onPlayerLeaveTip(PlayerLeftEvent event) {
        Rukkit.getConnectionManager().broadcastServerMessage(MessageFormat.format(LangUtil.getString("rukkit.playerLeft"), event.getPlayer().name));
    }

    @Override
    public void onLoad() {
        getLogger().info("BasePlugin::Load");
        getPluginManager().registerEventListener(this, this);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onDone() {

    }

    @Override
    public void loadConfig() {
        config = new PluginConfig();
        config.name = "base-plugin";
        config.author = "rukkit";
        config.version = "1.0.0";
        config.pluginClass = "cn.rukkit.plugin.internal.BasePlugin";
        config.apiVersion = "ANY";
    }
}
