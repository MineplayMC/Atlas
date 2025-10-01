package be.esmay.atlas.velocity.modules.gate.connection.mode.impl;

import be.esmay.atlas.velocity.AtlasVelocityPlugin;
import be.esmay.atlas.velocity.modules.gate.GateModule;
import be.esmay.atlas.velocity.modules.gate.connection.mode.AbstractConnectionMode;
import be.esmay.atlas.velocity.modules.gate.objects.LastConnectServerType;
import be.esmay.atlas.velocity.utils.ChatUtils;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public final class LastConnectedConnectionMode extends AbstractConnectionMode {

    @Override
    public RegisteredServer getServer(Player player, String server) {
        LastConnectServerType type = AtlasVelocityPlugin.getInstance().getDefaultConfiguration().getLastConnectServerType(server.toUpperCase());
        if (type == null) {
            player.sendMessage(ChatUtils.format(AtlasVelocityPlugin.getInstance().getMessagesConfiguration().getTargetServerOffline()));
            return null;
        }

        RegisteredServer registeredServer = AtlasVelocityPlugin.getModuleManager().get(GateModule.class).getLastConnectManager().getServer(player, type.getId());
        if (registeredServer == null) {
            player.sendMessage(ChatUtils.format(AtlasVelocityPlugin.getInstance().getMessagesConfiguration().getTargetServerOffline()));
            return null;
        }

        return registeredServer;
    }

}
