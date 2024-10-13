package edu.velv.wikidata_universe_api.models;

import java.util.ArrayList;
import java.util.List;

public class IncompleteDataQueue {

  private final List<String> entitiesToFetch = new ArrayList<>();
  private final List<String> datesToFetch = new ArrayList<>();

  public IncompleteDataQueue(ClientRequest req) {
    initializeUnfetchedDetails(req);
  }

  public void initializeUnfetchedDetails(ClientRequest req) {
    req.graph().getUnfetchedVertices().forEach(vertex -> {
      if (vertex.id() != null) {
        entitiesToFetch.add(vertex.id());
      } else if (vertex.label() != null) {
        datesToFetch.add(vertex.label());
      }
    });

    req.graph().getUnfetchedProperties().forEach(property -> {
      if (property.id() != null) {
        entitiesToFetch.add(property.id());
      }
    });
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
}
