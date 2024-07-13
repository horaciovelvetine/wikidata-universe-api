package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.implementation.DatatypeIdImpl;
import org.wikidata.wdtk.datamodel.implementation.MonolingualTextValueImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyIdValueImpl;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;

public class PropertyTests {
  private PropertyDocument propertyDoc;

  @BeforeEach
  public void setUp() {
    PropertyIdValueImpl propertyId = new PropertyIdValueImpl("P1", "http://www.wikidata.org/entity/");
    List<MonolingualTextValue> labels = List.of(new MonolingualTextValueImpl("test label", "en"));
    List<MonolingualTextValue> descriptions = List.of(new MonolingualTextValueImpl("test description", "en"));
    List<MonolingualTextValue> aliases = List.of(new MonolingualTextValueImpl("type", "en"));
    List<StatementGroup> statementGroups = List.of();
    DatatypeIdValue datatype = new DatatypeIdImpl(null, "wikibase-property");
    long revisionId = 0;

    propertyDoc = new PropertyDocumentImpl(propertyId, labels, descriptions, aliases, statementGroups, datatype,
        revisionId);
  }

  @Test
  public void init_wDefaults_Jackson() {
    Property property = new Property();
    assertNull(property.id(), "id should be null");
    assertNull(property.label(), "label should be null");
    assertNull(property.description(), "description should be null");
  }

  @Test
  public void init_wPropertyDocument_WDTK_Default() {
    Property property = new Property(propertyDoc);
    assertEquals("P1", property.id(), "id should be: P1");
    assertEquals("test label", property.label(), "label should be: test label");
    assertEquals("test description", property.description(), "description should be: test description");
  }

  @Test
  public void setsId() {
    Property property = new Property();
    property.setId("P1");
    assertEquals("P1", property.id(), "id should be: P1");
  }

  @Test
  public void setsLabel() {
    Property property = new Property();
    property.setLabel("test label");
    assertEquals("test label", property.label(), "label should be: test label");
  }

  @Test
  public void setsDescription() {
    Property property = new Property();
    property.setDescription("test description");
    assertEquals("test description", property.description(), "description should be: test description");
  }

}
