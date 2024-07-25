package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.velv.wikidata_universe_api.models.Edge;
import edu.velv.wikidata_universe_api.models.wikidata.ValueData.ValueType;

public class FetchQueue {
  private final Set<EntTgt> entities;
  private final Set<DateTgt> dates;
  private final Set<PropertyTgt> properties;
  private final Set<InvalidTgt> invalid;
  private final Set<FetchedTgt> fetched;

  public FetchQueue() {
    //default constructor
    entities = ConcurrentHashMap.newKeySet();
    dates = ConcurrentHashMap.newKeySet();
    properties = ConcurrentHashMap.newKeySet();
    invalid = ConcurrentHashMap.newKeySet();
    fetched = ConcurrentHashMap.newKeySet();
  }

  public void addUnfetchedEdgeDetails(Edge e, Integer n) {
    Integer nPlus = n + 1;
    addEntity(nPlus, e.tgtEndId());
    addProperty(nPlus, e.propertyId());
    if (e.type() == ValueType.DateTime) {
      addDate(nPlus, e.label());
    }
  }

  public boolean isEmpty(Integer n) {
    return !nDepthStillInQueue(entities, n) && !nDepthStillInQueue(dates, n) && !nDepthStillInQueue(properties, n);
  }

  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//
  //* PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE || PRIVATE *//

  private void addEntity(Integer n, String query) {
    if (queryAlreadyInQueue(entities, query)) {
      return;
    }
    entities.add(new EntTgt(n, query));
  }

  private void addDate(Integer n, String query) {
    if (queryAlreadyInQueue(dates, query)) {
      return;
    }
    dates.add(new DateTgt(n, query));
  }

  private void addProperty(Integer n, String query) {
    if (queryAlreadyInQueue(properties, query)) {
      return;
    }
    properties.add(new PropertyTgt(n, query));
  }

  private boolean queryAlreadyInQueue(Set<? extends Queryable> set, String query) {

    return queryAlreadyInSet(set, query) || queryAlreadyInSet(fetched, query) || queryAlreadyInSet(invalid, query);
  }

  private boolean queryAlreadyInSet(Set<? extends Queryable> set, String query) {
    return set.stream().anyMatch(t -> t.query().equals(query));
  }

  private boolean nDepthStillInQueue(Set<? extends Fetchable> set, Integer n) {
    return set.stream().anyMatch(t -> t.nDepth().equals(n));
  }

  //* RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS *//
  //* RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS *//
  //* RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS || RECORDS *//

  protected interface Queryable {
    String query();
  }

  protected interface Fetchable {
    Integer nDepth();
  }

  protected record EntTgt(Integer nDepth, String query) implements Queryable, Fetchable {
  }

  protected record DateTgt(Integer nDepth, String query) implements Queryable, Fetchable {
  }

  protected record PropertyTgt(Integer nDepth, String query) implements Queryable, Fetchable {
  }

  protected record InvalidTgt(String query) implements Queryable {
  }

  protected record FetchedTgt(String query) implements Queryable {
  }
}
