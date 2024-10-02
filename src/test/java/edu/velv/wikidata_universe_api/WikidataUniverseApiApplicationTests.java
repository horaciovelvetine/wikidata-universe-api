package edu.velv.wikidata_universe_api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.velv.wikidata_universe_api._Utils.FailedTestMessageTemplates;
import edu.velv.wikidata_universe_api.services.FR3DConfig;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

@SpringBootTest
public class WikidataUniverseApiApplicationTests implements FailedTestMessageTemplates {
  private final String local = "@WikidataUniverseApiApplicationTests ";
  @Autowired
  private WikidataServiceManager wikidataSrvcMngr;

  @Autowired
  private FR3DConfig fr3dConfig;

  @Test
  public void spring_boot_context_loads_correctly() {
    checkFR3DConfigValuesArePresent();
    assertNotNull(wikidataSrvcMngr, local + unableToFind + "WikidataServiceManager @Bean");
    assertNotNull(wikidataSrvcMngr.docProc, local + unableToFind + "EntDocProc @Bean");
    assertNotNull(wikidataSrvcMngr.api, local + unableToFind + "FetchBroker @Bean");
  }

  /**
   * Individually test for each of the values pulled from application.properties on startup from the FR3DConfig
   */
  private void checkFR3DConfigValuesArePresent() {
    assertNotNull(fr3dConfig.targetDensity(), local + unableToFind + "Target Density" + val);
    assertNotNull(fr3dConfig.repMult(), local + unableToFind + "Repulsion Multiplier" + val);
    assertNotNull(fr3dConfig.attrMult(), local + unableToFind + "Attraction Multiplier" + val);
    assertNotNull(fr3dConfig.tempMult(), local + unableToFind + "Temperature Multiplier" + val);
    assertNotNull(fr3dConfig.maxIters(), local + unableToFind + "Max Layout Iterations limit" + val);
    assertNotNull(fr3dConfig.maxIterMvmnt(), local + unableToFind + "Max per-Iteration movement" + val);
  }
}
