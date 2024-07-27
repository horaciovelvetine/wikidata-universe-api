package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.List;
import java.util.Map;

import io.vavr.CheckedFunction0;
import io.vavr.control.Either;
import io.vavr.control.Try;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

import edu.velv.wikidata_universe_api.models.err.WikiverseError;

public class FetchBroker {
  private static final String WIKI_SITE_KEY = "enwiki";
  private static final String DEFAULT_FALLBACK_ID = "Q3454165";
  private final WikibaseDataFetcher fetcher;

  public FetchBroker() {
    this.fetcher = WikibaseDataFetcher.getWikidataDataFetcher();
  }

  /**
   * Retrieves the origin entity by first searching by title, then widening to search all Entities.
   *
   * @param query the query to search for the origin entity
   * @return an Either object containing either the retrieved EntityDocument or a WikiverseError
   */
  protected Either<WikiverseError, EntityDocument> getOriginEntityByAny(String query) {
    Either<WikiverseError, EntityDocument> entityByTitle = fetchEntityByTitle(query);

    if (entityByTitle.isRight() || isFailedApiRequest(entityByTitle.getLeft())) {
      return entityByTitle;
    }

    Either<WikiverseError, WbSearchEntitiesResult> entityByAny = fetchEntityByAny(query);

    if (entityByAny.isLeft() && isNoSuchEntityError(entityByAny.getLeft())) {
      return fetchEntityById(DEFAULT_FALLBACK_ID);
    }

    return fetchEntityById(entityByAny.get().getEntityId());
  }

  protected Either<WikiverseError, Map<String, EntityDocument>> fetchEntitiesByQueueList(List<String> ids) {
    return Try.of(() -> fetcher.getEntityDocuments(ids))
        .toEither()
        .mapLeft(e -> new WikiverseError.WikidataServiceError.ApiRequestFailed(e.getMessage(), e));
  }

  protected Either<WikiverseError, Map<String, EntityDocument>> fetchNonEntsByQueueList(List<String> qNonEnts) {
    //TODO: The string or DateTime queries -- dont forget possible date format needed
    return Either.left(new WikiverseError.UnimplementedError("getNonEntsByQueueList not implemented"));
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  private Either<WikiverseError, EntityDocument> fetchEntityByTitle(String query) {
    Either<WikiverseError, EntityDocument> entityByTitle = fetchEntityWithErrorHandler(
        () -> fetcher.getEntityDocumentByTitle(WIKI_SITE_KEY, query));

    if (entityByTitle.isLeft()) {
      return Either.left(entityByTitle.getLeft());
    }

    return entityByTitle.get() == null
        ? Either.left(
            new WikiverseError.WikidataServiceError.NoSuchEntityFound("Unable to find entity by title: " + query))
        : entityByTitle;
  }

  private Either<WikiverseError, EntityDocument> fetchEntityById(String query) {
    Either<WikiverseError, EntityDocument> entityById = fetchEntityWithErrorHandler(
        () -> fetcher.getEntityDocument(query));

    if (entityById.isLeft()) {
      return Either.left(entityById.getLeft());
    }

    return entityById.get() == null
        ? Either
            .left(new WikiverseError.WikidataServiceError.NoSuchEntityFound("Unable to find entity by id: " + query))
        : entityById;
  }

  private Either<WikiverseError, List<WbSearchEntitiesResult>> fetchEntitiesBySearch(String query) {
    return fetchEntityWithErrorHandler(() -> fetcher.searchEntities(query));
  }

  private Either<WikiverseError, WbSearchEntitiesResult> fetchEntityByAny(String query) {
    return fetchEntitiesBySearch(query).flatMap(this::handleSearchedEntitiesResults);
  }

  private Either<WikiverseError, WbSearchEntitiesResult> handleSearchedEntitiesResults(
      List<WbSearchEntitiesResult> results) {
    return results.isEmpty()
        ? Either
            .left(new WikiverseError.WikidataServiceError.NoSuchEntityFound("Unable to find entity search:" + results))
        : Either.right(results.get(0));
  }

  private <T> Either<WikiverseError, T> fetchEntityWithErrorHandler(CheckedFunction0<T> supplier) {
    return Try.of(supplier)
        .toEither()
        .mapLeft(e -> new WikiverseError.WikidataServiceError.ApiRequestFailed(e.getMessage(), e));
  }

  //TODO: duplicate fetchEntityWithError adding different errors?

  private boolean isNoSuchEntityError(WikiverseError error) {
    return error instanceof WikiverseError.WikidataServiceError.NoSuchEntityFound;
  }

  private boolean isFailedApiRequest(WikiverseError error) {
    return error instanceof WikiverseError.WikidataServiceError.ApiRequestFailed;
  }

}