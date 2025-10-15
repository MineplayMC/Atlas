package be.esmay.atlas.velocity.modules.gate.models;

import be.esmay.atlas.velocity.utils.mariadb.DataStore;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Table(name = "gate_lastconnect_blacklisted")
public final class BlacklistedServerModel extends DataStore {

    @Column
    private String serverName;

    @Column
    private String bypassPermission;

}