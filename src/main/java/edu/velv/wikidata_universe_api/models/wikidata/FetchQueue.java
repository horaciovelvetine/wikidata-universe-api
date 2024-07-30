package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import edu.velv.wikidata_universe_api.models.jung_ish.Edge;

import java.util.function.Predicate;

public class FetchQueue {
  private static final String ENT_ID_PATTERN = "[PQ]\\d+";
  private static final int MAX_QUERY_SIZE = 50;
  private final Set<Target.Entity> entities;
  private final Set<Target.Invalid> invalid;
  private final Set<Target.Fetched> fetched;

  public FetchQueue() {
    entities = ConcurrentHashMap.newKeySet();
    invalid = ConcurrentHashMap.newKeySet();
    fetched = ConcurrentHashMap.newKeySet();
  }

  /**
   * Adds the unfetched values from a newly created edge into the Queue.
   */
  public void addUnfetchedEdgeValues(Edge e, Integer n) {
    Integer nP = n + 1;
    addEntityIfNotPresent(nP, e.propertyId());
    addEntityIfNotPresent(nP, e.label());
    addEntityIfNotPresent(nP, e.tgtEntId());
  }

  /**
   * Checks if the Queue is empty at a given depth of n.
   */
  public boolean isEmptyAtNDepth(Integer n) {
    return entities.stream().noneMatch(t -> t.n().equals(n));
  }

  /**
   * Retrieves the next batch of entity ID's at a given depth of n.
   */
  public List<String> getEntTargetsBatchAtN(Integer n) {
    return getTargetBatchAtN((n), q -> q.matches(ENT_ID_PATTERN));
  }

  /**
   * Retrieves the next batch of any value which is not an entitiy ID a given depth of n.
   */
  public List<String> getDateTargetsBatchAtN(Integer n) {
    return getTargetBatchAtN((n), q -> !q.matches(ENT_ID_PATTERN));
  }

  /**
   * Marks a query as successfully fetched.
   */
  public void fetchSuccess(String query) {
    removeQueryTargetFromQueue(query);
    fetched.add(new Target.Fetched(query));
  }

  /**
   * Marks a query as an invalid target.
   */
  public void fetchInvalid(String query) {
    removeQueryTargetFromQueue(query);
    invalid.add(new Target.Invalid(query));
  }

  @Override
  public String toString() {
    int totalInQueue = 0;
    for (Target.Entity entity : entities) {
      String keyValue = entity.query();
      int count = (int) entities.stream()
          .filter(e -> e.query().equals(keyValue))
          .count();
      totalInQueue += count;
    }
    // I wnat to know invalid queue size
    return "Queue={ Queued=" + totalInQueue + ", Fetched=" + fetched.size() + countTargetsByN() + ", Invalid="
        + invalid.size() + " }";
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  //TODO: remove debug details code
  
  private List<String> countTargetsByN() {
    Map<Integer, Integer> countByN = new HashMap<>();
    for (Target.Entity entity : entities) {
      int n = entity.n();
      countByN.put(n, countByN.getOrDefault(n, 0) + 1);
    }
    List<String> formattedCount = new ArrayList<>();
    for (Map.Entry<Integer, Integer> entry : countByN.entrySet()) {
      int n = entry.getKey();
      int count = entry.getValue();
      formattedCount.add("{n: " + n + ", count: " + count + "}");
    }
    return formattedCount;
  }

  private void removeQueryTargetFromQueue(String query) {
    entities.removeIf(t -> t.query().equals(query));
  }

  private List<String> getTargetBatchAtN(Integer n, Predicate<String> batchCondition) {
    return getTargetsAtN(n).filter(batchCondition).limit(MAX_QUERY_SIZE).toList();
  }

  private Stream<String> getTargetsAtN(Integer n) {
    return entities.stream()
        .filter(t -> t.n().equals(n))
        .map(Target::query);
  }

  private void addEntityIfNotPresent(Integer nP, String query) {
    if (isInvalid(query)
        || isAlreadyFetched(query)
        || isAlreadyQueued(query)) {
      return;
    }
    entities.add(new Target.Entity(nP, query));
  }

  private boolean isInvalid(String query) {
    return query == null || isQueryPresentInSet(invalid, query);
  }

  private boolean isAlreadyFetched(String query) {
    return isQueryPresentInSet(fetched, query);
  }

  private boolean isAlreadyQueued(String query) {
    return isQueryPresentInSet(entities, query);
  }

  private boolean isQueryPresentInSet(Set<? extends Target> set, String query) {
    return set.stream().anyMatch(t -> t.query().equals(query));
  }

  interface Fetchable {
    public Integer n();
  }

  public interface Target {
    public String query();

    record Entity(Integer n, String query) implements Target, Fetchable {
    }

    record Date(Integer n, String query) implements Target, Fetchable {
    }

    record Property(Integer n, String query) implements Target, Fetchable {
    }

    record Invalid(String query) implements Target {
    }

    record Fetched(String query) implements Target {
    }

  }
}
