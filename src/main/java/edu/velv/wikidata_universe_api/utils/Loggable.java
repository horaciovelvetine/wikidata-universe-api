package edu.velv.wikidata_universe_api.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public interface Loggable extends Timestampable {
  public static final String LOGFILE_DIR = "src/main/resources/logs";

  public default void print(String msg) {
    System.out.println(msg);
  }

  public default void print(String head, String msg) {
    System.out.println(head + ": " + msg);
  }

  public default void log(String msg) {
    try (FileWriter fileWriter = new FileWriter(LOGFILE_DIR + now() + ".log", true);
        PrintWriter printWriter = new PrintWriter(fileWriter)) {
      printWriter.println(now() + "::" + msg);
    } catch (IOException e) {
      throw new RuntimeException("Error writing to log file: " + e.getMessage());
    } finally {
      print("Logged@:: " + LOGFILE_DIR + now() + ".log");
    }
  }

}
