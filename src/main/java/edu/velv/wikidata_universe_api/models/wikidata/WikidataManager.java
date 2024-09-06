package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collection;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.WikidataServiceError.FetchRelatedWithTimeoutError;
import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.Constables;
import edu.velv.wikidata_universe_api.models.RequestPayload;
import edu.velv.wikidata_universe_api.models.jung_ish.Edge;
import edu.velv.wikidata_universe_api.models.jung_ish.Vertex;
import edu.velv.wikidata_universe_api.utils.Loggable;
import edu.velv.wikidata_universe_api.utils.ProcessTimer;
import io.vavr.control.Either;

public class WikidataManager implements Loggable {
  protected Integer n;
  protected Set<Property> properties;
  protected FetchQueue queue;
  protected ClientRequest session;
  protected FetchBroker wikidataApi;
  protected EntDocProc entProc;
  protected ScheduledExecutorService timeoutExecutor;

  public WikidataManager(ClientRequest parentSession) {
    this.n = 0;
    this.session = parentSession;
    this.properties = ConcurrentHashMap.newKeySet();
    this.wikidataApi = new FetchBroker();
    this.queue = new FetchQueue();
    this.entProc = new EntDocProc(parentSession);
    this.timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
  }

  /**
   * Uses the sanitized query to look through Wikidata's DB for a matching Entity which is then processed
   * 
   * @return An Optional<Err> if one is encountered
   */
  public Optional<Err> fetchInitQueryData() {
    return wikidataApi.fetchOriginEntityByAny(session.query()).fold(e -> {
      return Optional.of(e);
    }, entDoc -> {
      entProc.processWikiEntDocument(entDoc);
      return Optional.empty();
    });
  }

  /**
   * Uses a perscribed amount of time to allow fetches to be made to the Wikidata API to collect related data
   * 
   * @return An Optional<Err> if one is encountered
   */
  public Optional<Err> fetchRelatedWithTimeout() {
    Future<Optional<Err>> fetchTaskFuture = timeoutExecutor.submit(this::fetchRelatedTaskSingleRunner);
    try {
      return fetchTaskFuture.get(Constables.WD_TIMEOUT_LIMIT, Constables.WD_TIMEOUT_UNIT);
    } catch (Exception e) {
      return Optional.of(new FetchRelatedWithTimeoutError("fetchRelatedData timed out", e));
    } finally {
      fetchTaskFuture.cancel(true);
    }
  }

  public Set<Property> properties() {
    return this.properties;
  }

  public FetchQueue fetchQueue() {
    return this.queue;
  }

  public String toString() {
    return "Wikidata={ n=" + n + ", " + queue.toString() + " }";
  }

  public void addAllProperties(Collection<Property> props) {
    properties.addAll(props);
  }

  public void populateQueueWithPayload(RequestPayload payload) {
    this.queue = new FetchQueue(payload.queue());
  }

  // Protected
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  protected void addUnfetchedEdgeDetailsToQueue(Edge e) {
    queue.addUnfetchedEdgeValues(e, n);
  }

  protected void addSearchResultIDBackToQueue(Vertex v) {
    queue.addUnfetchedVertexValue(v, n);
  }

  protected void addProperty(Property p) {
    if (properties.contains(p))
      return;
    properties.add(p);
  }

  // Fetch Related Task...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  // should remove the duality of fetching dates using another mechanism
  private Optional<Err> fetchRelatedTaskSingleRunner() {
    while (n <= Constables.WD_MAX_N_FETCH_DEPTH) {
      List<String> tgtBatch = queue.getTargetBatchByPriority(n);
      boolean isEntBatch = tgtBatch.getFirst().matches(Regex.ENT_ID);
      Optional<Err> batchProcTask;

      if (isEntBatch) {
        batchProcTask = fetchAndHandleEntities(tgtBatch);
      } else {
        batchProcTask = fetchAndHandleDates(tgtBatch);
      }

      if (batchProcTask.isPresent()) {
        // exits with only API error, NoResultsErrs are relegated to invalid
        return batchProcTask;
      }

      if (queue.isEmptyAtNDepth(n)) {
        n += 1;
      }
      // ends loop...
    }
    return Optional.empty();
  }

