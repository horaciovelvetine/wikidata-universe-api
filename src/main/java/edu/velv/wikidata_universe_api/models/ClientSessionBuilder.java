package edu.velv.wikidata_universe_api.models;

import java.util.Optional;

import edu.velv.wikidata_universe_api.errors.Err;
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

    Optional<Err> createLayoutTask = sesh.layout().initialize();
    if (createLayoutTask.isPresent()) {
      return Either.left(createLayoutTask.get());
    }

    sesh.graphset().vertices().forEach(v -> {
      v.setCoords(sesh.layout().apply(v));
    });

    //TODO: below...
    // find some way to lock the origin @ (0,0,0)
    // build a fetch data collection to log speed of fetching w/ timeout @ WikidataManager
    // establish and build out responses, moving towards client resp...
    return Either.right(sesh);
  }
}
