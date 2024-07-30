package edu.velv.wikidata_universe_api.models.jung_ish;

import java.util.Set;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

//With <3 & credit to JUNG & it's contributors
// @https://github.com/jrtom/jung - thanks!
public class Graphset<V, E> {
  protected Set<V> vertices;
  protected Set<E> edges;

  public Graphset() {
    this.vertices = ConcurrentHashMap.newKeySet();
    this.edges = ConcurrentHashMap.newKeySet();
  }

  public Set<V> vertices() {
    return vertices;
  }

  public void addVertex(V v) {
    if (vertices.contains(v))
      return;
    vertices.add(v);
  }

  public Collection<E> edges() {
    return edges;
  }

  public void addEdge(E e) {
    if (edges.contains(e))
      return;
    edges.add(e);
  }

  @Override
  public String toString() {
    return "Graphset={ vertices=" + vertices.size() + ", " + "edges="
        + edges.size() + " }";
  }
}
