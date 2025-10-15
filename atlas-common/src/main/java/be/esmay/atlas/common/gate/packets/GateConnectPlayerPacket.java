package be.esmay.atlas.common.gate.packets;

import be.esmay.atlas.common.network.packet.Packet;
import be.esmay.atlas.common.network.packet.PacketHandler;
import be.esmay.atlas.common.network.packet.PacketUtils;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public final class GateConnectPlayerPacket implements Packet {

    private UUID uniqueId;
    private String server;
    private String requesterId;
    private String connectionType;

    @Override
    public int getId() {
        return 0x41;
    }

    @Override
    public void encode(ByteBuf buffer) {
        PacketUtils.writeString(buffer, this.uniqueId != null ? this.uniqueId.toString() : null);
        PacketUtils.writeString(buffer, this.server);
        PacketUtils.writeString(buffer, this.requesterId);
        PacketUtils.writeString(buffer, this.connectionType);
    }

    @Override
    public void decode(ByteBuf buffer) {
        String uuidString = PacketUtils.readString(buffer);
        this.uniqueId = uuidString != null ? UUID.fromString(uuidString) : null;
        this.server = PacketUtils.readString(buffer);
        this.requesterId = PacketUtils.readString(buffer);
        this.connectionType = PacketUtils.readString(buffer);
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handleGatePlayerConnect(this);
    }

}