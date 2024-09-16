package edu.velv.wikidata_universe_api.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vavr.CheckedFunction0;
import io.vavr.control.Either;
import io.vavr.control.Try;

import org.springframework.stereotype.Service;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

import edu.velv.wikidata_universe_api.Constables;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.NoSuchEntityFoundError;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.ApiUnavailableError;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError;

@Service
public class FetchBroker {
  private final WikibaseDataFetcher fetcher;

  public FetchBroker() {
    this.fetcher = WikibaseDataFetcher.getWikidataDataFetcher();
  }

  /**
   * Retrieves a matching EntityDocument by first searching for an Entity with a matching label,
   * then expanding to search across all attributes with more leinent match criteria. Provides handling
   * and Err(or)s in the case(s) that the Wikidata API is offline or No Such (matching) Record can be found
   * 
   * @param query
   * @return a matching EntityDocument or encountered Err(or)
   */
  protected Either<Err, EntityDocument> fetchEntityByAnyQueryMatch(String query) {
    return fetchEntityByTitleMatch(query).fold((Err err) -> {
      return handleNoTitleMatchResults(err, query);
    }, (EntityDocument doc) -> {
      return Either.right(doc);
    });
  }

  /**
   * Retrieves a list of matching EntityDocument's by searching for each provided Id value. Provides handling
   * to insert an Err(or) record when the Wikidata API returns a no-match {null} result for an id value
   * 
   * @param List<String> queryIds
   * @return A list of EntityDocuments mapped by their associated query values, or an encountered Err(or)
   */
  protected Either<Err, Map<String, Either<Err, EntityDocument>>> fetchEntitiesByIdList(List<String> queryIds) {
    Map<String, Either<Err, EntityDocument>> results = new HashMap<>();
    return fetchEntitiesByIds(queryIds).fold(err -> {
      return Either.left(err);
    }, response -> {
      response.entrySet().forEach(ent -> {
        results.put(ent.getKey(), handleNoSuchEntityResults(ent.getValue()));
      });
      return Either.right(results);
    });
  }

  /**
   * Individually searches for Dates using the formatted labels one at a time to build a set of date results. 
   * @apiNote This method sends individual requests for every date target slightly changing the pattern above
   * todo - test if there were a way to have this make use of the title fetch list 
   * 
   * @param List<String> dateTgts
   * @return a list of SearchResults mapped to their query value, or an Err(or) if one was encountered
   */
  protected Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> fetchEntitiesByDateList(
      List<String> dateBatch) {
    Map<String, Either<Err, WbSearchEntitiesResult>> results = new HashMap<>();
    for (String dateTgt : dateBatch) {
      Either<Err, WbSearchEntitiesResult> fetchRes = fetchSearchResultsByAnyMatch(dateTgt);
      if (fetchRes.isLeft() && fetchRes.getLeft().getClass().equals(ApiUnavailableError.class)) {
        // prevent spamming requests if Wikidata offline
        return Either.left(fetchRes.getLeft());
      }
      results.put(dateTgt, fetchRes);
    }
    return Either.right(results);
  }

  //? PRIVATE...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  /**
   * Fetches a list of EntityDocument's by searching the Wikidata API for the provided Entity values. 
   * Provides wrapped handling  in case the Wikidata API is offline or otherwise unavailable
   * 
   * @param List<String> queryIds
   * @return a list of EntityDocuments mapped by their associated query values, or an encountered Err(or)
   */
  private Either<Err, Map<String, EntityDocument>> fetchEntitiesByIds(List<String> queryIds) {
    return fetchWithApiUnavailableErrorHandler(() -> fetcher.getEntityDocuments(queryIds));
  }

  /**
   * Provides a wrapper to catch a failed title query, to try again (if the API is available)
   * widening the match criteria to look for Entities where any attributes might be a match
   * 
   * @param Err the error thrown by the predecessor to qualify further fetches
   * @param query
   * @return a matching EntityDocument or encountered Err(or)
   */
  private Either<Err, EntityDocument> handleNoTitleMatchResults(Err err, String query) {
    if (err instanceof NoSuchEntityFoundError) {
      return fetchEntityByAnyMatch(query);
    }
    return Either.left(err); // bubble non-matching errors up
  }

