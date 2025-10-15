package be.esmay.atlas.common.network.packet.packets;

import be.esmay.atlas.common.network.packet.Packet;
import be.esmay.atlas.common.network.packet.PacketHandler;
import be.esmay.atlas.common.network.packet.PacketUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MetadataUpdatePacket implements Packet {
    
    private static final Gson GSON = new Gson();
    private static final Type METADATA_TYPE = new TypeToken<Map<String, String>>(){}.getType();
    
    private String serverId;
    private Map<String, String> metadata;
    
    @Override
    public int getId() {
        return 0x22;
    }
    
    @Override
    public void encode(ByteBuf buffer) {
        PacketUtils.writeString(buffer, this.serverId);

        if (this.metadata == null) {
            PacketUtils.writeString(buffer, null);
            return;
        }

        String json = GSON.toJson(this.metadata, METADATA_TYPE);
        PacketUtils.writeString(buffer, json);
    }

    @Override
    public void decode(ByteBuf buffer) {
        try {
            this.serverId = PacketUtils.readString(buffer);

            String json = PacketUtils.readString(buffer);
            if (json == null) {
                this.metadata = null;
                return;
            }

            this.metadata = GSON.fromJson(json, METADATA_TYPE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode MetadataUpdatePacket from JSON", e);
        }
    }

    @Override
    public void handle(PacketHandler handler) {
        handler.handleMetadataUpdate(this);
    }
}