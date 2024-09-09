package edu.velv.wikidata_universe_api.errors;

public sealed interface WikidataServiceError extends Err {

  record NoSuchEntityFoundError(String message) implements WikidataServiceError {
    // Doesn't need a throwable, everything is working, there's simply no answer...
  }

  record ApiUnavailableError(String message, Throwable e) implements WikidataServiceError {
    // Sourced from an underlying error, needs throwable...
  }

  record FetchRelatedWithTimeoutError(String message, Throwable e) implements WikidataServiceError {
  }

}
