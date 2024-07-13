package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EdgeTests {

  @Test
  public void init_wDefaults_Jackson() {
    Edge edge = new Edge();
    assertNull(edge.srcEntId(), "srcEntId should be null");
    assertNull(edge.tgtEndId(), "tgtEndId should be null");
    assertNull(edge.propertyId(), "propertyId should be null");
  }

  @Test
  public void setSrcEntId() {
    Edge edge = new Edge();
    edge.setSrcEntId("srcEntId");
    assertEquals("srcEntId", edge.srcEntId(), "srcEntId should be 'srcEntId'");
  }

  @Test
  public void setTgtEndId() {
    Edge edge = new Edge();
    edge.setTgtEndId("tgtEntId");
    assertEquals("tgtEntId", edge.tgtEndId(), "tgtEndId should be 'tgtEntId'");
  }

  @Test
  public void setPropertyId() {
    Edge edge = new Edge();
    edge.setPropertyId("propertyId");
    assertEquals("propertyId", edge.propertyId(), "propertyId should be 'propertyId'");
  }

}
