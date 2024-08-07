package edu.velv.wikidata_universe_api.models;

import java.util.Collection;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import edu.velv.wikidata_universe_api.models.wikidata.FetchQueue;
import edu.velv.wikidata_universe_api.models.wikidata.Property;
import edu.velv.wikidata_universe_api.models.jung_ish.Vertex;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.models.jung_ish.Edge;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ResponseBody {
  Err err;
  Collection<Vertex> vertices;
  Collection<Edge> edges;
  Collection<Property> properties;
  FetchQueue queue;

  // for defaulting...
  public ResponseBody() {
    this.err = null;
    this.vertices = new ArrayList<>();
    this.edges = new ArrayList<>();
    this.properties = new ArrayList<>();
    this.queue = new FetchQueue();
  }

  public ResponseBody(Err e) {
    this.err = e;
    this.vertices = new ArrayList<>();
    this.edges = new ArrayList<>();
    this.properties = new ArrayList<>();
    this.queue = new FetchQueue();
  }

  public ResponseBody(ClientSession session) {
    this.err = null;
    this.vertices = session.graphset().vertices();
    this.edges = session.graphset().edges();
    this.properties = session.wikidataManager().properties();
    this.queue = session.wikidataManager().fetchQueue();
  }
}
