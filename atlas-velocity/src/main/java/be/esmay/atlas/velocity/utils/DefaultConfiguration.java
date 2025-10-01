package be.esmay.atlas.velocity.utils;

import be.esmay.atlas.velocity.modules.gate.objects.LastConnectServerType;
import be.esmay.atlas.velocity.utils.configuration.ConfigurateConfig;
import lombok.Getter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public final class DefaultConfiguration extends ConfigurateConfig {

    private final String version;
    private final String lobbyGroup;

    private final boolean gateEnabled;
    private final String mysqlHost;
    private final int mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUsername;
    private final String mysqlPassword;
    private final List<LastConnectServerType> gateLastConnectServers;

    public DefaultConfiguration(Path folder) {
        super(folder, "config.yml");

        this.version = this.rootNode.node("_version").getString("1");
        this.lobbyGroup = this.rootNode.node("lobby-group").getString("Lobby");

        this.gateEnabled = this.rootNode.node("gate", "enabled").getBoolean(false);
        this.mysqlHost = this.rootNode.node("gate", "mysql", "host").getString("localhost");
        this.mysqlPort = this.rootNode.node("gate", "mysql", "port").getInt(3306);
        this.mysqlDatabase = this.rootNode.node("gate", "mysql", "database").getString("atlas");
        this.mysqlUsername = this.rootNode.node("gate", "mysql", "username").getString("root");
        this.mysqlPassword = this.rootNode.node("gate", "mysql", "password").getString("");

        List<LastConnectServerType> lastConnectServers = new ArrayList<>();
        if (!this.rootNode.node("gate", "last-connect-server-types").empty()) {
            this.rootNode.node("gate", "last-connect-server-types").childrenMap().forEach((stageKey, configurationNode) -> {
                lastConnectServers.add(new LastConnectServerType((String) stageKey, configurationNode));
            });
        }

        this.gateLastConnectServers = lastConnectServers;

        this.saveConfiguration();
    }

    public LastConnectServerType getLastConnectServerType(String id) {
        return this.gateLastConnectServers.stream().filter(type -> type.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public LastConnectServerType findTypeByServerName(String serverName) {
        return this.gateLastConnectServers.stream().filter(type -> serverName.startsWith(type.getServerPrefix())).findFirst().orElse(null);
    }
}
