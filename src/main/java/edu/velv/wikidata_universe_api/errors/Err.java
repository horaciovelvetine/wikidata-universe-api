package edu.velv.wikidata_universe_api.errors;

import edu.velv.wikidata_universe_api.errors.Err.WikiverseServiceError.TutorialSlideDataUnavailableError;
import edu.velv.wikidata_universe_api.errors.Err.WikiverseServiceError.DebugDetailsError;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.ApiUnavailableError;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.NoSuchEntityFoundError;

public sealed interface Err permits Err.WikidataServiceError, Err.WikiverseServiceError, Err.RequestErrResponse {
  public String msg();

  sealed interface WikiverseServiceError extends Err {
    record DebugDetailsError(String msg) implements WikiverseServiceError {
      //? Default error
    }

    record FR3DLayoutProcessError(String msg, Exception e) implements WikidataServiceError {
    }

    record TutorialSlideDataUnavailableError(String msg, Exception e) implements WikidataServiceError {
      // catching the AboutRequest being unable to find the needed data to respond about a request
    }
  }

  sealed interface WikidataServiceError extends Err {
    record ApiUnavailableError(String msg, Throwable cause) implements WikidataServiceError {
      public ApiUnavailableError(Throwable cause) {
        this("The Wikidata API is currently offline, try again later.", cause);
      }
    }

    record NoSuchEntityFoundError(String msg, String query) implements WikidataServiceError {
      //? Searching by any returns no results for this query
      public NoSuchEntityFoundError(String query) {
        this("No such entity found for: " + query, query);
      }

      public NoSuchEntityFoundError() {
        this("default query");
      }
    }
  }

  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  record RequestErrResponse(int status, String msg, Err e) implements Err {
    //? Returned in ResponseBody if Err present summarizes thrown for debug
  }

  public static RequestErrResponse mapErrResponse(Err error) {
    return switch (error) {
      case DebugDetailsError e -> new RequestErrResponse(404, "A Debug, oh no squish it!", error);
      case ApiUnavailableError e ->
        new RequestErrResponse(404, "Wikidata's API is currently unavailable, try again later.", error);
      case NoSuchEntityFoundError e -> new RequestErrResponse(404,
          "Seems like there is no such matching record, check your search and try again.", error);
      case TutorialSlideDataUnavailableError e ->
        new RequestErrResponse(404, "Unable to read needed about Data file", error);
      default ->
        new RequestErrResponse(400, "Fallback, found a default error, retrace your steps and try again!", error);
    };
  }
}
