package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;

import edu.velv.wikidata_universe_api.models.jung_ish.Graphset;
import edu.velv.wikidata_universe_api.models.jung_ish.FR3DLayout;
import edu.velv.wikidata_universe_api.models.wikidata.WikidataManager;
import edu.velv.wikidata_universe_api.utils.QueryParamSanitizer;

public class ClientSession {
  protected String query;
  protected Dimension subjectDimensions;
  protected Graphset graphset;
  protected WikidataManager wikidata;
  protected FR3DLayout layout;

  protected ClientSession() {
  }

  public ClientSession(String query, String dimensions) {
    this.query = QueryParamSanitizer.sanitize(query);
    this.subjectDimensions = getDimensionsFromClient(dimensions);
    this.graphset = new Graphset();
    this.layout = new FR3DLayout(graphset, getDimensionsFromClient(dimensions));
    this.wikidata = new WikidataManager(this);
  }

  public ClientSession(ResponseBody payload) {
    this.query = payload.query();
    this.subjectDimensions = payload.dimensions();
    this.graphset = new Graphset(payload.vertices(), payload.edges());
    this.layout = new FR3DLayout(graphset, payload.dimensions());
    // properties and queue
    this.wikidata = new WikidataManager(this);
    this.wikidataManager().addAllProperties(payload.properties());
    this.wikidataManager().populateQueueWithPayload(payload);

  }

  public Graphset graphset() {
    return this.graphset;
  }

  public Dimension subjectDimensions() {
    return this.subjectDimensions;
  }

  public String query() {
    return this.query;
  }

  public WikidataManager wikidataManager() {
    return this.wikidata;
  }

  public FR3DLayout layout() {
    return this.layout;
  }

  public String details() {
    String br = "\n";
    return br + this.toString() + br + graphset.toString() + br + wikidata.toString();
  }

  @Override
  public String toString() {
    return "Session={ query=" + query + ", dimensions=" + subjectDimensions.width + "x" + subjectDimensions.height
        + " }";
  }

  private Dimension getDimensionsFromClient(String dimensions) {
    String[] split = dimensions.split("x");
    int width = (int) Math.floor(Double.parseDouble(split[0]));
    int height = (int) Math.floor(Double.parseDouble(split[1]));
    return new Dimension(width, height);
  }

}
