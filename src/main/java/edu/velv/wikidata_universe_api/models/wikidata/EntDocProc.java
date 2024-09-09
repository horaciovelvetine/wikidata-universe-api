package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.Iterator;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.Constables;
import edu.velv.wikidata_universe_api.models.jung_ish.Edge;
import edu.velv.wikidata_universe_api.models.jung_ish.Vertex;
import edu.velv.wikidata_universe_api.utils.Loggable;

public class EntDocProc implements Loggable {
  protected ClientRequest session;

  public EntDocProc(ClientRequest parentSession) {
    this.session = parentSession;
  }

  /**
   * Narrows the EntityDocument fetched from the Wikidata API into the correct type to be processed and added to the graphset
   */
  public void processWikiEntDocument(EntityDocument doc) {
    if (doc instanceof ItemDocumentImpl) {
      processItemDocument((ItemDocumentImpl) doc);
    } else if (doc instanceof PropertyDocumentImpl) {
      processPropertyDocument((PropertyDocumentImpl) doc);
    }
  }

  /**
   * Search Results contain no statement details, for now add the usable details, and put it's ID back in the queue to get
   * the remaining Statement details.
   */
  public void processSearchEntResult(WbSearchEntitiesResult searchResult) {
    Vertex v = new Vertex(searchResult);
    session.graphset().addVertex(v);
    session.wikidataManager().addSearchResultIDBackToQueue(v);
  }

  // PRIVATE...
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  private void processItemDocument(ItemDocumentImpl doc) {
    Vertex v = new Vertex(doc);
    if (vertexMatchesOriginalQuery(v)) {
      session.graphset().setOriginRef(v);
    }

    session.graphset().addVertex(v);
    processItemsStatements(doc);
  }

  private void processPropertyDocument(PropertyDocumentImpl doc) {
    Property p = new Property(doc);
    session.wikidataManager().addProperty(p);
  }

  private void processItemsStatements(ItemDocumentImpl doc) {
    String srcVertexId = doc.getEntityId().getId();
    Iterator<Statement> stmts = doc.getAllStatements();

    while (stmts.hasNext()) {
      Statement stmt = stmts.next();
      SnakData mainSnak = stmt.getMainSnak().accept(new SnakData());
      if (snakDefinesRelevantData(mainSnak)) {
        createEdgeFromSnak(mainSnak, srcVertexId);
      }
    }
  }

  private void createEdgeFromSnak(SnakData ms, String srcVertexId) {
    Edge e = new Edge(srcVertexId, ms);
    session.graphset().addEdge(e);
    session.wikidataManager().addUnfetchedEdgeDetailsToQueue(e);
  }

  private boolean snakDefinesRelevantData(SnakData ms) {
    return !(hasNullValues(ms) || hasExcludedDataType(ms) || hasExcludedEntityId(ms));
  }

  private boolean hasNullValues(SnakData ms) {
    return ms == null || ms.snakValue == null || ms.property == null;
  }

  private boolean hasExcludedDataType(SnakData ms) {
    return Constables.WD_EXCLUDED_DATA_TYPES.contains(ms.datatype);
  }

  private boolean hasExcludedEntityId(SnakData ms) {
    return Constables.WD_EXCLUDED_ENT_IDS.contains(ms.property.value)
        || Constables.WD_EXCLUDED_ENT_IDS.contains(ms.snakValue.value);
  }

  private boolean vertexMatchesOriginalQuery(Vertex v) {
    if (v.label() == null)
      return false;
    return session.query().toLowerCase().equals(v.label().toLowerCase());
  }
}