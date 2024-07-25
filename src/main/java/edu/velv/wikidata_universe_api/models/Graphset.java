package edu.velv.wikidata_universe_api.models;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Graphset {
  private String originalQuery;

  private Set<Vertex> vertices;
  private Set<Edge> edges;
  private Set<Property> properties;

  public Graphset() {
    this.vertices = ConcurrentHashMap.newKeySet();
    this.edges = ConcurrentHashMap.newKeySet();
    this.properties = ConcurrentHashMap.newKeySet();
  }

  public String originalQuery() {
    return originalQuery;
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

  public void addVertex(Vertex v) {
    if (vertices().contains(v)) {
      return;
    }
    vertices.add(v);
  }

  public void addEdge(Edge e) {
    if (edges().contains(e)) {
      return;
    }
    edges.add(e);
  }
}
