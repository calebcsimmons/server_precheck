package com.hexagram2021.mod_whitelist.server.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for mod ID validation regex pattern. Mod IDs must match the pattern: [a-z\d\-._]+ */
class ModIdValidationTest {

  // The same pattern used in ModIdListConfigValue
  private static final Pattern MOD_ID_PATTERN = Pattern.compile("[a-z\\d\\-._]+");

  private boolean isValidModId(String modId) {
    return MOD_ID_PATTERN.matcher(modId).matches();
  }

  @ParameterizedTest
  @DisplayName("Should accept valid mod IDs")
  @ValueSource(
      strings = {
        "fabric-api",
        "mod_whitelist",
        "minecraft",
        "fabricloader",
        "java",
        "mixinextras",
        "fabric-api-base",
        "fabric-api-lookup-api-v1",
        "some-mod-123",
        "mod.with.dots",
        "mod_with_underscores",
        "mod-with-dashes",
        "123numeric",
        "a",
        "mod123"
      })
  void testValidModIds(String modId) {
    assertTrue(isValidModId(modId), "Expected '" + modId + "' to be a valid mod ID");
  }

  @ParameterizedTest
  @DisplayName("Should reject invalid mod IDs")
  @ValueSource(
      strings = {
        "Mod Name", // Uppercase
        "mod name", // Space
        "MOD_WHITELIST", // All uppercase
        "Fabric-API", // Mixed case
        "mod@special", // Special character @
        "mod#hash", // Special character #
        "mod!exclaim", // Special character !
        "mod$dollar", // Special character $
        "mod%percent", // Special character %
        "mod&ampersand", // Special character &
        "mod*asterisk", // Special character *
        "mod+plus", // Special character +
        "mod=equals", // Special character =
        "mod/slash", // Special character /
        "mod\\backslash", // Special character \
        "mod:colon", // Special character :
        "mod;semicolon", // Special character ;
        "mod<less", // Special character <
        "mod>greater", // Special character >
        "mod?question", // Special character ?
        "mod[bracket", // Special character [
        "mod]bracket", // Special character ]
        "mod{brace", // Special character {
        "mod}brace", // Special character }
        "mod|pipe", // Special character |
        "mod~tilde" // Special character ~
      })
  void testInvalidModIds(String modId) {
    assertFalse(isValidModId(modId), "Expected '" + modId + "' to be an invalid mod ID");
  }

  @Test
  @DisplayName("Should reject empty mod ID")
  void testEmptyModId() {
    assertFalse(isValidModId(""), "Empty string should not be a valid mod ID");
  }

  @Test
  @DisplayName("Should reject mod ID with only whitespace")
  void testWhitespaceModId() {
    assertFalse(isValidModId("   "), "Whitespace-only string should not be a valid mod ID");
    assertFalse(isValidModId("\t"), "Tab should not be a valid mod ID");
    assertFalse(isValidModId("\n"), "Newline should not be a valid mod ID");
  }

  @Test
  @DisplayName("Should reject mod ID with leading/trailing spaces")
  void testModIdWithSpaces() {
    assertFalse(isValidModId(" fabric-api"), "Mod ID with leading space should be invalid");
    assertFalse(isValidModId("fabric-api "), "Mod ID with trailing space should be invalid");
    assertFalse(isValidModId(" fabric-api "), "Mod ID with surrounding spaces should be invalid");
  }

  @Test
  @DisplayName("Common hacking client mod IDs should be valid format")
  void testHackingClientModIdsAreValidFormat() {
    // These are blacklisted but should still be valid mod ID format
    assertTrue(isValidModId("aristois"));
    assertTrue(isValidModId("bleachhack"));
    assertTrue(isValidModId("meteor-client"));
    assertTrue(isValidModId("wurst"));
  }
}
