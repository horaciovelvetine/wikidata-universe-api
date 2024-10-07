package edu.velv.wikidata_universe_api.services;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import io.vavr.control.Either;

import org.springframework.beans.factory.annotation.Autowired;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.Property;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.errors.Err;

public class WikidataServiceManager implements Printable {

  @Autowired
  public FetchBroker api;

  @Autowired
  public EntDocProc docProc;

  public WikidataServiceManager() {
    // Default a constructor
  }

  /**
   * An injectable version for testing services
   */
  public WikidataServiceManager(FetchBroker api, EntDocProc docProc) {
    this.api = api;
    this.docProc = docProc;
  }

  /**
   * Uses the initial query value to find the target of the request and then gather the initial data
   * used to construct a Graph and return the initial results for the client. This doesn't include any
   * layout data placing a single vertex (the origin) at a new Point3D (0,0,0)
   */
  public Optional<Err> fetchInitQueryDataTask(ClientRequest req) {
    Either<Err, EntityDocument> initQueryDataTask = api.fetchEntityByAnyQueryMatch(req.query());

    if (initQueryDataTask.isLeft()) {
      return Optional.of(initQueryDataTask.getLeft());
    }

    ingestEntityDocAsVertexWithEdges(initQueryDataTask.get(), req);
    return Optional.empty();
  }

  /**
   * Fetch currently unknown values from the Wikidata API to complete the details of the Graphset, called
   * as a subsequent request from the client to ensure valid connection with Client (application) before 
   * initiating larger request load on Wikidata API.
   */
  public Optional<Err> fetchIncompleteDataTask(ClientRequest req) {

    IncompleteDataQueue taskQueue = new IncompleteDataQueue(req);

    while (taskQueue.incompleteDataStillInQueue()) {
      List<String> tgtBatch = taskQueue.getEntBatch();

      if (!tgtBatch.isEmpty()) {
        Optional<Err> ingestIdBatch = ingestUnfetchedTargetIdBatchData(tgtBatch, req, taskQueue);

        if (ingestIdBatch.isPresent()) {
          return ingestIdBatch;
        }

      } else {
        tgtBatch = taskQueue.getDateBatch();
        Optional<Err> ingestDateBatch = ingestUnfetchedTargetDateBatchData(tgtBatch, req, taskQueue);

        if (ingestDateBatch.isPresent()) {
          return ingestDateBatch;
        }
      }
    }
    return Optional.empty();
  }

  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  /**
   * Uses successfully fetched data from Wikidata to compose data storage objects and add
   * them back to the original ClientRequest.
   */
  private void ingestEntityDocAsVertexWithEdges(EntityDocument entDoc, ClientRequest req) {
    Optional<Vertex> isVert = docProc.createVertexFromUnknownEntDoc(entDoc, api.enLangKey());
    if (isVert.isPresent()) {
      Vertex vert = isVert.get();
      if (req.graph().hasNoExistingVertices())
        vert.setAsOrigin(); // marks only the first ent as an origin

      req.graph().addVertex(vert);
      ingestEdgesFromNewVertex(entDoc, req);
    }
  }

  /**
   * 
   * Called after successful creation of a new Vertex to create edges using the Statements
   * from the originating EntityDocument (ItemDocument). Each Edge is used to create an incomplete
   * Vertex & Property which are *added* along with the new Edge.
   *
   */
  private void ingestEdgesFromNewVertex(EntityDocument entDoc, ClientRequest req) {
    docProc.createRelatedEdgesFromStatements(entDoc).forEach(newEdge -> {
      Property newProp = new Property();
      Vertex tgtVert = new Vertex();

      newProp.id(newEdge.propertyId());
      tgtVert.label(newEdge.label());
      tgtVert.id(newEdge.tgtId());

      req.graph().addEdge(newEdge);
      req.graph().addProperty(newProp);
      req.graph().addVertex(tgtVert);
    });
  }

  /**
   * Fetches details for a batch of Wikidata Entities where only the ID value is currently known, then updates the existing 
   * Vertex or Property with the details of a successful fetch, or if there was no matching result removes mention of this value
   * from the Graphset to prevent bad data.
   * 
   * @apiNote - empty batches are only checked on dates because empty entity batches triggers the beginning of fetching dates
   */

