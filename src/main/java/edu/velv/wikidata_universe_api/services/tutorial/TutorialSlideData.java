package edu.velv.wikidata_universe_api.services.tutorial;

import edu.velv.wikidata_universe_api.interfaces.Loggable;
import edu.velv.wikidata_universe_api.models.TutorialSlide;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.JsonNode;

public class TutorialSlideData implements Loggable {
  private static final String CUR_TUTORIAL_SLIDES = "data/tutorial_slide_data_v2.json";
  private Map<String, TutorialSlide> slideData;

  public TutorialSlideData() {
    try {
      slideData = loadAllSlideData();
    } catch (Exception e) {
      throw new RuntimeException("Failed to load tutorial slide data", e);
    }
  }

  /**
   * @return The TutorialSlide associated with the provided target string;
   */
  public TutorialSlide getSlideData(String target) {
    return slideData.get(target);
  }

  /**
  * Loads all slide data from the JSON resource file.
  *
  * @return A map containing all tutorial slides, where the key is the slide identifier and the value is the TutorialSlide object.
  * @throws IOException If an I/O error occurs while reading the JSON resource file.
  */
  private Map<String, TutorialSlide> loadAllSlideData() throws IOException {
    JsonNode slidesJson = mapper.readTree(getSlideDataResource().getInputStream()).path("slides");

    Map<String, TutorialSlide> slides = new HashMap<>();
    Iterator<Map.Entry<String, JsonNode>> fieldset = slidesJson.fields();
    while (fieldset.hasNext()) {
      Map.Entry<String, JsonNode> field = fieldset.next();
      
      TutorialSlide slide = mapper.treeToValue(field.getValue(), TutorialSlide.class);
      slides.put(field.getKey(), slide);
    }
    return slides;
  }

  /**
  * Retrieves the resource containing the slide data.
  *
  * @return A Resource object pointing to the JSON file with the tutorial slide data.
  */
  private Resource getSlideDataResource() {
    return new ClassPathResource(CUR_TUTORIAL_SLIDES);
  }
}