package edu.velv.wikidata_universe_api.interfaces;

/**
 * Helper for system calls to print to the console 
 */
public interface Printable {

  default void print(String message) {
    System.out.println(message);
  }

  default void print(Exception excepts) {
    System.err.print(excepts.getMessage());
  }
}
