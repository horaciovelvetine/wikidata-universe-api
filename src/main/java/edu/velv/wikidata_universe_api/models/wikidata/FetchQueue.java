package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.velv.wikidata_universe_api.models.jung_ish.Edge;
import edu.velv.wikidata_universe_api.models.jung_ish.Vertex;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FetchQueue {
  private static final int MAX_QUERY_SIZE = 50;
  private static final String ENT_ID_PATTERN = "[PQ]\\d+";

  private final Map<Integer, Set<String>> queued;
  private final Set<String> invalid;
  @JsonIgnore
  private final Set<String> fetched; // for convienence

  public FetchQueue() {
    queued = new ConcurrentHashMap<>();
    invalid = ConcurrentHashMap.newKeySet();
    fetched = ConcurrentHashMap.newKeySet();
  }

  public void addUnfetchedEdgeValues(Edge e, Integer n) {
    Integer nP = n + 1;
    addEntityIfNotPresent(e.label(), nP);
    addEntityIfNotPresent(e.tgtEntId(), nP);
    addEntityIfNotPresent(e.propertyId(), nP);
  }

  public void addUnfetchedVertexValue(Vertex v, Integer n) {
    addEntityIfNotPresent(v.id(), (n + 1));
  }

  public boolean isEmptyAtNDepth(Integer n) {
    return queued.get(n) == null;
  }

  public List<String> getEntTargetBatch(Integer n) {
    return getTargetBatchAtN(n, q -> q.matches(ENT_ID_PATTERN));
  }

  public List<String> getDateTargetBatch(Integer n) {
    return getTargetBatchAtN(n, q -> !q.matches(ENT_ID_PATTERN));
  }

  public void fetchSuccessCleanup(String query) {
    removeQueryTarget(query);
    fetched.add(query);
  }

  public void fetchInvalidCleanup(String query) {
    removeQueryTarget(query);
    invalid.add(query);
  }

  @Override
  public String toString() {
    // should return total targets in queued, fetched, and invalid.
    // should total the target at each key value 
    int totalQueued = 0;
    for (Entry<Integer, Set<String>> que : queued.entrySet()) {
      totalQueued += que.getValue().size();

    }
    return "Queue={ total= " + totalQueued + ", fetched= " + fetched.size() + ", invalid= " + invalid.size() + " }";
  }

  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  protected void removeQueryTarget(String query) {
    queued.forEach((k, v) -> v.removeIf(str -> query.equals(str)));
    queued.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  protected List<String> getTargetBatchAtN(Integer n, Predicate<String> matchCondition) {
    Set<String> targetBatch = queued.get(n);
    if (targetBatch == null) {
      return List.of();
    }
    return targetBatch.stream().filter(matchCondition).map(x -> x).limit(MAX_QUERY_SIZE).toList();
  }

  protected void addEntityIfNotPresent(String query, Integer nPlus) {
    if (isInvalid(query) || isInQueue(query, nPlus) || isFetched(query)) {
      return;
    }

    if (queued.get(nPlus) == null) {
      Set<String> newQue = ConcurrentHashMap.newKeySet();
      newQue.add(query);
      queued.put(nPlus, newQue);
    }
    queued.get(nPlus).add(query);
  }

  protected boolean isInvalid(String query) {
    return query == null || invalid.contains(query);
  }

  protected boolean isInQueue(String query, Integer nPlus) {
    Set<String> que = queued.get(nPlus);
    if (que == null) {
      return false;
    } else {
      return queued.get(nPlus).contains(query);
    }
  }

  protected boolean isFetched(String query) {
    return fetched.contains(query);
  }
}
