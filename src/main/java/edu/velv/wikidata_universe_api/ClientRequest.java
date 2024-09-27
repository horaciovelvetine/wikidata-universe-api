package edu.velv.wikidata_universe_api;

import java.awt.Dimension;
import java.util.Optional;

import io.vavr.control.Either;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.models.Graphset;
import edu.velv.wikidata_universe_api.models.FR3DLayout;
import edu.velv.wikidata_universe_api.models.RequestPayloadData;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

public class ClientRequest {
  protected String query;
  protected Dimension dimensions;
  protected Graphset graph;
  protected FR3DLayout layout;

  private final WikidataServiceManager wikidata;

  public ClientRequest(WikidataServiceManager wd, String query) {
    this.query = this.sanitizeQueryString(query);
    this.dimensions = new Dimension();
    this.graph = new Graphset();
    this.layout = new FR3DLayout(dimensions, graph);
    this.wikidata = wd;
  }

  public ClientRequest(WikidataServiceManager wd, RequestPayloadData payload) {
    this.query = payload.query();
    this.dimensions = payload.dimensions();
    this.graph = new Graphset(payload.vertices(), payload.edges(), payload.properties());
    this.layout = new FR3DLayout(this.dimensions(), this.graph());
    this.wikidata = wd;
  }

  public String query() {
    return query;
  }

  public Dimension dimensions() {
    return dimensions;
  }

  public Graphset graph() {
    return graph;
  }

  /**
   * Fetches the closest matching ItemDocument from Wikidata's API by first searching by label
   * then searching for any matching text, and taking the best match. If no matching ItemDocument
   * exists, or there are problems with the Wikidata API returns an Optional<Err(or)>
   *
   * @return an error if one was encountered while carrying out the fetch request(s)
   */
  public Either<Err, ClientRequest> getInitialQueryData() {
    Optional<Err> fetchInitQueryTask = wikidata.fetchInitQueryDataTask(this);

    return fetchInitQueryTask.isPresent() ? Either.left(fetchInitQueryTask.get()) : Either.right(this);
  }

  /**
   * Fetches details for any Wikidata entity which has still not been completely fetched from the
   * Wikidata API. The data completedness of an entity will depend on it's type (e.x. date's originally
   * do not have their correleated QID).
   * 
   * @return an error if one was enountered while carrying out the fetches requests 
   */
  public Either<Err, ClientRequest> getUnfetchedData() {
    layout.lock(this.graph().getOriginVertex(), true);

    Optional<Err> fetchIncompleteDataTask = wikidata.fetchIncompleteDataTask(this);
    runLayoutAlgoProcess();
    return fetchIncompleteDataTask.isPresent() ? Either.left(fetchIncompleteDataTask.get()) : Either.right(this);
  }

  /**
   * Initializes, and steps the Layout process to provide vertices with updates layout positions - then updates
   * Graphset so that each Vertex is aware of its update Point3D coords.
   */
  private void runLayoutAlgoProcess() {
    layout.initialize();
    while (!layout.done()) {
      layout.step();
    }
    for (Vertex vert : graph.vertices()) {
      vert.coords(layout.apply(vert));
    }
  }

  /**
  * Removes portential garbage from the provided query value recieved as a query param from the client.
  */
  private String sanitizeQueryString(String query) {
    if (query == null || query.isBlank()) {
      return null;
    }
    return query.replaceAll("[^\\w\\s]", "").trim();
  }
}
