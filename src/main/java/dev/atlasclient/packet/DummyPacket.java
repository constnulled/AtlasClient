package dev.atlasclient.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record DummyPacket() implements CustomPayload {
  public static final PacketCodec<PacketByteBuf, DummyPacket> CODEC =
    CustomPayload.codecOf(DummyPacket::write, DummyPacket::new);

  public DummyPacket(PacketByteBuf buf) {
    this();
  }

  public void write(PacketByteBuf buf) {
  }

  @Override public Id<? extends CustomPayload> getId() {
    return null;
  }
}
