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

/**
 * Represents a fetch queue used in the Wikidata Universe API.
 * The fetch queue is responsible for managing the entities and targets to be fetched.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FetchQueue {
  @JsonIgnore
  private static final int MAX_QUERY_SIZE = 50;
  @JsonIgnore
  private static final String ENT_ID_PATTERN = "[PQ]\\d+";
  @JsonIgnore
  private final Set<String> fetched; // for convienence

  private final Map<Integer, Set<String>> queued;
  private final Set<String> invalid;

  public FetchQueue() {
    queued = new ConcurrentHashMap<>();
    invalid = ConcurrentHashMap.newKeySet();
    fetched = ConcurrentHashMap.newKeySet();
  }

  public FetchQueue(FetchQueue fq) {
    queued = new ConcurrentHashMap<>(fq.queued);
    invalid = ConcurrentHashMap.newKeySet();
    fetched = ConcurrentHashMap.newKeySet();
    invalid.addAll(fq.invalid);
  }

  /**
   * Checks and adds unfetched Edge details into the queue
   * 
   * @param e Edge with details to be added
   * @param n the current value of N
   */
  public void addUnfetchedEdgeValues(Edge e, Integer n) {
    Integer nP = n + 1;
    addEntityIfNotPresent(e.label(), nP);
    addEntityIfNotPresent(e.tgtId(), nP);
    addEntityIfNotPresent(e.propertyId(), nP);
  }

  /**
   * Checks and adds a Vertex's QID to the queue 
   * 
   * @param v Vertex with a QID to add
   * @param n the current value of N
   */
  public void addUnfetchedVertexValue(Vertex v, Integer n) {
    addEntityIfNotPresent(v.id(), (n + 1));
  }

  /**
   * Checks if the existing N value exists as a Key in the queue
   * 
   * @param n the depth value to check for
   * @return true if no key exists with the given value n
   */
  public boolean isEmptyAtNDepth(Integer n) {
    return queued.get(n) == null;
  }

  /**
   * Retrieves (a limited) 50 values from the Queue which are id's matching that of an Entity
   * @see org.wikidata.wdtk (Wikidata Java Toolkit)
   *
   * @param n depth value to retrieve values from
   * @return A list of id values which can be searched for
   */
  public List<String> getEntTargetBatch(Integer n) {
    return getTargetBatchAtN(n, q -> q.matches(ENT_ID_PATTERN));
  }

  /**
   * Retrieves (a limited) 50 values from the Queue which are dates
   * @see org.wikidata.wdtk (Wikidata Java Toolkit)
   * 
   * @param n depth value to retreive values from
   * @return A list of dates which can be searched for
   */
  public List<String> getDateTargetBatch(Integer n) {
    return getTargetBatchAtN(n, q -> !q.matches(ENT_ID_PATTERN));
  }

  /**
   * Remove a successfully retrieved target value from the queue and add it to the fetched list
   * 
   * @param query the value to remove from the queue (actively ignores n-depth)
   */
  public void fetchSuccessCleanup(String query) {
    removeQueryTarget(query);
    fetched.add(query);
  }

  /**
   * Remove a target with no results and marks it invalid to prevent further fetches
   * 
   * @param query the value to remove form the queue (actively ignores n-depth)
   */
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

  /** 
   * Removes any value which matches from the queue, regardless of depth.
  */
  protected void removeQueryTarget(String query) {
    queued.forEach((k, v) -> v.removeIf(str -> query.equals(str)));
    queued.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  /**
   * Get a batch of values which match a given predicate, 
   * limited to only 50 at a time per wikidata API's limits
   * 
   * @param n the depth from which to get values
   * @param matchCondition predicate condition to check entries against
   */
  protected List<String> getTargetBatchAtN(Integer n, Predicate<String> matchCondition) {
    Set<String> targetBatch = queued.get(n);
    if (targetBatch == null) {
      return List.of();
    }
    return targetBatch.stream().filter(matchCondition).map(x -> x).limit(MAX_QUERY_SIZE).toList();
  }

  /**
   * Checks if that value is already in the queue anywhere already, then add's the 
   * value to the queue creating a new n-depth key if it doesnt exist already. 
   * 
   * @param query value to be added to the queue
   * @param nPlus the depth at which the value should be recorded
   */
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

  /* Boolean checks */

  protected boolean isInvalid(String query) {
    return query == null || invalid.contains(query);
  }

  protected boolean isInQueue(String query, Integer nPlus) {
    Set<String> que = queued.get(nPlus);
    return que != null && que.contains(query);
  }

  protected boolean isFetched(String query) {
    return fetched.contains(query);
  }
}
