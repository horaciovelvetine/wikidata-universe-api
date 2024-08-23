package edu.velv.wikidata_universe_api.models.wikidata;

// import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import io.vavr.CheckedFunction0;
import io.vavr.control.Either;
import io.vavr.control.Try;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.WikidataServiceError.*;
import edu.velv.wikidata_universe_api.utils.Loggable;

public class FetchBroker implements Loggable {
  private static final String EN_WIKI_IRI = "enwiki";
  private static final String EN_LANG_WIKI = "en";
  private final WikibaseDataFetcher fetcher;

  public FetchBroker() {
    this.fetcher = WikibaseDataFetcher.getWikidataDataFetcher();
  }

  /**
   * Retrieves the EntityDocument which best matches the original query by first trying a title search,
   * then expanding to search across all entities in the Wikidata database, finally returning a default
   * if unable to find a good match for the provided query.
   *
   * @param query the query to search for the origin entity
   * @return an Either object containing either the retrieved EntityDocument or an encountered error
   */

  protected Either<Err, EntityDocument> fetchOriginEntityByAny(String query) {
    return fetchEntityDocumentByTitle(query).fold((Err err) -> {
      return handleNoTitleResults(err, query);
    }, (EntityDocument doc) -> {
      return Either.right(doc);
    });
  }

  /**
   * Fetches a list of EntityDocuments which match the provided list of tgtId queries.
   * 
   * @param tgtIds the List of IdValue's to search for
   * @return an Either object containing either the retrrieved Documents or an error
   */
  protected Either<Err, Map<String, Either<Err, EntityDocument>>> fetchEntitiesByIdList(List<String> tgtIds) {
    Map<String, Either<Err, EntityDocument>> results = new HashMap<>();
    return fetchEntityDocumentsByIds(tgtIds).mapLeft(e -> {
      return e;
    }).flatMap(fetched -> {
      fetched.entrySet().forEach((entry) -> {
        results.put(entry.getKey(), handleNoSuchEntityResults(entry.getValue()));
      });
      return Either.right(results);
    });
  }

  /**
   * Fetches a list of EntityDocuments which match the provided list of tgtDates.
   * 
   * @param tgtDates the List of IdValue's to search for
   * @return an Either object containing either the retrrieved Documents or an error
   */
  protected Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> fetchEntitiesByDateList(
      List<String> tgtDates) {
    Map<String, Either<Err, WbSearchEntitiesResult>> results = new HashMap<>();
    for (String tgtDate : tgtDates) {
      fetchSearchResultsByAny(tgtDate).mapLeft(e -> {
        return e;
      }).flatMap(result -> {
        results.put(tgtDate, handleNoSuchEntityResults(result));
        return null;
      });
    }
    return Either.right(results);
  }

  // HANDLER(S)
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  private Either<Err, EntityDocument> handleNoSuchEntityResults(EntityDocument doc) {
    return doc == null ? Either.left(new NoSuchEntityFoundError("@handleNoSuchEntityFoundID()"))
        : Either.right(doc);
  }

  private Either<Err, WbSearchEntitiesResult> handleNoSuchEntityResults(WbSearchEntitiesResult res) {
    return res == null ? Either.left(new NoSuchEntityFoundError("@handleNoSuchEntityFoundID()"))
        : Either.right(res);
  }

  private Either<Err, WbSearchEntitiesResult> handleSearchedEntitiesResults(
      List<WbSearchEntitiesResult> results) {
    return results.isEmpty()
        ? Either.left(new NoSuchEntityFoundError("@handleSearchedEntities()"))
        : Either.right(results.get(0));
  }

  private <T> Either<Err, T> fetchWithApiUnavailableHandler(CheckedFunction0<T> supplier) {
    return Try.of(supplier)
        .toEither()
        .mapLeft(e -> new ApiUnavailableError(e.getMessage(), e));
  }

  private Either<Err, EntityDocument> handleNoTitleResults(Err err, String query) {
    if (err instanceof NoSuchEntityFoundError) {
      return fetchQueryByAnyMatch(query); // Widens search to all entities
    }
    return Either.left(err);
  }

  // WDTK Fetches 
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  // BY TITLE...

  private Either<Err, EntityDocument> fetchEntityDocumentByTitle(String title) {
    return fetchWithApiUnavailableHandler(() -> fetcher.getEntityDocumentByTitle(EN_WIKI_IRI, title))
        .flatMap(this::handleNoSuchEntityResults);
  }

  // BY ID...

  private Either<Err, EntityDocument> fetchEntityDocumentById(String id) {
    return fetchWithApiUnavailableHandler(() -> fetcher.getEntityDocument(id))
        .flatMap(this::handleNoSuchEntityResults);
  }

  // BY IDS...

  private Either<Err, Map<String, EntityDocument>> fetchEntityDocumentsByIds(List<String> tgtIds) {
    return fetchWithApiUnavailableHandler(() -> fetcher.getEntityDocuments(tgtIds));
  }

  // BY ANY...

  private Either<Err, WbSearchEntitiesResult> fetchSearchResultsByAny(String query) {
    return fetchWithApiUnavailableHandler(() -> fetcher.searchEntities(query, EN_LANG_WIKI))
        .flatMap(this::handleSearchedEntitiesResults);
  }
  // BY ANY

  private Either<Err, EntityDocument> fetchQueryByAnyMatch(String query) {
    return fetchSearchResultsByAny(query).fold((Err err) -> {
      return Either.left(err);
    }, (WbSearchEntitiesResult result) -> {
      return fetchEntityDocumentById(result.getEntityId());
    });
  }
}