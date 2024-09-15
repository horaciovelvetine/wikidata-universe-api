package edu.velv.wikidata_universe_api;

import java.awt.Dimension;
import java.util.Optional;

import io.vavr.control.Either;

import org.springframework.beans.factory.annotation.Autowired;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.models.Graphset;
import edu.velv.wikidata_universe_api.models.FR3DLayout;
import edu.velv.wikidata_universe_api.models.RequestPayloadData;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

public class ClientRequest {
  @Autowired
  protected WikidataServiceManager wikidata;
  protected String query;
  protected Dimension dimensions;
  protected Graphset graph;
  protected FR3DLayout layout;

  public ClientRequest(String query) {
    this.query = this.sanitizeQueryString(query);
    this.graph = new Graphset();
  }

  public ClientRequest(RequestPayloadData payload) {
    this.query = payload.query();
    this.dimensions = payload.dimensions();
    this.graph = new Graphset(payload.vertices(), payload.edges(), payload.properties());
    this.layout = new FR3DLayout(this.dimensions(), this.graph());
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
    Optional<Err> fetchIncompleteDataTask = wikidata.fetchIncompleteDataTask(this);

    return Either.right(this);
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
