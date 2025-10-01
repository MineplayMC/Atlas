package be.esmay.atlas.velocity.modules.gate.connection.mode.impl;

import be.esmay.atlas.common.models.AtlasServer;
import be.esmay.atlas.velocity.AtlasVelocityPlugin;
import be.esmay.atlas.velocity.modules.gate.connection.mode.AbstractConnectionMode;
import be.esmay.atlas.velocity.utils.ChatUtils;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.ArrayList;
import java.util.List;

public final class LeastPlayersConnectionMode extends AbstractConnectionMode {

    @Override
    public RegisteredServer getServer(Player player, String server) {
        String currentServer = this.getCurrentServer(player);
        if (currentServer == null) return null;

        List<AtlasServer> servers = new ArrayList<>(this.getOnlineServers(server, currentServer));

        AtlasServer optimalServer = null;
        int optimalPlayers = 0;

        for (AtlasServer atlasServer : servers) {
            if (optimalServer == null) {
                optimalServer = atlasServer;
                continue;
            }

            int players = atlasServer.getServerInfo().getOnlinePlayers();

            if (players < optimalPlayers) {
                optimalServer = atlasServer;
                optimalPlayers = players;
            }
        }

        if (optimalServer == null || !this.isOnline(optimalServer)) {
            player.sendMessage(ChatUtils.format(AtlasVelocityPlugin.getInstance().getMessagesConfiguration().getNoTargetServerFound()));
            return null;
        }

        return this.getVelocityServer(optimalServer.getName());
    }

}
