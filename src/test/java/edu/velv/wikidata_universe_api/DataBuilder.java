package edu.velv.wikidata_universe_api;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.Graphset;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.models.Property;

public class DataBuilder {
  static ItemDocumentImpl mItemDoc = mock(ItemDocumentImpl.class);
  static ItemIdValue mItemId = mock(ItemIdValue.class);
  static PropertyDocument mPropDoc = mock(PropertyDocument.class);
  static PropertyIdValue mPropId = mock(PropertyIdValue.class);
  // duplicated vlaues
  static final String lbl = " label";
  static final String des = " description";
  static final String en = "en";

  /**
   * @return Graphset consisting of 6 vertices, 3 properties, and 7 edges where all of the data is completely fetched.
   */
  public static Graphset simpleFetchedGraphset() {
    List<Vertex> verts = List.of(vertex("1"), vertex("2"), vertex("3"), vertex("4"), vertex("5"), vertex("6"));
    List<Property> props = List.of(property("1"), property("2"), property("3"));
    // uses arr index => "Q || P" + "i + 1" to get ent id
    List<Edge> edges = List.of(
        edge(verts.get(0), verts.get(5), props.get(0)),
        edge(verts.get(5), verts.get(3), props.get(0)),
        edge(verts.get(5), verts.get(1), props.get(1)),
        edge(verts.get(1), verts.get(2), props.get(2)),
        edge(verts.get(1), verts.get(3), props.get(1)),
        edge(verts.get(3), verts.get(4), props.get(1)),
        edge(verts.get(2), verts.get(4), props.get(2)));
    return new Graphset(verts, edges, props);
  }

  /**
   * @return Graphset with 4 vertices (2 unfetched (1 date/1ent)), 2 properties (1 unfetched), and 3 edges (1 unfetched date)
   * 
   */
  public static Graphset simpleUnfetchedGraphset() {
    List<Vertex> verts = List.of(vertex("1"), vertexUnfEnt("2"), vertexUnfDate("3" + lbl), vertex("4"));
    List<Property> props = List.of(property("1"), propertyUnf("2"));
    List<Edge> edges = List.of(
        edge(verts.get(0), verts.get(1), props.get(0)),
        edgeUnfDate(verts.get(1), verts.get(2), props.get(0)),
        edge(verts.get(1), verts.get(3), props.get(1)));
    return new Graphset(verts, edges, props);
  }

  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  private static Vertex vertex(String idSfx) {
    String id = "Q" + idSfx;
    Vertex vert = new Vertex();
    vert.id(id);
    vert.fetched(true);
    return vert;
  }

  private static Vertex vertexUnfEnt(String idSfx) {
    Vertex vert = new Vertex();
    vert.id("Q" + idSfx);
    return vert;
  }

  private static Vertex vertexUnfDate(String label) {
    Vertex vert = new Vertex();
    vert.label(label);
    return vert;
  }

  private static Property property(String idSfx) {
    String id = "P" + idSfx;
    Property prop = new Property();
    prop.id(id);
    prop.fetched(true);
    return prop;
  }

  private static Property propertyUnf(String idSfx) {
    Property prop = new Property();
    prop.id("P" + idSfx);
    return prop;
  }

  private static Edge edge(Vertex src, Vertex tgt, Property prop) {
    Edge e = new Edge();
    e.srcId(src.id());
    e.tgtId(tgt.id());
    e.propertyId(prop.id());
    e.fetched(true);
    return e;
  }

  private static Edge edgeUnfDate(Vertex src, Vertex dateTgt, Property prop) {
    Edge e = new Edge();
    e.srcId(src.id());
    e.label(dateTgt.label());
    e.propertyId(prop.id());
    return e;
  }

}
