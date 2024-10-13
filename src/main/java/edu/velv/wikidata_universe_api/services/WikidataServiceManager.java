package edu.velv.wikidata_universe_api.services;

import java.util.Optional;
import java.util.List;

import io.vavr.control.Either;

import org.springframework.beans.factory.annotation.Autowired;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.IncompleteDataQueue;
import edu.velv.wikidata_universe_api.models.Property;
import edu.velv.wikidata_universe_api.models.Vertex;
import edu.velv.wikidata_universe_api.errors.Err;

public class WikidataServiceManager implements Printable {

  @Autowired
  protected FetchBroker api;

  @Autowired
  protected EntDocProc docProc;

  public WikidataServiceManager() {
    // Default constructor
  }

  public EntDocProc docProc() {
    return this.docProc;
  }

  public FetchBroker api() {
    return this.api;
  }

  public WikidataServiceManager(FetchBroker api, EntDocProc docProc) {
    this.api = api;
    this.docProc = docProc;
  }

  /**
   * Constructs the initial Graphset by first finding a suitable match for the provided Query then ingesting the needed data by creating Vertices, Edges, and Properties based on that initial EntitiyDocument result's details.
   */
  public Optional<Err> fetchInitialQueryData(ClientRequest req) {
    return api.fetchEntityByAnyQueryMatch(req.query()).fold(
        err -> Optional.of(err),
        entDocResult -> {
          processEntityDocument(entDocResult, req);
          return Optional.empty();
        });
  }

  /**
   * Creates a list of all the incomplete data currently in the Graphset, then iteratively
   * fetches details for each of those entities updating Vertices and Properties, and creating new Edges.
   */
  public Optional<Err> fetchIncompleteData(ClientRequest req) {
    IncompleteDataQueue taskQueue = new IncompleteDataQueue(req);
    Optional<Err> apiOffline = Optional.empty();
    while (!taskQueue.isEmpty()) {
      List<String> tgtBatch = taskQueue.getEntityBatch();

      if (!tgtBatch.isEmpty()) {
        Optional<Err> err = processEntityBatch(tgtBatch, req, taskQueue);
        if (err.isPresent()) {
          apiOffline = err;
        }
      } else {
        tgtBatch = taskQueue.getDateBatch();
        Optional<Err> err = processDateBatch(tgtBatch, req, taskQueue);
        if (err.isPresent()) {
          apiOffline = err;
        }
      }
    }
    return apiOffline;
  }

  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>
  //=====================================================================================================================>

  /**
   * Uses the provided EntityDocument to: Update an existing Vertex if found, check if that EntityDocument is of type ItemDocument and create a new Vertex, or check if the provided EntityDocument is of type PropertyDocument, and use it to update an existing Property. 
   * @apiNote This should cover every possible means this application encounters EntityDocuments form WikidataAPI.
   */
  private void processEntityDocument(EntityDocument entityDoc, ClientRequest req) {

    // Doc already exists as Vertex, update unknown values and ingest edge data
    if (req.graph().getVertexByIdOrLabel(entityDoc).isPresent()) {
      req.graph().getVertexByIdOrLabel(entityDoc).ifPresent(existingVert -> {
        existingVert.updateUnfetchedValues((ItemDocumentImpl) entityDoc, api().enLangKey());
        processEdgesFromVertex(entityDoc, req);
      });
      return;
    }

    // Doc is a new Item Document, create a new Vertex and ingest edge data
    if (docProc().createVertexFromUnknownEntDoc(entityDoc, api().enLangKey()).isPresent()) {
      docProc().createVertexFromUnknownEntDoc(entityDoc, api().enLangKey())
          .ifPresent(newVert -> {
            if (req.graph().hasNoExistingVertices()) {
              newVert.setAsOrigin();
            }
            req.graph().addVertex(newVert);
            processEdgesFromVertex(entityDoc, req);
          });
      return;
    }

    // Doc already exists as Property, update unknown values, but ignore statements
    req.graph().getPropertyById(entityDoc.getEntityId().getId()).ifPresent(existingProp -> {
      existingProp.updateUnfetchedValues((PropertyDocument) entityDoc, api().enLangKey());
    });
  }

  /**
   * Filters and ingests new Edges from a provided EntityDocument by iterating over and collecting relevant data from it's Statements. 
   * @apiNote responsible for the creation of any partially fetched Vertices, Properties, or Edges.
   */
  private void processEdgesFromVertex(EntityDocument entityDoc, ClientRequest req) {
    docProc.createRelatedEdgesFromStatements(entityDoc).forEach(edge -> {
      Property property = new Property();
      Vertex targetVertex = new Vertex();

      property.id(edge.propertyId());
      targetVertex.label(edge.label());
      targetVertex.id(edge.tgtId());

      req.graph().addEdge(edge);
      req.graph().addProperty(property);
      req.graph().addVertex(targetVertex);
    });
  }

  /**
   * Fetches details of a provided list of ID targets from the Wikidata API, then ingests those results as new Vertices, Edges, and Properties. 
   * @return Optional<ApiOfflineError> if the Wikidata API is unavailable
   */
  private Optional<Err> processEntityBatch(List<String> idBatch, ClientRequest req, IncompleteDataQueue queue) {
    return api.fetchEntitiesByIdList(idBatch).fold(
        err -> Optional.of(err),
        entityResults -> {
          entityResults.forEach((id, result) -> {
            if (result.isLeft()) {
              req.graph().removeInvalidSearchResultFromData(id);
            } else {
              processEntityDocument(result.get(), req);
            }
            queue.removeFromQueue(id);
          });
          return Optional.empty();
        });
  }

  /**
   * Fetches details of a provided list of Date targets from the Wikidata API, then updates their related Vertices with the newly fetched details.
   */
  private Optional<Err> processDateBatch(List<String> dateBatch, ClientRequest req, IncompleteDataQueue queue) {
    if (dateBatch.isEmpty()) {
      return Optional.empty();
    }

    return api.fetchEntitiesByDateList(dateBatch).fold(
        err -> Optional.of(err),
        dateResults -> {
          dateResults.forEach((id, result) -> {
            handleDateFetchResult(queue, id, result, req);
          });
          return Optional.empty();
        });
  }

  /**
   * Handles possible invalid & Err results from these fetches, and removes the target from mention in the Graph.
   */
  private void handleDateFetchResult(IncompleteDataQueue queue, String id, Either<Err, WbSearchEntitiesResult> result,
      ClientRequest req) {
    queue.removeFromQueue(id);
    if (result.isLeft()) {
      req.graph().removeInvalidSearchResultFromData(id);
    } else {
      req.graph().getVertexById(id).ifPresent(vertex -> vertex.updateUnfetchedValues(result.get()));
    }
  }
}