  private Optional<Err> fetchAndHandleEntities(List<String> batch) {
    Either<Err, Map<String, Either<Err, EntityDocument>>> results = wikidataApi.fetchEntitiesByIdList(batch);
    if (results.isLeft()) {
      return Optional.of(results.getLeft());
    }
    handleFetchResults(results.get());
    return Optional.empty();
  }

  private Optional<Err> fetchAndHandleDates(List<String> batch) {
    Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> results = wikidataApi.fetchEntitiesByDateList(batch);
    if (results.isLeft()) {
      return Optional.of(results.getLeft());
    }
    handleDateResults(results.get());
    return Optional.empty();
  }

  private void handleFetchResults(Map<String, Either<Err, EntityDocument>> results) {
    if (results != null) {
      for (Entry<String, Either<Err, EntityDocument>> result : results.entrySet()) {
        if (result.getValue().isLeft()) {
          log(buildInvalidLogString(result.getKey()));
          queue.fetchInvalidCleanup(result.getKey());
        }
        entProc.processWikiEntDocument(result.getValue().get());
        queue.fetchSuccessCleanup(result.getKey());
      }
    }
  }

  private void handleDateResults(Map<String, Either<Err, WbSearchEntitiesResult>> results) {
    if (results != null) {
      for (Entry<String, Either<Err, WbSearchEntitiesResult>> result : results.entrySet()) {
        if (result.getValue().isLeft()) {
          log(buildInvalidLogString(result.getKey()));
          queue.fetchInvalidCleanup(result.getKey());
        }
        entProc.processSearchEntResult(result.getValue().get());
        queue.fetchSuccessCleanup(result.getKey());
      }
    }
  }

  protected String buildInvalidLogString(String tgtQuery) {
    return "Invalid/Error: (" + tgtQuery + ") query.";
  }

  // private Optional<Err> fetchRelatedTask() {
  //   ProcessTimer taskTimer = new ProcessTimer();
  //   while (n <= Constables.WD_MAX_N_FETCH_DEPTH) {
  //     try {
  //       ProcessTimer fetchTimer = new ProcessTimer();

  //       List<String> dateBatch = queue.getDateTargetBatch(n);
  //       List<String> qBatch = queue.getEntTargetBatch(n);

  //       Either<Err, Map<String, Either<Err, EntityDocument>>> qResults = wikidataApi.fetchEntitiesByIdList(qBatch);
  //       Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> dateResults = wikidataApi
  //           .fetchEntitiesByDateList(dateBatch);

  //       if (qResults.isLeft()) {
  //         return Optional.of(qResults.getLeft());
  //       }
  //       // Collect fetch details for eval
  //       // === === === === === === === === === === === ===
  //       fetchTimer.stop(); // stop time for processing 
  //       int totalFetched = 0;
  //       totalFetched += qResults.get().entrySet().size();
  //       totalFetched += dateResults.get().entrySet().size();
  //       logFetch(fetchTimer.getElapsedTimeFormatted() + " :: " + totalFetched);
  //       // === === === === === === === === === === === ===

  //       handleDateResults(dateResults.get());
  //       handleFetchResults(qResults.get());

  //       if (queue.isEmptyAtNDepth(n)) {
  //         n += 1;
  //       }
  //     } catch (Exception e) {
  //       return Optional.of(new FetchRelatedWithTimeoutError("@fetchRelatedTask()", e));
  //     }
  //     print(session.details());
  //   }
  //   // === === === === === === === === === === === ===
  //   taskTimer.stop();
  //   logFetch(session.details());
  //   logFetch(taskTimer.getElapsedTimeFormatted() + "\n");
  //   // === === === === === === === === === === === ===

  //   return Optional.empty();
  // }
}