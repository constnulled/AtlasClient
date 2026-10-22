package dev.atlasclient.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import dev.atlasclient.Helper;

import java.io.*;
import java.util.UUID;

// Implement CustomPayload interface
public record TwoFAPayloadPacket() implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, TwoFAPayloadPacket> CODEC = PacketCodec.of(
        // ValueFirstEncoder - takes packet first, then buffer
        (packet, buf) -> packet.write(buf),
        // PacketDecoder - constructor that takes buffer
        TwoFAPayloadPacket::new
    );

    // Use the full class name for Id
    public static final CustomPayload.Id<TwoFAPayloadPacket> ID = 
        new CustomPayload.Id<>(Identifier.of("2fa", "2fa"));

    // Constructor for reading from buffer
    private TwoFAPayloadPacket(PacketByteBuf buf) {
        this();
    }

    // Write method
    private void write(PacketByteBuf buf) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            out.writeUTF("ForwardToPlayer");
            out.writeUTF(UUID.randomUUID().toString());
            out.writeUTF(MinecraftClient.getInstance().getGameProfile().getId().toString());
            ByteArrayOutputStream s1 = new ByteArrayOutputStream();
            DataOutputStream o1 = new DataOutputStream(s1);
            o1.writeUTF("SET_STATE");
            o1.writeUTF("AUTHENTICATED");
            byte[] msgBody = s1.toByteArray();
            out.writeShort((short) msgBody.length);
            out.write(msgBody);

            buf.writeBytes(stream.toByteArray());
        } catch (IOException e) {
            Helper.printChatMessage(e.getMessage());
        }
    }

    // Implement getId() method from CustomPayload interface
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
