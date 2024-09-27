package edu.velv.wikidata_universe_api.services;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import io.vavr.control.Either;
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.ClientRequest;
import edu.velv.wikidata_universe_api.models.Property;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.errors.Err;

public class WikidataServiceManager implements Printable {

  private FetchBroker api = new FetchBroker();

  private EntDocProc docProc = new EntDocProc();

  public WikidataServiceManager() {
    // Default a constructor
  }

  /**
   * Uses the initial query value to find the target of the request and then gather the initial data
   * used to construct a Graph (no layout for this request), then return the initial results for the client.
   * Results in a single vertex on screen, with further fetch details stored for a subsequent request task. 
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
    Optional<Vertex> isVert = docProc.createVertexFromUnknownEntDoc(entDoc);

    if (isVert.isPresent()) {
      Vertex vert = isVert.get();
      if (req.graph().hasNoExistingVertices())
        vert.setAsOrigin(); // marks only the first ent as an origin

      req.graph().addVertex(vert);
      ingestEdgesFromNewVertex(entDoc, req);

    } else {
      print("An unknown Entity Document has been encountered");
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
   */

  private Optional<Err> ingestUnfetchedTargetIdBatchData(List<String> idBatch, ClientRequest req,
      IncompleteDataQueue queue) {
    Either<Err, Map<String, Either<Err, EntityDocument>>> idResults = api.fetchEntitiesByIdList(idBatch);

    if (idResults.isLeft()) {
      return Optional.of(idResults.getLeft());
    }

    for (Entry<String, Either<Err, EntityDocument>> result : idResults.get().entrySet()) {
      queue.removeFromQueue(result.getKey());
      if (result.getValue().isLeft() || result.getValue().get() == null) { // NoSuchResultsErr removes tgt mention from graphset
        req.graph().removeInvalidSearchResultFromData(result.getKey());
      }
      EntityDocument resDoc = result.getValue().get();
      Optional<Vertex> isVert = docProc.createVertexFromUnknownEntDoc(resDoc);

      if (isVert.isPresent()) {
        req.graph().getVertexById(result.getKey()).get()
            .updateUnfetchedValues((ItemDocumentImpl) resDoc);
      } else if (resDoc instanceof PropertyDocument) { // is a Property is only other possibility
        req.graph().getPropertyById(result.getKey()).get()
            .updateUnfetchedValues((PropertyDocument) resDoc);
      } else {
        print("Encountered an unknown doc type while ingesting related entIDBatch");
      }
      ingestEdgesFromNewVertex(resDoc, req);
    }
    return Optional.empty();
  }

  /**
   * Fetches details for a batch of Wikidata Date Entities (individual fetches imposed by WikidataAPI limit), then uses that list of results
   * to update existing Vertices with the details of a successful fetch, or if there was no matching Date (...shouldn't be possible) removes mention
   * of this value from the Graphset to prevent bad data.
   */
  private Optional<Err> ingestUnfetchedTargetDateBatchData(List<String> dateBatch, ClientRequest req,
      IncompleteDataQueue queue) {
    if (dateBatch.isEmpty())
      return Optional.empty();

    Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> dateResults = api.fetchEntitiesByDateList(dateBatch);
    if (dateResults.isLeft()) {
      return Optional.of(dateResults.getLeft());
    }

    for (Entry<String, Either<Err, WbSearchEntitiesResult>> result : dateResults.get().entrySet()) {
      queue.removeFromQueue(result.getKey());
      if (result.getValue().isLeft()) {
        req.graph().removeInvalidSearchResultFromData(result.getKey());
      }

      Optional<Vertex> srcVertex = req.graph().getVertexById(result.getKey());
      if (srcVertex.isPresent()) {
        srcVertex.get().updateUnfetchedValues(result.getValue().get());
      }
    }
    return Optional.empty();
  }

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
