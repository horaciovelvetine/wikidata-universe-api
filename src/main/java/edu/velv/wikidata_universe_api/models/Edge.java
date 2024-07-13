package edu.velv.wikidata_universe_api.models;

public class Edge {
  private String srcEntId;
  private String tgtEntId;
  private String propertyId;

  public Edge() {
    // Default constructor
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
