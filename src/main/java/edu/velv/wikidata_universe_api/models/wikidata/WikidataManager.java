package edu.velv.wikidata_universe_api.models.wikidata;

import io.vavr.control.Either;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;

import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.utils.Loggable;
import edu.velv.wikidata_universe_api.models.err.WikiverseError;

public class WikidataManager implements Loggable {
  private final Integer MAX_FETCH_DEPTH = 2;
  private Integer n;
  private final ClientSession session;
  private final FetchBroker api;
  private final FetchQueue queue;
  private final EntDocProc entProc;

  public WikidataManager(ClientSession parentSession) {
    this.n = 0;
    this.session = parentSession;
    this.api = new FetchBroker();
    this.queue = new FetchQueue();
    this.entProc = new EntDocProc(parentSession);
  }

  public Optional<WikiverseError> fetchInitSessionData() {
    Either<WikiverseError, EntityDocument> originDocTry = api.getOriginEntityByAny(session.query());

    if (originDocTry.isLeft()) {
      return Optional.of(originDocTry.getLeft());
    }

    entProc.processWikiEntDocument(originDocTry.get());

    while (n <= MAX_FETCH_DEPTH) {
      // init a fetch from the queue
      fetchTargetsFromQueue();
      if (queue.isEmptyAtNDepth(n)) {
        print("stop!");
        n++;
      }
    }
    return Optional.empty();
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  protected void addUnfetchedEdgeDetailsToQueue(Edge e) {
    queue.addUnfetchedEdgeValues(e, n);
  }

  private void fetchTargetsFromQueue() {
    queue.getQueriesAtNDepth(n).forEach(q -> {
      queue.fetchSuccess(q);
    });
    print("fetching targets at depth " + n);
  }

  private Optional<WikiverseError> fetchTargetsInQueue() {
    //TODO: probably move this target parsing to the FetchQueue class
    List<String> qAll = queue.getQueriesAtNDepth(n);
    List<String> qNonEnts = filterNonEntQueries(qAll);
    List<String> qEnts = qAll.stream().filter(q -> !qNonEnts.contains(q)).collect(Collectors.toList());

    //CONSIDERS: nonEntDocs src edge will have no tgtEntId, needs handled
    Either<WikiverseError, Map<String, EntityDocument>> entDocsTry = api.fetchEntitiesByQueueList(qEnts);
    Either<WikiverseError, Map<String, EntityDocument>> nonEntDocsTry = api.fetchNonEntsByQueueList(qNonEnts);

    return Optional.empty();
  }

  private List<String> filterNonEntQueries(List<String> queries) {
    return queries.stream()
        .filter(q -> !q.matches("[PQ]\\d+"))
        .collect(Collectors.toList());
  }

}
