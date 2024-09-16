package edu.velv.wikidata_universe_api.models;

import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.vavr.Tuple2;

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

  /**
   * Checks if any of the data elements (Vertices & Properties) still have unfetched details
   */
  public boolean allDataFetched() {
    for (Vertex v : vertices) {
      if (!v.fetched()) {
        return false;
      }
    }

    for (Property p : properties) {
      if (!p.fetched()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Creates a set of strings which represent Wikidata Entities with unfetched details, checking both vertices and properties.
   * Ignores vertices which represent a date target, returning only ID values (e.g. "Q123", "P123")
   * 
   * @return a set of Wikidata searchable string targets
   */
  public List<String> getUnfetchedEntityIDTargetBatch() {
    List<String> batch = new ArrayList<>();
    int batchSize = 0;

    batch = addUnfetchedPropertyIdsToBatch(batch, batchSize);
    if (batchSize == 50)
      return batch;

    return addUnfetchedVertexIdsToBatch(batch, batchSize);
  }

  /**
   * Creates a set of string which represent Wikidata date Entities with details 
   */
  public List<String> getUnfetchedDateTargetBatch() {
    List<String> batch = new ArrayList<>();
    int count = 0;

    for (Vertex v : vertices) {
      if (v.isFetchedOrId())
        continue;

      batch.add(v.label());
      count++;
      if (count == 50)
        return batch;
    }
    return batch;
  }

  /**
   * Removes the given target value from mention in the data set by searching for any entitiy where it might be mentioned and
   * removing it. The target values are provided from Wikidata so this should be a particularly rare call/result. There are no
   * additional details to handle from this target.
   */
  public void removeInvalidSearchResultFromData(String targetValue) {
    // Remove vertices with matching id or label
    vertices.removeIf(v -> v.id().equals(targetValue) || v.label().equals(targetValue));

    // Remove properties with matching id
    properties.removeIf(p -> p.id().equals(targetValue));

    // Remove edges with matching tgtId, srcId, propertyId, or label
    edges.removeIf(e -> e.tgtId().equals(targetValue) || e.srcId().equals(targetValue) || e.propertyId().equals(targetValue) || e.label().equals(targetValue));
  }

  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  /**
   * Filters vertices for unfetched entities where an id() values is present, then updates the batch with this target info
   */
  private List<String> addUnfetchedVertexIdsToBatch(List<String> batch, int count) {
    for (Vertex v : vertices) {
      if (v.isFetchedOrDate())
        continue;
      batch.add(v.id());
      count++;
      if (count == 50)
        return batch;
    }
    return batch;
  }

  /**
   * Filters properties for unfetched entities, then updates the batch with this target info
   */
  private List<String> addUnfetchedPropertyIdsToBatch(List<String> batch, int count) {
    for (Property p : properties) {
      if (p.fetched())
        continue;
      batch.add(p.id());
      count++;
      if (count == 50)
        return batch;
    }
    return batch;
  }

}
