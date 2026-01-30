package com.hexagram2021.mod_whitelist.server.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for UUID validation in EXEMPT_PLAYERS config. */
class UuidValidationTest {

  // The same pattern used in UuidListConfigValue
  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$|^[0-9a-fA-F]{32}$");

  private boolean isValidUuid(String uuid) {
    return UUID_PATTERN.matcher(uuid).matches();
  }

  @Nested
  @DisplayName("UUID Format Validation")
  class UuidFormatValidation {

    @ParameterizedTest
    @DisplayName("Should accept valid UUIDs with dashes")
    @ValueSource(
        strings = {
          "069a79f4-44e9-4726-a5be-fca90e38aaf5",
          "853c80ef-3c37-49fd-aa49-938b674adae6",
          "00000000-0000-0000-0000-000000000000",
          "ffffffff-ffff-ffff-ffff-ffffffffffff",
          "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF",
          "12345678-1234-1234-1234-123456789abc",
          "abcdef12-3456-7890-abcd-ef1234567890"
        })
    void testValidUuidsWithDashes(String uuid) {
      assertTrue(isValidUuid(uuid), "Expected '" + uuid + "' to be a valid UUID");
    }

    @ParameterizedTest
    @DisplayName("Should accept valid UUIDs without dashes")
    @ValueSource(
        strings = {
          "069a79f444e94726a5befca90e38aaf5",
          "853c80ef3c3749fdaa49938b674adae6",
          "00000000000000000000000000000000",
          "ffffffffffffffffffffffffffffffff",
          "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
          "123456781234123412341234567890ab"
        })
    void testValidUuidsWithoutDashes(String uuid) {
      assertTrue(isValidUuid(uuid), "Expected '" + uuid + "' to be a valid UUID");
    }

    @ParameterizedTest
    @DisplayName("Should reject invalid UUIDs")
    @ValueSource(
        strings = {
          "",
          "not-a-uuid",
          "069a79f4-44e9-4726-a5be-fca90e38aaf", // Too short
          "069a79f4-44e9-4726-a5be-fca90e38aaf55", // Too long
          "069a79f4-44e9-4726-a5be-fca90e38aafg", // Invalid character 'g'
          "069a79f444e94726a5befca90e38aaf", // Too short (no dashes)
          "069a79f444e94726a5befca90e38aaf55", // Too long (no dashes)
          "069a79f4-44e9-4726-a5be", // Incomplete
          "069a79f4-44e9-4726-a5be-fca90e38aaf5-", // Trailing dash
          "-069a79f4-44e9-4726-a5be-fca90e38aaf5", // Leading dash
          "069a79f4--44e9-4726-a5be-fca90e38aaf5", // Double dash
          "069a79f4_44e9_4726_a5be_fca90e38aaf5", // Underscores instead of dashes
          "player_name", // Not a UUID at all
          "12345" // Too short
        })
    void testInvalidUuids(String uuid) {
      assertFalse(isValidUuid(uuid), "Expected '" + uuid + "' to be an invalid UUID");
    }
  }

  @Nested
  @DisplayName("UUID Comparison Logic")
  class UuidComparisonLogic {

    /** Simulates the containsUuid logic from UuidListConfigValue */
    private boolean containsUuid(String[] exemptList, UUID uuid) {
      String uuidStr = uuid.toString().toLowerCase();
      String uuidNoDashes = uuidStr.replace("-", "");

      for (String exemptUuid : exemptList) {
        String normalizedExempt = exemptUuid.toLowerCase().replace("-", "");
        if (normalizedExempt.equals(uuidNoDashes)) {
          return true;
        }
      }
      return false;
    }

    @Test
    @DisplayName("Should match UUID with dashes against list with dashes")
    void testMatchWithDashes() {
      String[] exemptList = {"069a79f4-44e9-4726-a5be-fca90e38aaf5"};
      UUID uuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

      assertTrue(containsUuid(exemptList, uuid));
    }

    @Test
    @DisplayName("Should match UUID against list without dashes")
    void testMatchWithoutDashes() {
      String[] exemptList = {"069a79f444e94726a5befca90e38aaf5"};
      UUID uuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

      assertTrue(containsUuid(exemptList, uuid));
    }

    @Test
    @DisplayName("Should match case-insensitively")
    void testCaseInsensitiveMatch() {
      String[] exemptList = {"069A79F4-44E9-4726-A5BE-FCA90E38AAF5"};
      UUID uuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

      assertTrue(containsUuid(exemptList, uuid));
    }

    @Test
    @DisplayName("Should not match different UUID")
    void testNoMatchDifferentUuid() {
      String[] exemptList = {"069a79f4-44e9-4726-a5be-fca90e38aaf5"};
      UUID uuid = UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6");

      assertFalse(containsUuid(exemptList, uuid));
    }

    @Test
    @DisplayName("Should match in list with multiple UUIDs")
    void testMatchInMultipleUuids() {
      String[] exemptList = {
        "00000000-0000-0000-0000-000000000000",
        "069a79f4-44e9-4726-a5be-fca90e38aaf5",
        "ffffffff-ffff-ffff-ffff-ffffffffffff"
      };
      UUID uuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

      assertTrue(containsUuid(exemptList, uuid));
    }

    @Test
    @DisplayName("Should not match in empty list")
    void testNoMatchEmptyList() {
      String[] exemptList = {};
      UUID uuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

      assertFalse(containsUuid(exemptList, uuid));
    }
  }
}
