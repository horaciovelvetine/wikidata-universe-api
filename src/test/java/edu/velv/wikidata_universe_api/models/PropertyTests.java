package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

// import edu.velv.wikidata_universe_api.Constables;

public class PropertyTests {
  Property property;
  PropertyDocument mProppaDoc = mock(PropertyDocument.class);

  @BeforeEach
  void setupDefaultProperty() {
    property = new Property();
  }

  @Test
  void constructs_default_property() {
    Property prop = new Property();
    assertNull(prop.id());
    assertNull(prop.label());
    assertNull(prop.description());
    assertFalse(prop.fetched());
  }

  @Test
  void id_gets_sets() {
    property.id("P31");
    assertEquals("P31", property.id());
  }

  @Test
  void label_gets_sets() {
    property.label("instance of");
    assertEquals("instance of", property.label());
  }

  @Test
  void description_gets_sets() {
    String metaDesc = "that class of which this subject is a particular example and member; different from P279 (subclass of); for example: K2 is an instance of mountain; volcano is a subclass of mountain (and an instance of volcanic landform)";
    property.description(metaDesc);
    assertEquals(metaDesc, property.description());
  }

  @Test
  void fetched_toggles() {
    property.fetched(true);
    assertTrue(property.fetched());
  }

  // @Test
  // void updateUnfetchedValues_works_with_PropertyDocument() {
  //   String lbl = "LABEL? LABLE?";
  //   String dsc = "DESC";
  //   when(mProppaDoc.findLabel(Constables.EN_LANG_WIKI_KEY)).thenReturn(lbl);
  //   when(mProppaDoc.findDescription(Constables.EN_LANG_WIKI_KEY)).thenReturn(dsc);

  //   property.updateUnfetchedValues(mProppaDoc);

  //   assertEquals(lbl, property.label());
  //   assertEquals(dsc, property.description());
  //   assertTrue(property.fetched());
  // }

}
