package be.esmay.atlas.velocity.modules.gate.connection.mode.impl;

import be.esmay.atlas.common.models.AtlasServer;
import be.esmay.atlas.velocity.AtlasVelocityPlugin;
import be.esmay.atlas.velocity.modules.gate.connection.mode.AbstractConnectionMode;
import be.esmay.atlas.velocity.utils.ChatUtils;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomServerConnectionMode extends AbstractConnectionMode {

    @Override
    public RegisteredServer getServer(Player player, String group) {
        String currentServer = this.getCurrentServer(player);
        if (currentServer == null) return null;

        List<AtlasServer> servers = this.getOnlineServers(group, currentServer);
        if (servers.isEmpty()) {
            player.sendMessage(ChatUtils.format(AtlasVelocityPlugin.getInstance().getMessagesConfiguration().getNoTargetServerFound()));
            return null;
        }

        AtlasServer atlasServer = servers.get(ThreadLocalRandom.current().nextInt(servers.size()));
        if (atlasServer == null || !this.isOnline(atlasServer)) {
            player.sendMessage(ChatUtils.format(AtlasVelocityPlugin.getInstance().getMessagesConfiguration().getNoTargetServerFound()));
            return null;
        }

        return this.getVelocityServer(atlasServer.getName());
    }

}
