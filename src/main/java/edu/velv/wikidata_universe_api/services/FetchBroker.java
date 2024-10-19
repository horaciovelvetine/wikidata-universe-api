package edu.velv.wikidata_universe_api.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vavr.CheckedFunction0;
import io.vavr.control.Either;
import io.vavr.control.Try;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.NoSuchEntityFoundError;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.ApiUnavailableError;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError;

public class FetchBroker {
  private final WikibaseDataFetcher fetcher;

  @Value("${edu.velv.WikiData.en_wiki_iri}")
  private String wikiIri;

  @Value("${edu.velv.WikiData.en_lang_wiki_key}")
  private String wikiLangKey;

  @Autowired
  public FetchBroker() {
    this(WikibaseDataFetcher.getWikidataDataFetcher());
  }

  public FetchBroker(WikibaseDataFetcher fetcher) {
    this.fetcher = fetcher; // provides an injectable constructor for non-IT testing
  }

  public String iri() {
    return this.wikiIri;
  }

  public String enLangKey() {
    return this.wikiLangKey;
  }

  /**
   * Retrieves a matching EntityDocument by using the provided QID value. Provides handling and Err(or)s in the 
   * case that the Wikidata API is offline or no such record can be found. 
   * 
   * @return a matching EntityDocument or encountered Err(or)
   */
  protected Either<Err, EntityDocument> fetchTargetEntityById(String QID) {
    return fetchEntityByIdMatch(QID).fold((Err err) -> {
      return Either.left(err);
    }, (EntityDocument doc) -> {
      return handleNoSuchEntityResults(doc, QID);
    });
  }

  /**
   * Retrieves a matching EntityDocument by first searching for an Entity with a matching label,
   * then expanding to search across all attributes with more leinent match criteria. Provides handling
   * and Err(or)s in the case(s) that the Wikidata API is offline or No Such (matching) Record can be found
   * 
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
   * @return A list of EntityDocuments mapped by their associated query values, or an encountered Err(or)
   */
  protected Either<Err, Map<String, Either<Err, EntityDocument>>> fetchEntitiesByIdList(List<String> queryIds) {
    Map<String, Either<Err, EntityDocument>> results = new HashMap<>();
    return fetchEntitiesByIds(queryIds).fold(err -> {
      return Either.left(err);
    }, response -> {
      response.entrySet().forEach(ent -> {
        results.put(ent.getKey(), handleNoSuchEntityResults(ent.getValue(), ent.getKey()));
      });
      return Either.right(results);
    });
  }

  /**
   * Individually searches for Dates using the formatted labels one at a time to build a set of date results. 
   * @apiNote This method sends individual requests for every date target slightly changing the pattern above
   * 
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
   * @return a list of EntityDocuments mapped by their associated query values, or an encountered Err(or)
   */
  private Either<Err, Map<String, EntityDocument>> fetchEntitiesByIds(List<String> queryIds) {
    return fetchWithApiUnavailableErrorHandler(() -> fetcher.getEntityDocuments(queryIds));
  }

  /**
   * Provides a wrapper to catch a failed title query, to try again (if the API is available)
   * widening the match criteria to look for Entities where any attributes might be a match
   * 
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
        .flatMap(res -> this.handleNoSuchEntityResults(res, id));
  }

  /**
   * Attempts to find any matching Entity which in some way matches the provided query
   * 
   * @return a matching SearchResult or encountered Err(or) 
   */
  private Either<Err, WbSearchEntitiesResult> fetchSearchResultsByAnyMatch(String query) {
    return fetchWithApiUnavailableErrorHandler(() -> fetcher.searchEntities(query, wikiLangKey))
        .flatMap(res -> this.handleSearchedEntitiesResults(res, query));
  }

  private final String noMatchFor = "was unable to find any match for: ";

  /**
   * Validates and returns the closest matching search result if it exists, else returns the appropriately instantiated Err(or)
   *
   * @return Search result or a NoSuchEntityFoundError
   */
  private Either<Err, WbSearchEntitiesResult> handleSearchedEntitiesResults(List<WbSearchEntitiesResult> results,
      String query) {
    return results.isEmpty()
        ? Either.left(new NoSuchEntityFoundError(
            "@handleSearchedEntitiesResults()" + noMatchFor + query))
        : Either.right(results.get(0));
  }

  /**
   * Attempts to find a matching Entity by looking for a matching title. Provides handling for an offline API and 
   * if no such record can be found by the Wikidata API. 
   * 
   * @return An EntityDocument match or an encountered Err(or)
   */
  protected Either<Err, EntityDocument> fetchEntityByTitleMatch(String query) {
    return fetchWithApiUnavailableErrorHandler(() -> fetcher.getEntityDocumentByTitle(wikiIri, query))
        .flatMap(res -> this.handleNoSuchEntityResults(res, query));
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
   * @return return a result match, else returns a NoSuchEntityFoundError
   */
  private Either<Err, EntityDocument> handleNoSuchEntityResults(EntityDocument res, String query) {
    return res == null
        ? Either.left(
            new NoSuchEntityFoundError("@handleNoSuchEntitiesResult()" + noMatchFor + query))
        : Either.right(res);
  }

}
