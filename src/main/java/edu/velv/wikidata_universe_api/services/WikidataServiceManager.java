package edu.velv.wikidata_universe_api.services;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import io.vavr.control.Either;

import org.springframework.beans.factory.annotation.Autowired;

import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;

import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.Edge;
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

    taskQueue.removeInvalidCharTargetsFromQueue(); // remove any target from fetch which may contain invalid chars, cleanup will remove these from mention

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
    cleanUpUnfetchedGraphVerts(req);
    cleanUpUnusedProperties(req);
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
    Optional<Vertex> existingVert = req.graph().getVertexByIdOrLabel(entityDoc);
    if (existingVert.isPresent() && entityDoc instanceof ItemDocumentImpl) {
      ItemDocumentImpl itemDoc = (ItemDocumentImpl) entityDoc;
      Vertex vertex = existingVert.get();
      vertex.updateUnfetchedValues(itemDoc, api().enLangKey());
      processEdgesFromVertex(entityDoc, req);
      return;
    }

    // Doc is a new Item Document, create a new Vertex and ingest edge data
    Optional<Vertex> newVert = docProc().createVertexFromUnknownEntDoc(entityDoc, api().enLangKey());
    if (newVert.isPresent()) {
      if (req.graph().hasNoExistingVertices()) {
        newVert.get().setAsOrigin();
      }
      req.graph().addVertex(newVert.get());
      processEdgesFromVertex(entityDoc, req);
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
              req.graph().removeTargetValueFromGraph(id);
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
          dateResults.forEach((date, result) -> {
            handleDateFetchResult(queue, date, result, req);
          });
          return Optional.empty();
        });
  }

  /**
   * Handles possible invalid & Err results from these fetches, and removes the target from mention in the Graph.
   */
  private void handleDateFetchResult(IncompleteDataQueue queue, String date, Either<Err, WbSearchEntitiesResult> result,
      ClientRequest req) {
    if (result.isLeft()) {
      req.graph().removeTargetValueFromGraph(date);
    } else {
      req.graph().getVertexById(date).ifPresent(vertex -> vertex.updateUnfetchedValues(result.get()));
    }
    queue.removeFromQueue(date);
  }

  /**
   * Removes mentions of currently unfetched Vertices from the Graph by iterating over the whole set of vertices
   */
  private void cleanUpUnfetchedGraphVerts(ClientRequest req) {
    for (Vertex vert : req.graph().vertices()) {
      if (!vert.fetched()) {
        if (vert.id() != null) {
          req.graph().removeTargetValueFromGraph(vert.id());
        }
        if (vert.label() != null) {
          req.graph().removeTargetValueFromGraph(vert.label());
        }
      }
    }
  }

  /**
   * Remove mentions of currently unused Properties (which have been removed after the unfetched vertices) by iterating over the remaining edges storing the used property PIDs then checks those against all of the Properties.
   */
  private void cleanUpUnusedProperties(ClientRequest req) {
    List<String> includeProps = new ArrayList<>();

    for (Edge edge : req.graph().edges()) {
      if (edge.propertyId() != null) {
        includeProps.add(edge.propertyId());
      }
    }
    for (Property prop : req.graph().properties()) {
      if (!includeProps.contains(prop.id())) {
        req.graph().removeTargetValueFromGraph(prop.id());
      }
    }
  }
}
