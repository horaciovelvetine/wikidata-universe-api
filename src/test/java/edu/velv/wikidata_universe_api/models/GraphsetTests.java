package edu.velv.wikidata_universe_api.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import io.vavr.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.velv.wikidata_universe_api._Utils.FailedTestMessageTemplates;
import edu.velv.wikidata_universe_api._Utils.TestDataBuilders;

public class GraphsetTests implements FailedTestMessageTemplates, TestDataBuilders {
  private final String src_ = "@GraphsetTests::";

  private Graphset graphset;

  @Test
  void constructs_default_empty_graphset() {
    graphset = new Graphset();
    checkGraphsetDataStorageNotNull();
    assertTrue(graphset.isEmpty(),
        src_ + "Default Graphset data: " + vert + c + edge + c + prop + "lists " + shouldBe + empty);
  }

  @Test
  void constructs_using_existing_data() {
    graphset = buildGraphset_generic();
    checkGraphsetDataStorageNotNull();
    assertEquals(5, graphset.vertexCount(), src_ + shouldBeEq);
    assertEquals(9, graphset.edgeCount(), src_ + shouldBeEq);
    assertEquals(graphset.propertyCount(), 3, src_ + shouldBeEq);
  }

  @Test
  void getIncidentEdges_finds_all_related_edges() {
    graphset = buildGraphset_generic();
    Optional<Vertex> vert1 = graphset.getVertexById("Q1");
    Optional<Vertex> vert5 = graphset.getVertexById("Q5");
    assertEquals(3, graphset.getIncidentEdges(vert1.orElse(null)).size(),
        src_ + "Q1's total incident edges" + shouldBeEq);
    assertEquals(5, graphset.getIncidentEdges(vert5.orElse(null)).size(),
        src_ + "Q5's total incident edges" + shouldBeEq);
  }

  @Test
  void getEndpoints_finds_correct_vertices() {
    graphset = buildGraphset_generic();
    Optional<Edge> edge = graphset.edges().stream().findAny();
    Optional<Tuple2<Vertex, Vertex>> endpoints = graphset.getEndpoints(edge.orElse(null));
    assertEquals(edge.map(Edge::srcId).orElse(null), endpoints.map(Tuple2::_1).map(Vertex::id).orElse(null));
    assertEquals(edge.map(Edge::tgtId).orElse(null), endpoints.map(Tuple2::_2).map(Vertex::id).orElse(null));
  }

  @Test
  void removeInvalidSearchResultFromData_removes_entity_id_target() {
    graphset = buildGraphset_generic();
    String q1 = "Q3";
    Optional<Vertex> origVertRef = graphset.getVertexById(q1);
    List<Edge> origEdgeRefs = getEdgesWhereStringReferenced(q1);

    assertTrue(origVertRef.isPresent(), src_ + shouldBe + ableToFind + vert + q1 + beforeRemoval);
    assertTrue(origEdgeRefs.size() > 0, src_ + shouldBe + ableToFind + q1 + relEdges + beforeRemoval);

    graphset.removeInvalidSearchResultFromData(q1);

    Optional<Vertex> vert = graphset.getVertexById(q1);
    List<Edge> edgeRefs = getEdgesWhereStringReferenced(q1);

    assertTrue(vert.isEmpty(), src_ + shouldBe + unableToFind + vert + q1 + afterRemoval);
    assertTrue(edgeRefs.size() == 0, src_ + shouldBe + unableToFind + q1 + relEdges + afterRemoval);
  }

  @Test
  void removeInvalidSearchResultFromData_removes_label_target() {
    graphset = buildGraphset_generic();
    String q1 = "Q1 label";
    Optional<Vertex> origVertRef = graphset.getVertexByLabel(q1);

    assertTrue(origVertRef.isPresent(), src_ + shouldBe + ableToFind + vert + q1 + beforeRemoval);

    graphset.removeInvalidSearchResultFromData(q1);

    Optional<Vertex> vertRef = graphset.getVertexByLabel(q1);
    List<Edge> edgeRefs = getEdgesWhereStringReferenced(q1);

    assertTrue(vertRef.isEmpty(), src_ + shouldBe + unableToFind + vert + q1 + afterRemoval);
    assertTrue(edgeRefs.size() == 0, src_ + shouldBe + unableToFind + q1 + relEdges + afterRemoval);
  }

  @Test
  void removeInvalidSearchResultFromData_removes_property_id_target() {
    graphset = buildGraphset_generic();
    String p1 = "P1";
    Optional<Property> origPropRef = graphset.getPropertyById(p1);
    List<Edge> origEdgeRefs = getEdgesWhereStringReferenced(p1);

    assertTrue(origPropRef.isPresent(), src_ + shouldBe + ableToFind + p1 + beforeRemoval);
    assertTrue(origEdgeRefs.size() > 0, src_ + shouldBe + ableToFind + p1 + relEdges + beforeRemoval);

    graphset.removeInvalidSearchResultFromData(p1);

    Optional<Property> propRef = graphset.getPropertyById(p1);
    List<Edge> edgeRefs = getEdgesWhereStringReferenced(p1);

    assertTrue(propRef.isEmpty(), src_ + shouldBe + unableToFind + vert + p1 + afterRemoval);
    assertTrue(edgeRefs.size() == 0, src_ + shouldBe + unableToFind + p1 + relEdges + afterRemoval);
  }

  /**
   * Helper checks all present edges for a possible mention of the provided value
   */
  private List<Edge> getEdgesWhereStringReferenced(String value) {
    return graphset.edges().stream()
        .filter(e -> {
          boolean srcMatch = e.srcId().equals(value);
          boolean tgtMatch = e.tgtId() != null && e.tgtId().equals(value);
          boolean propMatch = e.tgtId() != null && e.propertyId().equals(value);
          boolean lblMatch = e.label() != null && e.propertyId().equals(value);

          return srcMatch || tgtMatch || propMatch || lblMatch;
        })
        .collect(Collectors.toList());
  }

  /**
   * Helper checks that List objects for data storage are not null
   */
  private void checkGraphsetDataStorageNotNull() {
    assertNotNull(graphset.vertices(), src_ + unableToFind + "graphset.vertices()" + val);
    assertNotNull(graphset.properties(), src_ + unableToFind + "graphset.properties()" + val);
    assertNotNull(graphset.edges(), src_ + unableToFind + "graphset.edges()" + val);
  }

}
