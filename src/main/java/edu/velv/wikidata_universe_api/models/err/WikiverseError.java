package edu.velv.wikidata_universe_api.models.err;

public sealed interface WikiverseError
    permits WikiverseError.WikidataServiceError, WikiverseError.UnimplementedError {

  public sealed interface WikidataServiceError extends WikiverseError {
    record ApiRequestFailed(String message, Throwable cause) implements WikidataServiceError {
    }

    record NoSuchEntityFound(String message) implements WikidataServiceError {
    }

    record UnexpectedError(String message, Throwable cause) implements WikidataServiceError {
    }
  }

  record UnimplementedError(String message) implements WikiverseError {
  }
}
