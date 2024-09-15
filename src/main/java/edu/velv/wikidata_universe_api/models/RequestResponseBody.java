package edu.velv.wikidata_universe_api.models;

import java.util.ArrayList;
import java.util.Collection;

import edu.velv.wikidata_universe_api.ClientRequest;
import edu.velv.wikidata_universe_api.errors.Err;

import java.awt.Dimension;

/**
 * Data store model used to 'format' the results of operations on a ClientRequest. Used to omit
 * and provide only needed data for the Client (application) and make data easily accessible.
 */
public class RequestResponseBody {
  String query;
  Dimension dimensions;
  Collection<Vertex> vertices = new ArrayList<>();
  Collection<Edge> edges = new ArrayList<>();
  Collection<Property> properties = new ArrayList<>();
  Err err;

  public RequestResponseBody(Err error) {
    this.err = error;
  }

  public RequestResponseBody(ClientRequest request) {
    this.err = null;
    this.query = request.query();
    this.dimensions = request.dimensions();
    this.vertices = request.graph().vertices();
    this.edges = request.graph().edges();
    this.properties = request.graph().properties();
  }
}