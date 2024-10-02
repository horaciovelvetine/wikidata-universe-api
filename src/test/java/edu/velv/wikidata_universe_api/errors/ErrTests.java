package edu.velv.wikidata_universe_api.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class ErrTests {

  @Test
  public void constructs_debug_details_error_with_msg() {
    String expectedMessage = "Debug Details Msg";
    Err.WikiverseServiceError.DebugDetailsError error = new Err.WikiverseServiceError.DebugDetailsError(
        expectedMessage);

    assertEquals(expectedMessage, error.msg(), "Err's should at least have a message, bare minimum.");
  }

  @Test
  public void constructs_no_such_found_error_with_query() {
    String query = "testQuery";
    String expectedMessage = "No such entity found for: " + query;
    Err.WikidataServiceError.NoSuchEntityFoundError error = new Err.WikidataServiceError.NoSuchEntityFoundError(query);

    assertEquals(expectedMessage, error.msg());
    assertEquals(query, error.query());
  }

  @Test
  public void constructs_no_such_found_error_no_query() {
    String expectedMessage = "No such entity found for: default query";
    Err.WikidataServiceError.NoSuchEntityFoundError error = new Err.WikidataServiceError.NoSuchEntityFoundError();

    assertEquals(expectedMessage, error.msg());
    assertEquals("default query", error.query());
  }

  @Test
  public void constucts_api_unavailable_error_with_msg() {
    String expectedMessage = "Custom error message";
    Throwable expectedCause = new RuntimeException("Root cause");
    Err.WikidataServiceError.ApiUnavailableError error = new Err.WikidataServiceError.ApiUnavailableError(
        expectedMessage, expectedCause);

    assertEquals(expectedMessage, error.msg());
    assertSame(expectedCause, error.cause());
  }

  @Test
  public void constructs_api_unavailable_error_default_msg() {
    String expectedMessage = "The Wikidata API is currently offline, try again later.";
    Throwable expectedCause = new RuntimeException("Root cause");
    Err.WikidataServiceError.ApiUnavailableError error = new Err.WikidataServiceError.ApiUnavailableError(
        expectedCause);

    assertEquals(expectedMessage, error.msg());
    assertSame(expectedCause, error.cause());
  }
}