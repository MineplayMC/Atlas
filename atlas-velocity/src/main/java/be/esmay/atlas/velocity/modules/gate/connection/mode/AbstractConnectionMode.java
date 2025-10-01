package be.esmay.atlas.velocity.modules.gate.connection.mode;

import be.esmay.atlas.common.enums.ServerStatus;
import be.esmay.atlas.common.models.AtlasServer;
import be.esmay.atlas.velocity.AtlasVelocityPlugin;
import be.esmay.atlas.velocity.modules.scaling.api.AtlasVelocityAPI;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;

public abstract class AbstractConnectionMode {

    public abstract RegisteredServer getServer(Player player, String server);

    protected RegisteredServer getVelocityServer(String name) {
        return AtlasVelocityPlugin.getInstance().getProxyServer().getServer(name).orElse(null);
    }

    protected String getCurrentServer(Player player) {
        return player.getCurrentServer().map(serverConnection -> serverConnection.getServerInfo().getName()).orElse(null);
    }

    protected boolean isOnline(AtlasServer atlasServer) {
        return atlasServer.getServerInfo() != null && atlasServer.getServerInfo().getStatus() == ServerStatus.RUNNING;
    }

    protected List<AtlasServer> getOnlineServers(String group, String excludeServer) {
        return AtlasVelocityAPI.getServersByGroup(group)
                .stream()
                .filter(server -> server.getServerInfo() != null && server.getServerInfo().getStatus() == ServerStatus.RUNNING)
                .filter(server -> excludeServer != null && !server.getName().equals(excludeServer))
                .toList();
    }
}
