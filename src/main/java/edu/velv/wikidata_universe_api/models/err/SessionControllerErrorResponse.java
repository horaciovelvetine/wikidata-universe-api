package edu.velv.wikidata_universe_api.models.err;

public record SessionControllerErrorResponse(int status, String message) {

  private static final int SERVICE_UNAVAILABLE = 503;
  private static final String SU_MSG = "The MediaWiki API is Unavialble. Please try again later.";
  private static final int NOT_FOUND_ERR = 404;
  private static final String NF_MSG = "No entity found with the given query. Please try a different search.";
  private static final int INTERNAL_SERVER_ERR = 500;
  private static final String IS_MSG = "An unexpected error occurred with the MediaWiki API. Please try again later.";
  private static final int BAD_REQUEST_ERR = 444;
  private static final String BR_MSG = "An unexpected error occurred.";

  public static SessionControllerErrorResponse mapWikiverseErrorToResponse(WikiverseError err) {
    return switch (err) {
      case WikiverseError.WikidataServiceError.ApiRequestFailed e ->
        new SessionControllerErrorResponse(SERVICE_UNAVAILABLE, SU_MSG);
      case WikiverseError.WikidataServiceError.NoSuchEntityFound e ->
        new SessionControllerErrorResponse(NOT_FOUND_ERR, NF_MSG);
      case WikiverseError.WikidataServiceError.UnexpectedError e ->
        new SessionControllerErrorResponse(INTERNAL_SERVER_ERR, IS_MSG);
      default -> new SessionControllerErrorResponse(BAD_REQUEST_ERR, BR_MSG);
    };
  }
}
