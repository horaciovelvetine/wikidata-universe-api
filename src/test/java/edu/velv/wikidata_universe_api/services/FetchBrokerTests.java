package edu.velv.wikidata_universe_api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.vavr.control.Either;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import edu.velv.wikidata_universe_api._Utils.FailedTestMessageTemplates;
import edu.velv.wikidata_universe_api._Utils.TestDataBuilders;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.ApiUnavailableError;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.NoSuchEntityFoundError;

public class FetchBrokerTests implements FailedTestMessageTemplates, TestDataBuilders {
  private final String src_ = "@FetchBrokerTests";
  private final String entDocResults = "Entity Document results ";
  private final String dateDocResults = "Date Document results ";

  private WikibaseDataFetcher mFetcher;
  private FetchBroker fetchBroker;

  @BeforeEach
  public void setUpMockInjectedFetcher() {
    mFetcher = mock(WikibaseDataFetcher.class);
    fetchBroker = new FetchBroker(mFetcher);
  }

  @Test
  public void fetchEntityByAnyQueryMatch_returns_expected_results() throws Exception {
    ItemDocumentImpl mocDoc = mockItemDoc(1);

    //? any() as FetchBroker config values will be unconfigured outside the SpringBoot context
    when(mFetcher.getEntityDocumentByTitle(any(), eq(QID))).thenReturn(mocDoc);

    Either<Err, EntityDocument> result = fetchBroker.fetchEntityByAnyQueryMatch(QID);

    assertTrue(result.isRight(), src_ + entDocResults + shouldNotBe + empty);
    assertEquals(mocDoc, result.get(), src_ + entDocResults + shouldBeEq + "mocked ItemDoc");
  }

  @Test
  public void fetchEntityByAnyQueryMatch_handles_no_results_gracefully() throws Exception {

    when(mFetcher.getEntityDocumentByTitle(any(), eq(QID))).thenReturn(null);
    when(mFetcher.searchEntities(eq(QID), any(String.class))).thenReturn(null);

    Either<Err, EntityDocument> result = fetchBroker.fetchEntityByAnyQueryMatch(QID);
    assertTrue(result.isLeft(), src_ + entDocResults + shouldBe + "an Err when null results are given");
    assertTrue(result.getLeft() instanceof NoSuchEntityFoundError,
        src_ + entDocResults + shouldBe + " a NoSuchEntityFoundError with a helpful message");

  }

  @Test
  public void fetchEntityByAnyQueryMatch_doesnt_retry_if_API_offline() throws Exception {

    when(mFetcher.getEntityDocumentByTitle(any(), eq(QID))).thenThrow(MediaWikiApiErrorException.class);

    Either<Err, EntityDocument> result = fetchBroker.fetchEntityByAnyQueryMatch(QID);
    assertTrue(result.isLeft(),
        src_ + entDocResults + shouldBe + "an Err when a MediaWikiApiErrorException is thrown");
    assertTrue(result.getLeft() instanceof ApiUnavailableError,
        src_ + entDocResults + shouldBe + "wrapped in a WikidataServiceError to notify client");

  }

  @Test
  void fetchEntitiesByIdList_returns_expected_results() throws Exception {
    List<String> queryIds = Arrays.asList("Q1", "Q2");
    EntityDocument mEntDoc1 = mockItemDoc(1);
    EntityDocument mEntDoc2 = mockItemDoc(2);
    Map<String, EntityDocument> mResponse = Map.of("Q1", mEntDoc1, "Q2", mEntDoc2);

    when(mFetcher.getEntityDocuments(queryIds)).thenReturn(mResponse);

    Either<Err, Map<String, Either<Err, EntityDocument>>> result = fetchBroker.fetchEntitiesByIdList(queryIds);

    assertTrue(result.isRight(), src_ + entDocResults + shouldNotBe + empty);
    assertEquals(2, result.get().size(), src_ + expected + entDocResults + should + "have two ItemDoc results entries");
    assertEquals(mEntDoc1, result.get().get("Q1").get(),
        src_ + expected + entDocResults + shouldBeEq + "to mock of Q1");
    assertEquals(mEntDoc2, result.get().get("Q2").get(),
        src_ + expected + entDocResults + shouldBeEq + "to mock of Q2");
  }

  @Test
  void fetchEntitiesByIdList_doesnt_retry_if_API_offline() throws Exception {
    List<String> queryIds = Arrays.asList("Q1", "Q2");
    when(mFetcher.getEntityDocuments(queryIds)).thenThrow(MediaWikiApiErrorException.class);

    Either<Err, Map<String, Either<Err, EntityDocument>>> result = fetchBroker.fetchEntitiesByIdList(queryIds);

    assertTrue(result.isLeft(),
        src_ + entDocResults + shouldBe + "an Err when a MediaWikiApiErrorException is thrown");
    assertTrue(result.getLeft() instanceof ApiUnavailableError,
        src_ + entDocResults + shouldBe + "wrapped in a WikidataServiceError to notify client");

  }

  @Test
  void fetchEntitiesByDateList_returns_expected_results() throws Exception {
    List<String> dateBatch = Arrays.asList("2021-01-01", "2021-01-02");
    WbSearchEntitiesResult mockResult1 = mock(WbSearchEntitiesResult.class);
    WbSearchEntitiesResult mockResult2 = mock(WbSearchEntitiesResult.class);
    when(mFetcher.searchEntities(eq("2021-01-01"), nullable(String.class))).thenReturn(Arrays.asList(mockResult1));
    when(mFetcher.searchEntities(eq("2021-01-02"), nullable(String.class))).thenReturn(Arrays.asList(mockResult2));

    Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> result = fetchBroker
        .fetchEntitiesByDateList(dateBatch);

    assertTrue(result.isRight(), src_ + dateDocResults + shouldNotBe + empty);
    assertEquals(2, result.get().size(),
        src_ + expected + dateDocResults + should + "have two WbSearchEntitiesResult entries");
    assertEquals(mockResult1, result.get().get("2021-01-01").get(),
        src_ + dateDocResults + shouldBeEq + "mockDateResult 1");
    assertEquals(mockResult2, result.get().get("2021-01-02").get(),
        src_ + dateDocResults + shouldBeEq + "mockDateResult 2");
  }

  @Test
  void fetchEntitiesByDateList_doesnt_retry_if_API_offline() throws Exception {
    List<String> dateBatch = Arrays.asList("2021-01-01", "2021-01-02");
    when(mFetcher.searchEntities(eq("2021-01-01"), nullable(String.class))).thenThrow(MediaWikiApiErrorException.class);

    Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> result = fetchBroker
        .fetchEntitiesByDateList(dateBatch);

    assertTrue(result.isLeft(),
        src_ + dateDocResults + shouldBe + "an Err when a MediaWikiApiErrorException is thrown");
    assertTrue(result.getLeft() instanceof ApiUnavailableError,
        src_ + dateDocResults + shouldBe + "wrapped in a WikidataServiceError to notify client");
  }
}