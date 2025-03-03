package edu.velv.wikidata_universe_api.models;

import org.junit.jupiter.api.Test;

import edu.velv.wikidata_universe_api.models.ValueData.ValueType;
import edu.velv.wikidata_universe_api.services.FailedTestMsgTemplates;
import edu.velv.wikidata_universe_api.services.wikidata.WikidataTestDataBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;

class EdgeTests implements FailedTestMsgTemplates, WikidataTestDataBuilders {
  private final String src_ = "@EdgeTests:: ";
  Edge edge;

  @BeforeEach
  void setupDefaultEdge() {
    edge = new Edge();
  }

  @Test
  void constructs_default_edge() {
    String msg = src_ + edge + val + shouldBe + "null ";
    assertNull(edge.srcId(), msg);
    assertNull(edge.tgtId(), msg);
    assertNull(edge.propertyId(), msg);
    assertNull(edge.label(), msg);
    assertFalse(edge.fetched(), msg);
  }

  @Test
  void constructs_wikidata_details_mocked_ent_edge() {
    ValueData mSnakValue = mock(ValueData.class);
    when(mSnakValue.type()).thenReturn(ValueType.EntityId);
    when(mSnakValue.value()).thenReturn(QID);

    ValueData mPropValue = mock(ValueData.class);
    when(mPropValue.value()).thenReturn(PID);

    SnakData mSnakData = mock(SnakData.class);
    when(mSnakData.snakValue()).thenReturn(mSnakValue);
    when(mSnakData.property()).thenReturn(mPropValue);

    Edge edge = new Edge(QID, mSnakData);

    assertEquals(QID, edge.srcId(),
        src_ + edge + ".srcId() " + shouldBe + QID);
    assertEquals(QID, edge.tgtId(),
        src_ + edge + ".srcId() " + shouldBe + QID);
    assertEquals(PID, edge.propertyId(),
        src_ + edge + ".srcId() " + shouldBe + PID);
    assertNull(edge.label(),
        src_ + edge + ".label() " + shouldBe + "null ");
    assertTrue(edge.fetched(),
        src_ + edge + ".fetched() " + shouldBe + "true ");
  }

  @Test
  void constructs_wikidata_details_mocked_date_edge() {
    ValueData mSnakValue = mock(ValueData.class);
    when(mSnakValue.type()).thenReturn(ValueType.DateTime);
    when(mSnakValue.value()).thenReturn(la);

    ValueData mPropValue = mock(ValueData.class);
    when(mPropValue.value()).thenReturn(PID);

    SnakData mSnakData = mock(SnakData.class);
    when(mSnakData.snakValue()).thenReturn(mSnakValue);
    when(mSnakData.property()).thenReturn(mPropValue);

    Edge edge = new Edge(QID, mSnakData);

    assertEquals(QID, edge.srcId(),
        src_ + edge + ".srcId() " + shouldBe + QID);
    assertNull(edge.tgtId(),
        src_ + edge + ".tgtId() " + shouldBe + "null for DateTime's");
    assertEquals(PID, edge.propertyId(),
        src_ + edge + ".propertyId() " + shouldBe + PID);
    assertEquals(la, edge.label(),
        src_ + edge + ".srcId() " + shouldBe + la);
    assertFalse(edge.fetched(),
        src_ + "date " + edges + "should remain unfetched until updated with a .tgtId() value");
  }
}