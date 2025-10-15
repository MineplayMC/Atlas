package be.esmay.atlas.velocity.modules.gate.connection.lastconnect;

import be.esmay.atlas.velocity.modules.data.DataModule;
import be.esmay.atlas.velocity.modules.gate.GateModule;
import be.esmay.atlas.velocity.modules.gate.models.BlacklistedServerModel;
import be.esmay.atlas.velocity.modules.gate.models.LastConnectModel;
import be.esmay.atlas.velocity.modules.gate.objects.LastConnectServerType;
import be.esmay.atlas.velocity.utils.mariadb.Repository;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class LastConnectManager {

    private final GateModule gateModule;
    private final DataModule dataModule;
    private final List<BlacklistedServerModel> blacklistedServerModels = new ArrayList<>();

    @Getter
    private final Map<UUID, List<LastConnectModel>> userData = new HashMap<>();

    public void loadBlacklistedServers() {
        this.blacklistedServerModels.clear();
        this.blacklistedServerModels.addAll(this.dataModule.getClient().getRepository(BlacklistedServerModel.class).values().join());
    }

    public RegisteredServer getServer(Player player, String serverType) {
        LastConnectServerType lastConnectServerType = this.gateModule.getPlugin().getDefaultConfiguration().getLastConnectServerType(serverType);

        if (!this.userData.containsKey(player.getUniqueId()))
            return this.gateModule.getPlugin().getProxyServer().getServer(lastConnectServerType.getDefaultServer()).orElse(null);

        List<LastConnectModel> servers = this.userData.get(player.getUniqueId());

        LastConnectModel model = servers.stream().filter(lastConnectModel -> lastConnectModel.getServerType().equals(serverType)).findFirst().orElse(null);
        if (model == null || this.blacklistedServerModels
                .stream()
                .anyMatch(blacklistedServerModel -> blacklistedServerModel.getServerName().equalsIgnoreCase(model.getServerName().toLowerCase())
                        && !player.hasPermission(blacklistedServerModel.getBypassPermission())))
            return this.gateModule.getPlugin().getProxyServer().getServer(lastConnectServerType.getDefaultServer()).orElse(null);

        return this.gateModule.getPlugin().getProxyServer().getServer(model.getServerName()).orElse(null);
    }

    public LastConnectServerType findType(String server) {
        return this.gateModule.getPlugin().getDefaultConfiguration().findTypeByServerName(server);
    }

    public void saveServer(Player player, String server) {
        LastConnectServerType type = this.findType(server);
        if (type == null) return;

        RegisteredServer registeredServer = this.getServer(player, type.getId());
        if (registeredServer == null) return;

        this.userData.putIfAbsent(player.getUniqueId(), new ArrayList<>());

        Repository<LastConnectModel> repository = this.dataModule.getClient().getRepository(LastConnectModel.class);
        repository.getWhereAnd("unique_id", player.getUniqueId().toString(), "server_type", type.getId()).whenComplete((model, throwable) -> {
            if (throwable != null) {
                this.gateModule.getLogger().error("Could not save last connect server for {}!", player.getUsername(), throwable);
                return;
            }

            if (model == null) model = new LastConnectModel();

            model.setServerName(server);
            model.setServerType(type.getId());
            model.setUniqueId(player.getUniqueId());

            LastConnectModel finalModel = model;
            repository.save(finalModel).whenComplete((id, newThrowable) -> {
                if (newThrowable != null) {
                    this.gateModule.getLogger().error("Could not save last connect server for {}!", player.getUsername(), newThrowable);
                    return;
                }

                finalModel.setId(id);
                this.userData.get(player.getUniqueId()).removeIf(lastConnectModel -> lastConnectModel.getServerType().equals(type.getId()));
                this.userData.get(player.getUniqueId()).add(finalModel);
            });
        });
    }

    public void clearUser(UUID uuid) {
        this.userData.remove(uuid);
    }
}