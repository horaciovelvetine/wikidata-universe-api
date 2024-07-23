package edu.velv.wikidata_universe_api.models.err;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WikiverseServiceErrorTests {

  @Test
  void testNoDataFoundErrorConstructionAndProperties() {
    String expectedQuery = "Q123";
    var error = new WikiverseServiceError.WikidataSubServiceError.NoDataFoundError(expectedQuery);

    assertEquals(expectedQuery, error.query(), "The query property should match the constructor argument.");
  }

  @Test
  void testWikidataAPIUnavailableErrorConstructionAndProperties() {
    String expectedQuery = "Q123";
    Throwable cause = new RuntimeException("API Unavailable");
    var error = new WikiverseServiceError.WikidataSubServiceError.WikidataAPIUnavailableError(cause, expectedQuery);

    assertEquals(expectedQuery, error.query(), "The query property should match the constructor argument.");
    assertEquals(cause, error.cause(), "The cause property should match the constructor argument.");
  }
}
