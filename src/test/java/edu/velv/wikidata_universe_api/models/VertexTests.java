package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

// import edu.velv.wikidata_universe_api.Constables;

public class VertexTests {
  Vertex bert; // the vert
  ItemDocumentImpl mItemDoc = mock(ItemDocumentImpl.class);
  WbSearchEntitiesResult mSearchResult = mock(WbSearchEntitiesResult.class);

  @BeforeEach
  void setupDefaultVertex() {
    bert = new Vertex();
  }

  @Test
  void constructs_default_vertex() {
    assertNull(bert.id());
    assertNull(bert.label());
    assertNull(bert.description());
    assertFalse(bert.fetched());
  }

  @Test
  void id_gets_sets() {
    bert.id("Q42");
    assertEquals("Q42", bert.id());
  }

  @Test
  void label_gets_sets() {
    bert.label("Douglas Adams");
    assertEquals("Douglas Adams", bert.label());
  }

  @Test
  void description_gets_sets() {
    bert.description("English science fiction writer and humorist (1952–2001)");
    assertEquals("English science fiction writer and humorist (1952–2001)", bert.description());
  }

  @Test
  void fetched_toggles() {
    bert.fetched(true);
    assertTrue(bert.fetched());
  }

  @Test
  void coords_gets_sets() {
    assertEquals(bert.coords(), new Point3D(), "Default Coords should be (0,0,0)");
    bert.coords(new Point3D(1.0, 2.0, 3.0));
    assertEquals(bert.coords().x, 1.0, "X");
    assertEquals(bert.coords().y, 2.0, "Y");
    assertEquals(bert.coords().z, 3.0, "Z");
  }

  @Test
  void isFetchedOrDate_fetched_working() {
    bert.fetched(true);
    assertTrue(bert.isFetchedOrDate());
  }

  @Test
  void isFetchedOrDate_date_working() {
    bert.label("Date target: id currently still unfetched");
    assertTrue(bert.isFetchedOrDate());
  }

  @Test
  void isFetchedOrDate_unfetched_ent_working() {
    bert.id("Fetch me!");
    assertFalse(bert.isFetchedOrDate());
  }

  @Test
  void isFetchedOrId_fetched_working() {
    bert.fetched(true);
    bert.id("Im already fetched!");
    assertTrue(bert.isFetchedOrId());
  }

  // @Test
  // public void updateUnfetchedValues_works_with_ItemDocument() {
  //   String lbl = "LBL";
  //   String dsc = "DSC";
  //   when(mItemDoc.findLabel(Constables.EN_LANG_WIKI_KEY)).thenReturn(lbl);
  //   when(mItemDoc.findDescription(Constables.EN_LANG_WIKI_KEY)).thenReturn(dsc);

  //   bert.updateUnfetchedValues(mItemDoc);

  //   assertEquals(lbl, bert.label());
  //   assertEquals(dsc, bert.description());
  //   assertTrue(bert.fetched());
  // }

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
