/*
 * Copyright 2020-2022 RukkitDev Team and contributors.
 *
 * This project uses GNU Affero General Public License v3.0.You can find this license in the following link.
 * 本项目使用 GNU Affero General Public License v3.0 许可证，你可以在下方链接查看:
 *
 * https://github.com/RukkitDev/Rukkit/blob/master/LICENSE
 */

package cn.rukkit.plugin.internal;

import cn.rukkit.Rukkit;
import cn.rukkit.command.*;
import cn.rukkit.event.EventHandler;
import cn.rukkit.event.EventListener;
import cn.rukkit.event.server.ServerQuestionRespondEvent;
import cn.rukkit.game.NetworkPlayer;
import cn.rukkit.game.map.CustomMapLoader;
import cn.rukkit.game.map.OfficialMap;
import cn.rukkit.network.Connection;
import cn.rukkit.network.packet.Packet;
import cn.rukkit.plugin.PluginConfig;
import cn.rukkit.util.LangUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerCommandPlugin extends InternalRukkitPlugin implements EventListener {

    @EventHandler
    public void onResponse(ServerQuestionRespondEvent event) {
        Logger log = getLogger();
        if (event.getQid() == 99999) {
            log.info("question from player {}: {}", event.getPlayer().name, event.getRespondMessage());
        }
    }

    class SurrenderCallback implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            if (args.length >= 1) {
                int slot = Integer.parseInt(args[0]);
                NetworkPlayer player = Rukkit.getConnectionManager().getPlayerManager().get(slot);
                if (!player.isSurrounded) {
                    try {
                        Rukkit.getConnectionManager().broadcast(Packet.gameSurrounder(slot));
                    } catch (IOException e) {
                        getLogger().error("An error occurred:", e);
                    }
                }
            }
        }
    }

    class StateCallback implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            // TODO: Implement this method
            StringBuilder build = new StringBuilder();
            build.append("- State - \n");
            build.append("RAM Usage: " +  (Runtime.getRuntime().freeMemory() / 10240) + "M/" + (Runtime.getRuntime().totalMemory()) / 10240 + "M\n");
            build.append("Connections: " + Rukkit.getConnectionManager().size() + "\n");
            build.append("ThreadManager Tasks: " + Rukkit.getThreadManager().getActiveThreadCount() + "/" + Rukkit.getConfig().threadPoolCount);
            System.out.println(build);
        }
    }

    class PlayerListCallback implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            StringBuffer buffer = new StringBuffer("- Players -\n");
            for (Connection conn: Rukkit.getConnectionManager().getConnections()) {
                buffer.append(String.format("%s (Team %d) (%d ms)\n",conn.player.name, conn.player.team, (System.currentTimeMillis() - conn.pingTime)));
            }
            System.out.println(buffer);
        }
    }

    public class KickCallBack implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            // TODO: Implement this method
            if (args.length > 1 || !Rukkit.getGameServer().isGaming()) {
                int id = Integer.parseInt(args[0]);
                NetworkPlayer player = Rukkit.getConnectionManager().getPlayerManager().get(id);
                try {
                    player.isNull();
                    player.getConnection().kick(LangUtil.getString("chat.kicked"));
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(LangUtil.getString("chat.playerEmpty"));
                }
            }
        }
    }

    class HelpCallback implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            // TODO: Implement this method
            StringBuilder build = new StringBuilder();
            if (args.length > 0) {
                build.append("- Help -  Page " + args[0] + " \n");
                int page = Integer.valueOf(args[0]) - 1;
                for (int i = page * 10;i < Rukkit.getCommandManager().getLoadedServerCommand().entrySet().size();i++) {
                    if (i > page * 10 + 10) break;
                    ServerCommand cmd = (ServerCommand) ((Map.Entry) Rukkit.getCommandManager().getLoadedServerCommand().entrySet().toArray()[i]).getValue();
                    build.append(String.format("%s : %s", cmd.cmd, cmd.helpMessage) + "\n");
                }
            } else {
                build.append("- Help -  Page 1 \n");
                for (int i = 0;i < Rukkit.getCommandManager().getLoadedServerCommand().entrySet().size();i++) {
                    if (i > 10) break;
                    ServerCommand cmd = (ServerCommand) ((Map.Entry) Rukkit.getCommandManager().getLoadedServerCommand().entrySet().toArray()[i]).getValue();
                    build.append(String.format("%s : %s", cmd.cmd, cmd.helpMessage) + "\n");
                }
            }
            System.out.println(build.toString());
        }
    }

    class SayCallback implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            if (args.length >= 1) {
                Rukkit.getConnectionManager().broadcastServerMessage(args[0]);
            }
        }
    }

    class ShutdownCallback implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            Rukkit.shutdown("Server shutdown.");
        }
    }

    class KickAllCallback implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            Rukkit.getConnectionManager().broadcastServerMessage("Server kicked you.");
            try {
                if (!Rukkit.getGameServer().isGaming()) {
                    Rukkit.getConnectionManager().broadcast(Packet.kick("Server kicked you."));
                }
                Rukkit.getConnectionManager().disconnect();
            } catch (IOException e) {
                // Never happen.
            }
        }
    }

    public class MapsCallback implements ServerCommandListener {
        private int type;
        public MapsCallback(int type) {
            this.type = type;
        }
        @Override
        public void onSend(String[] args) {
            // TODO: Implement this method
            // Maps
            if (type == 0) {
                StringBuilder build = new StringBuilder();
                if (args.length > 0) {
                    build.append("- Maps -  Page ").append(args[0]).append(" \n");
                    int page = Integer.parseInt(args[0]) - 1;
                    for (int i = page * 10; i < OfficialMap.maps.length; i++) {
                        if (i > page * 10 + 10) break;
                        build.append(String.format("[%d] %s", i, OfficialMap.maps[i])).append("\n");
                    }
                } else {
                    build.append("- Help -  Page 1 \n");
                    for (int i = 0;i < 10;i++) {
                        build.append(String.format("[%d] %s", i, OfficialMap.maps[i])).append("\n");
                    }
                }
                // con.sendServerMessage(build.toString());
                System.out.println(build);
            } else {
                if (args.length > 0) {
                    if (args[0].startsWith("'")) {
                        String mapString = args[0].split("'")[1];
                        for (int i=0;i < OfficialMap.mapsName.length;i++) {
                            if (OfficialMap.mapsName[i].contains(mapString)) {
                                Rukkit.getRoundConfig().mapName = OfficialMap.maps[i];
                                Rukkit.getRoundConfig().mapType = 0;
                                try {
                                    Rukkit.getConnectionManager().broadcast(Packet.serverInfo());
                                } catch (IOException ignored) {}
                                break;
                            }
                        }
                        //ChannelGroups.broadcast(new Packet().chat(p.playerName, "-map " + cmd[1], p.playerIndex));
                        // return false;
                    }
                    int id = Integer.parseInt(args[0]);
                    Rukkit.getRoundConfig().mapName = OfficialMap.maps[id];
                    Rukkit.getRoundConfig().mapType = 0;
                }
            }
            // return false;
        }
    }

    public static class CustomMapsCallback implements ServerCommandListener {
        private final int type;
        public CustomMapsCallback(int type) {
            this.type = type;
        }
        @Override
        public void onSend(String[] args) {
            // TODO: Implement this method
            // Maps
            if (type == 0) {
                StringBuilder build = new StringBuilder();
                List<String> li = CustomMapLoader.getMapNameList();
                if (args.length > 0) {
                    build.append("- CustomMaps -  Page ").append(args[0]).append(" \n");
                    int page = Integer.parseInt(args[0]) - 1;
                    for (int i = page * 10;i < li.size();i++) {
                        if (i > page * 10 + 10) break;
                        build.append(String.format("[%d] %s", i, li.get(i))).append("\n");
                    }
                } else {
                    build.append("- Help -  Page 1 \n");
                    for (int i = 0; i < (Math.min(li.size(), 10)); i++) {
                        build.append(String.format("[%d] %s", i, li.get(i))).append("\n");
                    }
                }
                // con.sendServerMessage(build.toString());
                System.out.println(build);
            } else {
                if (args.length > 0) {
                    ArrayList<String> mapList = CustomMapLoader.getMapNameList();
                    int id = Integer.parseInt(args[0]);
                    Rukkit.getRoundConfig().mapName = mapList.get(id).toString();
                    Rukkit.getRoundConfig().mapType = 1;
                    try {
                        Rukkit.getConnectionManager().broadcast(Packet.serverInfo());
                    } catch (IOException ignored) {}
                }
            }
            // return false;
        }
    }

    public class QuestionCallback implements ServerCommandListener {
        @Override
        public void onSend(String[] args) {
            try {
                Rukkit.getConnectionManager().getPlayerManager().get(Integer.parseInt(args[0])).getConnection().handler.ctx.writeAndFlush(Packet.packetQuestion(99999, args[1]));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onLoad() {
        getLogger().info("ServerCommandPlugin::onLoad...");
        CommandManager mgr = Rukkit.getCommandManager();
        Rukkit.getPluginManager().registerEventListener(this, this);
        mgr.registerServerCommand(new ServerCommand("help", LangUtil.getString("chat.help"), 1, new HelpCallback(), this));
        mgr.registerServerCommand(new ServerCommand("kick", LangUtil.getString("chat.kick"), 1, new KickCallBack(), this));
        mgr.registerServerCommand(new ServerCommand("surrender", LangUtil.getString("chat.surrender"), 1, new SurrenderCallback(),this));
        mgr.registerServerCommand(new ServerCommand("state", LangUtil.getString("chat.state"), 0, new StateCallback(), this));
        mgr.registerServerCommand(new ServerCommand("list", LangUtil.getString("chat.list"), 0, new PlayerListCallback(),this));
        mgr.registerServerCommand(new ServerCommand("kickall", LangUtil.getString("server.kickAll"), 0, new KickAllCallback(), this));
        mgr.registerServerCommand(new ServerCommand("shutdown", LangUtil.getString("server.shutdown"), 0, new ShutdownCallback(), this));
        mgr.registerServerCommand(new ServerCommand("say", LangUtil.getString("server.say"), 1, new SayCallback(), this));
        mgr.registerServerCommand(new ServerCommand("maps", LangUtil.getString("chat.maps"), 1, new MapsCallback(0), this));
        mgr.registerServerCommand(new ServerCommand("map", LangUtil.getString("chat.map"), 1, new MapsCallback(1), this));
        mgr.registerServerCommand(new ServerCommand("cmaps", LangUtil.getString("chat.cmaps"), 1, new CustomMapsCallback(0), this));
        mgr.registerServerCommand(new ServerCommand("cmap", LangUtil.getString("chat.cmap"), 1, new CustomMapsCallback(1), this));
        mgr.registerServerCommand(new ServerCommand("question", "Question a player.", 2, new QuestionCallback(), this));

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
        // TODO: Implement this method
        config = new PluginConfig();
        config.name = "server-command-plugin";
        config.author = "rukkit";
        config.version = "1.0.0";
        config.pluginClass = "cn.rukkit.plugin.internal.ServerCommandPlugin";
        config.apiVersion = "ANY";
    }
}
