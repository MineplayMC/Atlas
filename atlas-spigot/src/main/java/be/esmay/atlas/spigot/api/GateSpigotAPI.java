package be.esmay.atlas.spigot.api;

import be.esmay.atlas.common.gate.enums.ConnectionType;
import be.esmay.atlas.common.gate.packets.GateConnectPlayerPacket;
import be.esmay.atlas.spigot.AtlasSpigotPlugin;
import be.esmay.atlas.spigot.network.AtlasNetworkClient;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public final class GateSpigotAPI {

    private static AtlasNetworkClient networkClient;
    private static AtlasSpigotPlugin plugin;
    private static boolean initialized = false;


    public static void initialize(AtlasNetworkClient networkClient, AtlasSpigotPlugin plugin) {
        GateSpigotAPI.networkClient = networkClient;
        GateSpigotAPI.plugin = plugin;
        GateSpigotAPI.initialized = true;
    }


    public static void shutdown() {
        GateSpigotAPI.networkClient = null;
        GateSpigotAPI.plugin = null;
        GateSpigotAPI.initialized = false;
    }

    public static void setInitialized(boolean initialized) {
        GateSpigotAPI.initialized = initialized;
    }

    public static void connect(Player player, ConnectionType type, String server) {
        connect(player, type.name(), server);
    }

    public static void connect(Player player, String type, String server) {
        if (!GateSpigotAPI.initialized) {
            return;
        }

        GateConnectPlayerPacket packet = new GateConnectPlayerPacket(player.getUniqueId(), server, System.getenv("SERVER_NAME"), type);
        GateSpigotAPI.networkClient.sendPacket(packet);
    }
}