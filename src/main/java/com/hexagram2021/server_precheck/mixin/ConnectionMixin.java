package com.hexagram2021.server_precheck.mixin;

import com.hexagram2021.server_precheck.common.network.IConnectionWithValidationResult;
import javax.annotation.Nullable;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin to add validation result storage to Connection. This allows us to store the mod validation
 * result during handshake and retrieve it later after authentication to decide whether to kick the
 * player.
 */
@Mixin(Connection.class)
public class ConnectionMixin implements IConnectionWithValidationResult {

  @Unique @Nullable private MutableComponent serverPreCheck$validationFailureMessage;

  @Override
  @Nullable
  public MutableComponent serverPreCheck$getValidationFailureMessage() {
    return this.serverPreCheck$validationFailureMessage;
  }

  @Override
  public void serverPreCheck$setValidationFailureMessage(@Nullable MutableComponent message) {
    this.serverPreCheck$validationFailureMessage = message;
  }
}
