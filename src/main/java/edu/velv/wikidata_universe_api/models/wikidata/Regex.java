package edu.velv.wikidata_universe_api.models.wikidata;

/**
 * Provides External sources for Regex patterns to match specific Wikidata ID types
 * 
 * @apiNote IDs are validated by checking first it is a single letter (P||Q) and followed by only numbers (0-9) of any length. 
 */
interface Regex {
  String ENT_ID = "[PQ]\\d+";
  String PROP_ID = "[P]\\d+";
  String VERT_ID = "[Q]\\d+";
}
