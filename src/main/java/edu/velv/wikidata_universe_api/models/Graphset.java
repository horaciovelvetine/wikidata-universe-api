package edu.velv.wikidata_universe_api.models;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.vavr.Tuple2;

import java.util.Optional;
import java.util.Set;
import java.util.Collection;

/**
 * Represents a graph set that contains vertices, edges, and properties.
 * With <3 & credit to JUNG & it's contributors
 * @https://github.com/jrtom/jung - thanks!
 */
public class Graphset {
  protected Set<Vertex> vertices = ConcurrentHashMap.newKeySet();
  protected Set<Edge> edges = ConcurrentHashMap.newKeySet();
  protected Set<Property> properties = ConcurrentHashMap.newKeySet();

  public Graphset() {
    //default constructor
  }

  public Graphset(Collection<Vertex> vertices, Collection<Edge> edges, Collection<Property> properties) {
    this.vertices.addAll(vertices);
    this.edges.addAll(edges);
    this.properties.addAll(properties);
  }

  public Set<Vertex> vertices() {
    return vertices;
  }

  public Set<Edge> edges() {
    return edges;
  }

  public Set<Property> properties() {
    return properties;
  }

  public Integer vertexCount() {
    return vertices.size();
  }

  public Integer edgeCount() {
    return edges.size();
  }

  public Integer propertyCount() {
    return properties.size();
  }

  /**
   * Validates a new Vertex has no existing duplicate before adding to the Graphset
   */
  public void addVertex(Vertex v) {
    Optional<Vertex> vertData = getVertexById(v.id());
    if (vertData.isEmpty()) {
      vertices.add(v);
    }
  }

  /**
   * Validates a new Edge has no existing duplicate before adding to Graphset
   */
  public void addEdge(Edge e) {
    Optional<Edge> edgeData = getEdgeByEdge(e);
    if (edgeData.isEmpty()) {
      edges.add(e);
    }
  }

  /**
   * Validates a new Property has no existing duplicate before adding to the Graphset
   */
  public void addProperty(Property p) {
    Optional<Property> propData = getPropertyById(p.id());
    if (propData.isEmpty()) {
      properties.add(p);
    }
  }

  /**
   * @param id string id to look for
   * @return a Vertex, or empty if none exists with the given id
   */
  public Optional<Vertex> getVertexById(String id) {
    return vertices.stream().filter(v -> v.id().equals(id)).findAny();
  }

  /**
   * @param id string id to look for
   * @return a Property, or empty if none exists with the given id
   */
  public Optional<Property> getPropertyById(String id) {
    return properties.stream().filter(p -> p.id().equals(id)).findAny();
  }

  /**
   * @apiNote Slight variation to prevent adding duplicate edges
   * 
   * @param edge the edge to look for
   * @return an Edge, or empty if none exists matching the provided
   */
  private Optional<Edge> getEdgeByEdge(Edge ed) {
    return edges.stream().filter(e -> e.srcId().equals(ed.srcId()) && e.tgtId().equals(ed.tgtId())).findAny();
  }

  /**
   * @param id string id to look for
   * @return a set containing any incident edges which exist
   */
  public Set<Edge> getIncidentEdges(Vertex vert) {
    return edges.stream()
        .filter(e -> e.srcId().equals(vert.id()) || e.tgtId().equals(vert.id()))
        .collect(Collectors.toSet());
  }

  /**
   * @param e Edge to find the endpoints of
   * @return a Tuple containing the endpoings, or empty if either Vertex is not present
   */
  public Optional<Tuple2<Vertex, Vertex>> getEndpoints(Edge e) {
    Optional<Vertex> src = getVertexById(e.srcId());
    Optional<Vertex> tgt = getVertexById(e.tgtId());
    if (src.isEmpty() || tgt.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new Tuple2<Vertex, Vertex>(src.get(), tgt.get()));
  }

  

}
