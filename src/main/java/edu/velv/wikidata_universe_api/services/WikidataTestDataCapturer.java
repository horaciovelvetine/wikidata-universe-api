package edu.velv.wikidata_universe_api.services;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl;
import org.wikidata.wdtk.datamodel.implementation.PropertyDocumentImpl;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WikidataTestDataCapturer implements Printable {
  @Value("${edu.velv.Wikiverse.test_data_dir}")
  private String testDataDir;
  private String json = ".json";

  private final ObjectMapper mapper;

  public WikidataTestDataCapturer() {
    this.mapper = new ObjectMapper();
  }

  public void captureWikidataEntityDoc(EntityDocument entDoc) {
    try {
      String fileName = entDoc.getEntityId().getId();
      switch (entDoc) {
        case ItemDocumentImpl cDoc -> {
          fileName += "-item-doc";
        }
        case PropertyDocumentImpl cDoc -> {
          fileName += "-property-doc";
        }
        default -> {
          fileName += "-entity-doc";
        }
      }
      fileName += json;
      mapper.writeValue(new File(testDataDir + fileName), entDoc);
    } catch (Exception e) {
      print(e);
    }
  }

  public EntityDocument readEntityDocFromStorage(String fileName) {
    try {
      return mapper.readValue(new File(testDataDir + fileName), EntityDocument.class);
    } catch (Exception e) {
      print(e);
      return null;
    }
  }

  public ItemDocumentImpl readItemDocFromStorage(String fileName) {
    try {
      //? values injected for site-iri are unused in tests...
      InjectableValues.Std injectableValues = new InjectableValues.Std();
      injectableValues.addValue("siteIri", "demoiri");

      mapper.setInjectableValues(injectableValues);
      return mapper.readValue(new File(testDataDir + fileName), ItemDocumentImpl.class);
    } catch (Exception e) {
      print(e);
      return null;
    }
  }

  public PropertyDocumentImpl readPropertyDocFromStorage(String fileName) {
    try {
      return mapper.readValue(new File(testDataDir + fileName), PropertyDocumentImpl.class);
    } catch (Exception e) {
      print(e);
      return null;
    }
  }
}
