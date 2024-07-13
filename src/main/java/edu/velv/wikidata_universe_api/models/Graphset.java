package edu.velv.wikidata_universe_api.models;

public class Graphset {
  private String originalQuery;

  public Graphset(String originalQuery) {
    this.originalQuery = originalQuery;
  }

  public String originalQuery() {
    return originalQuery;
  }
}
