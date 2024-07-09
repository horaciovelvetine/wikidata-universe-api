package edu.velv.wikidata_universe_api.controllers.apiv1;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.models.api.RequestResponse.InitialQueryResponse;

@CrossOrigin
@RestController
public class InitialQueryController {
  @GetMapping("/api/v1/initial-query")
  public InitialQueryResponse initialQuery(@RequestParam(value = "query", defaultValue = "Kevin Bacon") String query) {
    return new InitialQueryResponse(query);
  }
}
