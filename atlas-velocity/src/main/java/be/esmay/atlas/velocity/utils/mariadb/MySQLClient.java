package be.esmay.atlas.velocity.utils.mariadb;

import com.craftmend.storm.Storm;
import com.craftmend.storm.connection.hikaricp.HikariDriver;
import com.craftmend.storm.parser.types.TypeRegistry;
import com.craftmend.storm.parser.types.objects.StormTypeAdapter;
import com.zaxxer.hikari.HikariConfig;
import lombok.Getter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class MySQLClient {

    @Getter
    private Storm storm;

    private final Map<Class<? extends DataStore>, Repository<? extends DataStore>> repositories = new HashMap<>();

    public MySQLClient(String name, String host, int port, String database, String username, String password) {
        try {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaxLifetime(480000);
            config.setPoolName(name + " Pool");

            this.storm = new Storm(new HikariDriver(config));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void registerType(Class<?> clazz, StormTypeAdapter<?> adapter) {
        TypeRegistry.registerAdapter(clazz, adapter);
    }

    public <T extends DataStore> Repository<T> getRepository(Class<T> dataClass) {
        if (this.repositories.containsKey(dataClass)) {
            return (Repository<T>) this.repositories.get(dataClass);
        }

        Repository<T> createdTable = new Repository<>();
        createdTable.onCreate(this.storm, dataClass, this);
        this.repositories.put(dataClass, createdTable);

        return createdTable;
    }
}
