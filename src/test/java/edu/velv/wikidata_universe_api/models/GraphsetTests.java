package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GraphsetTests {

  @Test
  public void init_Graphset() {
    Graphset gs = new Graphset("Kevin Bacon");

    assertEquals("Kevin Bacon", gs.originalQuery());
  }
}
