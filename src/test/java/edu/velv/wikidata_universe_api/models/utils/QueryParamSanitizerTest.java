package edu.velv.wikidata_universe_api.models.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class QueryParamSanitizerTest {

  @Test
  public void sanitize_NullInput_ReturnsNull() {
    assertNull(QueryParamSanitizer.sanitize(null), "Sanitizing null should return null");
  }

  @Test
  public void sanitize_EmptyString_ReturnsNull() {
    assertNull(QueryParamSanitizer.sanitize(""), "Sanitizing an empty string should return null");
  }

  @Test
  public void sanitize_HTMLInjectionPrevention_ReplacesAngleBrackets() {
    assertEquals("Hello World", QueryParamSanitizer.sanitize("Hello <World>"),
        "Angle brackets should be replaced with HTML entities");
  }

  @Test
  public void sanitize_NonAlphanumericRemoval_RemovesNonAlphanumericCharacters() {
    assertEquals("HelloWorld", QueryParamSanitizer.sanitize("Hello@#World"),
        "Non-alphanumeric characters should be removed");
  }

  @Test
  public void sanitize_WhitespaceTrimming_TrimsWhitespace() {
    assertEquals("HelloWorld", QueryParamSanitizer.sanitize("  HelloWorld  "),
        "Leading and trailing whitespaces should be trimmed");
  }
}