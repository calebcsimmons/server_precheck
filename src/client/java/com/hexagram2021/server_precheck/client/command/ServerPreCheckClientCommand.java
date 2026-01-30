package com.hexagram2021.server_precheck.client.command;

import com.hexagram2021.server_precheck.client.ServerPreCheckClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ServerPreCheckClientCommand {

  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        ClientCommandManager.literal("serverprecheck")
            .then(
                ClientCommandManager.literal("list")
                    .executes(ServerPreCheckClientCommand::executeList))
            .then(
                ClientCommandManager.literal("build")
                    .then(
                        ClientCommandManager.literal("whitelist")
                            .executes(ServerPreCheckClientCommand::executeBuildWhitelist)))
            .then(
                ClientCommandManager.literal("count")
                    .executes(ServerPreCheckClientCommand::executeCount)));
  }

  private static int executeList(CommandContext<FabricClientCommandSource> context) {
    List<String> mods = ServerPreCheckClient.MODS;

    context
        .getSource()
        .sendFeedback(
            Component.literal("=== Installed Mods (" + mods.size() + ") ===")
                .withStyle(ChatFormatting.GOLD));

    for (String mod : mods) {
      MutableComponent modComponent =
          Component.literal("  • " + mod).withStyle(ChatFormatting.GRAY);
      context.getSource().sendFeedback(modComponent);
    }

    return mods.size();
  }

  private static int executeBuildWhitelist(CommandContext<FabricClientCommandSource> context) {
    List<String> mods = ServerPreCheckClient.MODS;

    // Build compact format for clipboard (JSON array content)
    StringBuilder compactBuilder = new StringBuilder();
    for (int i = 0; i < mods.size(); i++) {
      compactBuilder.append("\"").append(mods.get(i)).append("\"");
      if (i < mods.size() - 1) {
        compactBuilder.append(", ");
      }
    }
    String compactOutput = compactBuilder.toString();

    // Send header
    context
        .getSource()
        .sendFeedback(
            Component.literal("=== Server Pre-Check Config (" + mods.size() + " mods) ===")
                .withStyle(ChatFormatting.GOLD));

    // Copy to clipboard
    Minecraft.getInstance().keyboardHandler.setClipboard(compactOutput);

    context
        .getSource()
        .sendFeedback(
            Component.literal("✓ Copied " + mods.size() + " mod IDs to clipboard!")
                .withStyle(ChatFormatting.GREEN));

    context.getSource().sendFeedback(Component.literal(""));
    context
        .getSource()
        .sendFeedback(
            Component.literal("Paste into your config file's CLIENT_MOD_WHITELIST array:")
                .withStyle(ChatFormatting.YELLOW));

    // Show preview (truncated if too long)
    String preview;
    if (mods.size() > 5) {
      preview =
          "\""
              + mods.get(0)
              + "\", \""
              + mods.get(1)
              + "\", \""
              + mods.get(2)
              + "\", ... ("
              + (mods.size() - 3)
              + " more)";
    } else {
      preview = compactOutput;
    }
    context.getSource().sendFeedback(Component.literal(preview).withStyle(ChatFormatting.GRAY));

    return mods.size();
  }

  private static int executeCount(CommandContext<FabricClientCommandSource> context) {
    int count = ServerPreCheckClient.MODS.size();
    context
        .getSource()
        .sendFeedback(
            Component.literal("You have " + count + " mods installed.")
                .withStyle(ChatFormatting.AQUA));
    return count;
  }
}
