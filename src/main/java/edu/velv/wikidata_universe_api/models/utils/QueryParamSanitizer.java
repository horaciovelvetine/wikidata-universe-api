package edu.velv.wikidata_universe_api.models.utils;

public class QueryParamSanitizer {
  /**
   * Sanitizes inputs to prevent some forms of QueryParam based nonsense.
   * 
   * @param input the string to be sanitized
   * @return the sanitized string
   */
  public static String sanitize(String input) {
    if (input == null || input.isEmpty()) {
      return null;
    }
    String brackL = input.replaceAll("<", "");
    String brackR = brackL.replaceAll(">", "");
    String replaced = brackR.replaceAll("[^\\w\\s]", ""); //non-alphanumeric characters
    return replaced.trim();
  }
}
