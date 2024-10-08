package edu.velv.wikidata_universe_api.services;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

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

  public Optional<Err> fetchInitialQueryData(ClientRequest req) {
    Either<Err, EntityDocument> queryResult = api.fetchEntityByAnyQueryMatch(req.query());

    if (queryResult.isLeft()) {
      return Optional.of(queryResult.getLeft());
    }

    processEntityDocument(queryResult.get(), req);
    return Optional.empty();
  }

  public Optional<Err> fetchIncompleteData(ClientRequest req) {
    IncompleteDataQueue taskQueue = new IncompleteDataQueue(req);

    while (!taskQueue.isQueueEmpty()) {
      List<String> targetBatch = taskQueue.getEntityBatch();

      if (!targetBatch.isEmpty()) {
        Optional<Err> error = processEntityBatch(targetBatch, req, taskQueue);
        if (error.isPresent())
          return error;
      } else {
        targetBatch = taskQueue.getDateBatch();
        Optional<Err> error = processDateBatch(targetBatch, req, taskQueue);
        if (error.isPresent())
          return error;
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

  private void processEntityDocument(EntityDocument entityDoc, ClientRequest req) {
    Optional<Vertex> vertexOpt = docProc.createVertexFromUnknownEntDoc(entityDoc, api.enLangKey());
    vertexOpt.ifPresent(vertex -> {
      if (req.graph().hasNoExistingVertices()) {
        vertex.setAsOrigin();
      }
      req.graph().addVertex(vertex);
      processEdgesFromVertex(entityDoc, req);
    });
  }

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

  private Optional<Err> processEntityBatch(List<String> idBatch, ClientRequest req, IncompleteDataQueue queue) {
    return api.fetchEntitiesByIdList(idBatch).fold(
        err -> Optional.of(err),
        entityResults -> {
          entityResults.forEach((id, result) -> {
            handleEntityFetchResult(queue, id, result, req);
          });
          return Optional.empty();
        });
  }

  private void handleEntityFetchResult(IncompleteDataQueue queue, String id, Either<Err, EntityDocument> result,
      ClientRequest req) {
    queue.removeFromQueue(id);
    if (result.isLeft()) {
      req.graph().removeInvalidSearchResultFromData(id);
    } else {
      EntityDocument entityDoc = result.get();
      if (entityDoc instanceof ItemDocumentImpl) {
        req.graph().getVertexById(id)
            .ifPresent(vertex -> vertex.updateUnfetchedValues((ItemDocumentImpl) entityDoc, api.enLangKey()));
      } else if (entityDoc instanceof PropertyDocument) {
        req.graph().getPropertyById(id)
            .ifPresent(property -> property.updateUnfetchedValues((PropertyDocument) entityDoc, api.enLangKey()));
      }
    }
  }

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

  private void handleDateFetchResult(IncompleteDataQueue queue, String id, Either<Err, WbSearchEntitiesResult> result,
      ClientRequest req) {
    queue.removeFromQueue(id);
    if (result.isLeft()) {
      req.graph().removeInvalidSearchResultFromData(id);
    } else {
      req.graph().getVertexById(id).ifPresent(vertex -> vertex.updateUnfetchedValues(result.get()));
    }
  }

  /**
   * Composed during tasks to track what data needs to be fetched
   */
  private class IncompleteDataQueue {
    private final List<String> entitiesToFetch = new ArrayList<>();
    private final List<String> datesToFetch = new ArrayList<>();

    public IncompleteDataQueue(ClientRequest req) {
      initializeUnfetchedDetails(req);
    }

    private void initializeUnfetchedDetails(ClientRequest req) {
      req.graph().getUnfetchedVertices().forEach(vertex -> {
        if (vertex.id() != null) {
          entitiesToFetch.add(vertex.id());
        } else if (vertex.label() != null) {
          datesToFetch.add(vertex.label());
        }
      });

      req.graph().getUnfetchedProperties().forEach(property -> {
        if (property.id() != null) {
          entitiesToFetch.add(property.id());
        }
      });
    }

    public List<String> getEntityBatch() {
      return entitiesToFetch.stream().limit(50).toList();
    }

    public List<String> getDateBatch() {
      return datesToFetch.stream().limit(50).toList();
    }

    public boolean isQueueEmpty() {
      return entitiesToFetch.isEmpty() && datesToFetch.isEmpty();
    }

    public void removeFromQueue(String target) {
      entitiesToFetch.remove(target);
      datesToFetch.remove(target);
    }
  }
}
