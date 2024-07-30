package edu.velv.wikidata_universe_api.models.jung_ish;

import java.util.Set;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.vavr.Tuple2;

//With <3 & credit to JUNG & it's contributors
// @https://github.com/jrtom/jung - thanks!
public class Graphset {
  protected Set<Vertex> vertices;
  protected Set<Edge> edges;

  public Graphset() {
    this.vertices = ConcurrentHashMap.newKeySet();
    this.edges = ConcurrentHashMap.newKeySet();
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

  public int getVertexCount() {
    return vertices().size();
  }

  public Optional<Tuple2<Vertex, Vertex>> getEndpoints(Edge e) {
    Optional<Vertex> one = getVertex(e.srcEntId());
    Optional<Vertex> two = getVertex(e.tgtEntId());
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
