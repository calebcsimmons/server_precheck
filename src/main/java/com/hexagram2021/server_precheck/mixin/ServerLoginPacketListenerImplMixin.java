package com.hexagram2021.server_precheck.mixin;

import com.hexagram2021.server_precheck.common.network.IConnectionWithValidationResult;
import com.hexagram2021.server_precheck.common.utils.SPCLogger;
import com.hexagram2021.server_precheck.server.config.SPCServerConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Mixin to check player exemptions after authentication.
 *
 * This mixin intercepts the login process after the player has been authenticated
 * (when we know their UUID) and checks if they should be kicked due to mod validation
 * failure, unless they are in the EXEMPT_PLAYERS list.
 *
 * @see ServerHandshakePacketListenerImplMixin for the initial validation
 */
@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin {

    @Shadow
    @Final
    Connection connection;

    @Shadow
    public abstract void disconnect(net.minecraft.network.chat.Component reason);

    /**
     * Inject at the start of verifyLoginAndFinishConnectionSetup to check if the player
     * should be kicked due to mod validation failure.
     *
     * At this point, the player has been authenticated and we have their GameProfile (UUID).
     * We check if there's a pending validation failure and if the player is exempt.
     */
    @Inject(
        method = "verifyLoginAndFinishConnectionSetup",
        at = @At("HEAD"),
        cancellable = true
    )
    private void serverPreCheck$checkExemptionBeforeLogin(GameProfile gameProfile, CallbackInfo ci) {
        IConnectionWithValidationResult validationResult = (IConnectionWithValidationResult) this.connection;

        if (validationResult.serverPreCheck$hasValidationFailure()) {
            UUID playerUuid = gameProfile.id();
            String playerName = gameProfile.name();

            // Check if player is exempt
            if (SPCServerConfig.isPlayerExempt(playerUuid)) {
                SPCLogger.LOGGER.info("Player {} ({}) is exempt from mod validation - allowing connection",
                    playerName, playerUuid);
                validationResult.serverPreCheck$clearValidationFailure();
                // Continue with normal login
                return;
            }

            // Player is not exempt - kick them with the stored message
            MutableComponent failureMessage = validationResult.serverPreCheck$getValidationFailureMessage();
            SPCLogger.LOGGER.info("Disconnecting player {} ({}) due to mod validation failure",
                playerName, playerUuid);

            this.disconnect(failureMessage);
            ci.cancel();
        }
    }
}

