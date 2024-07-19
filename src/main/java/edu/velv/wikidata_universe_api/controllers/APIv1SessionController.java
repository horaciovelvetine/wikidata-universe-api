package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.models.Graphset;
import edu.velv.wikidata_universe_api.models.utils.QueryParamSanitizer;

@CrossOrigin
@RestController
public class APIv1SessionController {
  @GetMapping("/api/v1/init-session")
  public ResponseEntity<Graphset> initSession(
      @RequestParam(value = "query", defaultValue = "Kevin Bacon") String query) {
    Graphset graphset = new Graphset(QueryParamSanitizer.sanitize(query));

    return ResponseEntity.ok(graphset);
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
