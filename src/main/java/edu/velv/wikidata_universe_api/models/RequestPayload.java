package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;

import java.util.Collection;

import edu.velv.wikidata_universe_api.models.wikidata.FetchQueue;
import edu.velv.wikidata_universe_api.models.wikidata.Property;
import edu.velv.wikidata_universe_api.models.jung_ish.Vertex;
import edu.velv.wikidata_universe_api.models.jung_ish.Edge;

public class RequestPayload {
  String query;
  Dimension dimensions;
  Collection<Vertex> vertices;
  Collection<Edge> edges;
  Collection<Property> properties;
  FetchQueue queue;

  public RequestPayload(String query, Dimension dimensions, Collection<Vertex> vertices, Collection<Edge> edges, Collection<Property> properties, FetchQueue queue) {
    this.query = query;
    this.dimensions = dimensions;
    this.vertices = vertices;
    this.edges = edges;
    this.properties = properties;
    this.queue = queue;
  }

  public String query() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public Dimension dimensions() {
    return dimensions;
  }

  public void setDimensions(Dimension dimensions) {
    this.dimensions = dimensions;
  }

  public Collection<Vertex> vertices() {
    return vertices;
  }

  public void setVertices(Collection<Vertex> vertices) {
    this.vertices = vertices;
  }

  public Collection<Edge> edges() {
    return edges;
  }

  public void setEdges(Collection<Edge> edges) {
    this.edges = edges;
  }

  public Collection<Property> properties() {
    return properties;
  }

  public void setProperties(Collection<Property> properties) {
    this.properties = properties;
  }

  public FetchQueue queue() {
    return queue;
  }

  public void setQueue(FetchQueue queue) {
    this.queue = queue;
  }
}
