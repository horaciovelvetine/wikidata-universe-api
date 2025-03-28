package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;
import java.util.Optional;

import io.vavr.control.Either;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.WikiverseServiceError.FR3DLayoutProcessError;
import edu.velv.wikidata_universe_api.interfaces.Loggable;
import edu.velv.wikidata_universe_api.services.wikidata.WikidataServiceManager;

public class ClientRequest implements Loggable {
  protected String query;
  protected Dimension dimensions;
  protected Graphset graph;
  protected FR3DLayout layout;
  protected LayoutConfig layoutConfig;

  private final WikidataServiceManager wikidata;

  public ClientRequest(WikidataServiceManager wd, String query) {
    this.query = this.sanitizeQueryString(query);
    this.dimensions = new Dimension(800, 600); //default size scaled on init of layout 
    this.graph = new Graphset();
    this.layoutConfig = new LayoutConfig();
    this.layout = new FR3DLayout(this);
    this.wikidata = wd;
  }

  public ClientRequest(WikidataServiceManager wd, RequestPayloadData payload) {
    this.query = payload.query();
    this.dimensions = payload.dimensions();
    this.graph = new Graphset(payload.vertices(), payload.edges(), payload.properties());
    this.layoutConfig = payload.layoutConfig();
    this.layout = new FR3DLayout(this);
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

  public LayoutConfig layoutConfig() {
    return layoutConfig;
  }

  /**
   * Fetches the closest matching ItemDocument from Wikidata's API by first searching by label
   * then searching for any matching text, and taking the best match. If no matching ItemDocument
   * exists, or there are problems with the Wikidata API returns an Optional<Err(or)>
   *
   * @return an error if one was encountered while carrying out the fetch request(s)
   */
  public Either<Err, RequestResponseBody> getInitialQueryData() {
    Optional<Err> fetchInitQueryTask = wikidata.fetchInitialQueryData(this);
    print("fetchInitialQueryData() ends.");
    return fetchInitQueryTask.isPresent() ? Either.left(fetchInitQueryTask.get())
        : Either.right(new RequestResponseBody(this));
  }

  /**
   * Fetches details for any Wikidata entity which has still not been completely fetched from the
   * Wikidata API. The data completedness of an entity will depend on it's type (e.x. date's originally
   * do not have their correleated QID).
   * 
   * @return an error if one was enountered while carrying out the fetches requests 
   */
  public Either<Err, RequestResponseBody> getUnfetchedData() {
    graph().getOriginVertex().lock();
    Optional<Err> fetchIncompleteDataTask = wikidata.fetchIncompleteData(this);
    print("fetchIncompleteData() ends");
    if (fetchIncompleteDataTask.isEmpty()) {
      fetchIncompleteDataTask = runFR3DLayoutProcess();
    }
    print("runLayoutProcess() ends");
    return fetchIncompleteDataTask.isPresent() ? Either.left(fetchIncompleteDataTask.get())
        : Either.right(new RequestResponseBody(this));
  }

  /**
   * Fetches details for the query target (which will have been replaced by)
   */
  public Either<Err, RequestResponseBody> getClickTargetData() {
    graph().lockAll(); // lock all vertices to their coordinates 
    Optional<Err> clickTargetTask = wikidata.fetchInitialQueryData(this);

    if (clickTargetTask.isPresent()) {
      return Either.left(clickTargetTask.get());
    }

    clickTargetTask = wikidata.fetchIncompleteData(this);

    if (clickTargetTask.isEmpty()) {
      clickTargetTask = runFR3DLayoutProcess();
    }

    return clickTargetTask.isPresent() ? Either.left(clickTargetTask.get())
        : Either.right(new RequestResponseBody(this));
  }

  /**
   * Locks the origin and re-runs the layout.step() algo
   */
  public Either<Err, RequestResponseBody> refreshLayoutPositions() {
    graph().unlockAll();
    Optional<Err> refreshLayoutTask = runFR3DLayoutProcess();

    return refreshLayoutTask.isPresent() ? Either.left(refreshLayoutTask.get())
        : Either.right(new RequestResponseBody(this));
  }

  /**
   * Helper to call the layout process in its entirety and build a layout for the Request
   */
  public Optional<Err> runFR3DLayoutProcess() {
    try {
      layout.initializeLayout(this.layoutConfig);
      while (!layout.done()) {
        layout.step();
      }
      graph().updateVertexCoordinatesFromLayout(layout());
      return Optional.empty();
    } catch (Exception e) {
      return Optional.of(new FR3DLayoutProcessError("runFR3DLayoutProcessFailed", e));
    }
  }

  /**
  * Removes portential garbage from the provided query value recieved as a query param from the client.
  */
  private String sanitizeQueryString(String query) {
    if (query == null || query.isBlank()) {
      return null;
    }
    return query.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
  }

}
