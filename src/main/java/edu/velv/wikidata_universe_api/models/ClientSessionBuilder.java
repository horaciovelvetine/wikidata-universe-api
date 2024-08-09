package edu.velv.wikidata_universe_api.models;

import java.util.Optional;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.utils.SerializeData;
import io.vavr.control.Either;

public class ClientSessionBuilder {

  public static Either<Err, ClientSession> getInitialQueryData(String query) {
    ClientSession sesh = new ClientSession(query, "0x0");
    Optional<Err> initialDataTask = sesh.wikidataManager().fetchInitQueryData();
    if (initialDataTask.isPresent()) {
      return Either.left(initialDataTask.get());
    }
    // Creates and puts .json version of response in dir for use
    SerializeData.ResponseBody(new ResponseBody(sesh));
    return Either.right(sesh);
  }

  public static Either<Err, ClientSession> getRelatedEntityData(String query) {
    return null;
  }

  //dimesions start as '1x1' string format
  public static Either<Err, ClientSession> layoutGraphData(String dimensions) {
    return null;
  }

  //Private...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  private static Either<Err, ClientSession> initialize(String query, String dimensions) {
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
    while (!sesh.layout().done()) {
      try {
        sesh.layout().step();
      } catch (Exception e) {
        System.out.println("Step exception catch");
      }
    }
    sesh.graphset().vertices().forEach(v -> {
      v.setCoords(sesh.layout().apply(v));
    });

    //END 2
    //=====================================================================================================================>
    //=====================================================================================================================>
    //=====================================================================================================================>

    return Either.right(sesh);
  }
}
