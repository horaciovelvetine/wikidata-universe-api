package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIStatusController {
  @CrossOrigin
  @GetMapping("/api/status")
  public ResponseEntity<String> status() {
    return ResponseEntity.ok("API Online.");
  }
}
