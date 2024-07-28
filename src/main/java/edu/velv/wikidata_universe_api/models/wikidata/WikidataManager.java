package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.utils.Loggable;
import edu.velv.wikidata_universe_api.models.err.WikiverseError;
import edu.velv.wikidata_universe_api.models.err.WikiverseError.WikidataServiceError.FetchRelatedDataTimeout;

public class WikidataManager implements Loggable {
  private final Integer MAX_FETCH_DEPTH = 2;
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

  public Optional<WikiverseError> fetchInitQueryData() {
    return api.getOriginEntityByAny(session.query()).fold(e -> {
      return Optional.of(e);
    }, doc -> {
      entProc.processWikiEntDocument(doc);
      return Optional.empty();
    });
  }

  public Optional<WikiverseError> fetchRelatedDataWithTimeout() {
    Future<Optional<WikiverseError>> fetchTaskFuture = timeoutExecutor.submit(this::fetchRelatedTask);

    try {
      return fetchTaskFuture.get(10, TimeUnit.SECONDS);
    } catch (Exception e) {
      return Optional.of(new FetchRelatedDataTimeout("fetchRelatedData timed out", e));
    } finally {
      fetchTaskFuture.cancel(true);
    }
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  protected void addUnfetchedEdgeDetailsToQueue(Edge e) {
    queue.addUnfetchedEdgeValues(e, n);
  }

  private Optional<WikiverseError> fetchRelatedTask() {
    try {
      Thread.sleep(12000);
    } catch (Exception e) {
    }
    return Optional.empty();
  }
}