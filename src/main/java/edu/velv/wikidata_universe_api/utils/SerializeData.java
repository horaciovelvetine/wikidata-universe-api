package edu.velv.wikidata_universe_api.utils;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.velv.wikidata_universe_api.models.ResponseBody;

public class SerializeData {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String DIR = "logs/";

  public static void ResponseBody(ResponseBody body) {
    try {
      mapper.writerWithDefaultPrettyPrinter()
          .writeValue(new File(DIR + System.currentTimeMillis() + "-session-body.json"), body);
    } catch (Exception e) {
    }
  }

}
