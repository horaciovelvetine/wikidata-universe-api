package edu.velv.wikidata_universe_api.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl;
import org.wikidata.wdtk.datamodel.implementation.MonolingualTextValueImpl;

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;

import edu.velv.wikidata_universe_api.models.jung.Point3D;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Point2D;
import java.util.List;

public class VertexTests {
  private ItemDocument itemDoc;
  // private WbSearchEntitiesResult result;

  @BeforeEach
  public void setUp() {
    ItemIdValue itemId = new ItemIdValueImpl("Q1", "http://www.wikidata.org/entity/");
    List<MonolingualTextValue> labels = List.of(new MonolingualTextValueImpl("Earth", "en"));
    List<MonolingualTextValue> descriptions = List.of(new MonolingualTextValueImpl("planet", "en"));
    List<MonolingualTextValue> aliases = List.of(new MonolingualTextValueImpl("world", "en"));
    List<StatementGroup> statementGroups = List.of();
    List<SiteLink> siteLinks = List.of();
    long revisionId = 0;

    itemDoc = new ItemDocumentImpl(itemId, labels, descriptions, aliases, statementGroups, siteLinks, revisionId);

    // result = new WbSearchEntitiesResult("Q1", "Earth", "planet", "http://www.wikidata.org/entity/Q1");
  }

  @Test
  public void init_wDefaults_Jackson() {
    Vertex vertex = new Vertex();
    assertNull(vertex.id(), "Id should be null");
    assertNull(vertex.label(), "Id should be null");
    assertNull(vertex.description(), "Id should be null");
  }

  @Test
  public void init_wItemDocument_WDTK_Default() {
    Vertex vertex = new Vertex(itemDoc);
    assertEquals("Q1", vertex.id(), "Id should be Q1");
    // returns null when above should be "Earth", and verified is not null
    assertEquals("Earth", vertex.label(), "Label should be Earth");
    assertEquals("planet", vertex.description(), "Description should be planet");
  }

  @Test
  public void setsLabel() {
    Vertex vertex = new Vertex();
    vertex.setLabel("Earth");
    assertEquals("Earth", vertex.label(), "Label should be Earth");
  }

  @Test
  public void setsDescription() {
    Vertex vertex = new Vertex();
    vertex.setDescription("planet");
    assertEquals("planet", vertex.description(), "Description should be planet");
  }

  @Test
  public void setsLayoutCoords() {
    Vertex vertex = new Vertex();
    Point2D coords2D = new Point2D.Double(0.0, 0.0);
    Point3D coords3D = new Point3D(0.0, 0.0, 0.0);
    vertex.setLayoutCoords(coords2D, coords3D);
    assertEquals(coords2D, vertex.layoutCoords().coords2D(), "2D Coords should be (0.0, 0.0)");
    assertEquals(coords3D, vertex.layoutCoords().coords3D(), "3D Coords should be (0.0, 0.0, 0.0)");
  }
}