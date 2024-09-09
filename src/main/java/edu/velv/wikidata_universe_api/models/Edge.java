package edu.velv.wikidata_universe_api.models;

public class Edge {
  private String srcId;
  private String tgtId;
  private String propertyId;
  private String label;
  private boolean fetched;

  public Edge() {
    //Default constructor
  }

  public String srcId() {
    return srcId;
  }

  public String tgtId() {
    return tgtId;
  }

  public String propertyId() {
    return propertyId;
  }

  public String label() {
    return label;
  }

  public boolean fetched() {
    return fetched;
  }

  public void srcId(String srcId) {
    this.srcId = srcId;
  }

  public void tgtId(String tgtId) {
    this.tgtId = tgtId;
  }

  public void propertyId(String propertyId) {
    this.propertyId = propertyId;
  }

  public void label(String label) {
    this.label = label;
  }

  public void fetched(boolean fetched) {
    this.fetched = fetched;
  }
}
