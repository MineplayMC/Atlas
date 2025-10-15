package be.esmay.atlas.velocity.modules.gate.api;

import be.esmay.atlas.common.gate.enums.ConnectionType;
import be.esmay.atlas.velocity.modules.gate.connection.mode.AbstractConnectionMode;
import be.esmay.atlas.velocity.modules.gate.connection.mode.ConnectionModeRegistry;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class GateVelocityAPI {

    public static void connect(Player player, ConnectionType type, String server) {
        connect(player, type.name(), server);
    }

    public static boolean connect(Player player, String type, String server) {
        AbstractConnectionMode connectionMode = ConnectionModeRegistry.getConnectionMode(type);
        if (connectionMode == null) return false;

        RegisteredServer registeredServer = connectionMode.getServer(player, server);
        if (registeredServer == null) return false;

        player.createConnectionRequest(registeredServer).fireAndForget();
        return true;
    }

}