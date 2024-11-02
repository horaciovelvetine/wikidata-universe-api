package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;
import io.vavr.control.Either;

public class AboutRequest {
  private String stage;
  protected Dimension dimensions;
  protected Graphset graph;
  protected LayoutConfig layoutConfig = new LayoutConfig();

  private final WikidataServiceManager wikidata;

  public AboutRequest(WikidataServiceManager wd) {
    this.stage = "init";
    this.dimensions = new Dimension(1600, 1200);
    this.graph = initAboutGraphset();
    this.wikidata = wd;
  }

  public String stage() {
    return stage;
  }

  public Dimension dimensions() {
    return dimensions;
  }

  public Graphset graph() {
    return graph;
  }

  public LayoutConfig layoutConfig() {
    return layoutConfig;
  }

  public Either<Err, RequestResponseBody> getStage(String tgtStage) {
    return Either.right(new RequestResponseBody(this));
  }

  /**
   * Provide the initial Vertex for the first Request from the client regarding what
   */
  private Graphset initAboutGraphset() {
    Graphset abGraph = new Graphset();
    Vertex startVert = new Vertex();
    startVert.setAsOrigin();
    startVert.id("Q1");
    startVert.label("Vertex");
    startVert.description(
        "A visual representation of any Item inside of Wikidata, positioned inside the Wikiverse using [X,Y,Z] coordinates.");
    startVert.fetched(true);

    abGraph.addVertex(startVert);

    return abGraph;
  }

}
