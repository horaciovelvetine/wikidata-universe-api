package edu.velv.wikidata_universe_api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.velv.wikidata_universe_api.services.FR3DConfig;
import edu.velv.wikidata_universe_api.services.FailedTestMsgTemplates;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

@SpringBootTest
public class WikidataUniverseApiApplicationTests implements FailedTestMsgTemplates {
  private final String src_ = "@WikidataUniverseApiApplicationTests ";
  @Autowired
  private WikidataServiceManager wikidataSrvcMngr;

  @Autowired
  private FR3DConfig fr3dConfig;

  @Test
  public void spring_boot_context_loads_correctly() {
    checkWikidataToolkitConfigValuesArePresent();
    checkFR3DConfigValuesArePresent();
    assertNotNull(wikidataSrvcMngr, src_ + unableToFind + "WikidataServiceManager @Bean");
    assertNotNull(wikidataSrvcMngr.docProc(), src_ + unableToFind + "EntDocProc @Bean");
    assertNotNull(wikidataSrvcMngr.api(), src_ + unableToFind + "FetchBroker @Bean");
  }

  /**
   * Individually test for each of the values pulled from application.properties on startup from the FR3DConfig
   */
  private void checkFR3DConfigValuesArePresent() {
    assertNotNull(fr3dConfig.targetDensity(), src_ + unableToFind + "Target Density" + val);
    assertNotNull(fr3dConfig.repMult(), src_ + unableToFind + "Repulsion Multiplier" + val);
    assertNotNull(fr3dConfig.attrMult(), src_ + unableToFind + "Attraction Multiplier" + val);
    assertNotNull(fr3dConfig.tempMult(), src_ + unableToFind + "Temperature Multiplier" + val);
    assertNotNull(fr3dConfig.maxIters(), src_ + unableToFind + "Max Layout Iterations limit" + val);
    assertNotNull(fr3dConfig.maxIterMvmnt(), src_ + unableToFind + "Max per-Iteration movement" + val);
  }

  private void checkWikidataToolkitConfigValuesArePresent() {
    assertNotNull(wikidataSrvcMngr.api().enLangKey(), src_ + unableToFind + "Wikidata enLangKey()");
    assertNotNull(wikidataSrvcMngr.api().iri(), src_ + unableToFind + "Wikidata iri()");
    assertNotNull(wikidataSrvcMngr.docProc().excludedDataTypes(), src_ + unableToFind + "Wikidata excludedDataTypes()");
    assertNotNull(wikidataSrvcMngr.docProc().excludedIds(), src_ + unableToFind + "Wikidata excludedIds()");

  }
}
