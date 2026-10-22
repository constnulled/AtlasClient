package dev.atlasclient.inject.accessor;

import io.netty.channel.Channel;

public interface ClientConnectionAccessor {
  Channel atlasClient$getChannel();
}
