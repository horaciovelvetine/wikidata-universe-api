package edu.velv.wikidata_universe_api.models.err;

public sealed interface WikiverseServiceError
    permits WikiverseServiceError.WikidataSubServiceError, WikiverseServiceError.BlanketCauseableError,
    WikiverseServiceError.BlanketMessageableError {
  // WIKIDATA ERRORS
  public sealed interface WikidataSubServiceError extends WikiverseServiceError {
    record NoDataFoundError(String query) implements WikidataSubServiceError {
    }

    record WikidataAPIUnavailableError(Throwable cause, String query) implements WikidataSubServiceError {
    }
  }

  // WIKIVERSE ERRORS
  record BlanketMessageableError(String message) implements WikiverseServiceError {
  }

  record BlanketCauseableError(Throwable cause) implements WikiverseServiceError {
  }
}
