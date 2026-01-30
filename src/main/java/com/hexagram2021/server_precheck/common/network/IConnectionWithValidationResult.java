package com.hexagram2021.server_precheck.common.network;

import javax.annotation.Nullable;
import net.minecraft.network.chat.MutableComponent;

/**
 * Interface for storing mod validation results on a Connection. This allows the validation to
 * happen during handshake, but the kick to be deferred until after authentication (when we know the
 * player's UUID).
 */
public interface IConnectionWithValidationResult {

  /**
   * Gets the stored validation failure message, if any.
   *
   * @return the disconnect message if validation failed, or null if validation passed
   */
  @Nullable
  MutableComponent getValidationFailureMessage();

  /**
   * Sets the validation failure message.
   *
   * @param message the disconnect message to store, or null to clear
   */
  void setValidationFailureMessage(@Nullable MutableComponent message);

  /**
   * Checks if there is a pending validation failure.
   *
   * @return true if validation failed and the player should be kicked (unless exempt)
   */
  default boolean hasValidationFailure() {
    return getValidationFailureMessage() != null;
  }

  /** Clears any stored validation failure (used when player is exempt). */
  default void clearValidationFailure() {
    setValidationFailureMessage(null);
  }
}
