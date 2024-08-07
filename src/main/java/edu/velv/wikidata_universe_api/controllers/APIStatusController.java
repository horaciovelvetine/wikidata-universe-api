package edu.velv.wikidata_universe_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.models.ResponseBody;

@CrossOrigin
@RestController
public class APIStatusController {
  @GetMapping("/api/status")
  public ResponseEntity<String> status() {
    return ResponseEntity.ok("API Online.");
  };

  @GetMapping("/api/404")
  public ResponseEntity<String> getTestError() {
    return ResponseEntity.status(404).body("404 Error Test End.");
  }

  @GetMapping("/api/faking-it")
  public ResponseEntity<ResponseBody> getMethodName() {
    return ResponseEntity.ok(new ResponseBody());
  }

  @GetMapping("/api/await-success")
  public ResponseEntity<String> getAwaitedSuccess() {
    try {
      Thread.sleep(500);
      return ResponseEntity.status(200).body("200 Success Test.");
    } catch (Exception e) {
      return ResponseEntity.status(404).body("404 Successfully Failed to Await.");
    }
  }

}
