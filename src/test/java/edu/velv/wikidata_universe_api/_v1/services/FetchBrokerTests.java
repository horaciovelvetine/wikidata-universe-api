package edu.velv.wikidata_universe_api._v1.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vavr.control.Either;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.ApiUnavailableError;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.NoSuchEntityFoundError;

public class FetchBrokerTests {

  // private WikibaseDataFetcher wdtkFetcher = mock(WikibaseDataFetcher.class);
  // private FetchBroker fetchBroker = new FetchBroker(wdtkFetcher);

  // @Test
  // void fetchEntityByAnyQueryMatch_success() throws Exception {
  //   String query = "Q123";
  //   EntityDocument mEntDoc = mock(EntityDocument.class);
  //   when(wdtkFetcher.getEntityDocumentByTitle(anyString(), eq(query))).thenReturn(mEntDoc);

  //   Either<Err, EntityDocument> result = fetchBroker.fetchEntityByAnyQueryMatch(query);
  //   assertTrue(result.isRight(), "Should return a right instance of Either containing an EntityDocument (mock).");
  // }

  // @Test
  // void fetchEntityByAny_handles_no_such_entity_found() throws Exception {
  //   String q1 = "Q123";
  //   EntityDocument mEntDoc = null;
  //   when(wdtkFetcher.getEntityDocumentByTitle(anyString(), eq(q1))).thenReturn(mEntDoc);
  //   when(wdtkFetcher.searchEntities(anyString(), eq(q1))).thenReturn(Collections.emptyList());

  //   Either<Err, EntityDocument> result = fetchBroker.fetchEntityByAnyQueryMatch(q1);

  //   assertTrue(result.isLeft(), "Null & Empty List results should return a left instance of Either containing an Err.");
  //   assertTrue(result.getLeft() instanceof NoSuchEntityFoundError,
  //       "No matching records should return a NoSuchEntityFoundError.");
  // }

  // @Test
  // void fetchEntityByAny_handles_wikidata_api_unavailable_with_unavailable_error() throws Exception {
  //   String q1 = "Q123";
  //   MediaWikiApiErrorException err = mock(MediaWikiApiErrorException.class);
  //   when(wdtkFetcher.getEntityDocumentByTitle(anyString(), eq(q1))).thenThrow(err);

  //   Either<Err, EntityDocument> result = fetchBroker.fetchEntityByAnyQueryMatch(q1);
  //   assertTrue(result.isLeft(),
  //       "Should catch the MediaWikiApiErrorException thrown for an unavailable API, and wrap that result in an Either Left");
  //   assertTrue(result.getLeft() instanceof ApiUnavailableError, "Error should relay API Unavailable message");
  // }

  // @Test
  // void fetchEntitiesByIdList_success() throws Exception {
  //   List<String> queryIds = Arrays.asList("Q1", "Q2");
  //   EntityDocument mEntDoc1 = mock(EntityDocument.class);
  //   EntityDocument mEntDoc2 = mock(EntityDocument.class);
  //   Map<String, EntityDocument> mResponse = new HashMap<>();
  //   mResponse.put("Q1", mEntDoc1);
  //   mResponse.put("Q2", mEntDoc2);
  //   when(wdtkFetcher.getEntityDocuments(queryIds)).thenReturn(mResponse);

  //   Either<Err, Map<String, Either<Err, EntityDocument>>> result = fetchBroker.fetchEntitiesByIdList(queryIds);

  //   assertTrue(result.isRight());
  //   assertEquals(2, result.get().size());
  //   assertEquals(mEntDoc1, result.get().get("Q1").get());
  //   assertEquals(mEntDoc2, result.get().get("Q2").get());
  // }

  // @Test
  // void testFetchEntitiesByIdList_handles_no_suc_entity_found() throws Exception {
  //   List<String> queryIds = Arrays.asList("Q1", "Q2");
  //   Map<String, EntityDocument> mockResponse = new HashMap<>();
  //   mockResponse.put("Q1", null);
  //   mockResponse.put("Q2", null);
  //   when(wdtkFetcher.getEntityDocuments(queryIds)).thenReturn(mockResponse);

  //   Either<Err, Map<String, Either<Err, EntityDocument>>> result = fetchBroker.fetchEntitiesByIdList(queryIds);

  //   assertTrue(result.isRight());
  //   assertTrue(result.get().get("Q1").isLeft());
  //   assertTrue(result.get().get("Q2").isLeft());
  // }

  // @Test
  // void testFetchEntitiesByDateList_Success() throws Exception {
  //   List<String> dateBatch = Arrays.asList("2021-01-01", "2021-01-02");
  //   WbSearchEntitiesResult mockResult1 = mock(WbSearchEntitiesResult.class);
  //   WbSearchEntitiesResult mockResult2 = mock(WbSearchEntitiesResult.class);
  //   when(wdtkFetcher.searchEntities(eq("2021-01-01"), anyString())).thenReturn(Arrays.asList(mockResult1));
  //   when(wdtkFetcher.searchEntities(eq("2021-01-02"), anyString())).thenReturn(Arrays.asList(mockResult2));

  //   Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> result = fetchBroker
  //       .fetchEntitiesByDateList(dateBatch);

  //   assertTrue(result.isRight());
  //   assertEquals(2, result.get().size());
  //   assertEquals(mockResult1, result.get().get("2021-01-01").get());
  //   assertEquals(mockResult2, result.get().get("2021-01-02").get());
  // }

  void testFetchEntitiesByDateList_ApiUnavailable() {
    // List<String> dateBatch = Arrays.asList("2021-01-01", "2021-01-02");
    // when(fetcher.searchEntities(eq("2021-01-01"), anyString())).thenThrow(new RuntimeException("API unavailable"));

    // Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> result = fetchBroker
    //     .fetchEntitiesByDateList(dateBatch);

    // assertTrue(result.isLeft());
    // assertTrue(result.getLeft() instanceof Err.WikidataServiceError.ApiUnavailableError);
  }
}