package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.Set;
import java.util.Iterator;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;

import edu.velv.wikidata_universe_api.models.Property;
import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.models.utils.Loggable;

public class EntDocProc implements Loggable {
  private final ClientSession session;

  private static final Set<String> EXCLUDED_DATA = Set.of("external-id", "monolingualtext",
      "commonsMedia", "url", "globe-coordinate", "geo-shape", "wikibase-lexeme");

  private static final Set<String> EXCLUDED_ENT_IDS = Set.of("P1343", "P143", "P935", "P8687",
      "P3744", "P18", "P373", "P856", "P1748", "P21", "P11889", "P1424", "P11527", "P1545", "P5008",
      "P1889", "P813", "P214", "P213", "P227", "P244", "P268", "P1006", "P1711", "P648", "P1315",
      "P2163", "P3430", "P1015", "P1207", "P1225", "P4823", "P269", "P322", "P1871", "P691",
      "P4342", "P5361", "P2600", "P535", "P8094", "P7293", "P8189", "P950", "P8318", "P1263",
      "P2949", "P7029", "P7699", "P10227", "P409", "P8081", "P7902", "P4619", "P7369", "P3348",
      "P1368", "P11686", "P10832", "P5034", "P1415", "P6058", "P646", "P5869", "P461", "Q109429537",
      "P7452", "Q19478619", "P4666", "P345", "P2604", "P5007", "Q59522350", "Q32351192", "P1011",
      "P8402", "P2959", "P78", "P5323", "P6104");

  public EntDocProc(ClientSession parentSession) {
    this.session = parentSession;
  }

  public void processWikiEntDocument(EntityDocument doc) {
    if (doc instanceof ItemDocumentImpl) {
      processItemDocument((ItemDocumentImpl) doc);
    } else if (doc instanceof PropertyDocumentImpl) {
      processPropertyDocument((PropertyDocumentImpl) doc);
    }
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  private void processItemDocument(ItemDocumentImpl doc) {
    Vertex v = new Vertex(doc);
    session.graphset().addVertex(v);
    processItemsStatements(doc);
  }

  private void processPropertyDocument(PropertyDocumentImpl doc) {
    Property p = new Property(doc);
    session.graphset().addProperty(p);
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
    if (hasNullValues(ms))
      return false;
    if (hasExcludedDataType(ms))
      return false;
    if (hasExcludedEntityId(ms))
      return false;
    return true;
  }

  private boolean hasNullValues(SnakData ms) {
    return ms == null || ms.snakValue == null || ms.property == null;
  }

  private boolean hasExcludedDataType(SnakData ms) {
    return EXCLUDED_DATA.contains(ms.datatype);
  }

  private boolean hasExcludedEntityId(SnakData ms) {
    return EXCLUDED_ENT_IDS.contains(ms.property.value) || EXCLUDED_ENT_IDS.contains(ms.snakValue.value);
  }
}