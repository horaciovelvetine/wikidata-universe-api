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
  String query;
  Dimension dimensions;
  Collection<Vertex> vertices = new ArrayList<>();
  Collection<Edge> edges = new ArrayList<>();
  Collection<Property> properties = new ArrayList<>();
  Err err;

  /**
   * Represents an Error in the service process 
   */
  public RequestResponseBody(Err error) {
    this.err = error;
  }

  /**
   * Payload Data store passed back and forth between the Client and API
   */
  public RequestResponseBody(ClientRequest request) {
    this.err = null;
    this.query = request.query();
    this.dimensions = request.dimensions();
    this.vertices = request.graph().vertices();
    this.edges = request.graph().edges();
    this.properties = request.graph().properties();
  }

  /**
   * Response Status Message container for the initial request from the Client (application)
   */
  public RequestResponseBody(String apiStatusMessageString) {
    this.err = null;
    this.query = apiStatusMessageString;
    this.dimensions = new Dimension();
    this.vertices = null;
    this.edges = null;
    this.properties = null;
  }
}
