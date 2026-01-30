package com.hexagram2021.server_precheck.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the client's mod list. This is set by the client initializer and read by the mixin when
 * creating handshake packets.
 *
 * <p>This class is thread-safe: the mod list is stored as a volatile reference to an immutable
 * list, ensuring safe publication across threads.
 */
public class ModListHolder {
  private static volatile List<String> clientMods = Collections.emptyList();

  /**
   * Sets the client mod list. The provided list is copied and made immutable to ensure thread
   * safety.
   *
   * @param mods the list of mod IDs installed on the client
   */
  public static void setClientMods(List<String> mods) {
    clientMods = Collections.unmodifiableList(new ArrayList<>(mods));
  }

  /**
   * Gets the client mod list.
   *
   * @return an immutable list of mod IDs installed on the client
   */
  public static List<String> getClientMods() {
    return clientMods;
  }
}
