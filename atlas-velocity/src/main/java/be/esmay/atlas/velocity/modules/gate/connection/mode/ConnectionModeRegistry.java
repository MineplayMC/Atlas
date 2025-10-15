package be.esmay.atlas.velocity.modules.gate.connection.mode;

import be.esmay.atlas.velocity.AtlasVelocityPlugin;
import be.esmay.atlas.velocity.modules.gate.GateModule;
import be.esmay.atlas.velocity.modules.gate.connection.mode.impl.LastConnectedConnectionMode;
import be.esmay.atlas.velocity.modules.gate.connection.mode.impl.LeastPlayersConnectionMode;
import be.esmay.atlas.velocity.modules.gate.connection.mode.impl.RandomServerConnectionMode;
import be.esmay.atlas.velocity.modules.gate.connection.mode.impl.StaticServerConnectionMode;

import java.util.HashMap;
import java.util.Map;

public final class ConnectionModeRegistry {

    private static final Map<String, Class<? extends AbstractConnectionMode>> ACTIONS = new HashMap<>();

    static {
        ACTIONS.put("LAST_CONNECTED", LastConnectedConnectionMode.class);
        ACTIONS.put("LEAST_PLAYERS", LeastPlayersConnectionMode.class);
        ACTIONS.put("RANDOM", RandomServerConnectionMode.class);
        ACTIONS.put("STATIC", StaticServerConnectionMode.class);
    }

    public static void registerConnectionMode(String actionKey, Class<? extends AbstractConnectionMode> actionClass) {
        ACTIONS.put(actionKey, actionClass);
    }

    public static AbstractConnectionMode getConnectionMode(String key) {
        Class<? extends AbstractConnectionMode> actionClass = ACTIONS.get(key);
        if (actionClass == null) return null;

        try {
            return actionClass.getConstructor().newInstance();
        } catch (Exception e) {
            AtlasVelocityPlugin.getModuleManager().get(GateModule.class).getLogger().error("Failed to create connection mode instance for key: {}", key, e);
            return null;
        }
    }
}