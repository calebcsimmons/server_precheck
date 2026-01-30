package com.hexagram2021.server_precheck.client;

import com.google.common.collect.Lists;
import com.hexagram2021.server_precheck.ServerPreCheck;
import com.hexagram2021.server_precheck.client.command.ServerPreCheckClientCommand;
import com.hexagram2021.server_precheck.common.ModListHolder;
import com.hexagram2021.server_precheck.common.utils.SPCLogger;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public class ServerPreCheckClient implements ClientModInitializer {
  public static final List<String> MODS = Lists.newArrayList();

  @Override
  public void onInitializeClient() {
    MODS.clear();
    FabricLoader.getInstance().getAllMods().forEach(mod -> MODS.add(mod.getMetadata().getId()));
    MODS.sort(String::compareTo);

    // Set the mod list in the common holder for the mixin to access
    ModListHolder.setClientMods(MODS);

    // Register client commands
    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) -> {
          ServerPreCheckClientCommand.register(dispatcher);
        });

    hello();
  }

  public static void hello() {
    StringBuilder modlist = new StringBuilder();
    MODS.forEach(mod -> modlist.append('"').append(mod).append("\", "));
    SPCLogger.LOGGER.info(
        "%s v%s from the client! Modlist: [%s]"
            .formatted(ServerPreCheck.MOD_NAME, ServerPreCheck.MOD_VERSION, modlist.toString()));
  }
}
