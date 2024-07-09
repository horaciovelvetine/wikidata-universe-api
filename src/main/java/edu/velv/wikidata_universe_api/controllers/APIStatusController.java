package edu.velv.wikidata_universe_api.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.models.api.RequestResponse.StatusResponse;


@RestController
public class APIStatusController {
  @CrossOrigin
  @GetMapping("/api/status")
  public StatusResponse status() {
    return new StatusResponse();
  }
}
