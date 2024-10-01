package _v1.services;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.Statement;

import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.SnakData;
import edu.velv.wikidata_universe_api.models.ValueData;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.models.ValueData.ValueType;
import edu.velv.wikidata_universe_api.services.EntDocProc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.List;

public class EntDocProcTests {
  private final String QID = "Q123";
  private final String PID = "P123";
  private final String entStr = "entity-id";
  EntDocProc docProc = new EntDocProc();

  ItemDocumentImpl mItemDoc = mock(ItemDocumentImpl.class);
  ItemIdValue mItemId = mock(ItemIdValue.class);

  // @Test
  // void attemptVertexCreateUnkownTypeEntDoc_creates_vertex_when_expected() throws Exception {
  //   setupDouglasAdamsItemDocument();

  //   Optional<Vertex> result = docProc.createVertexFromUnknownEntDoc(mItemDoc);

  //   assertNotNull(result);
  //   assertEquals("Q42", result.get().id());
  //   assertEquals("Douglas Adams", result.get().label());
  //   assertEquals("English science fiction writer and humorist (1952–2001)", result.get().description());
  // }

  // @Test
  // void attemptVertexCreateUnkownTypeEntDoc_is_empty_when_expected() {
  //   PropertyDocument mockPropDoc = mock(PropertyDocument.class);
  //   Optional<Vertex> result = docProc.createVertexFromUnknownEntDoc(mockPropDoc);
  //   assertTrue(result.isEmpty(), "Should not create a Vertex with a Property Document");
  // }

  // @Test
  // void createRelatedEdgesFromStatements_creates_relevant_edges() {
  //   setupDouglasAdamsItemDocument();
  //   Statement stmt = validGenericStatement();
  //   Statement date = validDateStatement();

  //   List<Statement> mStmts = List.of(stmt, date);
  //   when(mItemDoc.getAllStatements()).thenReturn(mStmts.iterator());

  //   Set<Edge> results = docProc.createRelatedEdgesFromStatements(mItemDoc);

  //   assertTrue(results.size() == 2, "Should be 2 valid Edges created from 2 valid Statements");
  // }

  // @Test
  // void createRelatedEdgeFromStatements_ignores_excluded_and_invalid_data() {
  //   setupDouglasAdamsItemDocument();

  //   Statement badType = excludedDataTypeStatement();
  //   Statement badPID = excludedPIDStatement();
  //   Statement badQID = excludedQIDStatemnt();
  //   Statement nullProp = nullPropStatement();
  //   Statement nullVal = nullValueStatement();
  //   Statement nullStmt = nullStatement();

  //   List<Statement> invalidStatements = List.of(badType, badPID, badQID, nullProp, nullStmt, nullVal);
  //   when(mItemDoc.getAllStatements()).thenReturn(invalidStatements.iterator());

  //   Set<Edge> results = docProc.createRelatedEdgesFromStatements(mItemDoc);

  //   assertTrue(results.isEmpty());
  // }

  // @Test
  // void createRelatedEdgeFromStatements_ignores_property_tgt_edges() {
  //   setupDouglasAdamsItemDocument();

  //   // Mock a statement where the tgtId starts with "P"
  //   Statement propertyTgtStatement = propertyTgtStatement();

  //   List<Statement> statements = List.of(propertyTgtStatement);
  //   when(mItemDoc.getAllStatements()).thenReturn(statements.iterator());

  //   Set<Edge> results = docProc.createRelatedEdgesFromStatements(mItemDoc);

  //   // Assert that no edges are created
  //   assertTrue(results.isEmpty(), "Edges with tgtId starting with 'P' should not be created");
  // }

  private Statement propertyTgtStatement() {
    ValueData prop = entValueData(PID);
    ValueData val = entValueData("P456"); // tgtId starting with "P"
    SnakData snakData = new SnakData(entStr, prop, val);

    return dataToStatementPipeline(snakData);
  }

  private void setupDouglasAdamsItemDocument() {
    when(mItemId.getId()).thenReturn("Q42");
    when(mItemDoc.getEntityId()).thenReturn(mItemId);
    when(mItemDoc.findLabel("en")).thenReturn("Douglas Adams");
    when(mItemDoc.findDescription("en"))
        .thenReturn("English science fiction writer and humorist (1952–2001)");
  }

  private Statement validGenericStatement() {
    ValueData prop = entValueData(PID);
    ValueData val = entValueData(QID);
    SnakData snakData = new SnakData(entStr, prop, val);

    return dataToStatementPipeline(snakData);
  }

  private Statement validDateStatement() {
    ValueData prop = entValueData(PID);
    ValueData val = dateValueData("1945-12-22");
    SnakData snakData = new SnakData("time-value", prop, val);

    return dataToStatementPipeline(snakData);
  }

  private Statement excludedDataTypeStatement() {
    ValueData prop = entValueData(PID);
    ValueData val = entValueData(QID);
    SnakData snakData = new SnakData("external-id", prop, val);

    return dataToStatementPipeline(snakData);
  }

  private Statement excludedPIDStatement() {
    ValueData prop = entValueData("P213"); //==> from excluded list
    ValueData val = entValueData(QID);
    SnakData snakData = new SnakData(entStr, prop, val);

    return dataToStatementPipeline(snakData);
  }

  private Statement excludedQIDStatemnt() {
    ValueData prop = entValueData(PID);
    ValueData val = entValueData("Q32351192"); //==> from excluded list
    SnakData snakData = new SnakData(entStr, prop, val);

    return dataToStatementPipeline(snakData);
  }

  private Statement nullPropStatement() {
    ValueData val = entValueData(QID);
    SnakData snakData = new SnakData(entStr, null, val);

    return dataToStatementPipeline(snakData);
  }

  private Statement nullValueStatement() {
    ValueData prop = entValueData(PID);
    SnakData snakData = new SnakData(entStr, prop, null);

    return dataToStatementPipeline(snakData);
  }

  private Statement nullStatement() {
    return dataToStatementPipeline(null);
  }

  private Statement dataToStatementPipeline(SnakData data) {
    Snak mSnak = mock(Snak.class);
    when(mSnak.accept(any())).thenReturn(data);

    Statement mStmt = mock(Statement.class);
    when(mStmt.getMainSnak()).thenReturn(mSnak);
    return mStmt;
  }

  private ValueData entValueData(String QPid) {
    ValueData vd = new ValueData();
    vd.type = ValueType.EntityId;
    vd.value = QPid;
    return vd;
  }

  private ValueData dateValueData(String date) {
    ValueData vd = new ValueData();
    vd.type = ValueType.DateTime;
    vd.value = date;
    return vd;
  }

}
