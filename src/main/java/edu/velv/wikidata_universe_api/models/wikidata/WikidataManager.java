package edu.velv.wikidata_universe_api.models.wikidata;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;

import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.Edge;

public class WikidataManager {
  private final Integer MAX_FETCH_DEPTH = 2;
  private Integer n;
  private final FetchBroker api;
  private final FetchQueue queue;
  private final EntDocProc entProc;

  public WikidataManager(ClientSession parentSession) {
    this.n = 0;
    this.api = new FetchBroker();
    this.queue = new FetchQueue();
    this.entProc = new EntDocProc(parentSession);
  }

  public void fetchInitSessionData(String query) {
    EntityDocument originDoc = api.getOriginEntByQuery(query);
    entProc.processWikiEntDocument(originDoc);

    while (n <= MAX_FETCH_DEPTH) {
      // Fetch all entities in queue, if queue has no more @ currentQueueDepth, increment n

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

  private void fetchQueue() {
    // In charge of attempting to optimize the fetch process...
    //TO THE SKETCH PAD
  }

}
