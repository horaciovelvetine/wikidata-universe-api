package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.errors.*;
import edu.velv.wikidata_universe_api.errors.Err.DebugDetailsResponse;
import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin
@RestController
public class ClientRequestController {
  @GetMapping("/api/query-data")
  public ResponseEntity<RequestResponseBody> getInitQueryData(@RequestParam(required = true) String query) {
    return new ClientRequest(query)
        .getInitialQueryData()
        .mapLeft(Err::mapDebug)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  @PostMapping("api/related-data-queue")
  public ResponseEntity<RequestResponseBody> getInitRelatedData(@RequestBody RequestResponseBody payload) {
    return new ClientRequest(payload, false) // fetches values in the body of the request (from the queue).
        .getInitialRelatedData()
        .mapLeft(Err::mapDebug)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  @PostMapping("api/related-data-click")
  public ResponseEntity<RequestResponseBody> getClickTargetRelatedData(@RequestBody RequestResponseBody payload) {
    return new ClientRequest(payload, true) // ignores fetch values in the body and instead fills queue itself.
        .getClickRelatedData()
        .mapLeft(Err::mapDebug)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  private ResponseEntity<RequestResponseBody> buildSuccessResponse(ClientRequest session) {
    return ResponseEntity.status(200).body(new RequestResponseBody(session));
  }

  private ResponseEntity<RequestResponseBody> buildErrorResponse(DebugDetailsResponse err) {
    return ResponseEntity.status(err.status()).body(new RequestResponseBody(err));
  }
}
