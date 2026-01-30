# Server Pre-Check

A Fabric mod for Minecraft 1.21.x that allows server administrators to control which mods clients can use when connecting.

## Overview

**Required on both client and server.** The client sends its mod list to the server, which validates it against configured rules before allowing connection.

## Installation

1. Download the mod JAR file
2. Place it in your `mods` folder (both client and server)
3. Start Minecraft/Server
4. Configure the server settings (see below)

## Quick Start

1. **Generate your whitelist**: Have a client with acceptable mods run `/serverprecheck build whitelist` to copy the mod list to clipboard
2. **Configure**: Edit `config/server_precheck-config.json` on your server
3. **Choose mode**: Set `USE_WHITELIST_ONLY` to `true` (whitelist) or `false` (blacklist)
4. **Optional**: Add player UUIDs to `EXEMPT_PLAYERS` to bypass checks

## Configuration

### Validation Modes

**Blacklist Mode** (default, `USE_WHITELIST_ONLY = false`)
- Allows all mods except those in `CLIENT_MOD_BLACKLIST`
- Default blacklist: `aristois`, `bleachhack`, `meteor-client`, `wurst`

**Whitelist Mode** (`USE_WHITELIST_ONLY = true`)
- Only allows mods in `CLIENT_MOD_WHITELIST`
- Stricter control for curated modpacks

### Key Configuration Options

| Option | Type | Description |
|--------|------|-------------|
| `USE_WHITELIST_ONLY` | Boolean | `false` = blacklist mode, `true` = whitelist mode |
| `CLIENT_MOD_NECESSARY` | Array | Mods required to join (default: `["server_precheck"]`) |
| `CLIENT_MOD_WHITELIST` | Array | Allowed mods in whitelist mode |
| `CLIENT_MOD_BLACKLIST` | Array | Banned mods in blacklist mode |
| `EXEMPT_PLAYERS` | Array | Player UUIDs that bypass all checks |
| `MODPACK_NAME` | String | Shown in disconnect messages |
| `CUSTOM_DISCONNECT_MESSAGE` | String | Overrides default disconnect message |

**Message customization options** (`MSG_HEADER`, `MSG_UNAUTHORIZED_HEADER`, `MSG_MISSING_HEADER`, etc.) support i18n translation keys.

## Client Commands

| Command | Description |
|---------|-------------|
| `/serverprecheck list` | Lists all installed mods |
| `/serverprecheck count` | Shows the number of installed mods |
| `/serverprecheck build whitelist` | Copies mod list to clipboard for config |

## Utilities

**Finding a Mod ID**: Open the mod's JAR file → `fabric.mod.json` → look for the `"id"` field

**Finding a Player UUID**: Use [NameMC](https://namemc.com/) or check server logs

**Getting Mod List from Logs**: Check `.minecraft/logs/latest.log` for:
```
Server Pre-Check vx.x.x from the client! Modlist: ["fabric-api", ...]
```

## Example Configuration

```json
{
    "MODPACK_NAME": "My Server Modpack v1.0",
    "USE_WHITELIST_ONLY": false,
    "CLIENT_MOD_NECESSARY": ["server_precheck"],
    "CLIENT_MOD_BLACKLIST": ["aristois", "bleachhack", "meteor-client", "wurst"],
    "EXEMPT_PLAYERS": ["069a79f4-44e9-4726-a5be-fca90e38cmf5"]
}
```
