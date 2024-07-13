package edu.velv.wikidata_universe_api.models.api;

public abstract class RequestResponses {

  public record StatusResponse(String message) {
    public StatusResponse(String message) {
      this.message = message;
    }

    public StatusResponse() {
      this("API Online.");
    }
  }

  public record InitialQueryResponse(String query) {
    public InitialQueryResponse(String query) {
      this.query = query; // Default set @ InitialQueryController should be "Kevin Bacon"
    }
  }
}
