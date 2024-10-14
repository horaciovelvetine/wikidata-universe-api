package edu.velv.wikidata_universe_api.models;

import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.LabeledDocument;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import io.vavr.Tuple2;

/**
 * Represents a graph set that contains vertices, edges, and properties.
 * With <3 & credit to JUNG & it's contributors
 * @https://github.com/jrtom/jung - thanks!
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Graphset {
  protected Set<Vertex> vertices = ConcurrentHashMap.newKeySet();
  protected Set<Edge> edges = ConcurrentHashMap.newKeySet();
  protected Set<Property> properties = ConcurrentHashMap.newKeySet();

  public Graphset() {
    //default constructor
  }

  public Graphset(List<Vertex> payloadVerts, List<Edge> payloadEdges, List<Property> payloadProps) {
    //? prevents unintentional duplicates
    for (Vertex vert : payloadVerts) {
      addVertex(vert);
    }
    for (Edge edge : payloadEdges) {
      addEdge(edge);
    }
    for (Property prop : payloadProps) {
      addProperty(prop);
    }
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

  public Integer propertyCount() {
    return properties.size();
  }

  public Integer edgeCount() {
    return edges.size();
  }

  public boolean hasNoExistingVertices() {
    return vertexCount() == 0;
  }

  /**
   * Checks Vertices, Edges & Property are empty.
   */
  public boolean isEmpty() {
    return properties.isEmpty() || vertices.isEmpty() || edges.isEmpty();
  }

  /**
   * Validates a new Vertex has no existing duplicate before adding to the Graphset by checking if 
   * either the vertex id or label already exists.
   */
  public void addVertex(Vertex v) {
    Optional<Vertex> vertData;
    if (v.id() == null) {
      vertData = getVertexByLabel(v.label());
    } else {
      vertData = getVertexById(v.id());
    }
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
   * @return a Vertex, or empty if none exists with the given id
   */
  public Optional<Vertex> getVertexById(String id) {
    return vertices.stream().filter(v -> {
      String vertId = v.id();
      return vertId != null && vertId.equals(id);
    }).findAny();
  }

  /**
   * @apiNote for unfetched Date Vertices
   * @return a Vertex, or empty if none exists with the given label;
   */
  public Optional<Vertex> getVertexByLabel(String label) {
    return vertices.stream().filter(v -> {
      String vertLabel = v.label();
      return vertLabel != null && vertLabel.equals(label);
    }).findAny();
  }

  /**
   * Combines calling get's by Id & Label into one call based on the values present on the provided Vertex
   * @return A Vertex, or empty if none exists which match the provided Vertex details
   */
  public Optional<Vertex> getVertexByIdOrLabel(EntityDocument doc) {
    Optional<Vertex> existingVert = getVertexById(doc.getEntityId().getId());
    if (existingVert.isEmpty() && doc instanceof LabeledDocument) {
      LabeledDocument lDoc = (LabeledDocument) doc;
      existingVert = getVertexByLabel(lDoc.findLabel("en"));
    }
    return existingVert;
  }

  /**
   * @param id string id to look for
   * @return a Property, or empty if none exists with the given id
   */
  public Optional<Property> getPropertyById(String id) {
    return properties.stream().filter(p -> p.id().equals(id)).findAny();
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
   * Gets any Vertex object from the set where fetched is falsey
   */
  public List<Vertex> getUnfetchedVertices() {
    return vertices().stream().filter(v -> !v.fetched()).toList();
  }

  /**
   * Gets any Property object from the set where fetched is falsey
   */
  public List<Property> getUnfetchedProperties() {
    return properties().stream().filter(p -> !p.fetched()).toList();
  }

  public Vertex getOriginVertex() {
    return vertices.stream()
        .filter(Vertex::fetched)
        .findFirst()
        .orElse(null);
  }

  /**
   * Removes the given target value from mention in the data set by searching for any entitiy where it might be mentioned and
   * removing it. The target values are provided from Wikidata so this should be a particularly rare call/result. There are no
   * additional details to handle from this target.
   */
  public void removeInvalidSearchResultFromData(String targetValue) {
    // Remove vertices with matching id or label
    if (targetValue != null) {
      vertices.removeIf(v -> (v.id() != null && v.id().equals(targetValue)) ||
          (v.label() != null && v.label().equals(targetValue)));

      // Remove properties with matching id
      properties.removeIf(p -> p.id() != null && p.id().equals(targetValue));

      // Remove edges with matching tgtId, srcId, propertyId, or label
      edges.removeIf(e -> {
        boolean srcMatch = e.srcId() != null && e.srcId().equals(targetValue);
        boolean tgtMatch = e.tgtId() != null && e.tgtId().equals(targetValue);
        boolean propMatch = e.propertyId() != null && e.propertyId().equals(targetValue);
        boolean lblMatch = e.label() != null && e.label().equals(targetValue);

        return srcMatch || tgtMatch || propMatch || lblMatch;
      });
    }
  }

  /**
   * Iterates over the current set of vertices and updates their coordinates to those calculated by
   * the provided layout.
   */
  public void updateVertexCoordinatesFromLayout(FR3DLayout layout) {
    for (Vertex iVertex : vertices) {
      iVertex.coords(layout.apply(iVertex));
    }
  }

  /**
  * @return true if each Vertex in the genReqData's graph has a unique coordinate value
  */
  public boolean vertexCoordsUniqueForEach() {
    Map<Point3D, Vertex> coordsMap = new HashMap<>();
    for (Vertex v : vertices()) {
      Point3D coords = v.coords();
      if (coordsMap.containsKey(coords)) {
        return false;
      }
      coordsMap.put(coords, v);
    }
    return true;
  }

  /**
  * @apiNote Slight variation to prevent adding duplicate edges, only used to grab and update privately
  * 
  * @param edge the edge to look for
  * @return an Edge, or empty if none exists matching the provided
  */
  private Optional<Edge> getEdgeByEdge(Edge ed) {
    return edges.stream().filter(e -> {
      String tgtId = e.tgtId();
      return tgtId != null && e.srcId().equals(ed.srcId()) && tgtId.equals(ed.tgtId());
    }).findAny();
  }

}
