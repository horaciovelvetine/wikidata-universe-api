package edu.velv.wikidata_universe_api.errors;

public sealed interface Err permits Err.WikidataServiceError, Err.WikiverseServiceError {
  public String msg();

  sealed interface WikiverseServiceError extends Err {
    record DebugDetailsResponseError(String msg) implements WikiverseServiceError {
      //? General Error for use in Debug Responses to the Client
    }
  }

  sealed interface WikidataServiceError extends Err {
    record ApiUnavailableError(String msg, Throwable cause) implements WikidataServiceError {
      public ApiUnavailableError(Throwable cause) {
        this("The Wikidata API is currently offline, try again later.", cause);
      }
    }

    record NoSuchRecordFoundError(String msg, String query) implements WikidataServiceError {
      public NoSuchRecordFoundError(String query) {
        this("No such entity found for: " + query, query);
      }

      public NoSuchRecordFoundError() {
        this("default query");
      }
      //? Searching by any returns no results for this query
    }
  }
}
