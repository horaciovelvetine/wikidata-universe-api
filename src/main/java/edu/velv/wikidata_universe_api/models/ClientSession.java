package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;

import edu.velv.wikidata_universe_api.models.wikidata.WikidataManager;

public class ClientSession {
  private final String originalQuery;
  private final Dimension subjectDimensions;
  private final Graphset graphset;
  private final WikidataManager wikidata;

  public ClientSession(String originalQuery, String dimensions) {
    this.originalQuery = originalQuery;
    this.subjectDimensions = getDimensionsFromClient(dimensions);
    this.graphset = new Graphset();
    this.wikidata = new WikidataManager(this);
    wikidata.fetchInitSessionData(originalQuery);
  }

  public Graphset graphset() {
    return this.graphset;
  }

  public Dimension subjectDimensions() {
    return this.subjectDimensions;
  }

  public String originalQuery() {
    return this.originalQuery;
  }

  public WikidataManager wikidataManager() {
    return this.wikidata;
  }

  private Dimension getDimensionsFromClient(String dimensions) {
    String[] split = dimensions.split("x");
    int width = (int) Math.floor(Double.parseDouble(split[0]));
    int height = (int) Math.floor(Double.parseDouble(split[1]));
    return new Dimension(width, height);
  }
}