package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import edu.velv.wikidata_universe_api.models.Edge;

public class FetchQueue {
  private static final int MAX_QUERY_SIZE = 50;
  private final Set<Target.Entity> entities;
  private final Set<Target.Invalid> invalid;
  private final Set<Target.Fetched> fetched;

  public FetchQueue() {
    entities = ConcurrentHashMap.newKeySet();
    invalid = ConcurrentHashMap.newKeySet();
    fetched = ConcurrentHashMap.newKeySet();
  }

  public void addUnfetchedEdgeValues(Edge e, Integer n) {
    Integer nP = n + 1;
    addEntityIfNotPresent(nP, e.propertyId());
    addEntityIfNotPresent(nP, e.label());
    addEntityIfNotPresent(nP, e.srcEntId());
  }

  public boolean isEmptyAtNDepth(Integer n) {
    return entities.stream().noneMatch(t -> t.n().equals(n));
  }

  public List<String> getQueriesAtNDepth(Integer n) {
    return entities.stream()
        .filter(t -> t.n().equals(n))
        .map(Target::query)
        .limit(MAX_QUERY_SIZE)
        .collect(Collectors.toList());
  }

  public void fetchSuccess(String query) {
    entities.removeIf(t -> t.query().equals(query));
    fetched.add(new Target.Fetched(query));
  }

  public void fetchInvalid(String query) {
    entities.removeIf(t -> t.query().equals(query));
    invalid.add(new Target.Invalid(query));
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

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

  //* RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS *//
  //* RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS *//
  //* RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS *//

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
