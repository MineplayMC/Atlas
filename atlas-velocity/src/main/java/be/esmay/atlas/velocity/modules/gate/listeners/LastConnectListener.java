package be.esmay.atlas.velocity.modules.gate.listeners;

import be.esmay.atlas.velocity.modules.data.DataModule;
import be.esmay.atlas.velocity.modules.gate.GateModule;
import be.esmay.atlas.velocity.modules.gate.models.LastConnectModel;
import be.esmay.atlas.velocity.utils.Concurrency;
import com.jazzkuh.modulemanager.velocity.handlers.listeners.AbstractListener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public final class LastConnectListener extends AbstractListener {

    private final GateModule gateModule;
    private final DataModule dataModule;

    @Subscribe
    public void onJoin(PostLoginEvent event) {
        Concurrency.async().execute(() -> {
            Collection<LastConnectModel> models = this.dataModule.getClient().getRepository(LastConnectModel.class).valuesWhere("unique_id", event.getPlayer().getUniqueId().toString()).join();
            this.gateModule.getLastConnectManager().getUserData().put(event.getPlayer().getUniqueId(), new ArrayList<>(models));
        });
    }

    @Subscribe
    public void onSwitch(ServerConnectedEvent event) {
        if (event.getPreviousServer().isEmpty()) return;
        if (event.getServer() == null) return;

        this.gateModule.getLastConnectManager().saveServer(event.getPlayer(), event.getServer().getServerInfo().getName());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        this.gateModule.getLastConnectManager().clearUser(event.getPlayer().getUniqueId());
    }
}