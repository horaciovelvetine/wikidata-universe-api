package edu.velv.wikidata_universe_api;

import java.awt.Dimension;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

class ClientRequestTests {
  ClientRequest request;

  @BeforeEach
  void setupDefaultRequest() {
    request = new ClientRequest("Kevin Bacon");
  }

  @Test
  void constructs_request_with_query_provided_defalut() {
    assertEquals(request.query, "Kevin Bacon");
    assertEquals(request.dimensions, new Dimension());
    assertNotNull(request.graph);
    assertNotNull(request.layout);
  }
}