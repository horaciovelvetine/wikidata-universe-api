package edu.velv.wikidata_universe_api.models;

import edu.velv.wikidata_universe_api.models.ValueData.ValueType;

public class Edge {
  private String srcId;
  private String tgtId;
  private String propertyId;
  private String label;
  private boolean fetched;

  public Edge() {
    //Default constructor
    this.fetched = false;
  }

  /**
   * Create a new Edge based on the provided (source) Vertex Id and SnakData, called after
   * the successful creation of a new Vertex object
   * 
   * @apiNote fetched state decleration depends on the presence of both the src & tgt ID.
   * Label value being present indicates this edge points to a date (which does not provide a QID value to search), 
   * and reamins unfetched until the label value can be searched for & paired with the correlated QID.
   * 
   * @param srcEntId (string)
   * @param SnakData the details from the MainSnak of a Statement
   */
  public Edge(String srcEntId, SnakData mainSnak) {
    this.srcId = srcEntId;
    this.tgtId = tgtIdOrNullForDates(mainSnak);
    this.propertyId = mainSnak.property().value();
    this.label = labelForDatesOrNull(mainSnak);
    this.fetched = tgtId == null ? false : true;
  }

  public String srcId() {
    return srcId;
  }

  public String tgtId() {
    return tgtId;
  }

  public String propertyId() {
    return propertyId;
  }

  public String label() {
    return label;
  }

  public boolean fetched() {
    return fetched;
  }

  public void srcId(String srcId) {
    this.srcId = srcId;
  }

  public void tgtId(String tgtId) {
    this.tgtId = tgtId;
  }

  public void propertyId(String propertyId) {
    this.propertyId = propertyId;
  }

  public void label(String label) {
    this.label = label;
  }

  public void fetched(boolean fetched) {
    this.fetched = fetched;
  }

  private String tgtIdOrNullForDates(SnakData mainSnak) {
    return mainSnak.snakValue().type() != ValueType.DateTime ? mainSnak.snakValue().value() : null;
  }

  private String labelForDatesOrNull(SnakData mainSnak) {
    return mainSnak.snakValue().type() == ValueType.DateTime ? mainSnak.snakValue().value() : null;
  }
}
