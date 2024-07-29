package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;

import edu.velv.wikidata_universe_api.err.Err;
import edu.velv.wikidata_universe_api.err.WikidataServiceError.FetchRelatedWithTimeoutError;
import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.utils.Loggable;

import io.vavr.control.Either;

public class WikidataManager implements Loggable {
  private final Integer MAX_FETCH_DEPTH = 3;
  private Integer n;
  private final ClientSession session;
  private final FetchBroker api;
  private final FetchQueue queue;
  private final EntDocProc entProc;
  private final ScheduledExecutorService timeoutExecutor;

  public WikidataManager(ClientSession parentSession) {
    this.n = 0;
    this.session = parentSession;
    this.api = new FetchBroker();
    this.queue = new FetchQueue();
    this.entProc = new EntDocProc(parentSession);
    this.timeoutExecutor = Executors.newSingleThreadScheduledExecutor();
  }

  public Optional<Err> fetchInitQueryData() {
    return api.fetchOriginEntityByAny(session.query()).fold(e -> {
      return Optional.of(e);
    }, entDoc -> {
      entProc.processWikiEntDocument(entDoc);
      return Optional.empty();
    });
  }

  public Optional<Err> fetchRelatedWithTimeout() {
    Future<Optional<Err>> fetchTaskFuture = timeoutExecutor.submit(this::fetchRelatedTask);
    try {
      return fetchTaskFuture.get(1, TimeUnit.MINUTES);
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

  private Optional<Err> fetchRelatedTask() {
    while (n <= MAX_FETCH_DEPTH) {
      try {

        List<String> dateBatch = queue.getDateTargetsBatchAtN(n);
        List<String> qBatch = queue.getEntTargetsBatchAtN(n);
        Either<Err, Map<String, Either<Err, EntityDocument>>> qResults = api.fetchEntitiesByIdList(qBatch);
        Map<String, Either<Err, EntityDocument>> dateResults = api.fetchEntitiesByDateList(dateBatch);

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