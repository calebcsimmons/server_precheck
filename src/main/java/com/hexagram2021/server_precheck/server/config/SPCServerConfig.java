package com.hexagram2021.server_precheck.server.config;

import static com.hexagram2021.server_precheck.ServerPreCheck.MODID;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hexagram2021.server_precheck.ServerPreCheck;
import com.hexagram2021.server_precheck.common.utils.SPCLogger;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.tuple.Pair;

public class SPCServerConfig {
  public interface IConfigValue<T extends Serializable> {
    List<IConfigValue<?>> CONFIG_VALUES = Lists.newArrayList();

    String name();

    T value();

    void parseAsValue(JsonElement element);

    void checkValueRange() throws ConfigValueException;
  }

  public abstract static class ListConfigValue<T extends Serializable>
      implements IConfigValue<ArrayList<T>> {
    private final String name;
    private final ArrayList<T> value;

    @SafeVarargs
    public ListConfigValue(String name, T... defaultValues) {
      this(
          name, Arrays.stream(defaultValues).collect(Collectors.toCollection(Lists::newArrayList)));

      CONFIG_VALUES.add(this);
    }

    public ListConfigValue(String name, ArrayList<T> value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public void checkValueRange() throws ConfigValueException {
      this.value.forEach(
          v -> {
            if (!this.isValid(v)) {
              throw new ConfigValueException(this.createExceptionDescription(v));
            }
          });
    }

    @Override
    public void parseAsValue(JsonElement element) {
      this.value.clear();
      element.getAsJsonArray().asList().forEach(e -> this.value.add(this.parseAsElementValue(e)));
    }

    @Override
    public String name() {
      return this.name;
    }

    @Override
    public ArrayList<T> value() {
      return this.value;
    }

    protected abstract boolean isValid(T element);

    protected abstract String createExceptionDescription(T element);

    protected abstract T parseAsElementValue(JsonElement element);
  }

  public static class ModIdListConfigValue extends ListConfigValue<String> {

    public ModIdListConfigValue(String name, String... defaultValues) {
      super(name, defaultValues);
    }

    @SuppressWarnings("unused")
    public ModIdListConfigValue(String name, ArrayList<String> value) {
      super(name, value);
    }

    @Override
    protected boolean isValid(String element) {
      return Pattern.matches("[a-z\\d\\-._]+", element);
    }

    @Override
    protected String createExceptionDescription(String element) {
      return "\"%s\" is not a valid modid!".formatted(element);
    }

    @Override
    protected String parseAsElementValue(JsonElement element) {
      return element.getAsString();
    }
  }

  /**
   * Config value for a list of player UUIDs. UUIDs can be in standard format (with dashes) or
   * compact format (without dashes).
   */
  public static class UuidListConfigValue extends ListConfigValue<String> {
    // UUID pattern: 8-4-4-4-12 hex digits with dashes, or 32 hex digits without dashes
    private static final Pattern UUID_PATTERN =
        Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$|^[0-9a-fA-F]{32}$");

    public UuidListConfigValue(String name, String... defaultValues) {
      super(name, defaultValues);
    }

    @Override
    protected boolean isValid(String element) {
      return UUID_PATTERN.matcher(element).matches();
    }

    @Override
    protected String createExceptionDescription(String element) {
      return "\"%s\" is not a valid UUID! Expected format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
          .formatted(element);
    }

    @Override
    protected String parseAsElementValue(JsonElement element) {
      return element.getAsString();
    }

    /**
     * Checks if the given UUID is in the exempt list. Handles both dashed and non-dashed UUID
     * formats.
     */
    public boolean containsUuid(UUID uuid) {
      String uuidStr = uuid.toString().toLowerCase();
      String uuidNoDashes = uuidStr.replace("-", "");

      for (String exemptUuid : this.value()) {
        String normalizedExempt = exemptUuid.toLowerCase().replace("-", "");
        if (normalizedExempt.equals(uuidNoDashes)) {
          return true;
        }
      }
      return false;
    }
  }

  public static class BoolConfigValue implements IConfigValue<Boolean> {
    private final String name;
    private boolean value;

    public BoolConfigValue(String name, boolean value) {
      this.name = name;
      this.value = value;

      CONFIG_VALUES.add(this);
    }

    @Override
    public void checkValueRange() throws ConfigValueException {}

    @Override
    public void parseAsValue(JsonElement element) {
      this.value = element.getAsBoolean();
    }

    @Override
    public String name() {
      return this.name;
    }

    @Override
    public Boolean value() {
      return this.value;
    }
  }

  public static class StringConfigValue implements IConfigValue<String> {
    private final String name;
    private String value;

    public StringConfigValue(String name, String defaultValue) {
      this.name = name;
      this.value = defaultValue;

      CONFIG_VALUES.add(this);
    }

    @Override
    public void checkValueRange() throws ConfigValueException {}

    @Override
    public void parseAsValue(JsonElement element) {
      this.value = element.getAsString();
    }

    @Override
    public String name() {
      return this.name;
    }

    @Override
    public String value() {
      return this.value;
    }
  }

  private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
  private static final File CONFIG_FILE = CONFIG_DIR.resolve(MODID + "-config.json").toFile();
  private static final File README_FILE = CONFIG_DIR.resolve(MODID + "-config-readme.md").toFile();

  // Server Settings
  public static final StringConfigValue MODPACK_NAME = new StringConfigValue("MODPACK_NAME", "");
  public static final StringConfigValue CUSTOM_DISCONNECT_MESSAGE =
      new StringConfigValue("CUSTOM_DISCONNECT_MESSAGE", "");

  // Disconnect Message Templates (supports i18n translation keys or literal text)
  public static final StringConfigValue MSG_HEADER =
      new StringConfigValue("MSG_HEADER", "Connection rejected: Mod list mismatch\n\n");
  public static final StringConfigValue MSG_UNAUTHORIZED_HEADER =
      new StringConfigValue("MSG_UNAUTHORIZED_HEADER", "Unauthorized mods detected:\n");
  public static final StringConfigValue MSG_MISSING_HEADER =
      new StringConfigValue("MSG_MISSING_HEADER", "Missing required mods:\n");
  public static final StringConfigValue MSG_MOD_ENTRY =
      new StringConfigValue("MSG_MOD_ENTRY", "  - %s\n");
  public static final StringConfigValue MSG_MODPACK_INSTRUCTION =
      new StringConfigValue(
          "MSG_MODPACK_INSTRUCTION", "Please install and run only the following modpack:\n");
  public static final StringConfigValue MSG_CONTACT_ADMIN =
      new StringConfigValue(
          "MSG_CONTACT_ADMIN", "Please contact the server administrator for the correct mod list.");
  public static final StringConfigValue MSG_NOT_INSTALLED =
      new StringConfigValue(
          "MSG_NOT_INSTALLED",
          "Connection rejected: Server Pre-Check not installed\n\n"
              + "This server requires the Server Pre-Check mod to be installed on your client.\n"
              + "Please install the mod and try again.");

  // WhiteLists
  public static final BoolConfigValue USE_WHITELIST_ONLY =
      new BoolConfigValue("USE_WHITELIST_ONLY", false);
  public static final ModIdListConfigValue CLIENT_MOD_NECESSARY =
      new ModIdListConfigValue("CLIENT_MOD_NECESSARY", MODID);
  public static final ModIdListConfigValue CLIENT_MOD_WHITELIST =
      new ModIdListConfigValue(
          "CLIENT_MOD_WHITELIST",
          "fabric-api",
          "fabric-api-base",
          "fabric-api-lookup-api-v1",
          "fabric-biome-api-v1",
          "fabric-block-api-v1",
          "fabric-block-view-api-v2",
          "fabric-blockrenderlayer-v1",
          "fabric-client-tags-api-v1",
          "fabric-command-api-v1",
          "fabric-command-api-v2",
          "fabric-commands-v0",
          "fabric-containers-v0",
          "fabric-content-registries-v0",
          "fabric-convention-tags-v1",
          "fabric-crash-report-info-v1",
          "fabric-data-attachment-api-v1",
          "fabric-data-generation-api-v1",
          "fabric-dimensions-v1",
          "fabric-entity-events-v1",
          "fabric-events-interaction-v0",
          "fabric-events-lifecycle-v0",
          "fabric-game-rule-api-v1",
          "fabric-item-api-v1",
          "fabric-item-group-api-v1",
          "fabric-key-binding-api-v1",
          "fabric-keybindings-v0",
          "fabric-lifecycle-events-v1",
          "fabric-loot-api-v2",
          "fabric-message-api-v1",
          "fabric-mining-level-api-v1",
          "fabric-model-loading-api-v1",
          "fabric-models-v0",
          "fabric-networking-api-v1",
          "fabric-object-builder-api-v1",
          "fabric-particles-v1",
          "fabric-recipe-api-v1",
          "fabric-registry-sync-v0",
          "fabric-renderer-api-v1",
          "fabric-renderer-indigo",
          "fabric-renderer-registries-v1",
          "fabric-rendering-data-attachment-v1",
          "fabric-rendering-fluids-v1",
          "fabric-rendering-v0",
          "fabric-rendering-v1",
          "fabric-resource-conditions-api-v1",
          "fabric-resource-loader-v0",
          "fabric-screen-api-v1",
          "fabric-screen-handler-api-v1",
          "fabric-sound-api-v1",
          "fabric-transfer-api-v1",
          "fabric-transitive-access-wideners-v1",
          "fabricloader",
          "java",
          "minecraft",
          "mixinextras",
          MODID);
  public static final ModIdListConfigValue CLIENT_MOD_BLACKLIST =
      new ModIdListConfigValue(
          "CLIENT_MOD_BLACKLIST", "aristois", "bleachhack", "meteor-client", "wurst");

  // Player Exemptions - UUIDs of players who bypass mod validation
  public static final UuidListConfigValue EXEMPT_PLAYERS =
      new UuidListConfigValue("EXEMPT_PLAYERS");

  /**
   * Checks if a player UUID is exempt from mod validation.
   *
   * @param playerUuid the player's UUID
   * @return true if the player is exempt, false otherwise
   */
  public static boolean isPlayerExempt(UUID playerUuid) {
    return EXEMPT_PLAYERS.containsUuid(playerUuid);
  }

  public static List<Pair<String, MismatchType>> test(List<String> mods) {
    List<Pair<String, MismatchType>> ret = Lists.newArrayList();
    for (String mod : CLIENT_MOD_NECESSARY.value()) {
      if (!mods.contains(mod)) {
        ret.add(Pair.of(mod, MismatchType.UNINSTALLED_BUT_SHOULD_INSTALL));
      }
    }
    if (USE_WHITELIST_ONLY.value()) {
      for (String mod : mods) {
        if (!CLIENT_MOD_WHITELIST.value().contains(mod)) {
          ret.add(Pair.of(mod, MismatchType.INSTALLED_BUT_SHOULD_NOT_INSTALL));
        }
      }
    } else {
      for (String mod : mods) {
        if (CLIENT_MOD_BLACKLIST.value().contains(mod)) {
          ret.add(Pair.of(mod, MismatchType.INSTALLED_BUT_SHOULD_NOT_INSTALL));
        }
      }
    }
    return ret;
  }

  static {
    lazyInit();
  }

  private static void lazyInit() {
    try {
      // Ensure config directory exists (FabricLoader.getConfigDir() should already exist, but check
      // anyway)
      if (!Files.exists(CONFIG_DIR)) {
        Files.createDirectories(CONFIG_DIR);
      }

      if (CONFIG_FILE.exists()) {
        try (Reader reader = new FileReader(CONFIG_FILE)) {
          JsonElement json = JsonParser.parseReader(reader);
          loadFromJson(json.getAsJsonObject());
        }
        checkValues();
        saveConfig();
      } else {
        if (CONFIG_FILE.createNewFile()) {
          saveConfig();
        } else {
          SPCLogger.LOGGER.error("Could not create new file " + CONFIG_FILE);
        }
      }
      if (!README_FILE.exists()) {
        if (README_FILE.createNewFile()) {
          fillReadmeFile();
        } else {
          SPCLogger.LOGGER.error("Could not create new file " + README_FILE);
        }
      }
    } catch (IOException e) {
      SPCLogger.LOGGER.error("Error during loading config.", e);
    }
  }

  private static void fillReadmeFile() throws IOException {
    try (Writer writer = new FileWriter(README_FILE)) {
      writer.write("# Abstract\n\n");
      writer.write(
          "Thank you for choosing Server Pre-Check to protect your server from client hacking "
              + "mods. Let me introduce how it works and what you can do.\n\n");
      writer.write("This mod works on client and server separately:\n\n");
      writer.write(
          "- On the client side, it gathers all identifier of mods (\"mod_id\"s) and sends them "
              + "to the server.\n");
      writer.write(
          "- On the server side, it checks players who try to connect the server if they install "
              + "hacking mods, or if they do not install any necessary mods to avoid problems.\n\n");
      writer.write("But both sides are required. If not:\n\n");
      writer.write(
          "- Installed on the client side but not installed on the server side. The client player "
              + "can still enter the server and play, but this mod can not protect your server "
              + "from hacking.\n");
      writer.write(
          "- Installed on the server side but not installed on the client side. The client player "
              + "is not allowed to enter the server.\n\n");

      writer.write("# Server Settings\n\n");
      writer.write("## MODPACK_NAME\n\n");
      writer.write(
          "Set this to your modpack's name. When a player is rejected due to mod mismatch, "
              + "they will see a message like:\n");
      writer.write(
          "\"Please install and run only the following modpack: [your modpack name]\"\n\n");
      writer.write("Example: `\"MODPACK_NAME\": \"My Awesome Modpack v1.0\"`\n\n");
      writer.write("## CUSTOM_DISCONNECT_MESSAGE\n\n");
      writer.write(
          "If you want complete control over the disconnect message, set this to your custom "
              + "message.\n");
      writer.write(
          "When set, this message will be shown instead of the default detailed error "
              + "message.\n\n");
      writer.write(
          "Example: `\"CUSTOM_DISCONNECT_MESSAGE\": \"Please download our modpack from "
              + "example.com/modpack\"`\n\n");

      writer.write("# Disconnect Message Templates\n\n");
      writer.write(
          "You can customize individual parts of the disconnect message using these options:\n\n");
      writer.write("- `MSG_HEADER`: The header shown at the top of the disconnect message.\n");
      writer.write("- `MSG_UNAUTHORIZED_HEADER`: Header for the list of unauthorized mods.\n");
      writer.write("- `MSG_MISSING_HEADER`: Header for the list of missing required mods.\n");
      writer.write(
          "- `MSG_MOD_ENTRY`: Format for each mod entry. Use `%s` as placeholder for the "
              + "mod ID.\n");
      writer.write("- `MSG_MODPACK_INSTRUCTION`: Shown when MODPACK_NAME is set.\n");
      writer.write("- `MSG_CONTACT_ADMIN`: Shown when MODPACK_NAME is not set.\n");
      writer.write(
          "- `MSG_NOT_INSTALLED`: Shown when the client doesn't have Server Pre-Check "
              + "installed.\n\n");
      writer.write(
          "These can be set to translation keys for i18n support (e.g., "
              + "`server_precheck.disconnect.header`).\n\n");

      writer.write("# Player Exemptions\n\n");
      writer.write(
          "Use `EXEMPT_PLAYERS` to specify player UUIDs that bypass all mod validation "
              + "checks.\n");
      writer.write(
          "This is useful for server admins or testers who need to join with different "
              + "mods.\n\n");
      writer.write("Example: `\"EXEMPT_PLAYERS\": [\"069a79f4-44e9-4726-a5be-fca90e38aaf5\"]`\n\n");
      writer.write(
          "UUIDs can be specified with or without dashes. To find a player's UUID, check sites "
              + "like NameMC or your server logs.\n\n");

      writer.write("# Adding a mod to whitelist and blacklist\n\n");
      writer.write(
          "The config file is in \"<server directory>/config/server_precheck-config.json\". "
              + "If you want to add mods to the whitelist or blacklist, please read the "
              + "following guides.\n\n");
      writer.write(
          "First, you should find the identifier of the mod (modid), a simple way is open the "
              + "jar file with an archiver software (eg. WinZip, HaoZip, 7-Zip), open "
              + "\"fabric.mod.json\" and see what the value of key \"id\" is. For example, the "
              + "modid of Server Pre-Check mod is \"server_precheck\".\n\n");
      writer.write(
          "Then, add it to `CLIENT_MOD_NECESSARY` field if you want client players install it. "
              + "By default, it is blacklist mode, so you can add it to `CLIENT_MOD_BLACKLIST` "
              + "field if you do not want client players install it. If you want to use whitelist "
              + "mode instead, set `USE_WHITELIST_ONLY` to true and add all whitelist modids to "
              + "`CLIENT_MOD_WHITELIST` field.\n\n");
      writer.write(
          "In addition, if `USE_WHITELIST_ONLY` is true, `CLIENT_MOD_BLACKLIST` field is just "
              + "ignored while running the server. And if `USE_WHITELIST_ONLY` is false, "
              + "`CLIENT_MOD_WHITELIST` field is ignored instead.\n\n");
      writer.write(
          "As you might see, if fabric-api is installed, the modlist will contains quite a lot "
              + "of modids. You can run a client with this mod installed, and open "
              + "\".minecraft/logs/latest.log\", and you will see the following format line to "
              + "simplify gathering the modlist manually:\n\n");
      writer.write(
          "```\nServer Pre-Check vx.x.x from the client! Modlist: [\"fabric-api\", "
              + "\"fabric-api-base\", ...]\n```\n\n");
      writer.write(
          "Alternatively, use the `/serverprecheck build whitelist` command in-game to copy "
              + "your mod list to clipboard.\n\n");

      writer.write("# Issue tracker\n\n");
      writer.write(
          "Visit the project's issue tracker and post your issue and logs if you find any "
              + "problems with this mod.\n");
    }
  }

  private static void loadFromJson(JsonObject jsonObject) {
    SPCLogger.LOGGER.debug("Loading json config file.");
    IConfigValue.CONFIG_VALUES.forEach(
        iConfigValue -> {
          if (jsonObject.has(iConfigValue.name())) {
            iConfigValue.parseAsValue(jsonObject.get(iConfigValue.name()));
          }
        });
  }

  private static void saveConfig() throws IOException {
    SPCLogger.LOGGER.debug("Saving json config file.");
    try (Writer writer = new FileWriter(CONFIG_FILE)) {
      JsonObject configJson = new JsonObject();
      IConfigValue.CONFIG_VALUES.forEach(
          iConfigValue -> {
            Serializable value = iConfigValue.value();
            if (value instanceof Number number) {
              configJson.addProperty(iConfigValue.name(), number);
            } else if (value instanceof Boolean bool) {
              configJson.addProperty(iConfigValue.name(), bool);
            } else if (value instanceof String str) {
              configJson.addProperty(iConfigValue.name(), str);
            } else if (value instanceof List<?> list) {
              configJson.add(iConfigValue.name(), buildList(list));
            } else {
              SPCLogger.LOGGER.error("Unknown Config Value Type: " + value.getClass().getName());
            }
          });
      IConfigHelper.writeJsonToFile(writer, null, configJson, 0);
    }
  }

  private static JsonArray buildList(List<?> list) {
    JsonArray ret = new JsonArray();
    list.forEach(
        value -> {
          if (value instanceof Number number) {
            ret.add(number);
          } else if (value instanceof Boolean bool) {
            ret.add(bool);
          } else if (value instanceof String str) {
            ret.add(str);
          } else if (value instanceof List<?> list1) {
            ret.add(buildList(list1));
          } else {
            SPCLogger.LOGGER.error("Unknown Element Type from List: " + value.getClass().getName());
          }
        });
    return ret;
  }

  public static void checkValues() {
    IConfigValue.CONFIG_VALUES.forEach(IConfigValue::checkValueRange);
  }

  public static class ConfigValueException extends RuntimeException {
    public ConfigValueException(String message) {
      super(message);
    }
  }

  public static void hello() {
    SPCLogger.LOGGER.info(
        "%s v%s is protecting your server!"
            .formatted(ServerPreCheck.MOD_NAME, ServerPreCheck.MOD_VERSION));
  }
}
