package edu.velv.wikidata_universe_api.models.api;

public abstract class RequestResponse {

  public record StatusResponse(String status) {
    public StatusResponse(String status) {
      this.status = status;
    }

    public StatusResponse() {
      this("A-OK 200");
    }
  }

  public record InitialQueryResponse(String query) {
    public InitialQueryResponse(String query) {
      this.query = query; // Default set @ InitialQueryController should be "Kevin Bacon"
    }
  }
}
