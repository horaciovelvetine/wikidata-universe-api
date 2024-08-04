package edu.velv.wikidata_universe_api.controllers;

import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.velv.wikidata_universe_api.errors.*;
import edu.velv.wikidata_universe_api.errors.Err.DataSerializationError;
import edu.velv.wikidata_universe_api.errors.Err.DebugDetailsResponse;
import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.ClientSessionBuilder;
import edu.velv.wikidata_universe_api.models.jung_ish.Edge;
import edu.velv.wikidata_universe_api.models.jung_ish.Vertex;
import edu.velv.wikidata_universe_api.models.wikidata.FetchQueue;
import edu.velv.wikidata_universe_api.models.wikidata.Property;

@CrossOrigin
@RestController
public class APISessionController {
  @GetMapping("/api/init-session")
  public ResponseEntity<String> initSession(
      @RequestParam(required = true) String query,
      @RequestParam(required = true) String dimensions) {
    return ClientSessionBuilder.initialize(query, dimensions)
        .mapLeft(Err::mapDebug)
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

  private ResponseEntity<String> buildSuccessResponse(ClientSession session) {
    ObjectMapper mapper = new ObjectMapper();

    ClientSessionDataBody body = new ClientSessionDataBody(
        session.graphset().vertices(),
        session.graphset().edges(),
        session.wikidataManager().properties(),
        session.wikidataManager().fetchQueue());

    try {
      return ResponseEntity.status(200).body(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
    } catch (Exception e) {
      return buildErrorResponse(
          Err.mapDebug(new DataSerializationError("Encountered error @ buildSuccessResponse(): ", e)));
    }
  }

  private ResponseEntity<String> buildErrorResponse(DebugDetailsResponse err) {
    return ResponseEntity.status(err.status()).body(err.message());
  }

  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  private record ClientSessionDataBody(
      Collection<Vertex> vertices,
      Collection<Edge> edges,
      Collection<Property> properties,
      FetchQueue queue) {
  }
}
