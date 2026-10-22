package dev.atlasclient.inject.mixin.network.connection;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import dev.atlasclient.inject.accessor.ClientConnectionAccessor;
import org.spongepowered.asm.mixin.*;

@Mixin(ClientConnection.class) public class ClientConnection_2Mixin implements ClientConnectionAccessor {
  @Shadow private Channel channel;

  @Override public Channel atlasClient$getChannel() {
    return channel;
  }
}
