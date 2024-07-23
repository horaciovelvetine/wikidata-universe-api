package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.utils.QueryParamSanitizer;

@CrossOrigin
@RestController
public class APISessionController {
  @GetMapping("/api/v1/init-session")
  public ResponseEntity<String> initSession(
      @RequestParam(value = "query", defaultValue = "Kevin Bacon") String query,
      @RequestParam(value = "dimensions", defaultValue = "1600x900") String dimensions) {
    query = QueryParamSanitizer.sanitize(query);
    ClientSession session = new ClientSession(query, dimensions);
    // Subject emits init event => new session create, needs dimesnions & query
    return ResponseEntity.ok("Session Initializing[query=" + query + ", dimensions=" + dimensions + "]");
  }

  @GetMapping("/api/v1/update-session")
  public void updateSessionDimesnions() {
    // Subject emits windowResize event => update session dimesnions to run new layout
  }

  @GetMapping("/api/v1/update-graphset")
  public void updateGraphsetOnExplore() {
    // Subject emits click event => update graphset w/ newly fetched ents 
  }
}
