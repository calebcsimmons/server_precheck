package com.hexagram2021.server_precheck;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ServerPreCheck implements ModInitializer {
	public static final String MODID = "server_precheck";
	public static final String MOD_NAME = "Server Pre-Check";
	public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();

	@Override
	public void onInitialize() {
	}
}
