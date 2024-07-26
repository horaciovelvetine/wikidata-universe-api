package edu.velv.wikidata_universe_api.models.err;

public sealed interface WikiverseError permits WikiverseError.WikidataServiceError {

  public sealed interface WikidataServiceError extends WikiverseError {
    record ApiRequestFailed(String message) implements WikidataServiceError {
    }

    record NoSuchEntityFound(String message) implements WikidataServiceError {
    }

    record UnexpectedError(String message) implements WikidataServiceError {
    }
  }
}
