package com.hexagram2021.server_precheck.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DisconnectMessageBuilder.
 *
 * <p>Note: Most tests for DisconnectMessageBuilder require SPCServerConfig to be initialized, which
 * depends on FabricLoader. These should be run as integration tests in a Minecraft environment.
 *
 * <p>Integration test scenarios to implement:
 *
 * <p>Custom Message Tests: - Should use custom disconnect message when set - Should use custom
 * message for not installed case
 *
 * <p>Default Message Tests: - Should build message with unauthorized mods - Should build message
 * with missing mods - Should include modpack name when configured - Should show contact admin
 * message when no modpack name
 */
class DisconnectMessageBuilderTest {

  @Test
  @DisplayName("DisconnectMessageBuilder class should exist and be final")
  void testClassExists() {
    // Verify the class exists and has the expected structure
    assertTrue(
        java.lang.reflect.Modifier.isFinal(DisconnectMessageBuilder.class.getModifiers()),
        "DisconnectMessageBuilder should be a final class");
  }

  @Test
  @DisplayName("DisconnectMessageBuilder should have private constructor")
  void testPrivateConstructor() throws NoSuchMethodException {
    var constructor = DisconnectMessageBuilder.class.getDeclaredConstructor();
    assertTrue(
        java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()),
        "DisconnectMessageBuilder constructor should be private");
  }

  @Test
  @DisplayName("DisconnectMessageBuilder should have buildMismatchMessage method")
  void testBuildMismatchMessageMethodExists() {
    var methods = DisconnectMessageBuilder.class.getDeclaredMethods();
    boolean found = false;
    for (var method : methods) {
      if (method.getName().equals("buildMismatchMessage")) {
        found = true;
        assertTrue(
            java.lang.reflect.Modifier.isStatic(method.getModifiers()),
            "buildMismatchMessage should be static");
        assertTrue(
            java.lang.reflect.Modifier.isPublic(method.getModifiers()),
            "buildMismatchMessage should be public");
        break;
      }
    }
    assertTrue(found, "buildMismatchMessage method should exist");
  }

  @Test
  @DisplayName("DisconnectMessageBuilder should have buildNotInstalledMessage method")
  void testBuildNotInstalledMessageMethodExists() {
    var methods = DisconnectMessageBuilder.class.getDeclaredMethods();
    boolean found = false;
    for (var method : methods) {
      if (method.getName().equals("buildNotInstalledMessage")) {
        found = true;
        assertTrue(
            java.lang.reflect.Modifier.isStatic(method.getModifiers()),
            "buildNotInstalledMessage should be static");
        assertTrue(
            java.lang.reflect.Modifier.isPublic(method.getModifiers()),
            "buildNotInstalledMessage should be public");
        break;
      }
    }
    assertTrue(found, "buildNotInstalledMessage method should exist");
  }
}
