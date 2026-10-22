package dev.atlasclient.inject.mixin.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.handler.*;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.*;
import net.minecraft.network.state.NetworkState;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import dev.atlasclient.Helper;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(DecoderHandler.class) public class DecoderHandlerMixin<T extends PacketListener> {
  @Mutable @Final @Shadow private final NetworkState<T> state;

  @SuppressWarnings("unused") public DecoderHandlerMixin(NetworkState<T> state) {
    this.state = state;
  }

  /**
   * Prevents issues with via version wrongly translating packets. Notifies in chat about handling errors.
   *
   * @author SpigotRCE
   * @reason To prevent disconnection issues
   */
  @Overwrite() public void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> objects) {
    int i = buf.readableBytes();
    if (i != 0) {
      Packet<? super T> packet = this.state.codec().decode(buf);
      PacketType<? extends Packet<? super T>> packetType = packet.getPacketType();
      FlightProfiler.INSTANCE.onPacketReceived(this.state.id(), packetType, context.channel().remoteAddress(), i);
      if (buf.readableBytes() > 0) {
        Helper.printChatMessage("&cError handling packet " +
          this.state.id().getId() +
          "/" +
          packetType +
          " (" +
          packet.getClass().getSimpleName() +
          ") was larger than I expected, found " +
          buf.readableBytes() +
          " bytes extra whilst reading packet " +
          packetType);
        Helper.printChatMessage("&cThis is a warning, not an error. The client would've disconnect if this" +
          "wasn't in place!");
      } else {
        objects.add(packet);
        NetworkStateTransitionHandler.onDecoded(context, packet);
      }
    }
  }
}
