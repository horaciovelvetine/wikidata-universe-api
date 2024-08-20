package edu.velv.wikidata_universe_api.models.jung_ish;

import java.util.Set;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.vavr.Tuple2;

//With <3 & credit to JUNG & it's contributors
// @https://github.com/jrtom/jung - thanks!
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Graphset {
  protected Set<Vertex> vertices;
  protected Set<Edge> edges;
  @JsonIgnore
  protected Vertex origin;

  public Graphset() {
    this.vertices = ConcurrentHashMap.newKeySet();
    this.edges = ConcurrentHashMap.newKeySet();
  }

  public Graphset(Collection<Vertex> inpVerts, Collection<Edge> inpEdges) {
    this.vertices = ConcurrentHashMap.newKeySet();
    this.edges = ConcurrentHashMap.newKeySet();
    vertices.addAll(inpVerts);
    edges.addAll(inpEdges);
  }

  public Collection<Vertex> vertices() {
    return vertices;
  }

  public void addVertex(Vertex v) {
    if (vertices.contains(v))
      return;
    vertices.add(v);
  }

  public Collection<Edge> edges() {
    return edges;
  }

  public void addEdge(Edge e) {
    if (edges.contains(e))
      return;
    edges.add(e);
  }

  public Vertex getOriginRef() {
    return origin;
  }

  public int vertexCount() {
    return vertices().size();
  }

  public void setOriginRef(Vertex v) {
    this.origin = v;
  }

  public Optional<Tuple2<Vertex, Vertex>> getEndpoints(Edge e) {
    Optional<Vertex> one = getVertex(e.srcId());
    Optional<Vertex> two = getVertex(e.tgtId());
    if (one.isEmpty() || two.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new Tuple2<Vertex, Vertex>(one.get(), two.get()));
  }

  @Override
  public String toString() {
    return "Graphset={ vertices=" + vertices.size() + ", " + "edges="
        + edges.size() + " }";
  }

  private Optional<Vertex> getVertex(String id) {
    return vertices().stream().filter(v -> v.id().equals(id)).findAny();
  }
}
