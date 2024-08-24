package edu.velv.wikidata_universe_api.models;

import java.awt.Dimension;

import java.util.Collection;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import edu.velv.wikidata_universe_api.models.wikidata.FetchQueue;
import edu.velv.wikidata_universe_api.models.wikidata.Property;
import edu.velv.wikidata_universe_api.models.jung_ish.Vertex;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.errors.Err.DefaultResponseBodyError;
import edu.velv.wikidata_universe_api.models.jung_ish.Edge;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RequestResponseBody {
  Err err;
  String query;
  Collection<Vertex> vertices;
  Collection<Edge> edges;
  Collection<Property> properties;
  FetchQueue queue;
  Dimension dimensions;

  // for defaulting...
  public RequestResponseBody() {
    this.err = new DefaultResponseBodyError("No Body in Default response");
    this.vertices = new ArrayList<>();
    this.edges = new ArrayList<>();
    this.properties = new ArrayList<>();
    this.queue = new FetchQueue();
  }

  public RequestResponseBody(Err e) {
    this.err = e;
    this.vertices = new ArrayList<>();
    this.edges = new ArrayList<>();
    this.properties = new ArrayList<>();
    this.queue = new FetchQueue();
  }

  public RequestResponseBody(ClientRequest session) {
    this.err = null;
    this.vertices = session.graphset().vertices();
    this.edges = session.graphset().edges();
    this.properties = session.wikidataManager().properties();
    this.queue = session.wikidataManager().fetchQueue();
    this.query = session.query();
    this.dimensions = session.subjectDimensions();
  }

  public String query() {
    return this.query;
  }

  public Collection<Vertex> vertices() {
    return this.vertices;
  }

  public Collection<Edge> edges() {
    return this.edges;
  }

  public Collection<Property> properties() {
    return this.properties;
  }

  public FetchQueue queue() {
    return this.queue;
  }

  public Err err() {
    return this.err;
  }

  public Dimension dimensions() {
    return this.dimensions;
  }
}
