package edu.velv.wikidata_universe_api.models.wikidata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;

public class WikiDataAPIBrokerTests {
  private static final String douglasAdamsQID = "Q42";
  private static final String douglasAdamsTitle = "Douglas Adams";
  private static final String kevinBaconQID = "Q3454165";
  private static final String givenNamePQID = "P735";
  private final WikiDataAPIBroker broker = new WikiDataAPIBroker();

  @Test
  public void getsOriginByQuery_wItemQID() {
    EntityDocument originDoc = broker.getOriginEntByQuery(douglasAdamsQID);
    assertEquals(douglasAdamsQID, originDoc.getEntityId().getId(), "Entity ID should be Q42");
  }

  @Test
  public void getsOriginByQuery_wPropertyQID() {
    EntityDocument originDoc = broker.getOriginEntByQuery(givenNamePQID);
    assertEquals(givenNamePQID, originDoc.getEntityId().getId(), "Entity ID should be P735");
  }

  @Test
  public void getsOriginByQuery_wTitleQuery() {
    EntityDocument originDoc = broker.getOriginEntByQuery(douglasAdamsTitle);
    assertEquals(douglasAdamsQID, originDoc.getEntityId().getId(), "Entity ID should be Q42");
  }

  @Test
  public void getsOriginByQuery_wFailOverDefault() {
    EntityDocument originDoc = broker.getOriginEntByQuery("QFJAJJJJJ...");
    assertEquals(kevinBaconQID, originDoc.getEntityId().getId(), "Entity ID should be Q3454165");
  }
}
