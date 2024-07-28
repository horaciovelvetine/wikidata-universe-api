package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.err.SessionControllerErrorResponse;

@CrossOrigin
@RestController
public class APISessionController {
  @GetMapping("/api/init-session")
  public ResponseEntity<String> initSession(
      @RequestParam(required = true) String query,
      @RequestParam(required = true) String dimensions) {
    return ClientSession.initialize(query, dimensions)
        .mapLeft(SessionControllerErrorResponse::mapWikiverseErrorToResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  @GetMapping("/api/update-session")
  public void updateSessionDimesnions() {
    // Subject emits windowResize event => update session dimesnions to run new layout
  }

  @GetMapping("/api/update-graphset")
  public void updateGraphsetOnExplore() {
    // Subject emits click event => update graphset w/ newly fetched ents 
  }

  private ResponseEntity<String> buildSuccessResponse(Object body) {
    return ResponseEntity.status(200).body(body.toString());
  }

  private ResponseEntity<String> buildErrorResponse(SessionControllerErrorResponse err) {
    return ResponseEntity.status(err.status()).body(err.message());
  }
}
