package com.hexagram2021.server_precheck.server;

import com.hexagram2021.server_precheck.server.config.SPCServerConfig;
import com.hexagram2021.server_precheck.server.config.MismatchType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Builds disconnect messages for clients that fail mod list validation.
 * Supports both custom messages and detailed mismatch reports with i18n.
 */
public final class DisconnectMessageBuilder {

    private DisconnectMessageBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Builds a disconnect message for mod list mismatches.
     *
     * @param mismatches the list of mod mismatches (mod ID and mismatch type)
     * @return a formatted disconnect message component
     */
    public static MutableComponent buildMismatchMessage(List<Pair<String, MismatchType>> mismatches) {
        // Check for custom disconnect message first
        String customMessage = SPCServerConfig.CUSTOM_DISCONNECT_MESSAGE.value();
        if (customMessage != null && !customMessage.isEmpty()) {
            return Component.literal(customMessage);
        }

        // Build detailed error message using configurable templates
        MutableComponent reason = Component.translatable(SPCServerConfig.MSG_HEADER.value());

        // Separate unauthorized mods from missing mods
        List<String> unauthorizedMods = mismatches.stream()
            .filter(p -> p.getRight() == MismatchType.INSTALLED_BUT_SHOULD_NOT_INSTALL)
            .map(Pair::getLeft)
            .toList();
        List<String> missingMods = mismatches.stream()
            .filter(p -> p.getRight() == MismatchType.UNINSTALLED_BUT_SHOULD_INSTALL)
            .map(Pair::getLeft)
            .toList();

        if (!unauthorizedMods.isEmpty()) {
            reason.append(Component.translatable(SPCServerConfig.MSG_UNAUTHORIZED_HEADER.value()));
            for (String mod : unauthorizedMods) {
                reason.append(Component.translatable(SPCServerConfig.MSG_MOD_ENTRY.value(), mod));
            }
            reason.append(Component.literal("\n"));
        }

        if (!missingMods.isEmpty()) {
            reason.append(Component.translatable(SPCServerConfig.MSG_MISSING_HEADER.value()));
            for (String mod : missingMods) {
                reason.append(Component.translatable(SPCServerConfig.MSG_MOD_ENTRY.value(), mod));
            }
            reason.append(Component.literal("\n"));
        }

        // Add modpack name if configured
        String modpackName = SPCServerConfig.MODPACK_NAME.value();
        if (modpackName != null && !modpackName.isEmpty()) {
            reason.append(Component.translatable(SPCServerConfig.MSG_MODPACK_INSTRUCTION.value()));
            reason.append(Component.literal("  " + modpackName));
        } else {
            reason.append(Component.translatable(SPCServerConfig.MSG_CONTACT_ADMIN.value()));
        }
        return reason;
    }

    /**
     * Builds a disconnect message for when the client doesn't have Server Pre-Check installed.
     *
     * @return a formatted disconnect message component
     */
    public static MutableComponent buildNotInstalledMessage() {
        // Check for custom disconnect message first
        String customMessage = SPCServerConfig.CUSTOM_DISCONNECT_MESSAGE.value();
        if (customMessage != null && !customMessage.isEmpty()) {
            return Component.literal(customMessage);
        }

        return Component.translatable(SPCServerConfig.MSG_NOT_INSTALLED.value());
    }
}

