package edu.velv.wikidata_universe_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.RequestErrResponse;
import edu.velv.wikidata_universe_api.models.AboutRequest;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;
import edu.velv.wikidata_universe_api.services.Printable;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

@CrossOrigin
@RestController
public class AboutRequestsController implements Printable {
  @Autowired
  private WikidataServiceManager wikidataServiceManager;

  @GetMapping("api/current-status")
  public ResponseEntity<RequestResponseBody> getCurrentStatus() {
    return buildSuccessResponse(new RequestResponseBody("API Online."));
  }

  @GetMapping("api/about-details")
  public ResponseEntity<RequestResponseBody> getInitialAboutDetails() {
    return buildSuccessResponse(new RequestResponseBody(new AboutRequest(wikidataServiceManager)));
  }

  @GetMapping("api/about")
  public ResponseEntity<RequestResponseBody> getNextAboutDetails(@RequestParam String target) {
    return new AboutRequest(wikidataServiceManager).getStage(target).mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  private ResponseEntity<RequestResponseBody> buildSuccessResponse(RequestResponseBody responseBody) {
    return ResponseEntity.status(200).body(responseBody);
  }

  private ResponseEntity<RequestResponseBody> buildErrorResponse(RequestErrResponse error) {
    return ResponseEntity.status(error.status()).body(new RequestResponseBody(error));
  }

}
