package edu.velv.wikidata_universe_api.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EdgeTests {

  @Test
  void testDefaultConstructor() {
    Edge edge = new Edge();
    assertNull(edge.srcId());
    assertNull(edge.tgtId());
    assertNull(edge.propertyId());
    assertNull(edge.label());
    assertFalse(edge.fetched());
  }

  @Test
  void testSetterMethods() {
    Edge edge = new Edge();
    edge.srcId("A");
    edge.tgtId("B");
    edge.propertyId("P123");
    edge.label("Connection");
    edge.fetched(true);

    assertEquals("A", edge.srcId());
    assertEquals("B", edge.tgtId());
    assertEquals("P123", edge.propertyId());
    assertEquals("Connection", edge.label());
    assertTrue(edge.fetched());
  }

  @Test
  void testGetterMethods() {
    Edge edge = new Edge();
    edge.srcId("A");
    edge.tgtId("B");
    edge.propertyId("P123");
    edge.label("Connection");

    assertEquals("A", edge.srcId());
    assertEquals("B", edge.tgtId());
    assertEquals("P123", edge.propertyId());
    assertEquals("Connection", edge.label());
  }
}