package be.esmay.atlas.velocity.modules.gate.objects;

import lombok.Getter;
import lombok.Setter;
import org.spongepowered.configurate.ConfigurationNode;

@Getter
@Setter
public final class LastConnectServerType {

    private final String id;
    private final ConfigurationNode configurationNode;

    private String defaultServer;
    private String serverPrefix;

    public LastConnectServerType(String id, ConfigurationNode configurationNode) {
        this.id = id;
        this.configurationNode = configurationNode;

        this.defaultServer = configurationNode.node("default-server").getString("lobby");
        this.serverPrefix = configurationNode.node("server-prefix").getString("game-");
    }

}
