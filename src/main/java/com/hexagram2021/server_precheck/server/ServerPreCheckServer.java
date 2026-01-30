package com.hexagram2021.server_precheck.server;

import com.hexagram2021.server_precheck.server.config.SPCServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;

public class ServerPreCheckServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		SPCServerConfig.hello();
	}
}
