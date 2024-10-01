package edu.velv.wikidata_universe_api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import edu.velv.wikidata_universe_api._TestUtils.TestDataBuilders;
import edu.velv.wikidata_universe_api._TestUtils.FailedTestMessageTemplates;
import edu.velv.wikidata_universe_api.services.EntDocProc;
import edu.velv.wikidata_universe_api.services.FR3DConfig;
import edu.velv.wikidata_universe_api.services.FetchBroker;
import edu.velv.wikidata_universe_api.services.WikidataServiceManager;

@SpringBootTest
@AutoConfigureMockMvc
public class WikidataUniverseApiApplicationTests implements FailedTestMessageTemplates, TestDataBuilders {
  private final String local = "@WikidataUniverseApiApplicationTests ";
  @Autowired
  private WikidataServiceManager wikidataSrvcMngr;

  @Autowired
  private EntDocProc entDocProc;

  @Autowired
  private FetchBroker fetchBroker;

  @Autowired
  private FR3DConfig fr3dConfig;

  @Test
  public void spring_boot_context_loads_correctly() {
    checkFR3DConfigValuesArePresent();
    assertNotNull(wikidataSrvcMngr, local + unableToFind + "service/WikidataServiceManager @Bean");
    assertNotNull(wikidataSrvcMngr.docProc, local + unableToFind + "service/EntDocProc @Bean");
    assertNotNull(wikidataSrvcMngr.api, local + unableToFind + "service/FetchBroker @Bean");

  }

  private void checkFR3DConfigValuesArePresent() {
    assertNotNull(fr3dConfig.targetDensity(), local + unableToFind + "Target Density value");
    assertNotNull(fr3dConfig.repMult(), local + unableToFind + "Repulsion Multiplier value");
    assertNotNull(fr3dConfig.attrMult(), local + unableToFind + "Attraction Multiplier value");
    assertNotNull(fr3dConfig.tempMult(), local + unableToFind + "Temperature Multiplier value");
    assertNotNull(fr3dConfig.maxIters(), local + unableToFind + "Max Layout Iterations limit value");
    assertNotNull(fr3dConfig.maxIterMvmnt(), local + unableToFind + "Max per-Iteration movement value");
  }
}
