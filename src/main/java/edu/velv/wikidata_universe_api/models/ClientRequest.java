package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;
import java.util.Optional;

import edu.velv.wikidata_universe_api.models.jung_ish.Graphset;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.models.jung_ish.FR3DLayout;
import edu.velv.wikidata_universe_api.models.wikidata.WikidataManager;
import edu.velv.wikidata_universe_api.utils.QueryParamSanitizer;
import io.vavr.control.Either;

public class ClientRequest {
  protected String query;
  protected Dimension subjectDimensions;
  protected Graphset graphset;
  protected WikidataManager wikidata;
  protected FR3DLayout layout;

  protected ClientRequest() {
  }

  public ClientRequest(String query) {
    this.query = QueryParamSanitizer.sanitize(query);
    this.subjectDimensions = null;
    this.graphset = new Graphset();
    this.layout = new FR3DLayout();
    this.wikidata = new WikidataManager(this);
  }

  public ClientRequest(RequestResponseBody payload, boolean resetQueue) {
    this.query = payload.query();
    this.subjectDimensions = payload.dimensions();
    this.graphset = new Graphset(payload.vertices(), payload.edges());
    this.layout = new FR3DLayout(graphset, payload.dimensions());
    // properties and queue
    this.wikidata = new WikidataManager(this);
    this.wikidataManager().addAllProperties(payload.properties());
    if (!resetQueue) {
      this.wikidataManager().populateQueueWithPayload(payload);
    }
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

  /**
  * Retrieves data related to the provided query and returns a filled out ClientSession object representing the results found for the query. 
  * 
  * @return Either an Err(or) or a ClientSession object containing the originating vertex of a client session. 
  */

  public Either<Err, ClientRequest> getInitialQueryData() {
    Optional<Err> initialFetchTask = wikidataManager().fetchInitQueryData();
    if (initialFetchTask.isPresent()) {
      return Either.left(initialFetchTask.get());
    }
    return Either.right(this);
  }

  /**
   * Retrieves related data about a graphset from the fetch queue after initial query. 
   * Method reassembles the ClientSession w/ provided payload, fetches entity data from
   * the queue, and reassembles that data to fill out N1 of a query. Then initializes a 3Dlayout of the fetched to provide the client for view.
   * 
   * @return Either an Err(or) or a ClientSession object containing the related entity data.
   */
  public Either<Err, ClientRequest> getInitialRelatedData() {
    Optional<Err> fetchRelatedDataTask = wikidataManager().fetchRelatedWithTimeout();
    if (fetchRelatedDataTask.isPresent()) {
      return Either.left(fetchRelatedDataTask.get());
    }

    Optional<Err> createLayoutTask = layout().initialize();
    if (createLayoutTask.isPresent()) {
      return Either.left(createLayoutTask.get());
    }

    finalizeLayoutPositions();
    setVertexPositions();

    return Either.right(this);
  }

  /**
   * Retrieves data about and related to a Vertex which is an already existing member of a Graphset.
   * Client provides the click target aliased as a new 'query' which is used to fetch the clicked Vertex's statements and provide them as additional details to the client. 
   * 
   * @return Either an Err(or) or a ClientRequest object containing the new related entity data. 
   */
  public Either<Err, ClientRequest> getClickRelatedData() {
    layout().lock(true); // lock existing vertices to their current coordinates
    Optional<Err> initialFetchTask = wikidataManager().fetchInitQueryData();
    if (initialFetchTask.isPresent()) {
      Either.left(initialFetchTask.get());
    }

    return getInitialRelatedData();
  }

  private Dimension getDimensionsFromClient(String dimensions) {
    String[] split = dimensions.split("x");
    int width = (int) Math.floor(Double.parseDouble(split[0]));
    int height = (int) Math.floor(Double.parseDouble(split[1]));
    return new Dimension(width, height);
  }

  private void finalizeLayoutPositions() {
    while (!layout().done()) {
      layout().step();
    }
  }

  private void setVertexPositions() {
    graphset().vertices().forEach(v -> {
      v.setCoords(layout().apply(v));
    });
  }

}
