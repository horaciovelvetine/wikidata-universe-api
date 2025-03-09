package edu.velv.wikidata_universe_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.RequestErrResponse;
import edu.velv.wikidata_universe_api.interfaces.Loggable;
import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.RequestPayloadData;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;
import edu.velv.wikidata_universe_api.services.wikidata.WikidataServiceManager;

@CrossOrigin
@RestController
public class ClientRequestsController implements Loggable {
  @Autowired
  private WikidataServiceManager wikidataServiceManager;

  @GetMapping("api/query-data")
  public ResponseEntity<RequestResponseBody> getInitialQueryData(@RequestParam(required = true) String query) {
    print("getInitialQueryData() start: " + query);
    return new ClientRequest(
        wikidataServiceManager, query)
        .getInitialQueryData()
        .mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  /**
  * @apiNote - Expects a mostly (data) incomplete payload from the client and complete that @see Graphset. 
  */
  @PostMapping("api/fetch-related")
  public ResponseEntity<RequestResponseBody> fetchRelatedDataDetails(@RequestBody RequestPayloadData payload) {
    print("fetchRelatedDataDetails() start: " + payload.query());
    return new ClientRequest(wikidataServiceManager, payload)
        .getUnfetchedData()
        .mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  /**
   * @apiNote - Expects a data complete request, fetching new data at an N1 depth based on an altQuery target provided in the payload
   */
  @PostMapping("api/click-target")
  public ResponseEntity<RequestResponseBody> fetchClickTargetRelatedDetails(@RequestBody RequestPayloadData payload) {
    return new ClientRequest(wikidataServiceManager, payload)
        .getClickTargetData()
        .mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  @PostMapping("api/refresh-layout")
  public ResponseEntity<RequestResponseBody> refreshLayoutPositions(@RequestBody RequestPayloadData payload) {
    return new ClientRequest(wikidataServiceManager, payload)
        .refreshLayoutPositions()
        .mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  private ResponseEntity<RequestResponseBody> buildSuccessResponse(RequestResponseBody responseBody) {
    print("Response: " + responseBody.toString());
    return ResponseEntity.status(200).body(responseBody);
  }

  private ResponseEntity<RequestResponseBody> buildErrorResponse(RequestErrResponse error) {
    return ResponseEntity.status(error.status()).body(new RequestResponseBody(error));
  }
}
