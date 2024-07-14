package edu.velv.wikidata_universe_api.models.wikidata;

import java.util.ArrayList;
import java.util.List;

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WbSearchEntitiesResult;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

public class WikiDataAPIBroker {
  private WikibaseDataFetcher wbdf;

  public WikiDataAPIBroker() {
    wbdf = WikibaseDataFetcher.getWikidataDataFetcher();
  }

  public EntityDocument getOriginEntByQuery(String query) {
    EntityDocument originDoc = null;

    try {
      if (queryIsEntID(query)) {
        originDoc = getEntById(query);
      } else {
        originDoc = getEntByTitle(query);
      }

      if (originDoc == null) {
        List<WbSearchEntitiesResult> searchResults = getEntsByAnyMatch(query);
        if (!searchResults.isEmpty()) {
          originDoc = getEntById(searchResults.get(0).getEntityId());
        } else {
          originDoc = getEntById("Q3454165"); // Default to Kevin Bacon if no results
        }
      }
    } catch (Exception e) {
      //TODO handle exception
      System.err.println("Error @ getOriginEntByQuery()" + e.getMessage());
    }
    return originDoc;
  }

  private EntityDocument getEntById(String query) {
    try {
      return wbdf.getEntityDocument(query);
    } catch (Exception e) {
      //TODO handle exception
      System.err.println("Error @ getEntById()" + e.getMessage());
      return null;
    }
  }

  private EntityDocument getEntByTitle(String query) {
    try {
      return wbdf.getEntityDocumentByTitle("enwiki", query);
    } catch (Exception e) {
      //TODO handle exception
      System.err.println("Error @ getEntByTitle()" + e.getMessage());
      return null;
    }
  }

  private List<WbSearchEntitiesResult> getEntsByAnyMatch(String query) {
    List<WbSearchEntitiesResult> searchResults = new ArrayList<>();
    try {
      searchResults = wbdf.searchEntities(query, "en");
    } catch (Exception e) {
      // TODO: handle exception
      System.err.println("Error @ getEntsByAnyMatch()" + e.getMessage());
    }
    return searchResults;
  }

  private boolean queryIsEntID(String query) {
    return query.matches("[PQ]\\d*");
  }
}
