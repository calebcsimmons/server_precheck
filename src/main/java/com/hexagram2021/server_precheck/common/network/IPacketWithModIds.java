package com.hexagram2021.server_precheck.common.network;

import javax.annotation.Nullable;
import java.util.List;

public interface IPacketWithModIds {
	@Nullable
	List<String> getModIds();

	@SuppressWarnings("unused")
	void setModIds(@Nullable List<String> modIds);
}
