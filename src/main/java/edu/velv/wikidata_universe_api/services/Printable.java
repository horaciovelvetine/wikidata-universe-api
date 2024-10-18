package edu.velv.wikidata_universe_api.services;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;

import java.io.FileWriter;
import java.io.IOException;

public interface Printable {
  final String LOGS_DIR = "logs/";
  static final ObjectMapper mapper = new ObjectMapper();

  default void print(String message) {
    System.out.println(message);
  }

  default void print(Exception excepts) {
    System.err.print(excepts.getMessage());
  }

  default void logRunDetails(String details) {
    String fileName = "runtime.log";
    File logFile = new File(LOGS_DIR + fileName);
    try (FileWriter writer = new FileWriter(logFile, true)) {
      if (!logFile.exists()) {
        logFile.createNewFile();
      }
      writer.write(details + System.lineSeparator());
    } catch (IOException e) {
      print(e);
      print("Error writing to runtime log: " + details);
    }
  }

  default void logClientRequestData(ClientRequest request) {
    String fileName = LOGS_DIR + "client_request_" + System.currentTimeMillis() + ".json";
    try {
      String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new RequestResponseBody(request));
      File logFile = new File(fileName);
      try (FileWriter writer = new FileWriter(logFile)) {
        writer.write(json);
      }
    } catch (IOException e) {
      print(e);
      print("Error writing client request data to log: " + request);
    }
  }
}
