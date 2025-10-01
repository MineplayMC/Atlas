package be.esmay.atlas.velocity.modules.gate.connection.mode.impl;

import be.esmay.atlas.velocity.modules.gate.connection.mode.AbstractConnectionMode;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public final class StaticServerConnectionMode extends AbstractConnectionMode {

    @Override
    public RegisteredServer getServer(Player player, String server) {
        return this.getVelocityServer(server);
    }

}
