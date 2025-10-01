package be.esmay.atlas.common.network.packet.packets;

import be.esmay.atlas.common.network.packet.Packet;
import be.esmay.atlas.common.network.packet.PacketHandler;
import be.esmay.atlas.common.network.packet.PacketUtils;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ServerControlPacket implements Packet {
    
    private static final Gson GSON = new Gson();
    private String serverIdentifier; // Can be either server ID or server name
    private ControlAction action;
    private String requesterId;
    
    public enum ControlAction {
        START,
        STOP,
        RESTART
    }
    
    @Override
    public int getId() {
        return 0x31;
    }
    
    @Override
    public void encode(ByteBuf buffer) {
        ServerControlData data = new ServerControlData(this.serverIdentifier, this.action, this.requesterId);
        String json = GSON.toJson(data);
        PacketUtils.writeString(buffer, json);
    }

    @Override
    public void decode(ByteBuf buffer) {
        String json = PacketUtils.readString(buffer);
        ServerControlData data = GSON.fromJson(json, ServerControlData.class);

        this.serverIdentifier = data.serverIdentifier;
        this.action = data.action;
        this.requesterId = data.requesterId;
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handleServerControl(this);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ServerControlData {
        private String serverIdentifier;
        private ControlAction action;
        private String requesterId;
    }
}