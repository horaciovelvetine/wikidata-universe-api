package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.function.Predicate;

import edu.velv.wikidata_universe_api.models.Edge;

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
    addEntityIfNotPresent(nP, e.srcEntId());
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
    return getTargetBatchAtN(n, q -> q.matches(ENT_ID_PATTERN));
  }

  /**
   * Retrieves the next batch of any value which is not an entitiy ID a given depth of n.
   */
  public List<String> getDateTargetsBatchAtN(Integer n) {
    return getTargetBatchAtN(n, q -> !q.matches(ENT_ID_PATTERN));
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

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

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

  interface Target {
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
