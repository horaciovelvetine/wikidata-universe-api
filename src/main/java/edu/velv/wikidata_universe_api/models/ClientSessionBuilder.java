package edu.velv.wikidata_universe_api.models;

import java.util.Optional;

import edu.velv.wikidata_universe_api.err.Err;
import io.vavr.control.Either;

public class ClientSessionBuilder {
  public static Either<Err, ClientSession> initialize(String query, String dimensions) {
    ClientSession sesh = new ClientSession(query, dimensions);
    Optional<Err> fetchInitQueryTask = sesh.wikidataManager().fetchInitQueryData();
    if (fetchInitQueryTask.isPresent()) {
      return Either.left(fetchInitQueryTask.get());
    }

    Optional<Err> fetchRelatedDataTask = sesh.wikidataManager().fetchRelatedWithTimeout();

    if (fetchRelatedDataTask.isPresent()) {
      return Either.left(fetchRelatedDataTask.get());
    }

    //TODO: below...
    // * initialize layout coords for set
    // * create a response from a pruned client session

    return Either.right(sesh);
  }
}
