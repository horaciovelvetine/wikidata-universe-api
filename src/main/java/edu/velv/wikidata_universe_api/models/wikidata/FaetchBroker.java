package edu.velv.wikidata_universe_api.models.wikidata;

import java.io.IOException;
import java.util.List;

import io.vavr.CheckedFunction0;
import io.vavr.control.Either;
import io.vavr.control.Try;

import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder;
import org.wikidata.wdtk.datamodel.helpers.PropertyDocumentBuilder;
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyIdValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;

import edu.velv.wikidata_universe_api.models.err.WikiverseError;

public class FaetchBroker {
  private static final String WIKI_SITE_KEY = "enwiki";
  private static final String DEFAULT_FALLBACK_ID = "Q3454165";
  private final WikibaseDataFetcher fetcher;

  public FaetchBroker() {
    this.fetcher = WikibaseDataFetcher.getWikidataDataFetcher();
  }

  /**
   * Retrieves the origin entity by first searching by title, then widening to search all Entities.
   *
   * @param query the query to search for the origin entity
   * @return an Either object containing either the retrieved EntityDocument or a WikiverseError
   */
  public Either<WikiverseError, EntityDocument> getOriginEntityByAny(String query) {
    Either<WikiverseError, EntityDocument> entityByTitle = fetchEntityByTitle(query);

    if (entityByTitle.isRight() || isFailedApiRequest(entityByTitle.getLeft())) {
      return entityByTitle;
    }

    Either<WikiverseError, WbSearchEntitiesResult> entityByAny = fetchEntityByAny(query);

    if (isNoSuchEntityError(entityByAny.getLeft())) {
      return fetchEntityById(DEFAULT_FALLBACK_ID);
    }

    return entityByAny.map(this::convertAnyResultToEntityDoc);
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
    return fetchEntityWithErrorHandler(() -> fetcher.getEntityDocument(query));
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
        .mapLeft(e -> new WikiverseError.WikidataServiceError.ApiRequestFailed(e.getMessage()));
  }

  private boolean entIdIsItemDoc(String s) {
    return s.matches("[Q]\\d*");
  }

  private boolean entIdIsPropertyDoc(String s) {
    return s.matches("[P]\\d*");
  }

  private boolean isNoSuchEntityError(WikiverseError error) {
    return error instanceof WikiverseError.WikidataServiceError.NoSuchEntityFound;
  }

  private boolean isFailedApiRequest(WikiverseError error) {
    return error instanceof WikiverseError.WikidataServiceError.ApiRequestFailed;
  }

  private EntityDocument convertAnyResultToEntityDoc(WbSearchEntitiesResult searchResult) {
    String entId = searchResult.getEntityId();
    String entIri = searchResult.getConceptUri().replace(entId, "");
    String entDesc = searchResult.getDescription();
    String entLabel = searchResult.getLabel();
    String langCode = "en";
    EntityDocument doc = null;

    if (entIdIsItemDoc(entId)) {
      doc = createItemDocument(entId, entIri, entDesc, entLabel, langCode);
    } else if (entIdIsPropertyDoc(entId)) {
      doc = createPropertyDocument(entId, entIri, entDesc, entLabel, langCode);
    } else {
      throw new IllegalArgumentException("Entity ID is not a valid QID or PID.");
    }
    return doc;
  }

  private EntityDocument createItemDocument(String itemId, String itemIri, String description, String label,
      String SITE_KEYCode) {
    ItemIdValue idValue = new ItemIdValueImpl(itemId, itemIri);
    ItemDocumentBuilder builder = ItemDocumentBuilder.forItemId(idValue);
    builder.withDescription(description, SITE_KEYCode);
    builder.withLabel(label, SITE_KEYCode);
    return builder.build();
  }

  @SuppressWarnings("deprecation") //DT_PROPERTY is deprecated, required lib is not included in pom
  private EntityDocument createPropertyDocument(String propertyId, String propertyIri, String description, String label,
      String SITE_KEYCode) {
    PropertyIdValue idValue = new PropertyIdValueImpl(propertyId, propertyIri);
    PropertyDocumentBuilder builder = PropertyDocumentBuilder.forPropertyIdAndDatatype(idValue,
        DatatypeIdValue.DT_PROPERTY);
    builder.withDescription(description, SITE_KEYCode);
    builder.withLabel(label, SITE_KEYCode);
    return builder.build();
  }
}