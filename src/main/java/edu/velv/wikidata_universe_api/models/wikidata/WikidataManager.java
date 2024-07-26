package edu.velv.wikidata_universe_api.models.wikidata;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;

import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.err.WikiverseError;
import io.vavr.control.Either;

public class WikidataManager {
  private final Integer MAX_FETCH_DEPTH = 2;
  private Integer n;
  private final ClientSession session;
  private final FetchBroker api;
  private final FaetchBroker tApi;
  private final FetchQueue queue;
  private final EntDocProc entProc;

  public WikidataManager(ClientSession parentSession) {
    this.n = 0;
    this.session = parentSession;
    this.api = new FetchBroker();
    this.tApi = new FaetchBroker();
    this.queue = new FetchQueue();
    this.entProc = new EntDocProc(parentSession);
  }

  public void fetchInitSessionData() {
    Either<WikiverseError, EntityDocument> originDocTry = tApi.getOriginEntityByAny(session.query());
    entProc.processWikiEntDocument(originDocTry.get());

    while (n <= MAX_FETCH_DEPTH) {
      // get from queue
      if (queue.isEmpty(n)) {
        n++;
      }
    }
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  protected void addUnfetchedEdgeDetailsToQueue(Edge e) {
    queue.addUnfetchedEdgeDetails(e, n);
  }

}
