package dev.atlasclient.inject.mixin.network.connection;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import net.minecraft.network.*;
import net.minecraft.network.handler.*;
import dev.atlasclient.Constants;
import dev.atlasclient.netty.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientConnection.class, priority = 1001) public class ClientConnection_1Mixin {
  /**
   * Responsible for injecting our handlers into the pipeline to decode packets before via version does.
   */
  @Inject(method = "addHandlers", at = @At("RETURN")) private static void onAddHandlers(ChannelPipeline pipeline,
    NetworkSide side,
    boolean local,
    PacketSizeLogger packetSizeLogger,
    CallbackInfo ci) {
    if (pipeline.channel() instanceof SocketChannel) {
      pipeline.addBefore(HandlerNames.ENCODER,
        NettyConstants.ATLAS_C2S_HANDSHAKE_HANDLER,
        new AtlasC2SHandshakeHandler());
      pipeline.addBefore(HandlerNames.ENCODER, NettyConstants.ATLAS_ENCODER, new AtlasEncoder());
      pipeline.addBefore(HandlerNames.INBOUND_CONFIG,
        NettyConstants.ATLAS_S2C_PLUGIN_MESSAGE_HANDLER,
        new AtlasS2CPluginMessageHandler());
    } else {
      Constants.LOGGER.warn("Channel not an instance of netty socket");
    }
  }
}
