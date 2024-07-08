package edu.velv.wikidata_universe_api.controllers;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.velv.wikidata_universe_api.models.HelloWorld;

@RestController
public class HelloWorldController {

  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  @GetMapping("/greeting")
  public HelloWorld greeting(@RequestParam(value = "name", defaultValue = "Worlds!") String name) {
    return new HelloWorld(counter.incrementAndGet(), String.format(template, name));
  }
}
