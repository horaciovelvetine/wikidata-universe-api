package edu.velv.wikidata_universe_api.models;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

import edu.velv.wikidata_universe_api._Utils.FailedTestMessageTemplates;
import edu.velv.wikidata_universe_api.errors.Err;
import edu.velv.wikidata_universe_api.services.FR3DConfig;
import edu.velv.wikidata_universe_api.services.Printable;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;
import edu.velv.wikidata_universe_api.services.WikidataTestDataCapturer;

@SpringBootTest
class ClientRequestTests implements Printable, FailedTestMessageTemplates {
  private final String src_ = "@ClientRequestTest::";

  @Autowired
  private WikidataTestDataCapturer testDataCap;
  // @Autowired
  // private WikidataServiceManager wikidata;
  // @Autowired
  // private FR3DConfig config;
  @Value("${edu.velv.Wikiverse.fetch_test_data_if_available}")
  private boolean fetchDataIfNoneExists;

  // private ClientRequest testReq = new ClientRequest(wikidata, config, "Kevin Bacon");

  private ItemDocumentImpl retrieveKevinBaconTestDataDoc() {
    String kbTestDataPath = "Q3454165-item-doc.json";
    try {
      ItemDocumentImpl doc = testDataCap.readItemDocFromStorage(kbTestDataPath);
      if (doc == null && fetchDataIfNoneExists) {
        EntityDocument fetchedDoc = WikibaseDataFetcher.getWikidataDataFetcher().getEntityDocumentByTitle("en",
            "Kevin Bacon");
        testDataCap.captureWikidataEntityDoc(fetchedDoc);
        return testDataCap.readItemDocFromStorage(kbTestDataPath);
      }
    } catch (Exception e) {
      print(e);
    }
    return null;
  }

  @Test
  void demoTest() {
    ItemDocumentImpl doc = retrieveKevinBaconTestDataDoc();
    if (doc != null) {
      print("Doc is not null");
    } else {
      print("Doc is null");
    }
  }

}