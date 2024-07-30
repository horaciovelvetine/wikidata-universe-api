package edu.velv.wikidata_universe_api.models.jung_ish;

import edu.velv.wikidata_universe_api.models.wikidata.SnakData;
import edu.velv.wikidata_universe_api.models.wikidata.ValueData.ValueType;

public record Edge(String srcEntId, String tgtEntId, String propertyId, String label, ValueType type) {

  public Edge(String srcVertexId, SnakData mainSnak) {
    this(srcVertexId, getTgtEntIdIfNotDateType(mainSnak), mainSnak.property.value, getLabelIfDateTypePresent(mainSnak),
        mainSnak.snakValue.type);
  }

  /**
   * Returns the edge string attributes which have *typically* not been 
   * fetched yet as a part of the import process in a new array.
   */
  public String[] unfetchedEdgeDetailStrings() {
    return new String[] { tgtEntId, propertyId, label };
  }

  private static String getLabelIfDateTypePresent(SnakData mainSnak) {
    if (mainSnak.snakValue.type == ValueType.DateTime) {
      return mainSnak.snakValue.value;
    }
    return null;
  }

  private static String getTgtEntIdIfNotDateType(SnakData mainSnak) {
    if (mainSnak.snakValue.type != ValueType.DateTime) {
      return mainSnak.snakValue.value;
    }
    return null;
  }

  public boolean definesDateValue() {
    return type == ValueType.DateTime;
  }
}
