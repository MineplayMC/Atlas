package be.esmay.atlas.minestom.api;

import be.esmay.atlas.common.enums.ServerStatus;
import be.esmay.atlas.common.gate.enums.ConnectionType;
import be.esmay.atlas.common.gate.packets.GateConnectPlayerPacket;
import be.esmay.atlas.common.models.AtlasServer;
import be.esmay.atlas.common.models.ServerInfo;
import be.esmay.atlas.common.network.packet.packets.MetadataUpdatePacket;
import be.esmay.atlas.common.network.packet.packets.ServerControlPacket;
import be.esmay.atlas.minestom.AtlasMinestomPlugin;
import be.esmay.atlas.minestom.cache.NetworkServerCacheManager;
import be.esmay.atlas.minestom.network.AtlasNetworkClient;
import be.esmay.atlas.minestom.server.MinestomServerInfoManager;
import lombok.experimental.UtilityClass;
import net.minestom.server.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public final class GateMinestomAPI {

    private static AtlasNetworkClient networkClient;
    private static AtlasMinestomPlugin plugin;
    private static boolean initialized = false;
    

    public static void initialize(AtlasNetworkClient networkClient, AtlasMinestomPlugin plugin) {
        GateMinestomAPI.networkClient = networkClient;
        GateMinestomAPI.plugin = plugin;
        GateMinestomAPI.initialized = true;
    }
    

    public static void shutdown() {
        GateMinestomAPI.networkClient = null;
        GateMinestomAPI.plugin = null;
        GateMinestomAPI.initialized = false;
    }

    public static void setInitialized(boolean initialized) {
        GateMinestomAPI.initialized = initialized;
    }

    public static void connect(Player player, ConnectionType type, String server) {
        connect(player, type.name(), server);
    }

    public static void connect(Player player, String type, String server) {
        if (!GateMinestomAPI.initialized) {
            return;
        }

        GateConnectPlayerPacket packet = new GateConnectPlayerPacket(player.getUuid(), server, System.getenv("SERVER_NAME"), type);
        GateMinestomAPI.networkClient.sendPacket(packet);
    }
}