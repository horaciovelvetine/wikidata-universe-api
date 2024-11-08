package edu.velv.wikidata_universe_api.models;

import java.util.ArrayList;
import java.util.List;

public class IncompleteDataQueue {

  private final List<String> entitiesToFetch = new ArrayList<>();
  private final List<String> datesToFetch = new ArrayList<>();

  public IncompleteDataQueue(ClientRequest req) {
    addUnfetchedVerticesToQueue(req);
    addUnfetchedPropertiesToQueue(req);
  }

  public List<String> getEntityBatch() {
    return entitiesToFetch.stream().limit(50).toList();
  }

  public List<String> getDateBatch() {
    return datesToFetch.stream().limit(50).toList();
  }

  public boolean isEmpty() {
    return entitiesToFetch.isEmpty() && datesToFetch.isEmpty();
  }

  public void removeFromQueue(String target) {
    entitiesToFetch.remove(target);
    datesToFetch.remove(target);
  }

  public void addUnfetchedPropertiesToQueue(ClientRequest req) {
    req.graph().getUnfetchedProperties().forEach(prop -> {
      if (prop.id() != null) {
        entitiesToFetch.add(prop.id());
      }
    });
  }

  public void addUnfetchedVerticesToQueue(ClientRequest req) {
    req.graph().getUnfetchedVertices().forEach(vertex -> {
      if (vertex.id() != null) {
        entitiesToFetch.add(vertex.id());
      } else if (vertex.label() != null) {
        datesToFetch.add(vertex.label());
      }
    });
  }

  /**
   * Helper checks both queues for known invalid targets often found in the Wikidata API:
   * - Any entity or date containing charcters outside of {regex} a-z, A-Z, 0-9, and listed special charcters
   * - Any entity id which doesnt follow the known "PXXX || QXXX" conventions this application uses
   */
  public void removeInvalidCharTargetsFromQueue() {
    String regex = "^[a-zA-Z0-9,\\.\\-:!?]+$";
    String pattern = "^[PQ][0-9]+$";
    entitiesToFetch
        .removeIf(target -> !target.matches(regex) || (!target.matches(pattern) && target.matches("^[A-Z][0-9]+$")));
    datesToFetch.removeIf(target -> !target.matches(regex));
  }
}
