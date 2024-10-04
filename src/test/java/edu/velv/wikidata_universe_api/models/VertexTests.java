package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api._Utils.FailedTestMessageTemplates;
import edu.velv.wikidata_universe_api._Utils.TestDataBuilders;

// import edu.velv.wikidata_universe_api.Constables;

public class VertexTests implements TestDataBuilders, FailedTestMessageTemplates {
  private final String src_ = "@VertexTests:: ";
  Vertex bert; // the vert
  WbSearchEntitiesResult mSearchResult = mock(WbSearchEntitiesResult.class);

  @BeforeEach
  void setupDefaultVertex() {
    bert = new Vertex();
  }

  @Test
  void constructs_default_vertex() {
    String msg = src_ + vert + val + shouldBe + "null ";
    assertNull(bert.id(), msg);
    assertNull(bert.label(), msg);
    assertNull(bert.description(), msg);
    assertFalse(bert.fetched(), msg);
  }

  @Test
  public void updateUnfetchedValues_works_with_ItemDocument() {
    ItemDocumentImpl mocDoc = mockItemDocument(1);
    String qid = "Q1";

    bert.updateUnfetchedValues(mocDoc, en);

    assertEquals("Q1", bert.id(), src_ + vert + ".id() " + shouldBe + "Q1");
    assertEquals("Q1 " + la, bert.label(), src_ + vert + ".label() " + shouldBe + "Q1 " + la);
    assertEquals(de, bert.description(), src_ + vert + ".description() " + shouldBe + de);
    assertTrue(bert.fetched(), src_ + vert + ".label() " + shouldBe + "true ");
  }

  @Test
  public void updateUnfetchedValues_works_with_SearchResults() {
    String lbl = "LBL";
    String dsc = "DSC";
    when(mSearchResult.getLabel()).thenReturn(lbl);
    when(mSearchResult.getDescription()).thenReturn(dsc);

    bert.updateUnfetchedValues(mSearchResult);

    assertEquals(lbl, bert.label());
    assertEquals(dsc, bert.description());
    assertTrue(bert.fetched());
  }

}
