package edu.velv.wikidata_universe_api.models;

import java.util.Optional;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.utils.SerializeData;
import io.vavr.control.Either;

public class ClientSessionBuilder {

  /**
   * Retrieves data related to the provided query and returns a filled out ClientSession object representing the results found for the query. 
   * 
   * @param query The search query value
   * @param dimensions The size of the sketch from the client window
   * @return Either an error or a ClientSession object containing the originating 
   * vertex of a client session. 
   */
  public static Either<Err, ClientSession> getInitialQueryData(String query, String dimensions) {
    ClientSession sesh = new ClientSession(query, dimensions);
    Optional<Err> initialDataTask = sesh.wikidataManager().fetchInitQueryData();

    if (initialDataTask.isPresent()) {
      return Either.left(initialDataTask.get());
    }

    // SerializeData.ResponseBody(new ResponseBody(sesh));
    return Either.right(sesh);
  }

  /**
   * Retrieves related data about a graphset from the fetch queue after initial query. 
   * Method reassembles the ClientSession w/ provided payload, fetches entity data from
   * the queue, and reassembles that data to fill out N1 of a query. Then initializes a 
   * 3Dlayout of the fetched to provide the client for view.
   * 
   * @param payload The response body containing the data.
   * @return Either an error or a ClientSession object containing the related entity data.
   */
  public static Either<Err, ClientSession> getRelatedQueuedData(ResponseBody payload) {
    ClientSession sesh = new ClientSession(payload);
    Optional<Err> fetchRelatedDataTask = sesh.wikidataManager().fetchRelatedWithTimeout();

    if (fetchRelatedDataTask.isPresent()) {
      return Either.left(fetchRelatedDataTask.get());
    }

    Optional<Err> createLayoutTask = sesh.layout().initialize();
    if (createLayoutTask.isPresent()) {
      return Either.left(createLayoutTask.get());
    }

    while (!sesh.layout().done()) {
      sesh.layout().step();
    }
    sesh.graphset().vertices().forEach(v -> {
      v.setCoords(sesh.layout().apply(v));
    });

    // CREATE JSON OF RESPONSE AND PUT IN LOGS FOR USE
    // SerializeData.ResponseBody(new ResponseBody(sesh));
    return Either.right(sesh);
  }

  /**
   * Does the same but for a click event
   */
  public static Either<Err, ClientSession> getRealatedClickData(ResponseBody payload) {
    ClientSession sesh = new ClientSession(payload);

    sesh.layout.lock(true); //TODO: not effectively locking legacy values in place
    Optional<Err> queueRelatedDataTask = sesh.wikidataManager().fetchInitQueryData();

    if (queueRelatedDataTask.isPresent()) {
      return Either.left(queueRelatedDataTask.get());
    }

    Optional<Err> fetchRelatedDataTask = sesh.wikidataManager().fetchRelatedWithTimeout();
    if (fetchRelatedDataTask.isPresent()) {
      return Either.left(fetchRelatedDataTask.get());
    }

    Optional<Err> createLayoutTask = sesh.layout().initialize();
    if (createLayoutTask.isPresent()) {
      return Either.left(createLayoutTask.get());
    }

    while (!sesh.layout().done()) {
      sesh.layout().step();
    }
    sesh.graphset().vertices().forEach(v -> {
      v.setCoords(sesh.layout().apply(v));
    });

    // CREATE JSON OF RESPONSE AND PUT IN LOGS FOR USE
    SerializeData.ResponseBody(new ResponseBody(sesh));
    return Either.right(sesh);
  }
}
