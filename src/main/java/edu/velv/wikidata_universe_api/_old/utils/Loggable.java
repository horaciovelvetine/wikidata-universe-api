package edu.velv.wikidata_universe_api.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public interface Loggable extends Timestampable {
  static final String LOGFILE_DIR = "logs/";
  static final String FETCH_LOG = LOGFILE_DIR + "fetch-timings.log";

  public default void print(String msg) {
    System.out.println(msg);
  }

  public default void print(String head, String msg) {
    System.out.println(head + ": " + msg);
  }

  public default void log(String msg) {
    String now = now();
    String log = LOGFILE_DIR + now + ".log";
    String msgTrunc = msg.length() > 80 ? msg.substring(0, 80) + "..." : msg;

    try (FileWriter fileWriter = new FileWriter(log, true);
        PrintWriter printWriter = new PrintWriter(fileWriter)) {
      printWriter.println(now + "::" + msg);
    } catch (IOException e) {
      throw new RuntimeException("Error @ log(): " + e.getMessage());
    } finally {
      print("Logged@::" + log + "\n" + msgTrunc);
    }
  }

  default void logFetch(String txt) {
    String now = now();
    try (FileWriter fileWriter = new FileWriter(FETCH_LOG, true);
        PrintWriter printWriter = new PrintWriter(fileWriter)) {

      printWriter.println(now + "::" + txt);

    } catch (IOException e) {
      throw new RuntimeException("Error @ logFetchTiming(): " + e.getMessage());
    } finally {
      print("FetchDataLogged@::" + FETCH_LOG);
    }
  }

}
