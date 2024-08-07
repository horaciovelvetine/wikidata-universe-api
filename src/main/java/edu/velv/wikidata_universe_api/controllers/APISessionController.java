package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.errors.*;
import edu.velv.wikidata_universe_api.errors.Err.DebugDetailsResponse;
import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.ClientSessionBuilder;
import edu.velv.wikidata_universe_api.models.ResponseBody;

@CrossOrigin
@RestController
public class APISessionController {
  // @GetMapping("/api/init-session")
  // public ResponseEntity<ResponseBody> initSession(
  //     @RequestParam(required = true) String query,
  //     @RequestParam(required = true) String dimensions) {
  //   return ClientSessionBuilder.initialize(query, dimensions)
  //       .mapLeft(Err::mapDebug)
  //       .fold(this::buildErrorResponse, this::buildSuccessResponse);
  // }

  @GetMapping("/api/init-query-data")
  public ResponseEntity<ResponseBody> getInitQueryData(@RequestParam(required = true) String query) {
    return ClientSessionBuilder.getInitialQueryData(query)
        .mapLeft(Err::mapDebug)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  private ResponseEntity<ResponseBody> buildSuccessResponse(ClientSession session) {
    return ResponseEntity.status(200).body(new ResponseBody(session));
  }

  private ResponseEntity<ResponseBody> buildErrorResponse(DebugDetailsResponse err) {
    return ResponseEntity.status(err.status()).body(new ResponseBody(err));
  }
}
