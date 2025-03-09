package edu.velv.wikidata_universe_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.RequestErrResponse;
import edu.velv.wikidata_universe_api.interfaces.Printable;
import edu.velv.wikidata_universe_api.models.TutorialRequest;
import edu.velv.wikidata_universe_api.services.tutorial.TutorialSlideData;
import edu.velv.wikidata_universe_api.services.wikidata.WikidataServiceManager;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;

@CrossOrigin
@RestController
public class TutorialRequestsController implements Printable {
  @Autowired
  private WikidataServiceManager wikidataServiceManager;

  @Autowired
  private TutorialSlideData tutorialSlideData;

  @GetMapping("api/current-status")
  public ResponseEntity<RequestResponseBody> getCurrentStatus() {
    return buildSuccessResponse(new RequestResponseBody("API Online."));
  }

  @GetMapping("api/tutorial")
  public ResponseEntity<RequestResponseBody> getTutorialSlideDetails(@RequestParam String target) {
    return new TutorialRequest(wikidataServiceManager, tutorialSlideData) .getSlide(target).mapLeft(Err::mapErrResponse)
        .fold(this::buildErrorResponse, this::buildSuccessResponse);
  }

  private ResponseEntity<RequestResponseBody> buildSuccessResponse(RequestResponseBody responseBody) {
    return ResponseEntity.status(200).body(responseBody);
  }

  private ResponseEntity<RequestResponseBody> buildErrorResponse(RequestErrResponse error) {
    return ResponseEntity.status(error.status()).body(new RequestResponseBody(error));
  }

}
