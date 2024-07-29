package edu.velv.wikidata_universe_api.err;

import edu.velv.wikidata_universe_api.err.WikidataServiceError.ApiUnavailableError;
import edu.velv.wikidata_universe_api.err.WikidataServiceError.FetchRelatedWithTimeoutError;
import edu.velv.wikidata_universe_api.err.WikidataServiceError.NoSuchEntityFoundError;

public sealed interface Err permits WikidataServiceError, Err.DebugDetailsResponse {
  static final int ERR_CODE = 404;

  record DebugDetailsResponse(int status, String message, Err e) implements Err {
  }

  public static DebugDetailsResponse mapDebug(Err err) {
    return switch (err) {
      case NoSuchEntityFoundError e -> new DebugDetailsResponse(ERR_CODE, "No Such Entity Found", err);
      case ApiUnavailableError e -> new DebugDetailsResponse(ERR_CODE, "Wikidata Unavailable Error", err);
      case FetchRelatedWithTimeoutError e -> new DebugDetailsResponse(ERR_CODE, "Related Fetch Timed Out", err);
      default -> new DebugDetailsResponse(ERR_CODE, "Unexpected value: ", err);
    };
  }
}
