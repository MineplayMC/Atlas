package be.esmay.atlas.velocity.modules.gate.models;

import be.esmay.atlas.velocity.utils.mariadb.DataStore;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@Table(name = "gate_lastconnect")
public final class LastConnectModel extends DataStore {

    @Column
    private UUID uniqueId;

    @Column
    private String serverType;

    @Column
    private String serverName;

}
