package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;
import java.util.Optional;

import edu.velv.wikidata_universe_api.models.jung_ish.Graphset;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.models.jung_ish.FR3DLayout;
import edu.velv.wikidata_universe_api.models.wikidata.WikidataManager;
import edu.velv.wikidata_universe_api.utils.QueryParamSanitizer;
import edu.velv.wikidata_universe_api.utils.SerializeData;
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

  public ClientRequest(RequestPayload payload, boolean resetQueued) {
    this.query = payload.query();
    this.subjectDimensions = payload.dimensions();
    this.graphset = new Graphset(payload.vertices(), payload.edges());
    this.layout = new FR3DLayout(graphset, subjectDimensions);
    // contextually populates from prev reqeust response
    this.wikidata = new WikidataManager(this);
    if (!resetQueued) {
      this.wikidataManager().populateQueueWithPayload(payload);
    }
    this.wikidataManager().addAllProperties(payload.properties());
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
    layout().lock(true); // lock initial (origin) @ (0,0,0)
    // TODO This is predominently a timeout response, however on a timeout it should still lay itself out
    wikidataManager().fetchRelatedWithTimeout();

    layout().initialize();
    finalizeLayoutPositions();
    setVertexPositions();

    SerializeData.ResponseBody(new ResponseBody(this));

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

    SerializeData.ResponseBody(new ResponseBody(this));

    return getInitialRelatedData();
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
