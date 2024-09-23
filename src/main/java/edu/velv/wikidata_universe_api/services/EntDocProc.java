package edu.velv.wikidata_universe_api.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;

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
  public Optional<Vertex> attemptVertexCreateUnkownTypeEntDoc(EntityDocument doc) {
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
  public Set<Edge> createRelatedEdgesFromStatements(EntityDocument oDoc) {
    ItemDocument doc = (ItemDocument) oDoc;
    String srcId = doc.getEntityId().getId();
    Iterator<Statement> stmts = doc.getAllStatements();
    Set<Edge> newEdges = new HashSet<>();

    while (stmts.hasNext()) {
      Statement stmt = stmts.next();
      SnakData mainSnak = stmt.getMainSnak().accept(new SnakData());
      if (snakDefinesRelevantData(mainSnak)) {
        newEdges.add(new Edge(srcId, mainSnak));
      }
    }

    return newEdges;
  }

  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  /**
   * Wraps the validation methods used to double check that a MainSnak is something worth including
   * in the returned data set, in the process checking for:
   * - Missing or null data
   * - Excluded DataType(s) - mostly random external database Id values
   * - Excluded Entity Data - Wikidata internal (and Id specific) pre-excluded entities 
   * @see https://github.com/horaciovelvetine/ForceDrawnGraphs/blob/main/docs/dev/PROPERTY_EXCLUSION_LIST.md
   */
  private boolean snakDefinesRelevantData(SnakData ms) {
    return !(hasNullDataValues(ms) || definesExcludedDataType(ms) || definesExcludedEntityData(ms));
  }

  private boolean hasNullDataValues(SnakData ms) {
    return ms == null || ms.snakValue == null || ms.property == null;
  }

  private boolean definesExcludedDataType(SnakData ms) {
    return Constables.EXCLUDED_DATA_TYPES.contains(ms.datatype);
  }

  private boolean definesExcludedEntityData(SnakData ms) {
    return Constables.EXCLUDED_ENT_IDS.contains(ms.property.value)
        || Constables.EXCLUDED_ENT_IDS.contains(ms.snakValue.value);
  }
}
