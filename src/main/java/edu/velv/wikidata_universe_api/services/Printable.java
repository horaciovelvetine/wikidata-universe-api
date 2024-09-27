package edu.velv.wikidata_universe_api.services;

public interface Printable {
  default void print(String message) {
    System.out.println(message);
  }

  default void print(Exception excepts) {
    System.err.print(excepts.getMessage());
  }
}
