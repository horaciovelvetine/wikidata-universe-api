package edu.velv.wikidata_universe_api.models;

import edu.velv.wikidata_universe_api.models.wikidata.SnakData;
import edu.velv.wikidata_universe_api.models.wikidata.ValueData.ValueType;

public class Edge {
  private String srcEntId;
  private String tgtEntId;
  private String propertyId;
  private String label;
  private ValueType type;

  public Edge(String srcVertexId, SnakData mainSnak) {
    this.srcEntId = srcVertexId;
    this.propertyId = mainSnak.property.value;

  }

  public String srcEntId() {
    return srcEntId;
  }

  public String tgtEndId() {
    return tgtEntId;
  }

  public String propertyId() {
    return propertyId;
  }

  public String label() {
    return label;
  }
  public ValueType type() {
    return type;
  }

  public void setSrcEntId(String srcEntId) {
    this.srcEntId = srcEntId;
  }

  public void setTgtEndId(String tgtEntId) {
    this.tgtEntId = tgtEntId;
  }

  public void setPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }
}
