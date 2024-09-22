package edu.velv.wikidata_universe_api.services;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.vavr.control.Either;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.ClientRequest;
import edu.velv.wikidata_universe_api.models.Property;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.errors.Err;

@Service
public class WikidataServiceManager {
  @Autowired
  private FetchBroker api;

  @Autowired
  private EntDocProc docProc;

  /**
   * Uses the initial query value to find the target of the request and then gather the initial data
   * used to construct a Graph (no layout for this request), then return the initial results for the client.
   * Results in a single vertex on screen, with further fetch details stored for a subsequent request task. 
   * 
   * @param ClientRequest
   * @return an Err(or) if one was encountered during execution
   */
  public Optional<Err> fetchInitQueryDataTask(ClientRequest req) {
    Either<Err, EntityDocument> initQueryDataTask = api.fetchEntityByAnyQueryMatch(req.query());

    if (initQueryDataTask.isLeft()) {
      return Optional.of(initQueryDataTask.getLeft());
    }

    EntityDocument entDoc = initQueryDataTask.get();
    return ingestInitQueryDataDoc(entDoc, req);
  }

  /**
   * Fetch currently unknown values from the Wikidata API to complete the details of the Graphset, called
   * as a subsequent request from the client to ensure valid connection with Client (application) before 
   * initiating larger request load on Wikidata API.
   * 
   * @param req
   * @return an Err<er> if one was encountered during execution
   */
  public Optional<Err> fetchIncompleteDataTask(ClientRequest req) {
    while (!req.graph().allDataFetched()) {
      List<String> tgtBatch = req.graph().getUnfetchedEntityIDTargetBatch();
      if (tgtBatch.size() == 0) {
        tgtBatch = req.graph().getUnfetchedDateTargetBatch();
        ingestUnfetchedTargetDateBatchData(tgtBatch, req);
      }
      ingestUnfetchedTargetIdBatchData(tgtBatch, req);
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
  private Optional<Err> ingestInitQueryDataDoc(EntityDocument entDoc, ClientRequest req) {
    Optional<Vertex> isVert = docProc.attemptVertexCreateUnkownTypeEntDoc(entDoc);
    if (isVert.isPresent()) {
      Vertex itemDoc = isVert.get();
      req.graph().addVertex(itemDoc);
      ingestEdgesFromNewVertex(entDoc, req);
    }
    return Optional.empty();
  }

  /**
   * Called after successful creation of a new Vertex to create edges using the Statements
   * from the originating EntityDocument (ItemDocument). Each Edge is used to create an incomplete
   * Vertex & Property which are *added* along with the new Edge.
   */
  private void ingestEdgesFromNewVertex(EntityDocument entDoc, ClientRequest req) {
    docProc.createRelatedEdgesFromStatements(entDoc).forEach(newEdge -> {
      Property newProp = new Property();
      Vertex tgtVert = new Vertex();

      newProp.id(newEdge.propertyId());
      tgtVert.label(newEdge.label()); //? one of these is null depending on 
      tgtVert.id(newEdge.tgtId()); //? if this edge defines a date target

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
  private Optional<Err> ingestUnfetchedTargetIdBatchData(List<String> idBatch, ClientRequest req) {
    if (idBatch.isEmpty())
      return Optional.empty();

    Either<Err, Map<String, Either<Err, EntityDocument>>> idResults = api.fetchEntitiesByIdList(idBatch);
    if (idResults.isLeft()) {
      return Optional.of(idResults.getLeft());
    }
    for (Entry<String, Either<Err, EntityDocument>> result : idResults.get().entrySet()) {
      if (result.getValue().isLeft()) { // NoSuchResultsErr removes tgt mention from graphset
        req.graph().removeInvalidSearchResultFromData(result.getKey());
      }

      Optional<Vertex> isVert = docProc.attemptVertexCreateUnkownTypeEntDoc(result.getValue().get());
      if (isVert.isPresent()) {
        req.graph().getVertexById(isVert.get().id()).get()
            .updateUnfetchedValues((ItemDocumentImpl) result.getValue().get());
        ;
      } else { // is a Property is only other possibility
        req.graph().getPropertyById(result.getValue().get().getEntityId().getId()).get()
            .updateUnfetchedValues((PropertyDocument) result.getValue().get());
        ;
      }
    }
    return Optional.empty();
  }

  /**
   * Fetches details for a batch of Wikidata Date Entities (individual fetches imposed by WikidataAPI limit), then uses that list of results
   * to update existing Vertices with the details of a successful fetch, or if there was no matching Date (...shouldn't be possible) removes mention
   * of this value from the Graphset to prevent bad data.
   */
  private Optional<Err> ingestUnfetchedTargetDateBatchData(List<String> dateBatch, ClientRequest req) {
    if (dateBatch.isEmpty())
      return Optional.empty();

    Either<Err, Map<String, Either<Err, WbSearchEntitiesResult>>> dateResults = api.fetchEntitiesByDateList(dateBatch);
    if (dateResults.isLeft()) {
      return Optional.of(dateResults.getLeft());
    }

    for (Entry<String, Either<Err, WbSearchEntitiesResult>> result : dateResults.get().entrySet()) {
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
}
