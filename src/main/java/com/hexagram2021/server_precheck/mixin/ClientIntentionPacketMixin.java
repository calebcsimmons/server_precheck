package com.hexagram2021.server_precheck.mixin;

import com.hexagram2021.server_precheck.common.ModListHolder;
import com.hexagram2021.server_precheck.common.network.IPacketWithModIds;
import com.hexagram2021.server_precheck.common.utils.SPCLogger;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(ClientIntentionPacket.class)
public class ClientIntentionPacketMixin implements IPacketWithModIds {
	/**
	 * Use a synchronized WeakHashMap to associate mod IDs with packet instances.
	 * This is necessary because ClientIntentionPacket is now a record and cannot have instance fields.
	 * The map is synchronized to ensure thread safety when packets are created/read from multiple network threads.
	 */
	@Unique
	private static final Map<ClientIntentionPacket, List<String>> serverPreCheck$modIdsMap =
		Collections.synchronizedMap(new WeakHashMap<>());

	@Override @Nullable
	public List<String> getModIds() {
		return serverPreCheck$modIdsMap.get((ClientIntentionPacket)(Object)this);
	}

	@Override
	public void setModIds(@Nullable List<String> modIds) {
		if (modIds != null) {
			serverPreCheck$modIdsMap.put((ClientIntentionPacket)(Object)this, modIds);
		} else {
			serverPreCheck$modIdsMap.remove((ClientIntentionPacket)(Object)this);
		}
	}

	@Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/protocol/handshake/ClientIntent;)V", at = @At(value = "TAIL"))
	private void getModIdsFromInit(int protocolVersion, String hostName, int port, ClientIntent intention, CallbackInfo ci) {
		if(intention.equals(ClientIntent.LOGIN)) {
			serverPreCheck$modIdsMap.put((ClientIntentionPacket)(Object)this, ModListHolder.getClientMods());
		}
	}

	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
	private void getModIdsFromNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
		ClientIntentionPacket self = (ClientIntentionPacket)(Object)this;
		if(self.intention().equals(ClientIntent.LOGIN)) {
			try {
				List<String> modIds = friendlyByteBuf.readList(FriendlyByteBuf::readUtf);
				serverPreCheck$modIdsMap.put(self, modIds);
			} catch (DecoderException e) {
				SPCLogger.LOGGER.warn("Decoder exception occurs when parsing ClientIntentionPacket: ", e);
			}
		}
	}

	@Inject(method = "write", at = @At(value = "TAIL"))
	private void writeModIdsToNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
		List<String> modIds = serverPreCheck$modIdsMap.get((ClientIntentionPacket)(Object)this);
		if(modIds != null) {
			friendlyByteBuf.writeCollection(modIds, FriendlyByteBuf::writeUtf);
		}
	}
}
