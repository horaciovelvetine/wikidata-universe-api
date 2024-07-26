package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;

import edu.velv.wikidata_universe_api.models.err.WikiverseError;
import edu.velv.wikidata_universe_api.models.utils.QueryParamSanitizer;
import edu.velv.wikidata_universe_api.models.wikidata.WikidataManager;
import io.vavr.control.Either;

public class ClientSession {
  private final String query;
  private final Dimension subjectDimensions;
  private final Graphset graphset;
  private final WikidataManager wikidata;

  private ClientSession(String query, String dimensions) {
    this.query = QueryParamSanitizer.sanitize(query);
    this.subjectDimensions = getDimensionsFromClient(dimensions);
    this.graphset = new Graphset();
    this.wikidata = new WikidataManager(this);
  }

  public static Either<WikiverseError, ClientSession> initialize(String query, String dimensions) {
    ClientSession sesh = new ClientSession(query, dimensions);
    sesh.wikidata.fetchInitSessionData();

    return Either.right(sesh);
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

  private Dimension getDimensionsFromClient(String dimensions) {
    String[] split = dimensions.split("x");
    int width = (int) Math.floor(Double.parseDouble(split[0]));
    int height = (int) Math.floor(Double.parseDouble(split[1]));
    return new Dimension(width, height);
  }
}
