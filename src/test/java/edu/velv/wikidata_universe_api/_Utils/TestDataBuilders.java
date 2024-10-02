package edu.velv.wikidata_universe_api._Utils;

import java.awt.Dimension;
import java.util.List;

import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.Graphset;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.models.Property;
import edu.velv.wikidata_universe_api.models.RequestPayloadData;

public interface TestDataBuilders {
  final String la = "label";
  final String de = "description";

  /**
   * @return a Vertex with the provided ID & default fetched values
   */
  default Vertex buildVertex(Integer id) {
    Vertex v = new Vertex();
    String qid = "Q" + id;
    v.id(qid);
    v.label(qid + " " + la);
    v.description(de);
    v.fetched(true);
    return v;
  }

  /**
   * @return a Property with the provided ID & default fetched values
   */
  default Property buildProperty(Integer id) {
    Property p = new Property();
    String pid = "P" + id;
    p.id(pid);
    p.label(pid + " " + la);
    p.description(de);
    p.fetched(true);
    return p;
  }

  /**
   * @return an Edge with the provided src, tgt, and property ID values.
   */
  default Edge buildEdge(Integer sId, Integer tId, Integer pId) {
    Edge e = new Edge();
    e.srcId("Q" + sId);
    e.tgtId("Q" + tId);
    e.propertyId("P" + pId);
    e.fetched(true);
    return e;
  }

  /**
   * @return a 300 by 300 unit Dimension obj
   */
  default Dimension buildGenericDimensions() {
    return buildDimension(300, 300);
  }

  /**
   * @return a Dimension obj of the provided width and height 
   */
  default Dimension buildDimension(int width, int height) {
    return new Dimension(width, height);
  }

  default RequestPayloadData buildGenericRequestPayload() {
    Graphset gs = buildGenericGraphset();
    List<Vertex> verts = gs.vertices().stream().toList();
    List<Edge> edges = gs.edges().stream().toList();
    List<Property> props = gs.properties().stream().toList();
    return new RequestPayloadData("generic_graphset_test", buildGenericDimensions(), verts, edges,
        props);
  }

  /**
   * Creates a testable graphset of 5 vertices, 3 properties, and 9 edges. Each entity is fetched and considered complete withd default values applied for labels and descriptions.
   */
  default Graphset buildGenericGraphset() {
    List<Vertex> verts = List.of(
        buildVertex(1),
        buildVertex(2),
        buildVertex(3),
        buildVertex(4),
        buildVertex(5));
    List<Property> props = List.of(
        buildProperty(1),
        buildProperty(2),
        buildProperty(3));
    List<Edge> edges = List.of(
        buildEdge(1, 2, 1),
        buildEdge(3, 5, 1),
        buildEdge(1, 5, 1),
        buildEdge(1, 3, 2),
        buildEdge(4, 5, 2),
        buildEdge(2, 3, 2),
        buildEdge(2, 4, 3),
        buildEdge(2, 5, 3),
        buildEdge(5, 2, 3));
    return new Graphset(verts, edges, props);
  }
}
