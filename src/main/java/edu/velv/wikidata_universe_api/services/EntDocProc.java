package edu.velv.wikidata_universe_api.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;

import edu.velv.wikidata_universe_api.Constables;
import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.SnakData;
import edu.velv.wikidata_universe_api.models.Vertex;

public class EntDocProc {

  public EntDocProc() {
    //default constructor
  }

  /**
   * Type checks the provided document to attempt to create a Vertex from the provided doc.
   * If the result is empty it can be assumed the provided EntityDocument is of the subtype
   * PropertyDocument, and can instead be used (cast) to directly create a Property
   * 
   * @param EntityDocument generically typed wrapper result
   * @return a Vertex or empty if not passed the correct doc subtype 
   * 
   */
  public Optional<Vertex> createVertexFromUnknownEntDoc(EntityDocument doc) {
    if (doc instanceof ItemDocumentImpl) {
      Vertex newVert = new Vertex((ItemDocumentImpl) doc);
      return Optional.of(newVert);
    }
    return Optional.empty();
  }

  /**
   * Uses the (previously narrowed) ItemDocument's statements to filter for relevant data
   * and create a list of new Edge's
   * 
   * @param EntityDocument type narrowed since Edges are only created post successful VertexCreation
   * @return a set of unique Edge's
   * 
   */
  public Set<Edge> createRelatedEdgesFromStatements(EntityDocument doc) {
    Set<Edge> newEdges = new HashSet<>();
    if (doc instanceof StatementDocument) {
      StatementDocument castDoc = (StatementDocument) doc;
      String srcId = doc.getEntityId().getId();
      Iterator<Statement> stmts = castDoc.getAllStatements();
      while (stmts.hasNext()) {
        Statement stmt = stmts.next();
        SnakData mainSnak = stmt.getMainSnak().accept(new SnakData());
        if (snakDefinesRelevantData(mainSnak)) {
          newEdges.add(new Edge(srcId, mainSnak));
        }
      }
    }
    return newEdges;
  }

  /**
   * Checks the MainSnak from a Statement looking for missing or exclusionary data that the Statement may define. Most of this is looking for null data, then checking if the Statement defines some external database ID ref. Through development a list of excluded QID & PID values has been built. Often these are mismarked (should be .datatype() == 'external-id') but are only found at random.
   * @see https://github.com/horaciovelvetine/ForceDrawnGraphs/blob/main/docs/dev/PROPERTY_EXCLUSION_LIST.md
   */
  private boolean snakDefinesRelevantData(SnakData ms) {
    if (ms == null)
      return false;
    if (ms.snakValue() == null)
      return false;
    if (ms.property() == null)
      return false;
    if (ms.datatype() == null)
      return false;
    if (Constables.EXCLUDED_DATA_TYPES.contains(ms.datatype))
      return false;
    if (ms.property().value() == null)
      return false;
    if (Constables.EXCLUDED_ENT_IDS.contains(ms.property().value()))
      return false;
    if (ms.snakValue().value() == null)
      return false;
    if (ms.snakValue().value().startsWith("P"))
      return false;
    if (Constables.EXCLUDED_ENT_IDS.contains(ms.snakValue().value()))
      return false;
    return true;
  }
}
