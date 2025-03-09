package edu.velv.wikidata_universe_api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.velv.wikidata_universe_api.services.FailedTestMsgTemplates;
import edu.velv.wikidata_universe_api.services.wikidata.WikidataServiceManager;

@SpringBootTest
public class WikidataUniverseApiApplicationTests implements FailedTestMsgTemplates {
  private final String src_ = "@WikidataUniverseApiApplicationTests ";
  @Autowired
  private WikidataServiceManager wikidataSrvcMngr;

  @Test
  public void spring_boot_context_loads_correctly() {
    checkWikidataToolkitConfigValuesArePresent();
    assertNotNull(wikidataSrvcMngr, src_ + unableToFind + "WikidataServiceManager @Bean");
    assertNotNull(wikidataSrvcMngr.docProc(), src_ + unableToFind + "EntDocProc @Bean");
    assertNotNull(wikidataSrvcMngr.api(), src_ + unableToFind + "FetchBroker @Bean");
  }

  private void checkWikidataToolkitConfigValuesArePresent() {
    assertNotNull(wikidataSrvcMngr.api().enLangKey(), src_ + unableToFind + "Wikidata enLangKey()");
    assertNotNull(wikidataSrvcMngr.api().iri(), src_ + unableToFind + "Wikidata iri()");
    assertNotNull(wikidataSrvcMngr.docProc().excludedDataTypes(), src_ + unableToFind + "Wikidata excludedDataTypes()");
    assertNotNull(wikidataSrvcMngr.docProc().excludedIds(), src_ + unableToFind + "Wikidata excludedIds()");
  }
}
