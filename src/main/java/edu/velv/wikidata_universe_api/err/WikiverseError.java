package edu.velv.wikidata_universe_api.err;

public sealed interface WikiverseError
    permits WikiverseError.WikidataServiceError, WikiverseError.UnimplementedError, WikiverseError.JavaRuntimeError {

  public sealed interface WikidataServiceError extends WikiverseError {
    record ApiRequestFailed(String message, Throwable cause) implements WikidataServiceError {
    }

    record NoSuchEntityFound(String message) implements WikidataServiceError {
    }

    record UnexpectedError(String message, Throwable cause) implements WikidataServiceError {
    }

    record FailedQueryInit(String message) implements WikidataServiceError {
    }

    record FetchRelatedDataTimeout(String message, Throwable cause) implements WikidataServiceError {
    }

  }

  record JavaRuntimeError(String message, Throwable cause) implements WikiverseError {
  }

  record UnimplementedError(String message) implements WikiverseError {
  }
}
