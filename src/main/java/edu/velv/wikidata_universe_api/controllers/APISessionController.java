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

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin
@RestController
public class APISessionController {
  @GetMapping("/api/init-query-data")
  public ResponseEntity<ResponseBody> getInitQueryData(@RequestParam(required = true) String query) {
    return ClientSessionBuilder.getInitialQueryData(query)
        .mapLeft(Err::mapDebug)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  @PostMapping("api/init-related-data")
  public ResponseEntity<ResponseBody> getInitRelatedData(@RequestBody ResponseBody payload) {
    //TODO: handle init related data fetching, diff from event get related data tbd...
    return null;
  }

  @PostMapping("api/create-layout")
  public ResponseEntity<ResponseBody> createLayout(@RequestBody ResponseBody payload) {
    //TODO: take in details and provide complete coordinates
    return null;
  }

  private ResponseEntity<ResponseBody> buildSuccessResponse(ClientSession session) {
    return ResponseEntity.status(200).body(new ResponseBody(session));
  }

  private ResponseEntity<ResponseBody> buildErrorResponse(DebugDetailsResponse err) {
    return ResponseEntity.status(err.status()).body(new ResponseBody(err));
  }
}
