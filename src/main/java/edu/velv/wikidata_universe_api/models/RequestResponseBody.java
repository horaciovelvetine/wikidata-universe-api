package edu.velv.wikidata_universe_api.models;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import edu.velv.wikidata_universe_api.errors.Err;

import java.awt.Dimension;

/**
 * Data store model used to 'format' the results of operations on a ClientRequest. Used to omit
 * and provide only needed data for the Client (application) and make data easily accessible.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RequestResponseBody {
  public String query = null;
  Dimension dimensions;
  public Collection<Vertex> vertices = new ArrayList<>();
  public Collection<Edge> edges = new ArrayList<>();
  public Collection<Property> properties = new ArrayList<>();
  LayoutConfig layoutConfig = null;
  Err err = null;

  public RequestResponseBody(Err error) {
    this.err = error;
  }

  public RequestResponseBody(ClientRequest request) {
    this.query = request.query();
    this.dimensions = request.dimensions();
    this.vertices = request.graph().vertices();
    this.edges = request.graph().edges();
    this.properties = request.graph().properties();
    this.layoutConfig = request.layoutConfig();
  }

  public RequestResponseBody(String apiStatusMessageString) {
    this.query = apiStatusMessageString;
    this.dimensions = new Dimension();
  }

  public RequestResponseBody(TutorialRequest request) {
    this.query = request.message();
    this.dimensions = request.dimensions();
    this.vertices = request.graph().vertices();
    this.edges = request.graph().edges();
    this.properties = request.graph().properties();
    this.layoutConfig = request.layoutConfig();
  }

  @Override
  public String toString() {
    return "{query: " + this.query + ", verts: " + vertices.size() + ", props:" + properties.size() + ", edges: "
        + edges.size() + "}";
  }

}
