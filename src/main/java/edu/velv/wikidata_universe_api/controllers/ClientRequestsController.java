package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.ClientRequest;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.RequestErrResponse;
import edu.velv.wikidata_universe_api.models.RequestPayloadData;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin
@RestController
public class ClientRequestsController {
  @GetMapping("api/query-data")
  public ResponseEntity<RequestResponseBody> getInitialQueryData(@RequestParam(required = true) String query) {
    return new ClientRequest(query)
        .getInitialQueryData()
        .mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  @PostMapping("api/fetch-related")
  public ResponseEntity<RequestResponseBody> fetchRelatedDataDetails(@RequestBody RequestPayloadData payload) {
    return new ClientRequest(payload)
        .getUnfetchedData()
        .mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  @PostMapping("api/action/click-target")
  public ResponseEntity<RequestResponseBody> fetchClickTargetRelatedDataDetails(
      @RequestBody RequestPayloadData payload) {
    return null;
  }

  private ResponseEntity<RequestResponseBody> buildSuccessResponse(ClientRequest request) {
    return ResponseEntity.status(200).body(new RequestResponseBody(request));
  }

  private ResponseEntity<RequestResponseBody> buildErrorResponse(RequestErrResponse error) {
    return ResponseEntity.status(error.status()).body(new RequestResponseBody(error));
  }
}
