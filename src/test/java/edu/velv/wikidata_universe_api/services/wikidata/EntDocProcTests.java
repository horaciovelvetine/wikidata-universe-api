package edu.velv.wikidata_universe_api.services.wikidata;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;

import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.services.FailedTestMsgTemplates;
import edu.velv.wikidata_universe_api.services.wikidata.EntDocProc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.List;

@SpringBootTest
public class EntDocProcTests implements FailedTestMsgTemplates, WikidataTestDataBuilders {
  private final String src_ = "@EntDocProcTests:: ";
  private final String enWikiKey = "en";

  @Autowired
  EntDocProc docProc;

  @Test
  void attemptVertexCreateUnkownTypeEntDoc_creates_vertex_when_expected() throws Exception {
    Optional<Vertex> result = docProc.createVertexFromUnknownEntDoc(mockItemDoc(1), enWikiKey);
    String mId = "Q1"; // from the int 1 above...
    assertNotNull(result, src_ + unableToFind + vert);
    assertEquals(mId, result.get().id(), src_ + vert + "id " + shouldBe + mId);
    assertEquals(mId + " " + la, result.get().label(), src_ + vert + "label " + shouldBe + mId + " " + la);
    assertEquals(de, result.get().description(), src_ + vert + "description " + shouldBeEq + de);
  }

  @Test
  void attemptVertexCreateUnkownTypeEntDoc_is_empty_when_expected() {
    PropertyDocument mockPropDoc = mock(PropertyDocument.class);
    Optional<Vertex> result = docProc.createVertexFromUnknownEntDoc(mockPropDoc, enWikiKey);
    assertTrue(result.isEmpty(), src_ + shouldBe + empty);
  }

  @Test
  void createRelatedEdgesFromStatements_creates_relevant_edges() {
    ItemDocumentImpl mItemDoc = mockItemDoc(1);

    List<Statement> mStmts = List.of(
        mockStatement_valid_generic(),
        mockStatement_valid_date());
    when(mItemDoc.getAllStatements()).thenReturn(mStmts.iterator());

    Set<Edge> results = docProc.createRelatedEdgesFromStatements(mItemDoc);

    assertTrue(results.size() == 2, src_ + expected + "2 " + edges + "to be created");
  }

  @Test
  void createRelatedEdgeFromStatements_ignores_excluded_and_invalid_data() {
    ItemDocumentImpl mItemDoc = mockItemDoc(1);

    Statement badType = mockStatement_invalid_externalIdType();
    Statement badPID = mockStatement_invalid_property();
    Statement badQID = mockStatment_invalid_entity();
    Statement propertyTgt = mockStatement_invalid_propertyTarget();
    Statement nullProp = mockStatement_invalid_nullProp();
    Statement nullVal = mockStatement_invalid_nullValue();
    Statement nullStmt = mockStatement_invalid_null();

    List<Statement> invalidStatements = List.of(badType, badPID, badQID, nullProp, nullStmt, nullVal, propertyTgt);
    when(mItemDoc.getAllStatements()).thenReturn(invalidStatements.iterator());

    Set<Edge> results = docProc.createRelatedEdgesFromStatements(mItemDoc);

    assertTrue(results.isEmpty(), src_ + shouldBe + empty);
  }
}
