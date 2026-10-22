package dev.atlasclient.inject.mixin.api;

import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import dev.atlasclient.inject.accessor.PayloadTypeRegistryImplAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(PayloadTypeRegistryImpl.class)
public abstract class PayloadTypeRegistryImplMixin<B extends PacketByteBuf> implements PayloadTypeRegistryImplAccessor {
  @Shadow(remap = false) @Final private Map<Identifier, CustomPayload.Type<B, ? extends CustomPayload>> packetTypes;

  @Inject(method = "register", at = @At(value = "RETURN"))
  public <T extends CustomPayload> void register(CustomPayload.Id<T> id,
    PacketCodec<? super B, T> codec,
    CallbackInfoReturnable<CustomPayload.Type<? super B, T>> cir) {
  }

  @Override public ArrayList<String> atlasClient$getRegisteredChannelsByName() {
    return packetTypes.keySet().stream().map(Identifier::toString).collect(Collectors.toCollection(ArrayList::new));
  }
}