  /**
   * Attempts to find a matching SearchResult by matching any attribute to the provided query.
   * The best search result is used to then find the parent Entity Document (which contains the 
   * required relationship information used in the edge creation process).
   * 
   * @param query
   * @return a matching EntityDocument or encountered Err(or) 
   */
  private Either<Err, EntityDocument> fetchEntityByAnyMatch(String query) {
    return fetchSearchResultsByAnyMatch(query).fold((Err e) -> {
      return Either.left(e);
    }, (WbSearchEntitiesResult res) -> {
      return fetchEntityByIdMatch(res.getEntityId());
    });
  }

  /**
   * Attempts to find a matching Entity using the provided Wikimedia data model specific EID pattern
   * 
   * @param id
   * @return a matching EntityDocument or encountered Err(or)
   */
  private Either<Err, EntityDocument> fetchEntityByIdMatch(String id) {
    return fetchWithApiUnavailableErrorHandler(() -> fetcher.getEntityDocument(id))
        .flatMap(this::handleNoSuchEntityResults);
  }

  /**
   * Attempts to find any matching Entity which in some way matches the provided query
   * 
   * @param query 
   * @return a matching SearchResult or encountered Err(or) 
   */
  private Either<Err, WbSearchEntitiesResult> fetchSearchResultsByAnyMatch(String query) {
    return fetchWithApiUnavailableErrorHandler(() -> fetcher.searchEntities(query, Constables.EN_LANG_WIKI_KEY))
        .flatMap(this::handleSearchedEntitiesResults);
  }

  /**
   * Validates and returns the closest matching search result if it exists, else returns the appropriately instantiated Err(or)
   *
   * @param List<WbSearchEntitiesResult> results from the WikidataAPI.searchEntities() method
   * @return Search result or a NoSuchEntityFoundError
   */
  private Either<Err, WbSearchEntitiesResult> handleSearchedEntitiesResults(List<WbSearchEntitiesResult> results) {
    return results.isEmpty()
        ? Either.left(new NoSuchEntityFoundError(
            "@handleSearchedEntitiesResults() was unable to find any matching SearchResults"))
        : Either.right(results.get(0));
  }

  /**
   * Attempts to find a matching Entity by looking for a matching title. Provides handling for an offline API and 
   * if no such record can be found by the Wikidata API. 
   * 
   * @param query 
   * @return An EntityDocument match or an encountered Err(or)
   */
  private Either<Err, EntityDocument> fetchEntityByTitleMatch(String query) {
    return fetchWithApiUnavailableErrorHandler(() -> fetcher.getEntityDocumentByTitle(Constables.EN_WIKI_IRI, query))
        .flatMap(this::handleNoSuchEntityResults);
  }

  /**
   * Wraps the provided fetch request (made to the Wikidata API) inside a handler which catches and bubbles up
   * the correctly instantiated ApiUnavailableError record to the original fetch supplier method.
   * 
   * @return expected results of the fetchSupplier or ApiUnavailableError when the Wikidata API appears to be offline
   */
  private <T> Either<Err, T> fetchWithApiUnavailableErrorHandler(CheckedFunction0<T> fetchSupplier) {
    return Try.of(fetchSupplier)
        .toEither()
        .mapLeft(err -> new WikidataServiceError.ApiUnavailableError(err));
  }

  /**
   * Checks the provided result from the Wikidata API for null
   * 
   * @param WbSearchEntitiesResult response
   * @return return a result match, else returns a NoSuchEntityFoundError
   */
  private Either<Err, WbSearchEntitiesResult> handleNoSuchEntityResults(WbSearchEntitiesResult res) {
    return res == null
        ? Either.left(
            new NoSuchEntityFoundError("@handleNoSuchEntitiesResult() was unable to find a matching SearchResult"))
        : Either.right(res);
  }

  /**
   * Checks the provided result from the Wikidata API for null
   * 
   * @param EntityDocument response
   * @return return a result match, else returns a NoSuchEntityFoundError
   */
  private Either<Err, EntityDocument> handleNoSuchEntityResults(EntityDocument res) {
    return res == null
        ? Either.left(new NoSuchEntityFoundError("@handleNoSuchEntitiesResult() was unable to find a matching Entity"))
        : Either.right(res);
  }

}
