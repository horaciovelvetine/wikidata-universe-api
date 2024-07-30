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

import edu.velv.wikidata_universe_api.err.Err;
import edu.velv.wikidata_universe_api.err.WikidataServiceError.FetchRelatedWithTimeoutError;
import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.jung_ish.Edge;
import edu.velv.wikidata_universe_api.utils.Loggable;

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

  public Optional<Err> fetchInitQueryData() {
    return wikidataApi.fetchOriginEntityByAny(session.query()).fold(e -> {
      return Optional.of(e);
    }, entDoc -> {
      entProc.processWikiEntDocument(entDoc);
      return Optional.empty();
    });
  }

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
    return "Wikidata={ n=" + n + ", " + queue.toString() + " }\n";
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  protected void addUnfetchedEdgeDetailsToQueue(Edge e) {
    queue.addUnfetchedEdgeValues(e, n);
  }

  protected void addProperty(Property p) {
    if (properties.contains(p))
      return;
    properties.add(p);
  }

  private Optional<Err> fetchRelatedTask() {
    while (n <= N_DEPTH_MAX) {
      try {

        List<String> dateBatch = queue.getDateTargetsBatchAtN(n);
        List<String> qBatch = queue.getEntTargetsBatchAtN(n);
        Either<Err, Map<String, Either<Err, EntityDocument>>> qResults = wikidataApi.fetchEntitiesByIdList(qBatch);
        Map<String, Either<Err, EntityDocument>> dateResults = wikidataApi.fetchEntitiesByDateList(dateBatch);

        if (qResults.isLeft()) {
          return Optional.of(qResults.getLeft());
        }

        qResults.get().entrySet().forEach(entry -> {
          if (entry.getValue().isLeft()) {
            log(buildInvalidLogString(entry));
            queue.fetchInvalid(entry.getKey());
          }
          entProc.processWikiEntDocument(entry.getValue().get());
          queue.fetchSuccess(entry.getKey());
        });

        dateResults.entrySet().forEach(entry -> {
          if (entry.getValue().isLeft()) {
            log(buildInvalidLogString(entry));
            queue.fetchInvalid(entry.getKey());
          }
          entProc.processWikiEntDocument(entry.getValue().get());
          queue.fetchSuccess(entry.getKey());
        });

        if (queue.isEmptyAtNDepth(n)) {
          n += 1;
        }
        //stop
        print(session.details());
      } catch (Exception e) {
        print("here");
        return Optional.of(new FetchRelatedWithTimeoutError("@fetchRelatedTask()", e));
      }
    }
    return Optional.empty();
  }

  private String buildInvalidLogString(Entry<String, Either<Err, EntityDocument>> entry) {
    return "Invalid/Error: (" + entry.getKey() + ") query.";
  }
}