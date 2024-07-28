package edu.velv.wikidata_universe_api.err;

import edu.velv.wikidata_universe_api.err.WikiverseError.*;

public record SessionControllerErrorResponse(int status, String message) {

  private static final int SERVICE_UNAVAILABLE = 503;
  private static final String SU_MSG = "The MediaWiki API is Unavialble. Please try again later.";
  private static final int NOT_FOUND_ERR = 404;
  private static final String NF_MSG = "No entity found with the given query. Please try a different search.";
  private static final int INTERNAL_SERVER_ERR = 500;
  private static final String IS_MSG = "An unexpected error occurred with the MediaWiki API. Please try again later.";
  private static final int DEFAULT_ERR = 404;
  private static final String DEF_MSG = "Encountered an Unexpected Error, unable to continue. Restart the application.";

  public static SessionControllerErrorResponse mapWikiverseErrorToResponse(WikiverseError err) {
    return switch (err) {
      case WikidataServiceError.ApiRequestFailed e ->
        new SessionControllerErrorResponse(SERVICE_UNAVAILABLE, SU_MSG);
      case WikidataServiceError.NoSuchEntityFound e ->
        new SessionControllerErrorResponse(NOT_FOUND_ERR, NF_MSG);
      case WikidataServiceError.UnexpectedError e ->
        new SessionControllerErrorResponse(INTERNAL_SERVER_ERR, IS_MSG);
      default -> new SessionControllerErrorResponse(DEFAULT_ERR, DEF_MSG);
    };
  }
}
