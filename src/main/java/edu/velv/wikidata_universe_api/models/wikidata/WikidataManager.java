package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.WikidataServiceError.FetchRelatedWithTimeoutError;
import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.jung_ish.Edge;
import edu.velv.wikidata_universe_api.models.jung_ish.Vertex;
import edu.velv.wikidata_universe_api.utils.Loggable;
import edu.velv.wikidata_universe_api.utils.ProcessTimer;
import io.vavr.control.Either;

public class WikidataManager implements Loggable {
  protected static final Integer FETCH_TIMEOUT_MAX = 1;
  protected static final TimeUnit FETCH_TIMEOUT_UNIT = TimeUnit.MINUTES;
  protected static final Integer N_DEPTH_MAX = 1;
  protected Integer n;
  protected Set<Property> properties;
  protected ClientSession session;
  protected FetchBroker wikidataApi;
  protected FetchQueue queue;
  protected EntDocProc entProc;
  protected ScheduledExecutorService timeoutExecutor;

  public WikidataManager(ClientSession parentSession) {
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
    Future<Optional<Err>> fetchTaskFuture = timeoutExecutor.submit(this::fetchRelatedTask);
    try {
      return fetchTaskFuture.get(FETCH_TIMEOUT_MAX, FETCH_TIMEOUT_UNIT);
    } catch (Exception e) {
      return Optional.of(new FetchRelatedWithTimeoutError("fetchRelatedData timed out", e));
    } finally {
      fetchTaskFuture.cancel(true);
    }
  }

  public String toString() {
    return "Wikidata={ n=" + n + ", " + queue.toString() + " }";
  }

  // Private...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  protected void addUnfetchedEdgeDetailsToQueue(Edge e) {
    queue.addUnfetchedEdgeValues(e, n);
  }

  public void addSearchResultIDBackToQueue(Vertex v) {
    queue.addUnfetchedVertexValue(v, n);
  }

  protected void addProperty(Property p) {
    if (properties.contains(p))
      return;
    properties.add(p);
  }

  private String buildInvalidLogString(String tgtQuery) {
    return "Invalid/Error: (" + tgtQuery + ") query.";
  }

  private void handleFetchResults(Map<String, Either<Err, EntityDocument>> results) {
    for (Entry<String, Either<Err, EntityDocument>> result : results.entrySet()) {
      if (result.getValue().isLeft()) {
        log(buildInvalidLogString(result.getKey()));
        queue.fetchInvalid(result.getKey());
      }
      entProc.processWikiEntDocument(result.getValue().get());
      queue.fetchSuccess(result.getKey());
    }
  }

  private void handleDateResults(Map<String, Either<Err, WbSearchEntitiesResult>> results) {
    for (Entry<String, Either<Err, WbSearchEntitiesResult>> result : results.entrySet()) {
      if (result.getValue().isLeft()) {
        log(buildInvalidLogString(result.getKey()));
        queue.fetchInvalid(result.getKey());
      }
      entProc.processSearchEntResult(result.getValue().get());
      queue.fetchSuccess(result.getKey());
    }
  }

  // Fetch Related Task...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  private Optional<Err> fetchRelatedTask() {
    ProcessTimer taskTimer = new ProcessTimer();
    while (n <= N_DEPTH_MAX) {
      try {
        ProcessTimer fetchTimer = new ProcessTimer();

        List<String> dateBatch = queue.getDateTargetsBatchAtN(n);
        List<String> qBatch = queue.getEntTargetsBatchAtN(n);

        Either<Err, Map<String, Either<Err, EntityDocument>>> qResults = wikidataApi.fetchEntitiesByIdList(qBatch);
        Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> dateResults = wikidataApi
            .fetchEntitiesByDateList(dateBatch);

        if (qResults.isLeft()) {
          return Optional.of(qResults.getLeft());
        }
        // Collect fetch details for eval
        // === === === === === === === === === === === ===
        fetchTimer.stop(); // dont time processing 
        int totalFetched = 0;
        totalFetched += qResults.get().entrySet().size();
        totalFetched += dateResults.get().entrySet().size();
        logFetch(fetchTimer.getElapsedTimeFormatted() + " :: " + totalFetched);
        // === === === === === === === === === === === ===
        handleDateResults(dateResults.get());
        handleFetchResults(qResults.get());

        if (queue.isEmptyAtNDepth(n)) {
          n += 1;
        }
      } catch (Exception e) {
        return Optional.of(new FetchRelatedWithTimeoutError("@fetchRelatedTask()", e));
      }
    }
    // === === === === === === === === === === === ===
    taskTimer.stop();
    logFetch(session.details());
    logFetch(taskTimer.getElapsedTimeFormatted() + "\n");
    // === === === === === === === === === === === ===
    return Optional.empty();
  }
}