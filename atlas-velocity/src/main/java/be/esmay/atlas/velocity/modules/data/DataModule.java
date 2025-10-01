package be.esmay.atlas.velocity.modules.data;

import be.esmay.atlas.velocity.AtlasVelocityPlugin;
import be.esmay.atlas.velocity.modules.gate.models.BlacklistedServerModel;
import be.esmay.atlas.velocity.modules.gate.models.LastConnectModel;
import be.esmay.atlas.velocity.utils.mariadb.DataStore;
import be.esmay.atlas.velocity.utils.mariadb.MySQLClient;
import com.jazzkuh.modulemanager.velocity.VelocityModule;
import com.jazzkuh.modulemanager.velocity.VelocityModuleManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class DataModule extends VelocityModule<AtlasVelocityPlugin> {

    private MySQLClient client;

    public DataModule(VelocityModuleManager<AtlasVelocityPlugin> owningManager) {
        super(owningManager);
    }

    @Override
    public void onLoad() {
        this.client = new MySQLClient(
                "Atlas",
                AtlasVelocityPlugin.getInstance().getDefaultConfiguration().getMysqlHost(),
                AtlasVelocityPlugin.getInstance().getDefaultConfiguration().getMysqlPort(),
                AtlasVelocityPlugin.getInstance().getDefaultConfiguration().getMysqlDatabase(),
                AtlasVelocityPlugin.getInstance().getDefaultConfiguration().getMysqlUsername(),
                AtlasVelocityPlugin.getInstance().getDefaultConfiguration().getMysqlPassword()
        );

        List<Class<? extends DataStore>> tables = new ArrayList<>();
        tables.add(BlacklistedServerModel.class);
        tables.add(LastConnectModel.class);

        for (Class<? extends DataStore> table : tables) {
            this.client.getRepository(table);
        }

        this.getPlugin().getLogger().info("Successfully connected to the database.");
    }

    @Override
    public boolean shouldLoad() {
        return AtlasVelocityPlugin.getInstance().getDefaultConfiguration().isGateEnabled();
    }
}
