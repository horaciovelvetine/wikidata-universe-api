package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;

import edu.velv.wikidata_universe_api.models.ClientSession;
import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.models.wikidata.ValueData.ValueType;

public class EntDocProc {
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
    if (doc instanceof ItemDocument) {
      processItemDocument((ItemDocument) doc);
    } else if (doc instanceof PropertyDocument) {
      processPropertyDocument((PropertyDocument) doc);
    }
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  private void processItemDocument(ItemDocument doc) {
    Vertex v = new Vertex(doc);
    session.graphset().addVertex(v);
    CompletableFuture.runAsync(() -> processItemsStatements(doc));
  }

  private void processPropertyDocument(PropertyDocument docu) {
    //TBD... 
  }

  private void processItemsStatements(ItemDocument doc) {
    String srcVertexId = doc.getEntityId().getId();

    while (doc.getAllStatements().hasNext()) {
      Statement stmt = doc.getAllStatements().next();
      SnakData mainSnak = stmt.getMainSnak().accept(new SnakData());

      if (definesRelevantData(mainSnak)) {
        createEdgeFromSnakData(mainSnak, srcVertexId);
      }
    }
  }

  private void createEdgeFromSnakData(SnakData ms, String srcVertexId) {
    Edge e = new Edge(srcVertexId, ms);
    session.graphset().addEdge(e);
    session.wikidataManager().addUnfetchedEdgeDetailsToQueue(e);
  }

  private boolean definesRelevantData(SnakData ms) {
    return !isNonNull(ms) &&
        !isExcludedDataType(ms) &&
        !isExcludedEntityId(ms) &&
        isEntityIdType(ms.snakValue.type);
  }

  private boolean isNonNull(SnakData ms) {
    // Checks properties are not null
    return ms != null && ms.property != null && ms.snakValue != null;
  }

  private boolean isExcludedDataType(SnakData ms) {
    // Checks SnakData datatype against excluded types
    return EXCLUDED_DATA.contains(ms.datatype);
  }

  private boolean isExcludedEntityId(SnakData ms) {
    // Checks if invalidating entity ids are present
    return EXCLUDED_ENT_IDS.contains(ms.property.value) || EXCLUDED_ENT_IDS.contains(ms.snakValue.value);
  }

  private boolean isEntityIdType(ValueType valueType) {
    return valueType == ValueType.EntityId;
  }

}