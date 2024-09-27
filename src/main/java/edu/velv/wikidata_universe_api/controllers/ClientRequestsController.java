package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.velv.wikidata_universe_api.ClientRequest;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.RequestErrResponse;
import edu.velv.wikidata_universe_api.models.RequestPayloadData;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;
import edu.velv.wikidata_universe_api.services.Printable;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

@CrossOrigin
@RestController
public class ClientRequestsController implements Printable {
  private final WikidataServiceManager srvcMngr = new WikidataServiceManager();

  @GetMapping("api/status")
  public ResponseEntity<RequestResponseBody> getMethodName() {
    return ResponseEntity.status(200).body(new RequestResponseBody("API Online."));
  }

  @GetMapping("api/query-data")
  public ResponseEntity<RequestResponseBody> getInitialQueryData(@RequestParam(required = true) String query) {
    return new ClientRequest(
        srvcMngr, query)
        .getInitialQueryData()
        .mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  @PostMapping("api/fetch-related")
  public ResponseEntity<RequestResponseBody> fetchRelatedDataDetails(@RequestBody RequestPayloadData payload) {

    print("FetchRelated: " + payload.query());

    return new ClientRequest(srvcMngr, payload)
        .getUnfetchedData()
        .mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  private ResponseEntity<RequestResponseBody> buildSuccessResponse(ClientRequest request) {
    return ResponseEntity.status(200).body(new RequestResponseBody(request));
  }

  private ResponseEntity<RequestResponseBody> buildErrorResponse(RequestErrResponse error) {
    return ResponseEntity.status(error.status()).body(new RequestResponseBody(error));
  }
}