  private Optional<Err> ingestUnfetchedTargetIdBatchData(List<String> idBatch, ClientRequest req,
      IncompleteDataQueue queue) {
    return api.fetchEntitiesByIdList(idBatch).fold(
        err -> {
          return Optional.of(err);
        },
        entResults -> {
          for (Entry<String, Either<Err, EntityDocument>> result : entResults.entrySet()) {
            removeEntFetchedTarget(queue, result, req);
            EntityDocument resDoc = result.getValue().get();

            if (resDoc instanceof ItemDocumentImpl) {
              Vertex existingVert = req.graph().getVertexById(result.getKey()).get();
              existingVert.updateUnfetchedValues((ItemDocumentImpl) resDoc, api.enLangKey());

            } else if (resDoc instanceof PropertyDocument) {
              Property existingProp = req.graph().getPropertyById(result.getKey()).get();
              existingProp.updateUnfetchedValues((PropertyDocument) resDoc, api.enLangKey());

            }
          }
          return Optional.empty();
        });
  }

  /**
  * Uses the provided fetch result to remove it first from the incomplete queue, then checking for an NoSuchEntitiesErr removes any data which mentions
  * the non-existent vertex.
  */
  private void removeEntFetchedTarget(IncompleteDataQueue queue, Entry<String, Either<Err, EntityDocument>> entry,
      ClientRequest request) {
    queue.removeFromQueue(entry.getKey());
    if (entry.getValue().isLeft()) {
      request.graph().removeInvalidSearchResultFromData(entry.getKey());
    }
  }

  /**
   * Fetches details for a batch of Wikidata Date Entities (individual fetches imposed by WikidataAPI limit), then uses that list of results
   * to update existing Vertices with the details of a successful fetch, or if there was no matching Date (...shouldn't be possible) removes mention
   * of this value from the Graphset to prevent bad data.
   */
  private Optional<Err> ingestUnfetchedTargetDateBatchData(List<String> dateBatch, ClientRequest req,
      IncompleteDataQueue queue) {
    if (dateBatch.isEmpty()) {// only fetch details when the batch isn't empty
      return Optional.empty();
    }

    return api.fetchEntitiesByDateList(dateBatch).fold(errResults -> {
      return Optional.of(errResults);
    }, dateResults -> {
      for (Entry<String, Either<Err, WbSearchEntitiesResult>> result : dateResults.entrySet()) {
        removeDateFetchedTarget(queue, result, req);

        Optional<Vertex> srcVertex = req.graph().getVertexById(result.getKey());
        if (srcVertex.isPresent()) {
          srcVertex.get().updateUnfetchedValues(result.getValue().get());
        }
      }
      return Optional.empty();
    });
  }

  /**
   * Uses the provided fetch result to remove it first from the incomplete queue, then checking for an NoSuchEntitiesErr removes any data which mentions
   * the non-existent vertex.
   */
  private void removeDateFetchedTarget(IncompleteDataQueue queue,
      Entry<String, Either<Err, WbSearchEntitiesResult>> entry, ClientRequest request) {
    queue.removeFromQueue(entry.getKey());
    if (entry.getValue().isLeft()) {
      request.graph().removeInvalidSearchResultFromData(entry.getKey());
    }

  }

  /**
   * Composed during tasks to track what data needs to be fetched
   */
  private class IncompleteDataQueue {
    List<String> entsToFetch = new ArrayList<>();
    List<String> datesToFetch = new ArrayList<>();

    public IncompleteDataQueue(ClientRequest req) {
      initializeUnfetchedDetails(req);
    }

    private void initializeUnfetchedDetails(ClientRequest req) {
      List<Vertex> unfVerts = req.graph().getUnfetchedVertices();
      List<Property> unfProperties = req.graph().getUnfetchedProperties();

      for (Vertex vert : unfVerts) {
        if (vert.id() != null) {
          entsToFetch.add(vert.id());
        } else if (vert.label() != null) {
          datesToFetch.add(vert.label());
        }
      }

      for (Property prop : unfProperties) {
        if (prop.id() != null) {
          entsToFetch.add(prop.id());
        }
      }
    }

    public List<String> getEntBatch() {
      return entsToFetch.stream().limit(50).toList();
    }

    public List<String> getDateBatch() {
      return datesToFetch.stream().limit(50).toList();
    }

    public boolean incompleteDataStillInQueue() {
      return !entsToFetch.isEmpty() && !datesToFetch.isEmpty();
    }

    public void removeFromQueue(String tgt) {
      entsToFetch.remove(tgt);
      datesToFetch.remove(tgt);
    }
  }
}
