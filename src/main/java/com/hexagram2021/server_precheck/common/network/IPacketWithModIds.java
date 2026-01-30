package com.hexagram2021.server_precheck.common.network;

import java.util.List;
import javax.annotation.Nullable;

public interface IPacketWithModIds {
  @Nullable
  List<String> getModIds();

  @SuppressWarnings("unused")
  void setModIds(@Nullable List<String> modIds);
}
