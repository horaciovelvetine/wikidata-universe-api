package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;
import java.util.Optional;

import io.vavr.control.Either;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.services.FR3DConfig;
import edu.velv.wikidata_universe_api.services.Printable;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

public class ClientRequest implements Printable {
  protected String query;
  protected Dimension dimensions;
  protected Graphset graph;
  protected FR3DLayout layout;

  private final WikidataServiceManager wikidata;

  public ClientRequest(WikidataServiceManager wd, FR3DConfig config, String query) {
    this.query = this.sanitizeQueryString(query);
    this.dimensions = new Dimension(800, 600); // init w/ a default size, which will be scaled 
    this.graph = new Graphset();
    this.layout = new FR3DLayout(this, config);
    this.wikidata = wd;
  }

  public ClientRequest(WikidataServiceManager wd, FR3DConfig config, RequestPayloadData payload) {
    this.query = payload.query();
    this.dimensions = payload.dimensions();
    this.graph = new Graphset(payload.vertices(), payload.edges(), payload.properties());
    this.layout = new FR3DLayout(this, config);
    this.wikidata = wd;
  }

  public String query() {
    return query;
  }

  public Dimension dimensions() {
    return dimensions;
  }

  /**
   * @apiNote used in the layout to scale these by the total number of Vertices
   */
  public void dimensions(Dimension dim) {
    this.dimensions = dim;
  }

  public Graphset graph() {
    return graph;
  }

  public FR3DLayout layout() {
    return layout;
  }

  /**
   * Fetches the closest matching ItemDocument from Wikidata's API by first searching by label
   * then searching for any matching text, and taking the best match. If no matching ItemDocument
   * exists, or there are problems with the Wikidata API returns an Optional<Err(or)>
   *
   * @return an error if one was encountered while carrying out the fetch request(s)
   */
  public Either<Err, ClientRequest> getInitialQueryData() {
    Optional<Err> fetchInitQueryTask = wikidata.fetchInitialQueryData(this);
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
    graph().getOriginVertex().lock();
    Optional<Err> fetchIncompleteDataTask = wikidata.fetchIncompleteData(this);

    layout().scaleDimensionsToGraphsetSize(); // scale based on number of verts
    layout().initializeRandomPositions(); // initialize positions for new vertices
    layout().initializeLayoutConstants(); // initialize forces 

    while (!layout().done()) {
      layout().step();
    }

    graph().updateVertexCoordinatesFromLayout(layout());
    return fetchIncompleteDataTask.isPresent() ? Either.left(fetchIncompleteDataTask.get()) : Either.right(this);
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
