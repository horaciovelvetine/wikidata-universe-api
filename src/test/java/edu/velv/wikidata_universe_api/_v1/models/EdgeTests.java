package edu.velv.wikidata_universe_api._v1.models;

import org.junit.jupiter.api.Test;

import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.SnakData;
import edu.velv.wikidata_universe_api.models.ValueData;
import edu.velv.wikidata_universe_api.models.ValueData.ValueType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;

class EdgeTests {
  Edge edge;

  @BeforeEach
  void setupDefaultEdge() {
    edge = new Edge();
  }

  @Test
  void constructs_default_edge() {
    String msg = "Default Values should be either null or false!";
    assertNull(edge.srcId(), msg);
    assertNull(edge.tgtId(), msg);
    assertNull(edge.propertyId(), msg);
    assertNull(edge.label(), msg);
    assertFalse(edge.fetched(), msg);
  }

  @Test
  void constructs_wikidata_details_mocked_ent_edge() {
    String exQID = "QID";
    String exPID = "PID";
    ValueData mSnakValue = mock(ValueData.class);
    when(mSnakValue.type()).thenReturn(ValueType.EntityId);
    when(mSnakValue.value()).thenReturn(exQID);

    ValueData mPropValue = mock(ValueData.class);
    when(mPropValue.value()).thenReturn(exPID);

    SnakData mSnakData = mock(SnakData.class);
    when(mSnakData.snakValue()).thenReturn(mSnakValue);
    when(mSnakData.property()).thenReturn(mPropValue);

    Edge edge = new Edge(exQID, mSnakData);

    assertEquals(edge.srcId(), exQID);
    assertEquals(edge.tgtId(), exQID);
    assertEquals(edge.propertyId(), exPID);
    assertNull(edge.label(), "Label should be null when Edge summarizes a non-date targeted Edge");
    assertTrue(edge.fetched(), "Fetched should be true");
  }

  @Test
  void constructs_wikidata_details_mocked_date_edge() {
    String srcQID = "QID";
    String exPID = "PID";
    String exDate = "DATE";
    ValueData mSnakValue = mock(ValueData.class);
    when(mSnakValue.type()).thenReturn(ValueType.DateTime);
    when(mSnakValue.value()).thenReturn(exDate);

    ValueData mPropValue = mock(ValueData.class);
    when(mPropValue.value()).thenReturn(exPID);

    SnakData mSnakData = mock(SnakData.class);
    when(mSnakData.snakValue()).thenReturn(mSnakValue);
    when(mSnakData.property()).thenReturn(mPropValue);

    Edge edge = new Edge(srcQID, mSnakData);

    assertEquals(edge.srcId(), srcQID);
    assertNull(edge.tgtId(), "Target value should be null @ this point for Date Edges");
    assertEquals(edge.propertyId(), exPID);
    assertEquals(edge.label(), exDate, "Label should be assigned to DATE value");
    assertFalse(edge.fetched(),
        "Date Edges should remain unfetched until they are updated with their tgtId value is filled in.");
  }

  @Test
  void srcId_gets_sets() {
    edge.srcId("Q42");
    assertEquals("Q42", edge.srcId());
  }

  @Test
  void tgtId_gets_sets() {
    edge.tgtId("Q42");
    assertEquals("Q42", edge.tgtId());
  }

  @Test
  void propertyId_gets_sets() {
    edge.propertyId("P31");
    assertEquals("P31", edge.propertyId());
  }

  @Test
  void label_gets_sets() {
    edge.label("testable-label-values");
    assertEquals("testable-label-values", edge.label());
  }

  @Test
  void fetched_toggles() {
    edge.fetched(true);
    assertTrue(edge.fetched());
  }

}