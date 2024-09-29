package edu.velv.wikidata_universe_api.models;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.velv.wikidata_universe_api.DataBuilder;
import io.vavr.Tuple2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GraphsetTests {

  private Graphset graphset;

  @Test
  void constructs_default_graphset() {
    graphset = new Graphset();
    assertNotNull(graphset.vertices());
    assertNotNull(graphset.edges());
    assertNotNull(graphset.properties());
  }

  @Test
  void constructs_graphset_using_fetched_example_data() {
    graphset = DataBuilder.simpleFetchedGraphset();
    assertEquals(6, graphset.vertexCount());
    assertEquals(3, graphset.propertyCount());
    assertEquals(7, graphset.edgeCount());
  }

  @Test
  void constructs_graphset_using_unfetched_example_data() {
    graphset = DataBuilder.simpleUnfetchedGraphset();
    assertEquals(4, graphset.vertexCount());
    assertEquals(2, graphset.propertyCount());
    assertEquals(3, graphset.edgeCount());
  }

  @Test
  void getIncidentEdges_finds_all_related_edges() {
    graphset = DataBuilder.simpleFetchedGraphset();
    Optional<Vertex> vert1 = graphset.getVertexById("Q1");
    Optional<Vertex> vert6 = graphset.getVertexById("Q6");
    assertEquals(1, graphset.getIncidentEdges(vert1.orElse(null)).size(), "The Q1 Vertex should have only one edge");
    assertEquals(3, graphset.getIncidentEdges(vert6.orElse(null)).size(), "The Q6 Vertex should have 3 edges");
  }

  @Test
  void getEndpoints_finds_correct_vertices() {
    graphset = DataBuilder.simpleFetchedGraphset();
    Optional<Edge> edge = graphset.edges().stream().findAny();
    Optional<Tuple2<Vertex, Vertex>> endpoints = graphset.getEndpoints(edge.orElse(null));
    assertEquals(edge.map(Edge::srcId).orElse(null), endpoints.map(Tuple2::_1).map(Vertex::id).orElse(null));
    assertEquals(edge.map(Edge::tgtId).orElse(null), endpoints.map(Tuple2::_2).map(Vertex::id).orElse(null));
  }

  @Test
  void getEndpoints_empty_when_edge_still_unfetched() {
    graphset = DataBuilder.simpleUnfetchedGraphset();
    Optional<Edge> edge = graphset.edges().stream().filter(e -> e.tgtId() == null).findFirst();
    Optional<Tuple2<Vertex, Vertex>> missingEndpoint = graphset.getEndpoints(edge.orElse(null));
    assertTrue(missingEndpoint.isEmpty(),
        "The intended Edge should be an unfetched Date target, and should not be returned here.");
  }

  // @Test
  // void removeInvalidSearchResultFromData_removes_entity_id_target() {
  //   graphset = DataBuilder.simpleFetchedGraphset();
  //   String q1 = "Q3";
  //   Optional<Vertex> origVertRef = graphset.getVertexById(q1);
  //   List<Edge> origEdgeRefs = getEdgesWhereStringReferenced(q1);

  //   assertTrue(origVertRef.isPresent(), "Unable to find vertex with id: " + q1 + " before removal");
  //   assertTrue(origEdgeRefs.size() > 0, "Unable to find any edge references for: " + q1 + " before removal");

  //   graphset.removeInvalidSearchResultFromData(q1);

  //   Optional<Vertex> vert = graphset.getVertexById(q1);
  //   List<Edge> edgeRefs = getEdgesWhereStringReferenced(q1);

  //   assertTrue(vert.isEmpty(), "Should remove original vertex from Graphset");
  //   assertTrue(edgeRefs.size() == 0, "Should remove all Edge references");
  // }

  // @Test
  // void removeInvalidSearchResultFromData_removes_label_target() {
  //   graphset = DataBuilder.simpleFetchedGraphset();
  //   String q1 = "Q1 label";
  //   Optional<Vertex> origVertRef = graphset.getVertexByLabel(q1);

  //   assertTrue(origVertRef.isPresent(), "Unable to find vertex with label: " + q1 + " before removal");

  //   graphset.removeInvalidSearchResultFromData(q1);

  //   Optional<Vertex> vertRef = graphset.getVertexByLabel(q1);
  //   List<Edge> edgeRefs = getEdgesWhereStringReferenced(q1);

  //   assertTrue(vertRef.isEmpty(), "Should remove original vertex from Graphset");
  //   assertTrue(edgeRefs.size() == 0, "Should remove all Edge references");
  // }

  // @Test
  // void removeInvalidSearchResultFromData_removes_property_id_target() {
  //   graphset = DataBuilder.simpleFetchedGraphset();
  //   String p1 = "P1";
  //   Optional<Property> origPropRef = graphset.getPropertyById(p1);
  //   List<Edge> origEdgeRefs = getEdgesWhereStringReferenced(p1);

  //   assertTrue(origPropRef.isPresent(), "Unable to find property " + p1 + " before removal");
  //   assertTrue(origEdgeRefs.size() > 0, "Unable to find any edge references for: " + p1 + " before removal");

  //   graphset.removeInvalidSearchResultFromData(p1);

  //   Optional<Property> propRef = graphset.getPropertyById(p1);
  //   List<Edge> edgeRefs = getEdgesWhereStringReferenced(p1);

  //   assertTrue(propRef.isEmpty(), "Should remove original vertex from Graphset");
  //   assertTrue(edgeRefs.size() == 0, "Should remove all Edge references");
  // }

  /**
   * Helper checks all edge attribute values for mention of the provided target value and returns them.
   * @param String tgt
   * @return a list of edges if any exists
   */
  public List<Edge> getEdgesWhereStringReferenced(String id) {
    return graphset.edges().stream()
        .filter(e -> {
          boolean srcMatch = e.srcId().equals(id);
          boolean tgtMatch = e.tgtId() != null && e.tgtId().equals(id);
          boolean propMatch = e.tgtId() != null && e.propertyId().equals(id);
          boolean lblMatch = e.label() != null && e.propertyId().equals(id);

          return srcMatch || tgtMatch || propMatch || lblMatch;
        })
        .collect(Collectors.toList());
  }

}