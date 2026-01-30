package com.hexagram2021.server_precheck.mixin;

import com.hexagram2021.server_precheck.common.network.IConnectionWithValidationResult;
import com.hexagram2021.server_precheck.common.network.IPacketWithModIds;
import com.hexagram2021.server_precheck.server.DisconnectMessageBuilder;
import com.hexagram2021.server_precheck.server.config.SPCServerConfig;
import com.hexagram2021.server_precheck.server.config.MismatchType;
import net.minecraft.network.Connection;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mixin to intercept the login process and validate the client's mod list.
 *
 * Instead of immediately kicking players with invalid mods, we now store the
 * validation result on the Connection. The actual kick is deferred until after
 * authentication, allowing us to check if the player is in the EXEMPT_PLAYERS list.
 *
 * @see ServerLoginPacketListenerImplMixin for the post-authentication check
 */
@Mixin(ServerHandshakePacketListenerImpl.class)
public class ServerHandshakePacketListenerImplMixin {

	/**
	 * Redirect the setupInboundProtocol call to check mod list after the login listener is created.
	 * Instead of immediately disconnecting, we store the validation result on the connection
	 * so it can be checked after authentication (when we know the player's UUID).
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Redirect(
		method = "beginLogin",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"
		)
	)
	private void redirectSetupInboundProtocol(Connection connection, ProtocolInfo protocolInfo, net.minecraft.network.PacketListener packetListener, ClientIntentionPacket clientIntentionPacket, boolean transfer) {
		// First, set up the protocol as normal
		connection.setupInboundProtocol(protocolInfo, packetListener);

		// Check the mod list and store the result on the connection
		MutableComponent reason = serverPreCheck$checkModList(clientIntentionPacket);

		if (reason != null) {
			// Store the validation failure on the connection for later checking
			// The actual kick will happen after authentication in ServerLoginPacketListenerImplMixin
			((IConnectionWithValidationResult) connection).serverPreCheck$setValidationFailureMessage(reason);
		}
	}

	/**
	 * Checks the client's mod list against the server configuration.
	 *
	 * @param clientIntentionPacket the handshake packet containing the mod list
	 * @return a disconnect message if validation fails, or null if the client is allowed
	 */
	@Unique
	@Nullable
	private static MutableComponent serverPreCheck$checkModList(ClientIntentionPacket clientIntentionPacket) {
		IPacketWithModIds packetWithModIds = (IPacketWithModIds)(Object)clientIntentionPacket;
		List<String> modIds = packetWithModIds.getModIds();

		if (modIds != null) {
			List<Pair<String, MismatchType>> mismatches = SPCServerConfig.test(modIds);
			if (!mismatches.isEmpty()) {
				return DisconnectMessageBuilder.buildMismatchMessage(mismatches);
			}
		} else {
			// Client doesn't have Server Pre-Check installed
			return DisconnectMessageBuilder.buildNotInstalledMessage();
		}
		return null;
	}
}
