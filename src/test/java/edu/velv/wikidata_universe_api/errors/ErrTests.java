package edu.velv.wikidata_universe_api.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class ErrTests {

  @Test
  public void testDebugDetailsResponseErrorMsg() {
    String expectedMessage = "Debug Details Msg";
    Err.WikiverseServiceError.DebugDetailsResponseError error = new Err.WikiverseServiceError.DebugDetailsResponseError(
        expectedMessage);

    assertEquals(expectedMessage, error.msg());
  }

  @Test
  public void testNoSuchRecordFoundErrorWithQuery() {
    // Arrange
    String query = "testQuery";
    String expectedMessage = "No such entity found for: " + query;
    Err.WikidataServiceError.NoSuchRecordFoundError error = new Err.WikidataServiceError.NoSuchRecordFoundError(query);

    // Act
    String actualMessage = error.msg();

    // Assert
    assertEquals(expectedMessage, actualMessage);
    assertEquals(query, error.query());
  }

  @Test
  public void testNoSuchRecordFoundErrorDefaultConstructor() {
    // Arrange
    String expectedMessage = "No such entity found for: default query";
    Err.WikidataServiceError.NoSuchRecordFoundError error = new Err.WikidataServiceError.NoSuchRecordFoundError();

    // Act
    String actualMessage = error.msg();

    // Assert
    assertEquals(expectedMessage, actualMessage);
    assertEquals("default query", error.query());
  }

  @Test
  public void testApiUnavailableErrorWithMessageAndCause() {
    // Arrange
    String expectedMessage = "Custom error message";
    Throwable expectedCause = new RuntimeException("Root cause");
    Err.WikidataServiceError.ApiUnavailableError error = new Err.WikidataServiceError.ApiUnavailableError(
        expectedMessage, expectedCause);

    // Act
    String actualMessage = error.msg();
    Throwable actualCause = error.cause();

    // Assert
    assertEquals(expectedMessage, actualMessage);
    assertSame(expectedCause, actualCause);
  }

  @Test
  public void testApiUnavailableErrorWithCause() {
    // Arrange
    String expectedMessage = "The Wikidata API is currently offline, try again later.";
    Throwable expectedCause = new RuntimeException("Root cause");
    Err.WikidataServiceError.ApiUnavailableError error = new Err.WikidataServiceError.ApiUnavailableError(
        expectedCause);

    // Act
    String actualMessage = error.msg();
    Throwable actualCause = error.cause();

    // Assert
    assertEquals(expectedMessage, actualMessage);
    assertSame(expectedCause, actualCause);
  }
}
