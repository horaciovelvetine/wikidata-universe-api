package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;

import edu.velv.wikidata_universe_api._Utils.FailedTestMessageTemplates;
import edu.velv.wikidata_universe_api._Utils.TestDataBuilders;

public class PropertyTests implements TestDataBuilders, FailedTestMessageTemplates {
  private final String src_ = "@PropertyTests:: ";
  Property property;
  PropertyDocument mProppaDoc = mock(PropertyDocument.class);

  @BeforeEach
  void setupDefaultProperty() {
    property = new Property();
  }

  @Test
  void constructs_default_property() {
    Property prop = new Property();
    String msg = src_ + prop + val + shouldBe + "null on" + init;
    assertNull(prop.id(), msg);
    assertNull(prop.label(), msg);
    assertNull(prop.description(), msg);
    assertFalse(prop.fetched(), msg);
  }

  @Test
  void id_gets_sets() {
    property.id("P31");
    assertEquals("P31", property.id(), src_ + gs);
  }

  @Test
  void label_gets_sets() {
    property.label("instance of");
    assertEquals("instance of", property.label(), src_ + gs);
  }

  @Test
  void description_gets_sets() {
    String metaDesc = "that class of which this subject is a particular example and member; different from P279 (subclass of); for example: K2 is an instance of mountain; volcano is a subclass of mountain (and an instance of volcanic landform)";
    property.description(metaDesc);
    assertEquals(metaDesc, property.description(), src_ + gs);
  }

  @Test
  void fetched_toggles() {
    property.fetched(true);
    assertTrue(property.fetched(), src_ + gs);
  }

  @Test
  void updateUnfetchedValues_works_with_PropertyDocument() {
    String lbl = "LABEL? LABLE?";
    String dsc = "DESC";
    when(mProppaDoc.findLabel(en)).thenReturn(lbl);
    when(mProppaDoc.findDescription(en)).thenReturn(dsc);

    property.updateUnfetchedValues(mProppaDoc, en);

    assertEquals(lbl, property.label(), src_ + prop + ".label() " + shouldBe + lbl);
    assertEquals(dsc, property.description(), src_ + prop + ".description() " + shouldBe + dsc);
    assertTrue(property.fetched(), src_ + prop + ".fetched() " + shouldBe + "true");
  }

}
