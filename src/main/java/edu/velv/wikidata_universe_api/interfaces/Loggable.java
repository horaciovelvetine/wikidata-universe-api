package edu.velv.wikidata_universe_api.interfaces;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.velv.wikidata_universe_api.models.ClientRequest;
import edu.velv.wikidata_universe_api.models.RequestResponseBody;

public interface Loggable extends Printable {
  final String LOGS_DIR = "logs/";
  final String RUNTIME_LOG = "runtime.log";
  static final ObjectMapper mapper = new ObjectMapper();

  default void logRuntimeString(String msg) {
    File logfile = new File(LOGS_DIR + RUNTIME_LOG);
    try (FileWriter writer = new FileWriter(logfile, true)) {
      if (!logfile.exists()) {
        logfile.createNewFile();
      }
      writer.write(System.lineSeparator() + System.currentTimeMillis() + System.lineSeparator());
      writer.write(msg);
      writer.write(System.lineSeparator());
    } catch (Exception e) {
      print(e + "\n");
      print("Error @ logRuntimeString(): " + msg);
    }
  }

  default void logClientRequest(ClientRequest req) {
    File logFile = new File(LOGS_DIR + "client_request_" + System.currentTimeMillis() + ".json");
    try (FileWriter writer = new FileWriter(logFile, true)) {
      String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new RequestResponseBody(req));
      writer.write(json);
    } catch (IOException e) {
      print(e + "\n");
      print("Error writing client request data to log: " + req);
    }
  }

}
