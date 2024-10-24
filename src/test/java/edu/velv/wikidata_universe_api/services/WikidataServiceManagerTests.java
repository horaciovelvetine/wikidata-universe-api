package edu.velv.wikidata_universe_api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.WikidataServiceError.*;
import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.LayoutConfig;
import edu.velv.wikidata_universe_api.models.RequestPayloadData;

import io.vavr.control.Either;

@SpringBootTest
public class WikidataServiceManagerTests implements WikidataTestDataBuilders, FailedTestMsgTemplates {
  private final String src_ = "@WikidataServiceManagerTests::";
  private final String kbQuery = "Kevin Bacon";

  @Autowired
  private LayoutConfig config;

  @Autowired
  private EntDocProc docProc;

  private WikidataServiceManager injWikidataSrvc;

  private ClientRequest request;

  // @Test
  // void fetchInitQueryData_bubbles_up_API_offline_error() {
  //   FetchBroker mApi = mock(FetchBroker.class);
  //   when(mApi.fetchEntityByAnyQueryMatch(kbQuery))
  //       .thenReturn(Either.left(new ApiUnavailableError(new Throwable("API OFFLINE MOCK"))));

  //   injWikidataSrvc = new WikidataServiceManager(mApi, docProc);
  //   request = new ClientRequest(injWikidataSrvc, config, kbQuery);

  //   Either<Err, ClientRequest> getInitQueryDataRoutine = request.getInitialQueryData();
  //   assertTrue(getInitQueryDataRoutine.isLeft(),
  //       src_ + expected + "An Err to be thrown when the API is unavailable, which lets us know.");
  // }

  // @Test
  // void fetchInitQueryData_builds_graphset_stg1_for_KevinBacon_test_data() {
  //   // setup to retrieve stored copy of previous request w/ 
  //   FetchBroker mApi = mock(FetchBroker.class);
  //   when(mApi.fetchEntityByAnyQueryMatch(kbQuery)).thenReturn(Either.right(buildKevinBaconDocFromData()));
  //   injWikidataSrvc = new WikidataServiceManager(mApi, docProc);

  //   Either<Err, ClientRequest> initQueryReq = new ClientRequest(injWikidataSrvc, config, kbQuery).getInitialQueryData();
  //   assertTrue(initQueryReq.isRight(), src_ + expected + "A valid ClientRequest to be provided");

  //   request = initQueryReq.get();
  //   String msg = src_ + expected + "there to be a total of - ";

  //   captureClientRequest(request, "init-query-task-fin");

  //   assertTrue(request.graph().vertexCount() == 39, msg + "39 Vertices.");
  //   assertTrue(request.graph().edgeCount() == 38, msg + "38 Edges.");
  //   assertTrue(request.graph().propertyCount() == 24, msg + "24 Properties.");
  // }

  // @Test
  // void fetchIncompleteData_ingests_unfetched_data_by_NoSuchError_removals() {
  //   FetchBroker mApi = mock(FetchBroker.class);
  //   RequestPayloadData data = readClientRequestPayloadFromStorage(kbTestUnfDataCxRequest);

  //   setup_mockDateListResults_no_results(mApi);
  //   setup_mockEntIdListResults_no_results(mApi);

  //   injWikidataSrvc = new WikidataServiceManager(mApi, docProc);
  //   Either<Err, ClientRequest> fetchIncompleteDataRoutine = new ClientRequest(injWikidataSrvc, config, data)
  //       .getUnfetchedData();

  //   assertTrue(fetchIncompleteDataRoutine.isRight(),
  //       src_ + expected + "an updated ClientRequest object for a RequestPayload with unfetched data");

  //   request = fetchIncompleteDataRoutine.get();

  //   assertTrue(request.graph().vertexCount() == 1,
  //       src_ + expected + "Mocked NoSuchEnt responses should remove all data except Origin Query vertex from Graphset");
  // }

  // @Test
  // void fetchIncompleteData_skips_fetches_and_runs_layout_for_KevinBacon_test_data() {
  //   FetchBroker mApi = mock(FetchBroker.class);
  //   RequestPayloadData data = readClientRequestPayloadFromStorage(kbTestUnfDataCxRequest);
  //   injWikidataSrvc = new WikidataServiceManager(mApi, docProc);
  //   request = new ClientRequest(injWikidataSrvc, config, data);

  //   // ignore data incompleteness, manually run layout steps...
  //   request.layout().initialize();
  //   request.layout().lock(request.graph().getOriginVertex(), true);
  //   while (!request.layout().done()) {
  //     request.layout().step();
  //   }

  //   request.graph().updateVertexCoordinatesFromLayout(request.layout());
  //   captureClientRequest(request, "ignore-and-layout-unfetch");
  //   assertTrue(request.graph().vertexCoordsUniqueForEach(),
  //       src_ + expected + "Vertex coords to be unique after layout");
  // }

  // /**
  //  * Provides a matched list of generic NoSuchEntityFoundError's for any List of String EntID targets
  //  * @apiNote - NoSuchEntityFound errors will remove mention of the data from the given Graphset
  //  */
  // private void setup_mockEntIdListResults_no_results(FetchBroker mApi) {
  //   when(mApi.fetchEntitiesByIdList(any())).thenAnswer(invocation -> {
  //     List<String> ids = invocation.getArgument(0);
  //     // Mock the response based on the ids
  //     Map<String, Either<Err, EntityDocument>> mockResult = new HashMap<>();
  //     ids.forEach(tgt -> {
  //       mockResult.put(tgt, Either.left(new NoSuchEntityFoundError(tgt)));
  //     });
  //     return Either.right(mockResult);
  //   });
  // }

  // /**
  //  * Provides a matched list of generic NoSuchEntityFoundError's for any List of String Date targets
  //  * @apiNote - NoSuchEntityFound errors will remove mention of the data from the given Graphset
  //  */
  // private void setup_mockDateListResults_no_results(FetchBroker mApi) {
  //   when(mApi.fetchEntitiesByDateList(any())).thenAnswer(invocation -> {
  //     List<String> dates = invocation.getArgument(0);
  //     // Mock the response based on the dates
  //     Map<String, Either<Err, WbSearchEntitiesResult>> mockResult = new HashMap<>();
  //     dates.forEach(tgt -> {
  //       mockResult.put(tgt, Either.left(new NoSuchEntityFoundError(tgt)));
  //     });
  //     return Either.right(mockResult);
  //   });
  // }

}
